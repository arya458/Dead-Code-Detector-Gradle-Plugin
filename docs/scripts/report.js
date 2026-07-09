(function() {
    "use strict";

    // ---------- DATA (single source of truth) ----------
    const FOOTER_TEXT = `© 2025 Aria Danesh — Dead Code Detector · MIT License · Contact: <a href="mailto:aria.danesh.work@gmail.com">aria.danesh.work@gmail.com</a>`;

    // Sample report data for the preview
    const SAMPLE_DATA = {
        deadClasses: ['com.example.UnusedClass', 'com.example.AnotherUnused'],
        deadMethods: [
            { owner: 'com.example.MainKt', name: 'unusedFunction', desc: '(Ljava/lang/String;)V', annotations: [] },
            { owner: 'com.example.Utils', name: 'legacyHelper', desc: '()Z', annotations: ['Deprecated'] },
            { owner: 'com.example.DataProcessor', name: 'processOld', desc: '(Lcom/example/Data;)V', annotations: [] }
        ],
        deadFields: [
            { owner: 'com.example.Config', name: 'DEPRECATED_VALUE', desc: 'Ljava/lang/String;', annotations: [] }
        ],
        deadResources: [
            { type: 'layout', name: 'activity_unused' },
            { type: 'drawable', name: 'ic_old_logo' }
        ],
        unusedDependencies: ['com.example:unused-library:1.0.0']
    };

    // main content: arrays of sections (cards)
    const CONTENT_SECTIONS = [
        {
            type: "card",
            title: "📊 Interactive HTML Report",
            body: `<p class="muted">
                       The Dead Code Detector generates an interactive HTML report with <strong>search</strong> and <strong>filter</strong> capabilities.
                       Below is a live preview of what the report looks like when populated with data.
                   </p>
                   <p>— <strong>Aria Danesh</strong></p>`
        },
        {
            type: "card",
            title: "📋 Report Preview",
            body: `<p class="muted">This preview simulates the report using sample data. In a real execution, the plugin embeds the actual JSON data.</p>
                   <div id="reportPreview">
                       <div class="preview-header">
                           <span>📄 Dead Code Detector Report</span>
                           <span class="badge">v0.0.5</span>
                       </div>
                       <div class="preview-body">
                           <div style="display:flex; gap:20px; flex-wrap:wrap; margin-bottom:12px;">
                               <div><strong>Dead Classes:</strong> <span id="classCount">0</span></div>
                               <div><strong>Dead Methods:</strong> <span id="methodCount">0</span></div>
                               <div><strong>Dead Fields:</strong> <span id="fieldCount">0</span></div>
                               <div><strong>Dead Resources:</strong> <span id="resourceCount">0</span></div>
                               <div><strong>Unused Dependencies:</strong> <span id="depCount">0</span></div>
                           </div>

                           <div class="filter-bar">
                               <input type="text" id="searchInput" placeholder="🔍 Search..." onkeyup="window.applyFilters && window.applyFilters()">
                               <select id="typeFilter" onchange="window.applyFilters && window.applyFilters()">
                                   <option value="all">All Types</option>
                                   <option value="class">Classes</option>
                                   <option value="method">Methods</option>
                                   <option value="field">Fields</option>
                                   <option value="resource">Resources</option>
                                   <option value="dependency">Dependencies</option>
                               </select>
                               <button onclick="window.resetFilters && window.resetFilters()">Reset</button>
                               <span style="margin-left:auto; color:var(--muted); font-size:0.85rem;"><span id="resultCount">0</span> items</span>
                           </div>

                           <div id="resultsContainer">
                               <!-- Rendered by JavaScript -->
                           </div>
                       </div>
                   </div>`
        },
        {
            type: "card",
            title: "🔧 How the Report Works",
            body: `<p>The plugin generates the HTML report in one of two ways:</p>
                   <ol>
                       <li><strong>Embedded JSON</strong> – the plugin writes the report data directly into the HTML as a JavaScript object (<code>window.reportData</code>).</li>
                       <li><strong>External JSON</strong> – the plugin generates a separate <code>report.json</code> file, and the HTML loads it via <code>fetch()</code>.</li>
                   </ol>
                   <p>Both methods produce the same interactive experience.</p>`
        },
        {
            type: "card",
            title: "📦 Sample Report Data Structure",
            body: `<pre><code>{
        "deadClasses": ["com.example.UnusedClass"],
        "deadMethods": [
            { "owner": "com.example.MainKt", "name": "unusedFunction", "desc": "(Ljava/lang/String;)V", "annotations": [] }
        ],
        "deadFields": [
            { "owner": "com.example.MainKt", "name": "unusedField", "desc": "Ljava/lang/String;", "annotations": [] }
        ],
        "deadResources": [
            { "type": "layout", "name": "activity_unused" }
        ],
        "unusedDependencies": [
            "com.example:unused-library:1.0.0"
        ]
    }</code></pre>`
        },
        {
            type: "card",
            title: "💡 Integration in Your Plugin",
            body: `<p>If you're extending the plugin, here's how the HTML report is written:</p>
                   <pre><code>// Kotlin snippet (inside ReportWriter)
    val htmlTemplate = this::class.java.getResourceAsStream("/report-template.html")
        .bufferedReader().readText()
    val populated = htmlTemplate.replace("/*REPORT_DATA_PLACEHOLDER*/", gson.toJson(reportData))
    File("build/reports/dead-code-detector/report.html").writeText(populated)</code></pre>
                   <p class="muted">The template uses a placeholder that gets replaced with the JSON data at generation time.</p>`
        },
        {
            type: "card",
            style: "text-align:center; background: rgba(124,58,237,0.03);",
            body: `<p style="margin:0; font-size:0.95rem;" class="muted">
                       📚 Want to customize the report? Check the <a href="configuration.html">configuration guide</a> or <a href="contact.html">contact me</a>.
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

        // Initialize the report preview after content is rendered
        initReportPreview();
    }

    // ---------- REPORT PREVIEW FUNCTIONS ----------
    function initReportPreview() {
        // Use sample data if no window.reportData is present
        const reportData = window.reportData || SAMPLE_DATA;

        function renderItems(filterText, typeFilter) {
            const container = document.getElementById('resultsContainer');
            if (!container) return;
            
            const items = [];
            const filter = filterText.toLowerCase();

            function addItem(category, obj, display) {
                let match = true;
                if (filter && !display.toLowerCase().includes(filter)) match = false;
                if (typeFilter !== 'all' && category !== typeFilter) match = false;
                if (match) items.push({ category, display, obj });
            }

            if (reportData.deadClasses) {
                reportData.deadClasses.forEach(c => addItem('class', { name: c }, 'Class: ' + c));
            }
            if (reportData.deadMethods) {
                reportData.deadMethods.forEach(m => addItem('method', m, 'Method: ' + m.owner + '.' + m.name + m.desc));
            }
            if (reportData.deadFields) {
                reportData.deadFields.forEach(f => addItem('field', f, 'Field: ' + f.owner + '.' + f.name + ' : ' + f.desc));
            }
            if (reportData.deadResources) {
                reportData.deadResources.forEach(r => addItem('resource', r, 'Resource: ' + r.type + '/' + r.name));
            }
            if (reportData.unusedDependencies) {
                reportData.unusedDependencies.forEach(d => addItem('dependency', { name: d }, 'Dependency: ' + d));
            }

            const resultCount = document.getElementById('resultCount');
            if (resultCount) resultCount.innerText = items.length;

            if (items.length === 0) {
                container.innerHTML = '<div class="empty-state">No items match your filters.</div>';
                return;
            }

            // Group by category
            const grouped = {};
            items.forEach(item => {
                if (!grouped[item.category]) grouped[item.category] = [];
                grouped[item.category].push(item);
            });

            const order = ['class', 'method', 'field', 'resource', 'dependency'];
            const labels = {
                class: '📂 Dead Classes',
                method: '🔧 Dead Methods',
                field: '🏷️ Dead Fields',
                resource: '📦 Dead Resources',
                dependency: '📦 Unused Dependencies'
            };

            let html = '';
            order.forEach(cat => {
                const list = grouped[cat] || [];
                if (list.length === 0) return;
                html += `<div class="section-header">${labels[cat] || cat} (${list.length})</div>`;
                list.forEach(item => {
                    let display = '';
                    if (cat === 'class') {
                        display = `<div class="result-item"><span class="owner">${item.obj.name}</span></div>`;
                    } else if (cat === 'method') {
                        const ann = item.obj.annotations && item.obj.annotations.length > 0
                            ? item.obj.annotations.map(a => `<span class="badge" style="background:#6b21a5;font-size:0.7rem;">${a}</span>`).join(' ')
                            : '';
                        display = `<div class="result-item"><span class="owner">${item.obj.owner}</span>.<span class="name">${item.obj.name}</span><span class="desc">${item.obj.desc}</span> ${ann}</div>`;
                    } else if (cat === 'field') {
                        display = `<div class="result-item"><span class="owner">${item.obj.owner}</span>.<span class="name">${item.obj.name}</span> <span class="desc">: ${item.obj.desc}</span></div>`;
                    } else if (cat === 'resource') {
                        display = `<div class="result-item"><span class="owner">${item.obj.type}</span>/<span class="name">${item.obj.name}</span></div>`;
                    } else if (cat === 'dependency') {
                        display = `<div class="result-item" style="border-left-color:#f59e0b;"><span class="name">${item.obj.name}</span></div>`;
                    }
                    html += display;
                });
            });

            container.innerHTML = html;
        }

        // Expose functions globally for the inline onclick handlers
        window.applyFilters = function() {
            const search = document.getElementById('searchInput');
            const type = document.getElementById('typeFilter');
            if (search && type) {
                renderItems(search.value, type.value);
            }
        };

        window.resetFilters = function() {
            const search = document.getElementById('searchInput');
            const type = document.getElementById('typeFilter');
            if (search) search.value = '';
            if (type) type.value = 'all';
            renderItems('', 'all');
        };

        // Update summary counts
        const classCount = document.getElementById('classCount');
        const methodCount = document.getElementById('methodCount');
        const fieldCount = document.getElementById('fieldCount');
        const resourceCount = document.getElementById('resourceCount');
        const depCount = document.getElementById('depCount');
        
        if (classCount) classCount.innerText = (reportData.deadClasses || []).length;
        if (methodCount) methodCount.innerText = (reportData.deadMethods || []).length;
        if (fieldCount) fieldCount.innerText = (reportData.deadFields || []).length;
        if (resourceCount) resourceCount.innerText = (reportData.deadResources || []).length;
        if (depCount) depCount.innerText = (reportData.unusedDependencies || []).length;

        // Initial render
        renderItems('', 'all');
    }

    // ---------- MOUNT ----------
    // Render navbar using the shared module
    if (typeof window.DeadCodeNavbar !== 'undefined') {
        const currentPage = window.location.pathname.split('/').pop() || 'report.html';
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