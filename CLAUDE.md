# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

AIFileOrganizer is a Kotlin Multiplatform (KMP) project currently targeting **Desktop (JVM) only**. It uses Compose Multiplatform for UI, Koin for DI, and the [Koog](https://github.com/JetBrains/koog) agent framework to talk to a **locally running Ollama** instance.

What the app does: pick a folder, scan it, send each supported file's content to the local model, and get a better filename back. Suggestions either wait for approval (review mode) or are applied immediately (auto-pilot mode). Renames are recorded so they can be undone.

## Commands

- Run the desktop app: `./gradlew :desktopApp:run`
- Run with hot reload: `./gradlew :desktopApp:hotRun --auto`
- Build everything: `./gradlew build`
- Run tests: `./gradlew :shared:jvmTest`
- Package native distributables (dmg/msi/deb): `./gradlew :desktopApp:packageDmg`, `packageMsi`, `packageDeb`

The app calls a local Ollama server at `http://localhost:11434`. The user picks which pulled model to use from a picker in the header (backed by `OllamaClient.getModels()`); there's no hardcoded default model anymore, so at least one model must be pulled (`ollama pull gemma3:4b` is a reasonable one — it has vision).

## Architecture

Two Gradle modules, declared in `settings.gradle.kts`:

- **`:shared`** — KMP library holding everything: UI, presentation, domain, and data. `commonMain` holds all shared code; `jvmMain` holds the JVM `actual` implementations and JVM-only file I/O. Even though only the JVM target is wired up today, code should still go through `expect`/`actual` (see `Platform.kt` / `Platform.jvm.kt`, `DirectoryPicker.kt`, `FilePreview.kt`) rather than assuming JVM APIs in `commonMain`.
- **`:desktopApp`** — JVM application module. `main.kt` starts Koin (once, before `application {}`), opens the `Window`, builds the native `MenuBar`, and renders `App()`. Packaging config lives in `desktopApp/build.gradle.kts`.

### Layers in `:shared`

```
presentation/organizer   MVI screen: OrganizerState / OrganizerAction / OrganizerViewModel
        │                + components/ (stateless composables) + designsystem/ (theme, colors, icons, dimens)
        ▼
domain/usecase           RenameSuggestionUseCase — the only thing that talks to the LLM
        │                GetAvailableOllamaModelsUseCase — lists pulled models for the picker
        │                domain/model — ScannedFile, RenameSuggestion, TokenUsage, …
        ▼
data/filesystem          FileScanner, FileRenamer, FileTypeClassifier, FileNameSanitizer
data/content             TextContentExtractor
```

- `di/AppModule.kt` (commonMain) provides the `OllamaClient`, `PromptExecutor`, use cases and ViewModel. `di/PlatformModule.kt` (jvmMain) binds the JVM implementations of the data interfaces. Both are started in `main.kt`.
- The ViewModel coordinates; business logic lives in use cases and the `data/` helpers. Follow that split when adding features.
- The screen is the only place a ViewModel is touched (`OrganizerRoot`); everything under `components/` takes plain state + lambdas so it stays previewable.

### Key files

- `di/AppModule.kt` — provides a single `OllamaClient` (default base URL, `http://localhost:11434`), reused by both the `PromptExecutor` and `GetAvailableOllamaModelsUseCase`. No `LLModel` singleton anymore — see below.
- `domain/usecase/GetAvailableOllamaModelsUseCase.kt` — wraps `OllamaClient.getModels()` in a `Result` and pairs each card with its thinking support (see below) as `OllamaModelInfo`; this is also how `OrganizerViewModel` detects "Ollama unreachable".
- `data/ollama/OllamaCapabilityProbe.kt` + `jvmMain/.../JvmOllamaCapabilityProbe.kt` — one extra `/api/show` call per model, purely to read the raw `capabilities` array. Exists because Koog's converter maps Ollama's `thinking` capability to nothing. Results are cached per model name in the use case.
- `presentation/organizer/OllamaModelUi.kt` — UI-safe projection of Koog's `OllamaModelCard` (`toOllamaModelUi()`), with derived fields the model picker needs: formatted size/param/context labels, `supportsVision`/`supportsTools`, `supportedFileTypes`, `isRecommended` (== has vision, since that's the only capability gap that matters for this app's file types).
- `presentation/organizer/OrganizerViewModel.kt` — holds `availableModelCards: List<OllamaModelCard>` privately (not in state) so `currentLLModel()` can rebuild a real `LLModel` via Koog's `OllamaModelCard.toLLModel()` for whichever model is selected. `loadModels()` runs at `init` and again on every `scan()`/`OnRefreshModels`; a failed fetch leaves the last known-good list alone rather than clearing the picker.
- `domain/usecase/RenameSuggestionUseCase.kt` — builds the prompt and calls `promptExecutor.executeStructured<RenameSuggestion>()`. Takes `model: LLModel` per call now (not injected), so the ViewModel decides which model to use per request.
- `presentation/organizer/components/` — top bar, side nav, folder toolbar, file list/grid, stat cards, token usage bar, toast, `ModelPicker.kt` (header chip + accordion dialog for model selection), `OllamaUnavailableBanner.kt` (shown when a scan needs the LLM and Ollama didn't respond).

Dependency versions are centralized in `gradle/libs.versions.toml` (version catalog) — add new dependencies there, not as inline coordinates in module `build.gradle.kts` files.

## Notes for AI-feature work

### Thinking / reasoning

Two Koog 1.0.0 gaps to know about, both worked around rather than patched:

- **Capability is dropped on read.** `OllamaManagementConverters.toLLMCapabilities` maps `Capability.THINKING -> listOf()`, so `OllamaModelCard.capabilities` never carries `LLMCapability.Thinking`. `OllamaCapabilityProbe` re-reads `/api/show` to recover it; `OllamaModelUi.supportsThinking` and the picker's capability row/dot come from there, not from Koog.
- **`OllamaParams.think` is dropped on write.** `executeStructured` → `StructuredRequest.updatePrompt` → `Prompt.withUpdatedParams` → `LLMParams.copy`, which returns a base `LLMParams` (OllamaParams' `copy` is an overload, not an override), so the subtype and its `think` field vanish before the request is built. `RenameSuggestionUseCase.thinkingParams` therefore sends the flag as `additionalProperties["think"]`, which survives the copy and gets flattened to the request root by `AdditionalPropertiesFlatteningSerializer`. `ThinkingParamTest` guards this.

The toggle (`OrganizerState.thinkingEnabled`, off by default) only appears in the header for a model that reports the capability — `state.thinkingRequest` is `null` for the rest, since Ollama errors on `think` for models that can't. Thinking is part of the suggestion cache key because it changes the answer, and reasoning tokens land in `eval_count`, so `TokenUsageBar` already accounts for them.

- Models are user-selected now, not hardcoded — capabilities come from `OllamaModelCard.capabilities` (via `OllamaClient.getModels()` → Ollama's `/api/show`), not a fixed list. `Vision.Image` is required for image files — without it Koog rejects image parts in the prompt, which is why the picker flags models lacking it.
- Ollama not running is expected. It's handled at two levels: `OrganizerState.ollamaUnavailable` drives a prominent, persistent banner (`OllamaUnavailableBanner`) when a scan has suggestible files and the availability check fails — this is louder than the corner `StatusToast` on purpose, since a silent per-file failure spray was the previous (worse) behavior. Per-file failures (rename or extraction errors unrelated to connectivity) still surface as `FileItemStatus.Failed`.
- **Verify Koog APIs against the jars**, not against docs or memory — decompile from `~/.gradle/caches/modules-2/files-2.1/ai.koog/` with `javap`, or unzip the `-sources.jar` for readable Kotlin. The 1.0.0 API differs from most published examples.

### Token usage

Per-request token counts come from the provider's own accounting, not an estimate:

- `executeStructured` returns `Result<StructuredResponse<T>>`; `StructuredResponse.message.metaInfo` is a `ResponseMetaInfo` carrying `inputTokensCount` / `outputTokensCount` / `totalTokensCount` (all nullable).
- `OllamaClient` fills these from Ollama's `prompt_eval_count` and `eval_count`, and leaves `modelId` null.
- `RenameSuggestionUseCase` maps them into a `TokenUsage` and returns it alongside the suggestion in `RenameSuggestionResult`. The ViewModel stores it per file (`FileItemUi.tokenUsage`) and accumulates a session total (`OrganizerState.tokenUsage` / `llmRequestCount`), rendered by `TokenUsageBar` and the per-row token chip.
- This accounts for exactly one LLM call because no `StructureFixingParser` is passed. If one is added, its repair calls burn tokens that never reach `StructuredResponse.message` — at that point move the accounting into a `PromptExecutor` decorator instead.

## Logging

`kotlin-logging` (`KotlinLogging.logger {}`, a file-level `private val` per class) over SLF4J, with `slf4j-simple` as a `runtimeOnly` provider in `:desktopApp` and in `:shared`'s `jvmTest`. Without a provider every line is dropped — that's what the old "No SLF4J providers were found" startup warning meant, and Koog's own logs were being swallowed too. `desktopApp/src/main/resources/simplelogger.properties` sets the level; flip `defaultLogLevel` to `debug` to see what Koog actually sends Ollama.

Every `catch` that ends in a UI state (scan, suggestion, rename, undo, model listing) logs first with enough context to reproduce — file path, model id, thinking flag.

**Bad model output** is its own case, since it's the common one:

- `RenameSuggestionUseCase` catches the `SerializationException` from Koog's parse step, logs it, and rethrows it as `ModelOutputException`. The log line carries the malformed output itself: kotlinx appends `JSON input: <raw text>` to its own message, so the actual thing the model said ends up in the log without any extra plumbing.
- The UI shows "<model> returned an unreadable answer — try another model" (`OrganizerViewModel.toFailureMessage`) rather than "Unexpected JSON token at offset 0…", which tells the user nothing actionable.
- Not every local model honours Ollama's `format` schema. When a model consistently answers in prose or YAML, that's the model, not the prompt — `StructureFixingParser` (an extra repair call per failure, with the token-accounting caveat noted above) is the alternative to switching models.

## Testing

`:shared` has `commonTest` (pure logic: `FileNameSanitizerTest`, `FileTypeClassifierTest`) and `jvmTest` (file I/O against temp dirs, plus `OrganizerScreenRenderTest` and `components/ModelPickerDialogRenderTest`).

Both render tests compose off-screen with `ImageComposeScene` (including the `ModelPickerDialog`'s `androidx.compose.ui.window.Dialog` — it renders fine headless in this harness, no real window needed) — they're crash smoke tests for composition/layout failures, not visual ones. When adding a new file status or screen state, add it to `OrganizerScreenRenderTest`'s `populatedState`; new model-capability combinations go in `ModelPickerDialogRenderTest`. Visual review is done by running the app.

## Not built yet

- PDF content extraction (`FileType.PDF` files are scanned and listed but never sent to the model).
- No persistence — folder, history and token totals are lost on exit.
