package io.github.arya458.report

import io.github.arya458.DeadCodeDetectorExtension
import io.github.arya458.model.DependencyAnalyzerModel
import io.github.arya458.model.DeadCodeModel
import java.io.File
import java.nio.charset.StandardCharsets

class ReportWriter(private val extension: DeadCodeDetectorExtension) {

    fun write(result: DeadCodeModel, deps: DependencyAnalyzerModel, file: File) {
        file.writeText(buildTextReport(result, deps), StandardCharsets.UTF_8)
        val htmlFile = File(file.parent, file.nameWithoutExtension + ".html")
        htmlFile.writeText(buildHtmlReport(result, deps), StandardCharsets.UTF_8)
        val jsonFile = File(file.parent, file.nameWithoutExtension + ".json")
        jsonFile.writeText(buildJsonReport(result, deps), StandardCharsets.UTF_8)
        printSummary(result, deps)
    }

    private fun buildTextReport(result: DeadCodeModel, deps: DependencyAnalyzerModel): String {
        return buildString {
            appendLine("╔══════════════════════════════════════════════════════════════════════════════════════════╗")
            appendLine("║                            Dead Code Detector Report                                     ║")
            appendLine("╚══════════════════════════════════════════════════════════════════════════════════════════╝")
            appendLine()
            appendLine("👤 Developed by Aria Danesh")
            appendLine("📦 GitHub : https://github.com/arya458/Dead-Code-Detector-Gradle-Plugin")
            appendLine("✉️  Email  : aria.danesh.work@gmail.com")
            appendLine()
            appendLine("─────────────────────────────── Summary ───────────────────────────────")
            appendLine(" • Dead methods    : ${result.deadMethods.size}")
            appendLine(" • Dead fields     : ${result.deadFields.size}")
            appendLine(" • Dead classes    : ${result.deadClasses.size}")
            if (extension.includeResources) appendLine(" • Dead resources  : ${result.deadResources.size}")
            if (extension.analyzeDependencies) appendLine(" • Unused deps     : ${deps.deadDeps.size}")
            appendLine("────────────────────────────────────────────────────────────────────────")
            appendLine()

            if (result.deadClasses.isNotEmpty()) {
                appendLine("📂 Dead Classes:")
                result.deadClasses.forEach { appendLine("   - $it") }
                appendLine()
            }

            if (result.deadMethods.isNotEmpty()) {
                appendLine("🔧 Dead Methods:")
                result.deadMethods.groupBy { it.owner.replace('/', '.') }.forEach { (klass, methods) ->
                    appendLine("   Class: $klass")
                    methods.forEach { m -> appendLine("     • ${m.name}${m.desc}") }
                }
                appendLine()
            }

            if (result.deadFields.isNotEmpty()) {
                appendLine("🏷 Dead Fields:")
                result.deadFields.groupBy { it.owner.replace('/', '.') }.forEach { (klass, fields) ->
                    appendLine("   Class: $klass")
                    fields.forEach { f -> appendLine("     • ${f.name} : ${f.desc}") }
                }
                appendLine()
            }

            if (extension.includeResources && result.deadResources.isNotEmpty()) {
                appendLine("📦 Dead Resources:")
                result.deadResources.groupBy { it.first }.forEach { (type, resources) ->
                    appendLine("   Type: $type")
                    resources.forEach { (_, name) -> appendLine("     • $name") }
                }
                appendLine()
            }

            if (extension.analyzeDependencies && deps.deadDeps.isNotEmpty()) {
                appendLine("📦 Unused Dependencies:")
                deps.deadDeps.forEach { dep -> appendLine("   - $dep") }
                appendLine()
            }
        }
    }

    private fun buildHtmlReport(result: DeadCodeModel, deps: DependencyAnalyzerModel): String {
        val deadClassesJson = result.deadClasses.joinToString(",") { "\"$it\"" }
        val deadMethodsJson = result.deadMethods.joinToString(",") { m ->
            """{"owner":"${m.owner}","name":"${m.name}","desc":"${m.desc}","annotations":[${m.annotations.joinToString(",") { "\"$it\"" }}]}"""
        }
        val deadFieldsJson = result.deadFields.joinToString(",") { f ->
            """{"owner":"${f.owner}","name":"${f.name}","desc":"${f.desc}","annotations":[${f.annotations.joinToString(",") { "\"$it\"" }}]}"""
        }
        val deadResourcesJson = result.deadResources.joinToString(",") { """{"type":"${it.first}","name":"${it.second}"}""" }
        val deadDepsJson = deps.deadDeps.joinToString(",") { "\"$it\"" }

        val classCount = result.deadClasses.size
        val methodCount = result.deadMethods.size
        val fieldCount = result.deadFields.size
        val resourceCount = result.deadResources.size
        val depCount = deps.deadDeps.size

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Dead Code Detector Report</title>
                <style>
                    * { box-sizing: border-box; }
                    body { font-family: 'Segoe UI', Roboto, Arial, sans-serif; margin: 0; padding: 20px; background: #f4f6f9; }
                    .container { max-width: 1400px; margin: auto; background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }
                    h1 { color: #2c3e50; margin-top: 0; border-bottom: 3px solid #3498db; padding-bottom: 10px; }
                    .summary { display: flex; flex-wrap: wrap; gap: 20px; background: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; }
                    .summary-item { flex: 1; min-width: 150px; text-align: center; }
                    .summary-item .number { font-size: 2rem; font-weight: bold; color: #2c3e50; }
                    .summary-item .label { color: #7f8c8d; font-size: 0.9rem; }
                    .badge { display: inline-block; padding: 4px 10px; border-radius: 20px; font-size: 0.8rem; margin: 2px; }
                    .badge-danger { background: #dc3545; color: white; }
                    .badge-success { background: #28a745; color: white; }
                    .badge-warning { background: #ffc107; color: #212529; }
                    .filter-section { margin: 20px 0; display: flex; flex-wrap: wrap; gap: 10px; align-items: center; }
                    .filter-section input, .filter-section select { padding: 8px 12px; border: 1px solid #ced4da; border-radius: 6px; font-size: 1rem; }
                    .filter-section input { flex: 1; min-width: 200px; }
                    .section { margin-top: 30px; }
                    .section h2 { background: #e9ecef; padding: 8px 15px; border-radius: 6px; cursor: pointer; display: flex; justify-content: space-between; }
                    .section h2 .count { font-weight: normal; font-size: 0.9rem; color: #6c757d; }
                    .section-content { display: block; padding: 10px 0; }
                    .section-content.collapsed { display: none; }
                    .item { margin: 5px 0; padding: 5px 10px; border-radius: 4px; transition: background 0.2s; }
                    .item:hover { background: #f1f3f5; }
                    .item .owner { font-weight: 600; color: #2c3e50; }
                    .item .name { color: #495057; }
                    .item .desc { color: #6c757d; font-size: 0.9rem; }
                    .item .ann { color: #6f42c1; font-size: 0.8rem; }
                    .dep-item { background: #fff3cd; padding: 5px 10px; border-radius: 4px; margin: 3px 0; }
                    .no-data { color: #28a745; font-style: italic; }
                    .footer { margin-top: 40px; text-align: center; color: #6c757d; font-size: 0.9rem; border-top: 1px solid #dee2e6; padding-top: 20px; }
                </style>
            </head>
            <body>
            <div class="container">
                <h1>📊 Dead Code Detector Report</h1>
                <div class="summary">
                    <div class="summary-item"><div class="number">$classCount</div><div class="label">Dead Classes</div></div>
                    <div class="summary-item"><div class="number">$methodCount</div><div class="label">Dead Methods</div></div>
                    <div class="summary-item"><div class="number">$fieldCount</div><div class="label">Dead Fields</div></div>
                    ${if (extension.includeResources) """<div class="summary-item"><div class="number">$resourceCount</div><div class="label">Dead Resources</div></div>""" else ""}
                    ${if (extension.analyzeDependencies) """<div class="summary-item"><div class="number">$depCount</div><div class="label">Unused Dependencies</div></div>""" else ""}
                </div>

                <div class="filter-section">
                    <input type="text" id="searchInput" placeholder="🔍 Search (class, method, field, resource...)" onkeyup="applyFilters()">
                    <select id="typeFilter" onchange="applyFilters()">
                        <option value="all">All Types</option>
                        <option value="class">Classes</option>
                        <option value="method">Methods</option>
                        <option value="field">Fields</option>
                        <option value="resource">Resources</option>
                        <option value="dependency">Dependencies</option>
                    </select>
                    <select id="annotationFilter" onchange="applyFilters()">
                        <option value="all">Any Annotation</option>
                        <option value="spring">Spring</option>
                        <option value="android">Android</option>
                        <option value="other">Other</option>
                    </select>
                    <button onclick="resetFilters()">Reset</button>
                    <span style="margin-left:auto;"><span id="resultCount">0</span> items</span>
                </div>

                <div id="results"></div>

                <div class="footer">
                    Generated by Dead Code Detector Gradle Plugin &bull; Developed by Aria Danesh
                </div>
            </div>

            <script>
                const data = {
                    classes: [$deadClassesJson],
                    methods: [$deadMethodsJson],
                    fields: [$deadFieldsJson],
                    resources: [$deadResourcesJson],
                    dependencies: [$deadDepsJson]
                };

                function renderItems(filterText, typeFilter, annFilter) {
                    const container = document.getElementById('results');
                    const items = [];
                    const filter = filterText.toLowerCase();

                    function addItem(category, obj, display) {
                        let match = true;
                        if (filter && !display.toLowerCase().includes(filter)) match = false;
                        if (typeFilter !== 'all' && category !== typeFilter) match = false;
                        if (annFilter !== 'all') {
                            const anns = obj.annotations || [];
                            let has = false;
                            if (annFilter === 'spring') has = anns.some(a => a.includes('spring'));
                            else if (annFilter === 'android') has = anns.some(a => a.includes('android'));
                            else if (annFilter === 'other') has = anns.some(a => !a.includes('spring') && !a.includes('android'));
                            if (!has) match = false;
                        }
                        if (match) items.push({ category, display, obj });
                    }

                    data.classes.forEach(c => addItem('class', { name: c }, 'Class: ' + c));
                    data.methods.forEach(m => addItem('method', m, 'Method: ' + m.owner + '.' + m.name + m.desc));
                    data.fields.forEach(f => addItem('field', f, 'Field: ' + f.owner + '.' + f.name + ' : ' + f.desc));
                    data.resources.forEach(r => addItem('resource', r, 'Resource: ' + r.type + '/' + r.name));
                    data.dependencies.forEach(d => addItem('dependency', { name: d }, 'Dependency: ' + d));

                    document.getElementById('resultCount').innerText = items.length;

                    if (items.length === 0) {
                        container.innerHTML = '<p class="no-data">No dead items match your filters.</p>';
                        return;
                    }

                    let html = '';
                    const grouped = {};
                    items.forEach(item => {
                        if (!grouped[item.category]) grouped[item.category] = [];
                        grouped[item.category].push(item);
                    });

                    const order = ['class', 'method', 'field', 'resource', 'dependency'];
                    const labels = { class: '📂 Dead Classes', method: '🔧 Dead Methods', field: '🏷️ Dead Fields', resource: '📦 Dead Resources', dependency: '📦 Unused Dependencies' };

                    order.forEach(cat => {
                        const list = grouped[cat] || [];
                        if (list.length === 0) return;
                        html += `<div class="section"><h2 onclick="toggleSection(this)"><span>${'$'}{labels[cat] || cat}</span><span class="count">${'$'}{list.length}</span></h2><div class="section-content">`;
                        list.forEach(item => {
                            let display = '';
                            if (cat === 'class') {
                                display = `<div class="item"><span class="owner">${'$'}{item.obj.name}</span></div>`;
                            } else if (cat === 'method') {
                                const ann = item.obj.annotations ? item.obj.annotations.map(a => `<span class="badge badge-warning">${'$'}{a}</span>`).join(' ') : '';
                                display = `<div class="item"><span class="owner">${'$'}{item.obj.owner}</span>.<span class="name">${'$'}{item.obj.name}</span><span class="desc">${'$'}{item.obj.desc}</span> ${'$'}{ann}</div>`;
                            } else if (cat === 'field') {
                                const ann = item.obj.annotations ? item.obj.annotations.map(a => `<span class="badge badge-warning">${'$'}{a}</span>`).join(' ') : '';
                                display = `<div class="item"><span class="owner">${'$'}{item.obj.owner}</span>.<span class="name">${'$'}{item.obj.name}</span> <span class="desc">: ${'$'}{item.obj.desc}</span> ${'$'}{ann}</div>`;
                            } else if (cat === 'resource') {
                                display = `<div class="item"><span class="owner">${'$'}{item.obj.type}</span>/<span class="name">${'$'}{item.obj.name}</span></div>`;
                            } else if (cat === 'dependency') {
                                display = `<div class="dep-item">${'$'}{item.obj.name}</div>`;
                            }
                            html += display;
                        });
                        html += '</div></div>';
                    });

                    container.innerHTML = html;
                }

                function applyFilters() {
                    const search = document.getElementById('searchInput').value;
                    const type = document.getElementById('typeFilter').value;
                    const ann = document.getElementById('annotationFilter').value;
                    renderItems(search, type, ann);
                }

                function resetFilters() {
                    document.getElementById('searchInput').value = '';
                    document.getElementById('typeFilter').value = 'all';
                    document.getElementById('annotationFilter').value = 'all';
                    applyFilters();
                }

                function toggleSection(header) {
                    const content = header.nextElementSibling;
                    content.classList.toggle('collapsed');
                }

                applyFilters();
            </script>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildJsonReport(result: DeadCodeModel, deps: DependencyAnalyzerModel): String {
        return """
        {
          "deadClasses": ${result.deadClasses.joinToString(",") { "\"$it\"" }},
          "deadMethods": ${result.deadMethods.joinToString(",") {
            """{"owner":"${it.owner}","name":"${it.name}","desc":"${it.desc}","annotations":[${it.annotations.joinToString(",") { "\"$it\"" }}]}"""
        }},
          "deadFields": ${result.deadFields.joinToString(",") {
            """{"owner":"${it.owner}","name":"${it.name}","desc":"${it.desc}","annotations":[${it.annotations.joinToString(",") { "\"$it\"" }}]}"""
        }},
          "deadResources": ${result.deadResources.joinToString(",") { """{"type":"${it.first}","name":"${it.second}"}""" }},
          "unusedDependencies": ${deps.deadDeps.joinToString(",") { "\"$it\"" }}
        }
        """.trimIndent()
    }

    private fun printSummary(result: DeadCodeModel, deps: DependencyAnalyzerModel) {
        val ansi = object {
            val RESET = "\u001B[0m"
            val RED = "\u001B[31m"
            val GREEN = "\u001B[32m"
            val YELLOW = "\u001B[33m"
            val CYAN = "\u001B[36m"
            val BOLD = "\u001B[1m"
        }

        println()
        println("${ansi.BOLD}${ansi.CYAN}==== Dead Code Detector Summary ====${ansi.RESET}")
        println("${ansi.YELLOW} • Dead methods   :${ansi.RESET} ${result.deadMethods.size}")
        println("${ansi.YELLOW} • Dead fields    :${ansi.RESET} ${result.deadFields.size}")
        println("${ansi.YELLOW} • Dead classes   :${ansi.RESET} ${result.deadClasses.size}")
        if (extension.includeResources) {
            println("${ansi.YELLOW} • Dead resources :${ansi.RESET} ${result.deadResources.size}")
        }
        if (extension.analyzeDependencies) {
            println("${ansi.YELLOW} • Unused deps    :${ansi.RESET} ${deps.deadDeps.size}")
        }

        if (result.hasDeadCode() || (extension.analyzeDependencies && deps.hasUnusedDependencies())) {
            println("${ansi.RED}⚠ Dead code, resources, or unused dependencies found.${ansi.RESET}")
        } else {
            println("${ansi.GREEN}✔ No dead code, resources, or unused dependencies detected!${ansi.RESET}")
        }

        println("${ansi.BOLD}${ansi.CYAN}==== Report saved at ====${ansi.RESET}")
    }
}