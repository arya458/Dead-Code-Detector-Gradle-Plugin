# Architecture – Dead Code Detector (branch `early`)

## Build layout

```
root (samples)
├── TestApp /
├── TestSpring /
└── pluginManagement.includeBuild("gradle-plugin")

gradle-plugin/          ← included build (provides the Gradle plugin)
├── settings.gradle.kts   maps ../core and ../platforms/*
├── build.gradle.kts      java-gradle-plugin
├── ../core
└── ../platforms/{android,kmp,spring,ktor}
```

This is the standard Gradle composite pattern so that samples can resolve:

```kotlin
plugins {
    id("io.github.arya458.dead-code-detector")
}
```

without publishing to Maven first.

## Modules

| Module | Role | Gradle API? |
|--------|------|-------------|
| `core` | Bytecode scan, DeadCodeAnalyzer, models | No |
| `platforms:*` | Platform keep-rules (Strategy) | No |
| `gradle-plugin` | Plugin + Extension + Task + adapters | Yes |

## Version

`0.1.0-early`
