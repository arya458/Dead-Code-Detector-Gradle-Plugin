// navbar.js - Reusable navigation component
(function() {
    "use strict";

    // ---------- CONFIGURATION (single source of truth) ----------
    const NAV_LINKS = [
        { label: 'Home', href: 'index.html' },
        { label: 'Installation', href: 'installation.html' },
        { label: 'Configuration', href: 'configuration.html' },
        { label: 'HTML Report', href: 'report.html' },
        { label: 'CI', href: 'ci.html' },
        { label: 'Contribute', href: 'contributing.html' },
        { label: 'Contact', href: 'contact.html' }
    ];

    // ---------- PUBLIC API ----------
    window.DeadCodeNavbar = {
        /**
         * Renders the navigation into a container element
         * @param {HTMLElement|string} container - The container element or its ID
         * @param {Object} options - Optional configuration
         * @param {string} options.currentPage - Current page filename to highlight (optional)
         * @param {string} options.className - Additional CSS class for the nav (optional)
         */
        render: function(container, options) {
            options = options || {};
            const containerEl = typeof container === 'string' 
                ? document.getElementById(container) 
                : container;
            
            if (!containerEl) {
                console.error('Navbar container not found');
                return;
            }

            const nav = document.createElement('nav');
            if (options.className) {
                nav.className = options.className;
            }

            NAV_LINKS.forEach(link => {
                const a = document.createElement('a');
                a.href = link.href;
                a.textContent = link.label;
                
                // Optional: highlight current page
                if (options.currentPage && link.href === options.currentPage) {
                    a.style.color = '#7c3aed'; // accent color
                    a.style.fontWeight = '600';
                }
                
                nav.appendChild(a);
            });

            containerEl.replaceChildren(nav);
        },

        /**
         * Get the raw navigation data if needed elsewhere
         */
        getNavLinks: function() {
            return NAV_LINKS.slice(); // return a copy
        }
    };
})();