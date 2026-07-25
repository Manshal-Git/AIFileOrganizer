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
- `domain/usecase/GetAvailableOllamaModelsUseCase.kt` — wraps `OllamaClient.getModels()` in a `Result`; this is also how `OrganizerViewModel` detects "Ollama unreachable".
- `presentation/organizer/OllamaModelUi.kt` — UI-safe projection of Koog's `OllamaModelCard` (`toOllamaModelUi()`), with derived fields the model picker needs: formatted size/param/context labels, `supportsVision`/`supportsTools`, `supportedFileTypes`, `isRecommended` (== has vision, since that's the only capability gap that matters for this app's file types).
- `presentation/organizer/OrganizerViewModel.kt` — holds `availableModelCards: List<OllamaModelCard>` privately (not in state) so `currentLLModel()` can rebuild a real `LLModel` via Koog's `OllamaModelCard.toLLModel()` for whichever model is selected. `loadModels()` runs at `init` and again on every `scan()`/`OnRefreshModels`; a failed fetch leaves the last known-good list alone rather than clearing the picker.
- `domain/usecase/RenameSuggestionUseCase.kt` — builds the prompt and calls `promptExecutor.executeStructured<RenameSuggestion>()`. Takes `model: LLModel` per call now (not injected), so the ViewModel decides which model to use per request.
- `presentation/organizer/components/` — top bar, side nav, folder toolbar, file list/grid, stat cards, token usage bar, toast, `ModelPicker.kt` (header chip + accordion dialog for model selection), `OllamaUnavailableBanner.kt` (shown when a scan needs the LLM and Ollama didn't respond).

Dependency versions are centralized in `gradle/libs.versions.toml` (version catalog) — add new dependencies there, not as inline coordinates in module `build.gradle.kts` files.

## Notes for AI-feature work

- Models are user-selected now, not hardcoded — capabilities come from `OllamaModelCard.capabilities` (via `OllamaClient.getModels()` → Ollama's `/api/show`), not a fixed list. `Vision.Image` is required for image files — without it Koog rejects image parts in the prompt, which is why the picker flags models lacking it.
- Ollama not running is expected. It's handled at two levels: `OrganizerState.ollamaUnavailable` drives a prominent, persistent banner (`OllamaUnavailableBanner`) when a scan has suggestible files and the availability check fails — this is louder than the corner `StatusToast` on purpose, since a silent per-file failure spray was the previous (worse) behavior. Per-file failures (rename or extraction errors unrelated to connectivity) still surface as `FileItemStatus.Failed`.
- **Verify Koog APIs against the jars**, not against docs or memory — decompile from `~/.gradle/caches/modules-2/files-2.1/ai.koog/` with `javap`, or unzip the `-sources.jar` for readable Kotlin. The 1.0.0 API differs from most published examples.

### Token usage

Per-request token counts come from the provider's own accounting, not an estimate:

- `executeStructured` returns `Result<StructuredResponse<T>>`; `StructuredResponse.message.metaInfo` is a `ResponseMetaInfo` carrying `inputTokensCount` / `outputTokensCount` / `totalTokensCount` (all nullable).
- `OllamaClient` fills these from Ollama's `prompt_eval_count` and `eval_count`, and leaves `modelId` null.
- `RenameSuggestionUseCase` maps them into a `TokenUsage` and returns it alongside the suggestion in `RenameSuggestionResult`. The ViewModel stores it per file (`FileItemUi.tokenUsage`) and accumulates a session total (`OrganizerState.tokenUsage` / `llmRequestCount`), rendered by `TokenUsageBar` and the per-row token chip.
- This accounts for exactly one LLM call because no `StructureFixingParser` is passed. If one is added, its repair calls burn tokens that never reach `StructuredResponse.message` — at that point move the accounting into a `PromptExecutor` decorator instead.

## Testing

`:shared` has `commonTest` (pure logic: `FileNameSanitizerTest`, `FileTypeClassifierTest`) and `jvmTest` (file I/O against temp dirs, plus `OrganizerScreenRenderTest` and `components/ModelPickerDialogRenderTest`).

Both render tests compose off-screen with `ImageComposeScene` (including the `ModelPickerDialog`'s `androidx.compose.ui.window.Dialog` — it renders fine headless in this harness, no real window needed) — they're crash smoke tests for composition/layout failures, not visual ones. When adding a new file status or screen state, add it to `OrganizerScreenRenderTest`'s `populatedState`; new model-capability combinations go in `ModelPickerDialogRenderTest`. Visual review is done by running the app.

## Not built yet

- PDF content extraction (`FileType.PDF` files are scanned and listed but never sent to the model).
- No persistence — folder, history and token totals are lost on exit.
