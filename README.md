# ⚠️ Not Working On Android Im Working On It
# Dead Code Detector Gradle Plugin

**Dead Code Detector** is a Kotlin-based Gradle plugin that analyzes compiled classes, resources, and dependencies, generating a human-readable report of unused (dead) code, resources, and libraries in your project.

---

## Table of contents

* [Why use this plugin?](#why-use-this-plugin)
* [Features](#features)
* [Quickstart (install & run)](#quickstart-install--run)
* [Configuration](#configuration)
* [Tasks](#tasks)
* [Report format & example](#report-format--example)
* [CI integration](#ci-integration)
* [How it works (brief)](#how-it-works-brief)
* [Limitations & notes](#limitations--notes)
* [Contributing](#contributing)
* [License](#license)

---

## Why use this plugin?

Keeping a codebase tidy improves maintainability, build times, and developer confidence. This plugin helps you find:

* Unreferenced top-level functions and fields
* Unused classes
* Dead resources
* Unused dependencies

It produces a plain-text report you can read locally or archive in CI.

## Features

* Detects unused classes, top-level functions, and top-level fields in Kotlin/Java projects
* Detects unused dependencies in your Gradle build
* Detects unused resources (optionally scanning `src/main/resources` and `src/test/resources`)
* Grouped, human-readable reports (method signatures, field types, dependency notations)
* Configurable to keep public API, include/exclude tests, scan/exclude resources, exclude packages
* Support for ignoring elements annotated with specific annotations
* Optionally fail the build when dead code is detected
* Can run manually or be attached to `check` for CI

## Quickstart (install & run)

Apply the plugin in your module's `build.gradle.kts`:

```kotlin
plugins {
  id("io.github.arya458.dead-code-detector") version "0.0.5"
}
```

Then run the detector manually:

```bash
./gradlew deadCodeDetector
```

Or include it in your verification pipeline:

```bash
./gradlew check
```

By default the plugin writes its output to:

```
build/reports/dead-code-detector/report.txt
```

(You can override this in the configuration: see below.)

## Configuration

Configure the plugin with the `deadCodeDetector` extension in your module's `build.gradle.kts`:

```kotlin
deadCodeDetector {
    // Fail the build if any dead code is detected
    failOnDeadCode = true

    // Scan test classes + test resources when true
    includeTests = false

    // If true, public API (public functions/fields) are ignored from detection
    keepPublicApi = false

    // (Planned) Automatically clear all dead code — not yet implemented
    // clearAllDeadCode = true

    // Resource scanning
    includeResources = false
    resourceDir = "src/main/resources"
    testResourceDir = "src/test/res"

    // Add package prefixes (or regex-like entries) to exclude from scanning
    excludePackages.add("com.mycompany.generated")

    // Ignore elements annotated with these annotations
    keepAnnotations.add("javax.inject.Inject")
}
```

### Notes about configuration

* If you want to detect unused public API, set `keepPublicApi = false`. Be careful: this may flag intentionally public functions used by other modules or reflective consumers.
* If you enable `includeResources`, the plugin will scan your resource directories (`resourceDir`, `testResourceDir`) for unused entries.
* If you want to detect unused dependencies, simply run the plugin — unused dependency detection is included in the report.
* If the plugin cannot find compiled classes, run `./gradlew classes` (or your module's compile tasks) before running the detector.

## Tasks

* `deadCodeDetector` — main task that scans compiled classes, resources, and dependencies, producing the report
* Integration with `check` — if wired in the plugin, `./gradlew check` will run the detector (depending on configuration)

## Report format & example

The report is a plain text file grouped by dead classes, methods, fields, resources, and dependencies. A typical (shortened) example looks like this:

```
# Dead Code Detector Report

Summary:
  * Dead methods: 2
  * Dead fields: 1
  * Dead classes: 1
  * Dead resources: 1
  * Unused dependencies: 1

Dead Classes:
  * com.example.unused.MyUnusedClass

Dead Methods:
Class: com.example.MainKt • deadCode(String) : void

Dead Fields:
Class: com.example.MainKt • unusedValue : String

Dead Resources:
  * src/main/resources/old_config.json

Unused Dependencies:
  * implementation("com.squareup.retrofit2:retrofit:2.9.0")
```

The plugin attempts to present readable signatures, resource paths, and dependency notations.

## CI integration

Example GitHub Actions workflow:

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
        run: ./gradlew check --no-daemon
```

If you prefer the detector not to fail CI, set `failOnDeadCode = false` in the extension.

## How it works (brief)

The plugin analyzes compiled class files, resources, and dependency references to determine which items are never referenced. Bytecode-level analysis makes detection reliable for compiled artifacts, but reflection or generated code may cause false positives.

If you plan to use this in multi-module projects, run the detector in modules where compiled classes are available; otherwise, point to the compiled class directories if supported by the plugin configuration.

## Limitations & notes

* **Local variables** are not detected (Kotlin compiler may remove them in bytecode).
* **Reflection, dynamic calls, annotation processors, and dependency injection** can create false positives — code/resources/dependencies used only reflectively might appear unused.
* **Public API**: if your project exposes public functions/classes used by external consumers, set `keepPublicApi = true` to avoid false positives.
* Run the plugin after building the classes (`./gradlew classes`) so compiled output is present.
* `clearAllDeadCode` is currently a planned feature and not implemented.

## Contributing

Contributions and issues are welcome.

Ideas for improvements:

* Implement `clearAllDeadCode` option
* Add HTML output or colored console output
* Add more fine-grained ignore rules (annotations, regex, resource types)
* Add integration with Detekt or other static analysis tools

Please open issues or PRs against this repository.

## License

MIT License — feel free to use and modify the code.

---

*Generated by a friendly README polish to make the repository easier for others to use.*
