# Architecture – branch `early`

## Local development (plugin resolution)

All modules live in **one** Gradle multi-project build:

```
:core
:platforms:android | kmp | spring | ktor
:gradle-plugin
:TestApp | :TestSpring
```

Samples apply the plugin like this (no Maven publish required):

```kotlin
buildscript {
    dependencies {
        classpath(project(":gradle-plugin"))
    }
}
apply(plugin = "io.github.arya458.dead-code-detector")
```

After publishing to the Plugin Portal / mavenLocal, consumers use:

```kotlin
plugins {
    id("io.github.arya458.dead-code-detector") version "x.y.z"
}
```
