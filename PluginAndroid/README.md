# Dead Code Detector Gradle Plugin (Android Edition)

> ⚠️ Android support is in progress

**Dead Code Detector** is a Kotlin-based Gradle plugin that analyzes compiled classes, Android resources, and dependencies, generating a report of unused (dead) code and libraries in your project.

---

## Table of Contents

* [Why use this plugin?](#why-use-this-plugin)
* [Features](#features)
* [Quickstart](#quickstart)
* [Configuration](#configuration)
* [Tasks](#tasks)
* [Report format & example](#report-format--example)
* [CI integration](#ci-integration)
* [How it works](#how-it-works)
* [Limitations & notes](#limitations--notes)
* [Contributing](#contributing)
* [License](#license)

---

## Why use this plugin?

Maintaining a clean Android codebase improves build times, maintainability, and developer confidence. This plugin helps you detect:

* Unused classes, top-level functions, and fields
* Dead resources (layouts, drawables, etc.)
* Unused dependencies

---

## Features

* Detects unused classes, methods, and fields in Kotlin/Java Android projects
* Detects unused Android resources (`res/`)
* Detects unused dependencies
* Generates grouped, human-readable reports
* Configurable: keep public API, include/exclude tests, scan/exclude resources, ignore packages or annotations
* Optionally fail the build if dead code is detected

---

## Quickstart

Apply the **Android-specific plugin** in your `app/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("io.github.arya458.dead-code-detector-android")
}

android {
    namespace = "io.github.arya458.testandroidjetpack"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.arya458.testandroidjetpack"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// Dead Code Detector Configuration

deadCodeDetector {
    failOnDeadCode = true
    includeResources = true
    keepPublicApi = true
}
```

Run the detector manually:

```bash
./gradlew deadCodeDetectorDebug
./gradlew deadCodeDetectorRelease
```

Default report location:

```
app/build/reports/dead-code-detector/report.txt
```

---

## Configuration

```kotlin
deadCodeDetector {
    failOnDeadCode = true          // Fail the build if dead code is detected
    includeResources = true        // Scan Android resources (res/)
    keepPublicApi = true           // Ignore public API from detection
    excludePackages.add("com.mycompany.generated")  // Exclude packages
    keepAnnotations.add("javax.inject.Inject")     // Ignore elements with annotations
}
```

**Notes:**

* Run after building the app (`./gradlew assembleDebug`) so compiled classes and resources exist.
* If `keepPublicApi = false`, public functions/classes may be flagged even if used by other modules.

---

## Tasks

* `deadCodeDetectorDebug` — scans the debug variant
* `deadCodeDetectorRelease` — scans the release variant
* Integration with `check` can be added for CI pipelines

---

## Report format & example

```
# Dead Code Detector Report

Summary:
  * Dead methods: 2
  * Dead fields: 1
  * Dead classes: 1
  * Dead resources: 3
  * Unused dependencies: 1

Dead Classes:
  * com.example.unused.MyUnusedClass

Dead Methods:
Class: com.example.MainKt • deadCode(String) : void

Dead Fields:
Class: com.example.MainKt • unusedValue : String

Dead Resources:
  * res/layout/old_layout.xml
  * res/drawable/old_icon.png

Unused Dependencies:
  * implementation("com.squareup.retrofit2:retrofit:2.9.0")
```

---

## CI Integration

GitHub Actions example:

```yaml
name: CI
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
      - name: Run Gradle build + dead-code-detector
        run: ./gradlew assembleDebug deadCodeDetectorDebug --no-daemon
```

Set `failOnDeadCode = false` if you don’t want CI to fail.

---

## How it works

The plugin scans compiled class files, Android resources, and dependency references to detect unused elements. Reflection, generated code, or annotation processors may create false positives.

---

## Limitations & Notes

* **Local variables** are ignored (removed by Kotlin/Java compiler).
* **Reflection/dynamic calls** may appear unused.
* **Public API**: enable `keepPublicApi = true` to avoid false positives.
* Run after compiling the project to ensure class files exist.

---

## Contributing

* Ideas: HTML/colored reports, integration with Detekt, more annotation rules
* Pull requests and issues are welcome

---

## License

MIT License
