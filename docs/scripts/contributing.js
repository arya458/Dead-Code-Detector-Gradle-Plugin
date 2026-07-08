(function() {
    "use strict";

    // ---------- DATA (single source of truth) ----------
    const FOOTER_TEXT = `© 2025 Aria Danesh — Dead Code Detector · MIT License · Contact: <a href="mailto:aria.danesh.work@gmail.com">aria.danesh.work@gmail.com</a>`;

    // main content: arrays of sections (cards)
    const CONTENT_SECTIONS = [
        {
            type: "card",
            title: "🙌 Contributing to Dead Code Detector",
            body: `<p>Thank you for your interest in making this plugin better! Whether you're fixing a bug, adding a feature, or improving docs – every contribution matters.</p>
                   <p>— <strong>Aria Danesh</strong> (maintainer)</p>`
        },
        {
            type: "card",
            title: "🔍 Ways to Contribute",
            body: `<ul>
                       <li><strong>Report bugs / false positives</strong> – include plugin version, Gradle version, and a minimal reproducer.</li>
                       <li><strong>Suggest features</strong> – open an issue first to discuss the design.</li>
                       <li><strong>Submit pull requests</strong> – focus on small, well‑tested changes.</li>
                       <li><strong>Improve documentation</strong> – fix typos, add examples, or clarify wording.</li>
                   </ul>`
        },
        {
            type: "card",
            title: "🛠️ Development Setup",
            body: `<ol>
                       <li>Fork the repo and clone:
                           <pre><code>git clone https://github.com/your-username/Dead-Code-Detector-Gradle-Plugin.git</code></pre>
                       </li>
                       <li>Open the project in <strong>IntelliJ IDEA</strong> (or any Kotlin‑friendly IDE).</li>
                       <li>Build and test locally:
                           <pre><code>./gradlew build</code></pre>
                       </li>
                   </ol>
                   <p class="muted">The plugin uses <strong>ASM</strong> for bytecode analysis and <strong>Gradle's SourceSet</strong> API for classpath discovery.</p>`
        },
        {
            type: "card",
            title: "📁 Project Structure",
            body: `<pre><code>Plugin/src/main/kotlin/io/github/arya458/
├── DeadCodeDetectorExtension.kt      # Configuration DSL
├── DeadCodeDetectorPlugin.kt         # Plugin entry point
├── analysis/
│   ├── ClassScanner.kt               # ASM bytecode scanner
│   ├── DeadCodeAnalyzer.kt           # Core analysis logic
│   ├── DependencyAnalyzer.kt         # Unused dependency detection
│   ├── ResourceScanner.kt            # Resource scanning (Android/Spring)
│   └── platform/
│       ├── PlatformKeepRules.kt      # Interface for platform rules
│       ├── AndroidKeepRules.kt
│       ├── SpringKeepRules.kt
│       └── KmmKeepRules.kt
├── model/                            # Data classes
├── report/
│   └── ReportWriter.kt               # Text/HTML/JSON report generation
└── task/
    └── DeadCodeDetectorTask.kt       # Gradle task implementation</code></pre>`
        },
        {
            type: "card",
            title: "➕ Adding a New Platform",
            body: `<p>If you want to add support for another framework (e.g., Quarkus, Micronaut):</p>
                   <ol>
                       <li>Create a new class implementing <code>PlatformKeepRules</code> (e.g., <code>QuarkusKeepRules</code>).</li>
                       <li>Add it to the <code>detectPlatformRules()</code> method in <code>DeadCodeAnalyzer</code>.</li>
                       <li>Update documentation and configuration samples.</li>
                   </ol>
                   <p class="muted">Example: <code>SpringKeepRules</code> keeps classes annotated with <code>@RestController</code> and methods with <code>@GetMapping</code>.</p>`
        },
        {
            type: "card",
            title: "✅ Code Style & Best Practices",
            body: `<ul>
                       <li>Follow <a href="https://kotlinlang.org/docs/coding-conventions.html" target="_blank">Kotlin coding conventions</a>.</li>
                       <li>Keep functions <strong>small and focused</strong> – each function should do one thing.</li>
                       <li>Write <strong>unit tests</strong> for new analysis rules.</li>
                       <li>Document public APIs with <strong>KDoc</strong>.</li>
                       <li>Use <code>project.logger</code> instead of <code>println</code> or <code>System.err</code>.</li>
                   </ul>`
        },
        {
            type: "card",
            title: "🧪 Testing",
            body: `<p>Run the integration test project in <code>test-project/</code> to verify changes manually. For automated testing, we use:</p>
                   <pre><code>./gradlew test</code></pre>
                   <p class="muted">If you add a new feature, please add a test case in <code>src/test/</code>.</p>`
        },
        {
            type: "card",
            title: "📥 Pull Request Process",
            body: `<ol>
                       <li>Branch from <code>main</code>.</li>
                       <li>Write clear commit messages (e.g., <code>fix(android): keep Fragment lifecycle methods</code>).</li>
                       <li>Ensure CI passes (GitHub Actions runs automatically).</li>
                       <li>Request review from <strong>@arya458</strong> (Aria Danesh).</li>
                       <li>Address feedback and keep the PR focused – it's easier to review small changes.</li>
                   </ol>
                   <p class="muted">I usually review PRs within 2–3 days. If I haven't responded, feel free to ping me.</p>`
        },
        {
            type: "card",
            title: "📌 Roadmap (Help Wanted)",
            body: `<ul>
                       <li>✅ HTML report with search/filter</li>
                       <li>⏳ Automatic dead code removal (opt‑in, dangerous)</li>
                       <li>⏳ Better ignore rules (regex, annotation patterns, file extensions)</li>
                       <li>⏳ Support for <code>build.gradle</code> (Groovy) alongside <code>.kts</code></li>
                       <li>⏳ Integration with Detekt / KtLint</li>
                   </ul>
                   <p class="muted">If you're interested in any of these, open an issue or start a PR – I'm happy to guide you.</p>`
        },
        {
            type: "card",
            title: "📄 License",
            body: `<p>All contributions are licensed under the <strong>MIT License</strong>, same as the project.</p>
                   <p>By contributing, you agree that your work will be distributed under this license.</p>`
        },
        {
            type: "card",
            style: "text-align:center; background: rgba(124,58,237,0.05); border-color: #7c3aed;",
            body: `<p style="margin:0; font-size:1.1rem;">
                       ❤️ <strong>Thank you</strong> for helping keep this plugin reliable, fast, and useful for everyone.
                   </p>
                   <p style="margin-top:8px; font-size:0.9rem;" class="muted">
                       — <strong>Aria Danesh</strong>
                   </p>`
        }
    ];

    // ---------- RENDER HELPERS ----------
    function renderFooter(container) {
        container.innerHTML = FOOTER_TEXT;
    }

    function renderContent(container) {
        container.innerHTML = "";
        CONTENT_SECTIONS.forEach((section, index) => {
            const card = document.createElement("div");
            card.className = "card";
            if (section.style) {
                card.style.cssText = section.style;
            }
            if (index > 0 && !section.style) {
                card.style.marginTop = "20px";
            }
            
            if (section.title) {
                const h = document.createElement("h2");
                h.style.marginTop = "0";
                h.textContent = section.title;
                card.appendChild(h);
            }

            const bodyWrapper = document.createElement("div");
            bodyWrapper.innerHTML = section.body;
            card.appendChild(bodyWrapper);
            container.appendChild(card);
        });
    }

    // ---------- MOUNT ----------
    // Render navbar using the shared module
    if (typeof window.DeadCodeNavbar !== 'undefined') {
        const currentPage = window.location.pathname.split('/').pop() || 'contributing.html';
        window.DeadCodeNavbar.render('navbarContainer', {
            currentPage: currentPage
        });
    } else {
        console.warn('DeadCodeNavbar module not loaded. Check navbar.js path.');
    }

    const contentContainer = document.getElementById('mainContent');
    const footerContainer = document.getElementById('pageFooter');

    if (contentContainer) renderContent(contentContainer);
    if (footerContainer) renderFooter(footerContainer);
})();