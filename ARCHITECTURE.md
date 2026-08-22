# Architecture – Dead Code Detector Gradle Plugin

> Branch: `early`  
> Status: Modular skeleton (migration in progress)

## Goals

- Clean separation of concerns (SOLID)
- Easy to add new platforms (Open/Closed)
- `core` must be free of Gradle API (testable in isolation)
- Platform-specific logic isolated via Strategy pattern

## Module Map

```
Dead-Code-Detector-Gradle-Plugin/
├── core/                          # Pure analysis – NO Gradle dependency
│   └── io.github.arya458.deadcode.core
│       ├── model/
│       ├── scanner/
│       ├── analyzer/              # CallGraph, Reachability, DeadCodeAnalyzer
│       ├── platform/              # PlatformKeepRules, EntryPointDetector interfaces
│       ├── report/
│       └── util/
│
├── platforms/
│   ├── android/                   # AndroidKeepRules + Manifest + Resources
│   ├── kmp/                       # KMP source-set aware rules
│   ├── spring/                    # Spring annotations + config scanning
│   └── ktor/                      # Ktor routing + application.conf
│
├── gradle-plugin/                 # Plugin + Extension + Task (Gradle API only here)
│   └── io.github.arya458.deadcode
│       ├── DeadCodeDetectorPlugin
│       ├── DeadCodeDetectorExtension
│       ├── task/
│       └── internal/              # Platform detection, wiring, adapters
│
├── Plugin/                        # LEGACY – will be removed after full migration
└── samples/                       # (future) test projects
```

## Key Interfaces (core)

```kotlin
interface PlatformKeepRules {
    fun shouldKeepClass(className: String, annotations: Map<String, Set<String>>): Boolean
    fun shouldKeepMethod(method: MethodRef, classAnnotations: Map<String, Set<String>>): Boolean
    fun shouldKeepField(field: FieldRef): Boolean
    fun shouldKeepResource(type: String, name: String): Boolean
}

interface EntryPointDetector {
    fun detect(context: AnalysisContext): Set<EntryPoint>
}

interface CallGraphBuilder {
    fun build(scan: ClassScanModel): CallGraph
}

interface ReachabilityAnalyzer {
    fun findUnreachable(graph: CallGraph, entryPoints: Set<EntryPoint>): DeadCodeModel
}
```

## Platform Registration

Platforms register themselves via `PlatformRegistry` (or ServiceLoader later).

```kotlin
object PlatformRegistry {
    fun register(rules: PlatformKeepRules)
    fun register(detector: EntryPointDetector)
    fun resolve(context: AnalysisContext): PlatformContext
}
```

## Migration Plan

1. ✅ Skeleton modules + interfaces
2. Move models + ClassScanner + DeadCodeAnalyzer into `core`
3. Move keep rules into platform modules
4. Rewrite Task as thin orchestrator in `gradle-plugin`
5. Delete legacy `Plugin/` module
6. Add real Call Graph + Reachability
7. Tests + CI

## Dependency Rules

| Module          | Can depend on          | Must NOT depend on     |
|-----------------|------------------------|------------------------|
| core            | ASM, ClassGraph, Kotlin| Gradle, Android, Spring|
| platforms:*     | core                   | Gradle API             |
| gradle-plugin   | core + platforms       | –                      |
