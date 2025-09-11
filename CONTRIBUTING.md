# Contributing to Dead Code Detector

Thank you for considering contributing! 🙏  
Your help keeps this plugin reliable, up-to-date, and useful for everyone.

---

## Ways to Contribute

### 1. Bug Reports / Issues
- Open an issue if you find a bug, a false positive, or missing feature.
- Include:
    - Plugin version (e.g., `0.0.3` or latest)
    - Gradle version
    - Minimal reproducible example or sample project
    - Expected vs. actual behavior

### 2. Pull Requests
- Branch from `main` and keep commits focused.
- Include a short, descriptive commit message.
- Reference the issue number if applicable.

### 3. Documentation
- Fix typos, clarify explanations, or add examples.
- Docs are located in the `docs/` folder.
- Feel free to improve layout, clarity, or code samples.

### 4. Feature Suggestions
- Open an issue before implementing major new features.
- Examples:
    - Better ignore rules
    - Enhanced HTML report (charts, tables)
    - CI/CD integration improvements

---

## Code Style & Best Practices

- Maintain readability and consistency in Kotlin/Java and Gradle DSL (`build.gradle.kts`).
- Use descriptive variable names and small, focused functions.
- Avoid committing auto-generated or build files (`build/`).

---

## Development Workflow

1. Fork the repository.
2. Clone your fork locally:
   ```bash
   git clone https://github.com/yourusername/Dead-Code-Detector-Gradle-Plugin.git
   ```
3. Create a branch:
   ```bash
   git checkout -b my-feature
   ```
4. Make your changes and commit:
   ```bash
   git add .
   git commit -m "Short, descriptive commit message"
   ```
5. Push your branch:
   ```bash
   git push origin my-feature
   ```
6. Open a Pull Request against `main`.

---

## Code Review & Feedback

- PRs are usually reviewed within a few days.
- Expect constructive feedback — small, incremental improvements are preferred.
- PRs may be merged once approved and after passing CI checks.

---

## License & Attribution

All contributions are licensed under **MIT**, same as the project.  
By contributing, you agree to license your contributions under MIT.

---

**Thank you** for helping improve Dead Code Detector!  
Your contributions make it safer, cleaner, and more efficient for everyone. 🚀
