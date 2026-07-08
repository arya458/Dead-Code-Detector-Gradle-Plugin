(function() {
    "use strict";

    // ---------- DATA (single source of truth) ----------
    const FOOTER_TEXT = `© 2025 Aria Danesh — Dead Code Detector · MIT License · Contact: <a href="mailto:aria.danesh.work@gmail.com">aria.danesh.work@gmail.com</a>`;

    // main content: arrays of sections (cards)
    const CONTENT_SECTIONS = [
        {
            type: "card",
            title: "⚙️ Configuration",
            body: `<p class="muted">Configure the plugin with the <code>deadCodeDetector</code> extension in your module's <code>build.gradle.kts</code> (or <code>build.gradle</code>).</p>
                   <p>Below is the full template with all available options. Copy, tweak, and paste it into your build file.</p>
                   <p>— <strong>Aria Danesh</strong></p>`
        },
        {
            type: "card",
            title: "📝 Full Configuration Template",
            body: `<pre><code>deadCodeDetector {
    // ───────────────────────────────────────────────
    // General Settings
    // ───────────────────────────────────────────────

    // Fail the build if any dead code is detected
    failOnDeadCode = false

    // Scan test classes and test resources
    includeTests = false

    // Keep public API (public methods/fields)
    keepPublicApi = true

    // Platform detection: "auto", "android", "spring", "kmm"
    platform = "auto"

    // ───────────────────────────────────────────────
    // Resource Scanning (Android / general resources)
    // ───────────────────────────────────────────────

    includeResources = true
    resourceDir = "src/main/res"
    testResourceDir = "src/test/res"

    // ───────────────────────────────────────────────
    // Dependency Analysis
    // ───────────────────────────────────────────────

    analyzeDependencies = true
    failOnUnusedDependencies = false

    // ───────────────────────────────────────────────
    // Spring Framework Specific
    // ───────────────────────────────────────────────

    scanConfigFiles = true
    configDirs = listOf("src/main/resources")

    // ───────────────────────────────────────────────
    // Exclusion Rules (string or regex patterns)
    // ───────────────────────────────────────────────

    excludePackages.add("com.example.generated")
    excludeClasses.add("com.example.UnwantedClass")
    excludeMethods.add(Regex(".*unused.*"))
    excludeFields.add(Regex(".*logger.*"))

    // ───────────────────────────────────────────────
    // Keep Annotations (items with these annotations are preserved)
    // ───────────────────────────────────────────────

    keepAnnotations.add("javax.inject.Inject")
    keepAnnotations.add("com.example.MyCustomAnnotation")

    // ───────────────────────────────────────────────
    // Advanced: Custom Keep Rules
    // ───────────────────────────────────────────────

    customKeepRules = { type, annotations, methodRef ->
        // type: "method", "field", or "class"
        // annotations: Map<String, Set<String>> of class annotations
        // methodRef: MethodRef? (null for field/class)
        // Return true to keep the item, false to mark it as dead
        false
    }

    // ───────────────────────────────────────────────
    // Performance
    // ───────────────────────────────────────────────

    parallelScan = true
}</code></pre>`
        },
        {
            type: "card",
            title: "📋 Options Reference",
            body: `<div class="option-grid">
                       <div class="option-item">
                           <div class="name">failOnDeadCode</div>
                           <span class="type">Boolean</span>
                           <div class="desc">If <code>true</code>, the build fails when dead code, resources, or unused dependencies are found.</div>
                           <div class="default">Default: <code>false</code></div>
                       </div>
                       <div class="option-item">
                           <div class="name">includeTests</div>
                           <span class="type">Boolean</span>
                           <div class="desc">Include test classes and test resources in the analysis.</div>
                           <div class="default">Default: <code>false</code></div>
                       </div>
                       <div class="option-item">
                           <div class="name">keepPublicApi</div>
                           <span class="type">Boolean</span>
                           <div class="desc">Keep all <code>public</code> methods and fields – useful for libraries.</div>
                           <div class="default">Default: <code>true</code></div>
                       </div>
                       <div class="option-item">
                           <div class="name">platform</div>
                           <span class="type">String</span>
                           <div class="desc">Force a specific platform: <code>"auto"</code>, <code>"android"</code>, <code>"spring"</code>, or <code>"kmm"</code>.</div>
                           <div class="default">Default: <code>"auto"</code></div>
                       </div>
                       <div class="option-item">
                           <div class="name">includeResources</div>
                           <span class="type">Boolean</span>
                           <div class="desc">Enable scanning for unused resource files (Android <code>res/</code>, Spring configs).</div>
                           <div class="default">Default: <code>true</code></div>
                       </div>
                       <div class="option-item">
                           <div class="name">resourceDir</div>
                           <span class="type">String</span>
                           <div class="desc">Path to your main resource directory (relative to project root).</div>
                           <div class="default">Default: <code>"src/main/res"</code></div>
                       </div>
                       <div class="option-item">
                           <div class="name">testResourceDir</div>
                           <span class="type">String</span>
                           <div class="desc">Path to your test resource directory.</div>
                           <div class="default">Default: <code>"src/test/res"</code></div>
                       </div>
                       <div class="option-item">
                           <div class="name">analyzeDependencies</div>
                           <span class="type">Boolean</span>
                           <div class="desc">Enable unused dependency detection (checks <code>implementation</code> scope).</div>
                           <div class="default">Default: <code>true</code></div>
                       </div>
                       <div class="option-item">
                           <div class="name">failOnUnusedDependencies</div>
                           <span class="type">Boolean</span>
                           <div class="desc">Fail the build if unused dependencies are found.</div>
                           <div class="default">Default: <code>false</code></div>
                       </div>
                       <div class="option-item">
                           <div class="name">scanConfigFiles</div>
                           <span class="type">Boolean</span>
                           <div class="desc">Scan Spring XML/YAML/properties files for class references.</div>
                           <div class="default">Default: <code>true</code></div>
                       </div>
                       <div class="option-item">
                           <div class="name">configDirs</div>
                           <span class="type">List&lt;String&gt;</span>
                           <div class="desc">Directories to scan for Spring configuration files.</div>
                           <div class="default">Default: <code>["src/main/resources"]</code></div>
                       </div>
                       <div class="option-item">
                           <div class="name">excludePackages</div>
                           <span class="type">MutableList&lt;String&gt;</span>
                           <div class="desc">Packages to exclude from analysis (prefix match). Useful for generated code.</div>
                           <div class="default">Default: <code>[]</code></div>
                       </div>
                       <div class="option-item">
                           <div class="name">excludeClasses</div>
                           <span class="type">MutableList&lt;String&gt;</span>
                           <div class="desc">Fully qualified class names to exclude.</div>
                           <div class="default">Default: <code>[]</code></div>
                       </div>
                       <div class="option-item">
                           <div class="name">excludeMethods</div>
                           <span class="type">MutableList&lt;Regex&gt;</span>
                           <div class="desc">Method names (regex) to exclude from dead detection.</div>
                           <div class="default">Default: <code>[]</code></div>
                       </div>
                       <div class="option-item">
                           <div class="name">excludeFields</div>
                           <span class="type">MutableList&lt;Regex&gt;</span>
                           <div class="desc">Field names (regex) to exclude from dead detection.</div>
                           <div class="default">Default: <code>[]</code></div>
                       </div>
                       <div class="option-item">
                           <div class="name">keepAnnotations</div>
                           <span class="type">MutableList&lt;String&gt;</span>
                           <div class="desc">Fully qualified annotation names – any element with these annotations is preserved.</div>
                           <div class="default">Default: <code>["javax.persistence.Entity", ...]</code></div>
                       </div>
                       <div class="option-item">
                           <div class="name">customKeepRules</div>
                           <span class="type">Function</span>
                           <div class="desc">Advanced: custom Kotlin lambda to decide if an item should be kept. Receives <code>(type, annotations, methodRef)</code>.</div>
                           <div class="default">Default: <code>{ _, _, _ -> false }</code></div>
                       </div>
                       <div class="option-item">
                           <div class="name">parallelScan</div>
                           <span class="type">Boolean</span>
                           <div class="desc">Enable parallel scanning of class files for faster execution.</div>
                           <div class="default">Default: <code>true</code></div>
                       </div>
                   </div>`
        },
        {
            type: "card",
            title: "💡 Tips & Notes",
            body: `<ul>
                       <li><strong>Library modules</strong> – set <code>keepPublicApi = true</code> to avoid removing public API used externally.</li>
                       <li><strong>Generated code</strong> – use <code>excludePackages</code> to ignore Dagger, Hilt, or other generated classes.</li>
                       <li><strong>Platform auto‑detection</strong> – works by checking applied plugins (<code>android</code>, <code>spring-boot</code>). Override with <code>platform = "..."</code> if needed.</li>
                       <li><strong>Custom Keep Rules</strong> – ideal for framework‑specific logic not covered by built‑in rules.</li>
                       <li><strong>Performance</strong> – set <code>parallelScan = false</code> if you hit memory limits in large projects.</li>
                   </ul>`
        },
        {
            type: "card",
            style: "text-align:center; background: rgba(124,58,237,0.03);",
            body: `<p style="margin:0; font-size:0.95rem;" class="muted">
                       📚 Need more help? Check the <a href="ci.html">CI integration guide</a> or <a href="contact.html">contact me</a>.
                   </p>
                   <p style="margin-top:6px; font-size:0.85rem;" class="muted">
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
        const currentPage = window.location.pathname.split('/').pop() || 'configuration.html';
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