# Architecture – branch `early`

## Plugin resolution (local)

```
root settings
  pluginManagement { includeBuild("gradle-plugin") }
  include(TestApp, TestSpring)

gradle-plugin/   ← included build
  settings maps ../core, ../platforms/*
  registers id("io.github.arya458.dead-code-detector")
```

Samples:
```kotlin
plugins {
    id("io.github.arya458.dead-code-detector")  // no version needed
}
```

## Build plugin alone

```bash
./gradlew --project-dir gradle-plugin jar
# or from root after composite resolves:
./gradlew :TestApp:deadCodeDetector
```
