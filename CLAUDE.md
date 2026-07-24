# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

AIFileOrganizer is a Kotlin Multiplatform (KMP) project currently targeting **Desktop (JVM) only**. It uses Compose Multiplatform for UI and the [Koog](https://github.com/JetBrains/koog) agent framework to talk to a **locally running Ollama** instance for AI features. The project is an early-stage scaffold (largely the default KMP/Compose template) with a single working AI integration point in `Agent.kt`.

## Commands

- Run the desktop app: `./gradlew :desktopApp:run`
- Run with hot reload: `./gradlew :desktopApp:hotRun --auto`
- Build everything: `./gradlew build`
- Run tests: `./gradlew test` (test infra is wired via `commonTest` + `kotlin-test`, but no test sources exist yet)
- Package native distributables (dmg/msi/deb): `./gradlew :desktopApp:packageDmg`, `packageMsi`, `packageDeb`

The app calls a local Ollama server at `http://localhost:11434` — Ollama must be running with the `gemma3:4b` model pulled for the AI agent features to work (see Architecture below).

## Architecture

Two Gradle modules, declared in `settings.gradle.kts`:

- **`:shared`** — KMP library. `commonMain` holds all UI and shared logic; `jvmMain` holds JVM-specific `actual` implementations (currently just `Platform.jvm.kt`). Even though only the JVM target is wired up today, code should still go through the `expect`/`actual` pattern (see `Platform.kt` / `Platform.jvm.kt`) rather than assuming JVM-only APIs, since the module is structured for future multiplatform targets.
- **`:desktopApp`** — JVM application module. `main.kt` is the entry point; it just opens a `Window` and renders the shared `App()` composable. Desktop packaging config (app name, native distribution formats) lives in `desktopApp/build.gradle.kts`.

Key files in `shared/src/commonMain/kotlin/com/manshal79/aifileorganizer/`:

- `App.kt` — root `@Composable`, currently a demo screen (button reveals a greeting + an AI-generated response rendered as Markdown via `multiplatform-markdown-renderer`).
- `Agent.kt` — wraps a Koog `AIAgent` configured with `MultiLLMPromptExecutor(OllamaClient())` and a custom `LLModel` pointing at `gemma3:4b`. `Agent.ask(prompt)` is the single entry point for LLM calls and already catches connection failures, returning an error string rather than throwing (Ollama not running is the expected failure mode to handle when touching this code).
- `Platform.kt` / `Platform.jvm.kt` — `expect`/`actual` platform abstraction.

Dependency versions are centralized in `gradle/libs.versions.toml` (version catalog) — add new dependencies there, not as inline coordinates in module `build.gradle.kts` files.

## Notes for AI-feature work

- The Koog `LLModel` in `Agent.kt` hardcodes provider (`LLMProvider.Ollama`), model id (`gemma3:4b`), and capabilities (`Temperature`, `Schema.JSON.Basic`, `Tools`). If you change the model, keep the `capabilities` list consistent with what that model actually supports.
- `Agent` is currently constructed directly in composables (`Agent()` in `App.kt`) rather than injected — there is no DI framework set up in this project yet.
