Dead Code Detector Gradle Plugin

Dead Code Detector is a Kotlin-based Gradle plugin that analyzes compiled classes and detects unused code in your project.

It identifies:

🔹 Unused top-level functions

🔹 Unused top-level fields (val/var)

🔹 Unused classes

The plugin generates a detailed, human-readable report, helping you maintain a clean and maintainable codebase.

Features

Detects dead methods, fields, and classes in Kotlin/Java projects

Generates grouped, readable reports with human-readable method signatures and field types

Configurable options for keeping public API or excluding specific packages

Optionally fails the build if dead code is detected

Can integrate with Gradle check or run manually

Installation
Step 1: Apply the plugin
plugins {
id("com.github.arya458.dead-code-detector") version "0.1.0"
}

Step 2: Configure the plugin
deadCodeDetector {
failOnDeadCode = false           // Set true to fail the build on dead code
keepPublicApi = false            // Set false to detect unused public functions/fields
includeTests = false             // Set true to scan test classes
excludePackages.add("com.external.lib") // Exclude specific packages
}

Step 3: Run the detector

Run manually:

./gradlew deadCodeDetector


Or automatically during check:

./gradlew check


Report location:

build/reports/dead-code-detector/report.txt

Report Format

Summary: Total dead methods, fields, and classes

Dead Classes: Classes never referenced

Dead Methods: Grouped by class with readable method signatures

Dead Fields: Grouped by class with human-readable types

Example snippet:

Dead Code Detector Report
=========================

Summary:
- Dead methods: 2
- Dead fields: 1
- Dead classes: 1

Dead Classes:
- com.example.unused.MyUnusedClass

Dead Methods:
Class: com.example.MainKt
• deadCode(String) : void

Dead Fields:
Class: com.example.MainKt
• unusedValue : String

Notes

⚠️ Local variables inside functions are not detected because the Kotlin compiler may remove them in bytecode

✅ Works best on compiled classes; run ./gradlew classes before scanning if needed

📌 Can be integrated into CI pipelines for automated code quality checks

Contribution

Contributions are welcome! You can help by:

Adding detection for unused local variables (source-level analysis)

Improving report formatting (HTML or colored output)

Integrating with other static analysis tools like Detekt

License

MIT License – free to use and modify