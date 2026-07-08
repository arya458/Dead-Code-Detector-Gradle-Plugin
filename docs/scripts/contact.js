(function() {
    "use strict";

    // ---------- DATA (single source of truth) ----------
    const FOOTER_TEXT = `© 2025 Aria Danesh — Dead Code Detector · MIT License · Contact: <a href="mailto:aria.danesh.work@gmail.com">aria.danesh.work@gmail.com</a>`;

    // main content: arrays of sections (cards)
    const CONTENT_SECTIONS = [
        {
            type: "card",
            style: "text-align:center; border-color: rgba(124,58,237,0.3); background: rgba(124,58,237,0.05);",
            title: "📬 Contact",
            body: `<p style="font-size:1.1rem;">Got a question, found a bug, or have a feature request?</p>
                   <p style="font-size:1.1rem;">I'd love to hear from you!</p>
                   <p style="margin-top:20px;">
                       <span class="contact-email">
                           <a href="mailto:aria.danesh.work@gmail.com">aria.danesh.work@gmail.com</a>
                       </span>
                   </p>
                   <p class="muted" style="margin-top:8px;">— <strong>Aria Danesh</strong> (developer &amp; maintainer)</p>`
        },
        {
            type: "card",
            title: "📋 Before you reach out",
            body: `<p>To help me respond faster and more effectively, please include:</p>
                   <ul>
                       <li><strong>Plugin version</strong> – e.g., <code>0.0.5</code></li>
                       <li><strong>Gradle version</strong> – e.g., <code>8.5</code></li>
                       <li><strong>Project type</strong> – Android, Spring, KMM, or other</li>
                       <li><strong>What you tried</strong> – steps to reproduce the issue</li>
                       <li><strong>What you expected</strong> – vs. what actually happened</li>
                       <li><strong>Attach the report file</strong> – <code>build/reports/dead-code-detector/report.txt</code> or <code>.html</code></li>
                   </ul>
                   <p class="muted">If possible, provide a <strong>minimal reproducer</strong> – a small project that demonstrates the issue.</p>`
        },
        {
            type: "card",
            style: "background: rgba(40,167,69,0.05); border-color: rgba(40,167,69,0.2);",
            title: "⏱️ Response Time",
            body: `<p>I usually respond within <strong>24–48 hours</strong> during weekdays. If I haven't replied after 3 days, feel free to send a gentle follow-up.</p>
                   <p class="muted">I'm based in <strong>Tehran, Iran</strong> (UTC+3:30), so there might be a time zone difference.</p>`
        },
        {
            type: "card",
            title: "🔗 Connect Elsewhere",
            body: `<div class="social-links">
                       <a href="https://github.com/arya458" target="_blank">
                           <svg width="20" height="20" viewBox="0 0 16 16" fill="currentColor">
                               <path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z"/>
                           </svg>
                           GitHub
                       </a>
                       <a href="https://www.linkedin.com/in/aria-danesh-574ab8162/" target="_blank">
                           <svg width="20" height="20" viewBox="0 0 16 16" fill="currentColor">
                               <path d="M0 1.146C0 .513.526 0 1.175 0h13.65C15.474 0 16 .513 16 1.146v13.708c0 .633-.526 1.146-1.175 1.146H1.175C.526 16 0 15.487 0 14.854V1.146zm4.943 3.713a1.393 1.393 0 100-2.786 1.393 1.393 0 000 2.786zm-1.084 1.64h2.168v6.912H3.859V6.499zm3.424 0h2.077v.957h.03c.29-.55 1.002-1.13 2.064-1.13 2.209 0 2.617 1.454 2.617 3.346v3.738h-2.167V10.17c0-.808-.014-1.849-1.126-1.849-1.127 0-1.3.88-1.3 1.79v3.238H7.283V6.499z"/>
                           </svg>
                           LinkedIn
                       </a>
                   </div>`
        },
        {
            type: "card",
            style: "text-align:center; background: rgba(124,58,237,0.03);",
            body: `<p style="margin:0; font-size:0.95rem;" class="muted">
                       ⭐ If you find this plugin useful, consider giving it a <strong>star</strong> on GitHub!
                   </p>
                   <p style="margin-top:6px; font-size:0.85rem;" class="muted">
                       — Your support means a lot ❤️
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
        const currentPage = window.location.pathname.split('/').pop() || 'contact.html';
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