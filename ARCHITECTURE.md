# Architecture – Dead Code Detector (branch `early`)

## Modules

| Module | Role | Gradle API? |
|--------|------|-------------|
| `core` | Bytecode scan, DeadCodeAnalyzer, models, PlatformKeepRules interfaces | No |
| `platforms:android` | AndroidKeepRules | No |
| `platforms:kmp` | KmpKeepRules | No |
| `platforms:spring` | SpringKeepRules | No |
| `platforms:ktor` | KtorKeepRules | No |
| `gradle-plugin` | Plugin, Extension, Task, Project adapters, ReportWriter | Yes |

## Flow

1. `DeadCodeDetectorTask` resolves class dirs via `ClassDirectoryResolver`
2. `BytecodeClassScanner` (core) builds `ClassScanModel`
3. `ResourceScanner` / `DependencyAnalyzer` (gradle-plugin) gather resources & deps
4. `PlatformResolver` picks Strategy keep-rules
5. `DeadCodeAnalyzer` (core) computes dead code
6. `ReportWriter` writes txt/html/json

## Legacy

The old monolithic `Plugin/` module has been removed from `settings.gradle.kts`.

## Version

`0.1.0-early`
