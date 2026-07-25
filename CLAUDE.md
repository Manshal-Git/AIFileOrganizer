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

The app calls a local Ollama server at `http://localhost:11434` — Ollama must be running with the `gemma3:4b` model pulled for the AI features to work.

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
        │                domain/model — ScannedFile, RenameSuggestion, TokenUsage, …
        ▼
data/filesystem          FileScanner, FileRenamer, FileTypeClassifier, FileNameSanitizer
data/content             TextContentExtractor
```

- `di/AppModule.kt` (commonMain) provides the `LLModel`, `PromptExecutor`, use cases and ViewModel. `di/PlatformModule.kt` (jvmMain) binds the JVM implementations of the data interfaces. Both are started in `main.kt`.
- The ViewModel coordinates; business logic lives in use cases and the `data/` helpers. Follow that split when adding features.
- The screen is the only place a ViewModel is touched (`OrganizerRoot`); everything under `components/` takes plain state + lambdas so it stays previewable.

### Key files

- `di/AppModule.kt` — the Koog `LLModel`: provider `LLMProvider.Ollama`, id `gemma3:4b`, capabilities `Temperature`, `Schema.JSON.Basic`, `Tools`, `Vision.Image`, `contextLength = 40_960`.
- `domain/usecase/RenameSuggestionUseCase.kt` — builds the prompt and calls `promptExecutor.executeStructured<RenameSuggestion>()`. Single entry point for LLM calls.
- `presentation/organizer/OrganizerViewModel.kt` — scan → suggest loop, rename, undo, token accounting.
- `presentation/organizer/components/` — top bar, side nav, folder toolbar, file list/grid, stat cards, token usage bar, toast.

Dependency versions are centralized in `gradle/libs.versions.toml` (version catalog) — add new dependencies there, not as inline coordinates in module `build.gradle.kts` files.

## Notes for AI-feature work

- If you change the model, keep the `capabilities` list consistent with what that model actually supports. `Vision.Image` is required for image files — without it Koog rejects image parts in the prompt.
- Ollama not running is the expected failure mode. Failures surface per file as `FileItemStatus.Failed`, and folder-level failures as `OrganizerState.error`.
- **Verify Koog APIs against the jars**, not against docs or memory — decompile from `~/.gradle/caches/modules-2/files-2.1/ai.koog/` with `javap`. The 1.0.0 API differs from most published examples.

### Token usage

Per-request token counts come from the provider's own accounting, not an estimate:

- `executeStructured` returns `Result<StructuredResponse<T>>`; `StructuredResponse.message.metaInfo` is a `ResponseMetaInfo` carrying `inputTokensCount` / `outputTokensCount` / `totalTokensCount` (all nullable).
- `OllamaClient` fills these from Ollama's `prompt_eval_count` and `eval_count`, and leaves `modelId` null.
- `RenameSuggestionUseCase` maps them into a `TokenUsage` and returns it alongside the suggestion in `RenameSuggestionResult`. The ViewModel stores it per file (`FileItemUi.tokenUsage`) and accumulates a session total (`OrganizerState.tokenUsage` / `llmRequestCount`), rendered by `TokenUsageBar` and the per-row token chip.
- This accounts for exactly one LLM call because no `StructureFixingParser` is passed. If one is added, its repair calls burn tokens that never reach `StructuredResponse.message` — at that point move the accounting into a `PromptExecutor` decorator instead.

## Testing

`:shared` has `commonTest` (pure logic: `FileNameSanitizerTest`, `FileTypeClassifierTest`) and `jvmTest` (file I/O against temp dirs, plus `OrganizerScreenRenderTest`).

`OrganizerScreenRenderTest` renders the screen off-screen with `ImageComposeScene` in each state — it's a crash smoke test for composition/layout failures, not a visual one. When adding a new file status or screen state, add it to `populatedState` there. Visual review is done by running the app.

## Not built yet

- PDF content extraction (`FileType.PDF` files are scanned and listed but never sent to the model).
- No persistence — folder, history and token totals are lost on exit.
