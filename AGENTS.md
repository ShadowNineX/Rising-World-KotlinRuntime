# Repository Guidelines

## Project Structure & Module Organization

This is a compact Gradle Kotlin JVM plugin project for the Rising World API. Main Kotlin code lives in `src/main/kotlin/shadownine/KotlinRuntime/`, with `KotlinPlugin.kt` as the plugin entry point. Runtime/plugin resources live in `src/main/resources/`; `resources/plugin.yml` describes the Rising World plugin metadata, and `ModInfo.kt.template` is expanded during the build into generated Kotlin source. Gradle wrapper and build configuration files are at the repository root. Generated outputs go to `build/` and packaged artifacts go to `dist/`; do not commit generated artifacts unless a release process explicitly requires it.

## Build, Test, and Development Commands

Use the Gradle wrapper from the repository root:

```powershell
.\gradlew.bat build
```

Builds the Kotlin runtime JAR, generates `ModInfo.kt`, copies the JAR into `dist/Kotlin-Runtime`, and creates a versioned ZIP in `dist/`.

```powershell
.\gradlew.bat clean
```

Removes `build/`, `dist/`, and `target/` outputs.

```powershell
.\gradlew.bat tasks
```

Lists available Gradle tasks, including custom packaging tasks such as `fatJar` and `zipDist`.

Before building, set `PLUGINAPIJAR` in `gradle.properties` to the local Rising World `PluginAPI.jar` path, for example `PLUGINAPIJAR=C:/Path/To/PluginAPI.jar`.

## Coding Style & Naming Conventions

Use Kotlin idioms and 4-space indentation. Keep packages under `shadownine.KotlinRuntime` unless there is a clear reason to introduce a new namespace. Use PascalCase for classes and objects, camelCase for functions and variables, and concise names that match Rising World plugin concepts. Avoid bundling `PluginAPI.jar`; it is intentionally declared as `compileOnly`.

## Testing Guidelines

There is currently no dedicated `src/test` tree or test framework configured. For behavior changes, at minimum run `.\gradlew.bat build` and manually verify the plugin loads in a Rising World game or server with the expected startup/shutdown log messages. If tests are added later, place them under `src/test/kotlin` and wire them through Gradle's `test` task.

## Commit & Pull Request Guidelines

Recent commits use short imperative messages such as `Fix version` or descriptive summaries such as `Rename project to ShadowNine and update build`. Keep commits focused and mention versioning, packaging, or runtime behavior when relevant. Pull requests should include a short description, build verification, any manual Rising World test results, and linked issues when applicable. Include screenshots or logs only when they clarify plugin loading or packaging behavior.

## Security & Configuration Tips

Keep local machine paths, especially `PLUGINAPIJAR`, in `gradle.properties` and out of committed source changes when they are user-specific. Do not check in proprietary or external API JARs.
