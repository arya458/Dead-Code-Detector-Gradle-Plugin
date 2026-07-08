(function() {
    "use strict";

    // ---------- DATA (single source of truth) ----------
    const FOOTER_TEXT = `© 2025 Aria Danesh — Dead Code Detector · MIT License · Contact: <a href="mailto:aria.danesh.work@gmail.com">aria.danesh.work@gmail.com</a>`;

    // main content: arrays of sections (cards)
    const CONTENT_SECTIONS = [
        {
            type: "card",
            title: "Keep your codebase tidy — but don't rush to delete",
            body: `<p class="muted">In my experience, removing unused code is one of those tasks that pays off slowly but significantly. This plugin helps you find candidates safely: classes, top‑level functions, resources, and even declared Gradle dependencies that appear unused.</p>
                   <p>It's lightweight, runs on compiled classes (so results reflect the actual bytecode), and can be run locally or in CI. Use it as a guide — not an automatic refactor tool.</p>
                   <p style="margin-top:12px"><strong>Quick start</strong></p>
                   <pre><code>plugins {
        id("io.github.arya458.dead-code-detector") version "0.0.5"
    }

    # run
    ./gradlew deadCodeDetector</code></pre>
                   <p class="muted">Default report: <span class="kbd">build/reports/dead-code-detector/report.txt</span></p>`
        },
        {
            type: "card",
            title: "What this plugin reports",
            body: `<ul>
                    <li>Dead classes, top‑level functions, and top‑level fields</li>
                    <li>Unused resource files (Android, Spring configs, etc.)</li>
                    <li>Unused Gradle dependencies</li>
                </ul>
                <p class="muted">Note: reflection, JNI, or frameworks using runtime binding (DI) may make code look unused. Use <em>keep</em> rules to avoid false positives.</p>
                <p class="muted">Developed with ❤️ by <strong>Aria Danesh</strong> – <a href="https://github.com/arya458">GitHub</a></p>`
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
            if (index > 0) {
                card.style.marginTop = "16px";
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
        const currentPage = window.location.pathname.split('/').pop() || 'index.html';
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