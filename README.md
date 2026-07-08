# ☠️ Dead Code Detector Gradle Plugin

> **Find and remove unused code before it becomes technical debt.**

A powerful Gradle plugin that detects **unused classes, methods, fields, resources, and dependencies** across **JVM, Spring Boot, Android, and Kotlin Multiplatform** projects.

📱 **Looking for the Android version?**

👉 https://github.com/arya458/Dead-Code-Detector-Gradle-Plugin/tree/main/PluginAndroid

---

<p align="center">

[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.arya458.dead-code-detector?style=for-the-badge)](https://plugins.gradle.org/plugin/io.github.arya458.dead-code-detector)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](https://opensource.org/licenses/MIT)
![Kotlin](https://img.shields.io/badge/Kotlin-2.x-purple?style=for-the-badge)
![ASM](https://img.shields.io/badge/ASM-Bytecode-green?style=for-the-badge)

</p>

---

# 📚 Table of Contents

* [Why Dead Code Detector?](#-why-dead-code-detector)
* [Features](#-features)
* [Installation](#-installation)
* [Configuration](#-configuration)
* [Reports](#-reports)
* [How It Works](#-how-it-works)
* [Supported Platforms](#-supported-platforms)
* [Roadmap](#-roadmap)
* [Contributing](#-contributing)
* [License](#-license)
* [Contact](#-contact)

---

# 🚀 Why Dead Code Detector?

As projects grow, unused code inevitably accumulates.

Dead classes, methods, fields, resources, and dependencies increase maintenance costs, slow down development, and make codebases harder to understand.

Dead Code Detector helps you automatically identify unused code so you can safely remove it and keep your project clean and maintainable.

---

# ✨ Features

| Feature                         | Supported |
| ------------------------------- | :-------: |
| Detect unused classes           |     ✅     |
| Detect unused methods           |     ✅     |
| Detect unused fields            |     ✅     |
| Detect unused dependencies      |     ✅     |
| Detect unused Android resources |     ✅     |
| Spring Boot support             |     ✅     |
| Kotlin Multiplatform support    |     ✅     |
| JVM projects                    |     ✅     |
| Platform-aware keep rules       |     ✅     |
| Custom keep annotations         |     ✅     |
| Parallel scanning               |     ✅     |
| Interactive HTML report         |     ✅     |
| JSON report                     |     ✅     |
| Plain text report               |     ✅     |

---

# 📦 Installation

Add the plugin to your `build.gradle.kts`.

```kotlin
plugins {
    id("io.github.arya458.dead-code-detector") version "0.0.10"
}
```

Run:

```bash
./gradlew deadCodeDetector
```

---

# ⚙️ Configuration

```kotlin
deadCodeDetector {

    // Fail the build if dead code is found
    failOnDeadCode = false

    // Include test sources
    includeTests = false

    // Keep public API
    keepPublicApi = true

    // auto | android | spring | kmm
    platform = "auto"

    // Resources
    includeResources = true
    resourceDir = "src/main/res"
    testResourceDir = "src/test/res"

    // Dependency analysis
    analyzeDependencies = true
    failOnUnusedDependencies = false

    // Spring configuration scanning
    scanConfigFiles = true
    configDirs = listOf("src/main/resources")

    // Exclusions
    excludePackages.add("com.example.generated")
    excludeClasses.add("com.example.Legacy")

    // Keep annotations
    keepAnnotations.add("javax.inject.Inject")

    // Advanced keep rules
    customKeepRules = { type, annotations, method ->
        false
    }

    // Performance
    parallelScan = true
}
```

---

# 📊 Reports

Generated reports are located at:

```text
build/
└── reports/
    └── dead-code-detector/
        ├── report.txt
        ├── report.html
        └── report.json
```

## 📄 Text Report

A human-readable report suitable for terminals and CI logs.

---

## 🌐 HTML Report

Interactive report with:

* Search
* Filtering
* Statistics
* Grouped results
* Responsive interface

---

## 📦 JSON Report

Perfect for:

* CI/CD pipelines
* GitHub Actions
* SonarQube
* Custom tooling
* Automated quality checks

---

# 📋 Example Output

```text
==== Dead Code Detector ====

Dead classes          : 5
Dead methods          : 18
Dead fields           : 9
Unused resources      : 14
Unused dependencies   : 2

✔ Reports generated successfully
```

---

# 🧠 How It Works

The plugin analyzes compiled bytecode using **ASM**.

It automatically:

* Collects all classes
* Collects all methods
* Collects all fields
* Tracks references
* Detects unused code
* Scans Android resources
* Scans Spring configuration
* Detects unused Gradle dependencies
* Applies platform-specific keep rules
* Generates detailed reports

---

# 🚀 Supported Platforms

| Platform             | Status |
| -------------------- | :----: |
| JVM                  |    ✅   |
| Spring Boot          |    ✅   |
| Android              |    ✅   |
| Kotlin Multiplatform |    ✅   |

---

# 🗺️ Roadmap

Future improvements include:

* IntelliJ IDEA plugin
* Maven plugin
* Automatic dead code removal
* SARIF report generation
* SonarQube integration
* GitHub Code Scanning support
* Incremental analysis
* Performance improvements

---

# 🤝 Contributing

Contributions are welcome!

Whether you'd like to:

* 🐞 Report a bug
* ✨ Suggest a feature
* 📖 Improve the documentation
* 💻 Submit a Pull Request

please read **CONTRIBUTING.md** before contributing.

---

# 📜 License

This project is licensed under the **MIT License**.

Copyright © 2025 Aria Danesh

See the **LICENSE** file for details.

---

# 📬 Contact

**Aria Danesh**

📧 [aria.danesh.work@gmail.com](mailto:aria.danesh.work@gmail.com)

🐙 GitHub: https://github.com/arya458

---

# ⭐ Support the Project

If you find this plugin useful, please consider giving the repository a ⭐ on GitHub.

It helps the project reach more developers and motivates future improvements.

Thank you for your support! ❤️
