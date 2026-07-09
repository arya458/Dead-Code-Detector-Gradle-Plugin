(function() {
    "use strict";

    // ---------- DATA (single source of truth) ----------
    const FOOTER_TEXT = `© 2025 Aria Danesh — Dead Code Detector · MIT License · Contact: <a href="mailto:aria.danesh.work@gmail.com">aria.danesh.work@gmail.com</a>`;

    // main content: arrays of sections (cards)
    const CONTENT_SECTIONS = [
        {
            type: "card",
            title: "🚀 CI Integration",
            body: `<p class="muted">Run the Dead Code Detector automatically on every build in your CI/CD pipeline.</p>
                   <p>This guide covers <strong>GitHub Actions</strong>, <strong>GitLab CI</strong>, <strong>Jenkins</strong>, and other Gradle‑compatible CI systems.</p>
                   <p>— <strong>Aria Danesh</strong></p>`
        },
        {
            type: "card",
            title: "📦 GitHub Actions",
            body: `<p>Here's a complete workflow that runs the detector as part of your verification pipeline.</p>
                   <pre><code>name: CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Cache Gradle dependencies
        uses: actions/cache@v3
        with:
          path: ~/.gradle/caches
          key: \${{ runner.os }}-gradle-\${{ hashFiles('**/*.gradle*') }}
          restore-keys: \${{ runner.os }}-gradle-

      - name: Build and run dead-code-detector
        run: ./gradlew check --no-daemon</code></pre>
                   
                   <div class="tip-box">
                       <strong>💡 Tip:</strong> Set <code>failOnDeadCode = true</code> in your extension to make the CI job fail when dead code is found.
                   </div>
                   
                   <p>If you don't want CI to fail, set <code>failOnDeadCode = false</code> and the plugin will only generate reports.</p>`
        },
        {
            type: "card",
            title: "📦 Other CI Platforms",
            body: `<div class="platform-grid">
                       <div class="platform-card">
                           <div class="icon">⚙️</div>
                           <h4>GitLab CI</h4>
                           <pre style="font-size:0.75rem; padding:10px;"><code>.gitlab-ci.yml
build:
  image: gradle:jdk17
  script:
    - gradle check --no-daemon
  artifacts:
    paths:
      - build/reports/dead-code-detector/</code></pre>
                       </div>
                       <div class="platform-card">
                           <div class="icon">🏗️</div>
                           <h4>Jenkins</h4>
                           <pre style="font-size:0.75rem; padding:10px;"><code>pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        sh './gradlew check --no-daemon'
      }
    }
  }
  post {
    always {
      archiveArtifacts 'build/reports/dead-code-detector/*'
    }
  }
}</code></pre>
                       </div>
                       <div class="platform-card">
                           <div class="icon">☁️</div>
                           <h4>CircleCI</h4>
                           <pre style="font-size:0.75rem; padding:10px;"><code>.circleci/config.yml
version: 2.1
jobs:
  build:
    docker:
      - image: cimg/openjdk:17.0
    steps:
      - checkout
      - run: ./gradlew check --no-daemon
      - store_artifacts:
          path: build/reports/dead-code-detector</code></pre>
                       </div>
                       <div class="platform-card">
                           <div class="icon">🖥️</div>
                           <h4>Self‑hosted (any)</h4>
                           <pre style="font-size:0.75rem; padding:10px;"><code># Just run the task directly
./gradlew deadCodeDetector

# Reports are written to:
# build/reports/dead-code-detector/</code></pre>
                       </div>
                   </div>`
        },
        {
            type: "card",
            title: "📊 Report Artifacts",
            body: `<p>The plugin generates three report files in <code>build/reports/dead-code-detector/</code>:</p>
                   <ul>
                       <li><strong>report-{project}.txt</strong> – plain text summary (human‑readable)</li>
                       <li><strong>report-{project}.html</strong> – interactive HTML report with search/filter</li>
                       <li><strong>report-{project}.json</strong> – machine‑readable JSON for further processing</li>
                   </ul>
                   <p class="muted">Configure your CI to archive these artifacts for later inspection.</p>`
        },
        {
            type: "card",
            title: "🔧 Recommended CI Configuration",
            body: `<p>For best results, I recommend this setup:</p>
                   <pre><code>deadCodeDetector {
    failOnDeadCode = false          # Don't fail CI by default
    failOnUnusedDependencies = false
    includeTests = true             # Also scan test code
    keepPublicApi = true            # If you're building a library
    parallelScan = true
}</code></pre>
                   <p class="muted">Then review the reports manually or integrate with a dashboard. Once you're confident, enable <code>failOnDeadCode</code> to enforce zero dead code.</p>`
        },
        {
            type: "card",
            title: "🧪 Running Only the Detector (without other tasks)",
            body: `<p>If you want to run the detector standalone (without building the whole project):</p>
                   <pre><code>./gradlew deadCodeDetector</code></pre>
                   <p class="muted">This requires that your project has already been compiled (<code>classes</code> task runs automatically as a dependency).</p>`
        },
        {
            type: "card",
            style: "text-align:center; background: rgba(124,58,237,0.03);",
            body: `<p style="margin:0; font-size:0.95rem;" class="muted">
                       📚 Need help with your specific CI setup? <a href="contact.html">Contact me</a> – I'm happy to help.
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
        const currentPage = window.location.pathname.split('/').pop() || 'ci.html';
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