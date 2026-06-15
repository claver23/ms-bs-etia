/* ═══════════════════════════════════════════════
   🧭  ETIA — Navigation Helpers
   ═══════════════════════════════════════════════ */

/* Server-side navigation — each page is a real URL */
function navigateTo(url) {
    window.location.href = url;
}

/* Logout: clear all caches and go back to welcome */
function logout() {
    if (!confirm('¿Cerrar sesión? Volverás a la pantalla de configuración inicial.')) return;
    _clearAllEtiaState();
    window.location.href = '/';
}

/* Clear all ETIA-related state from browser storage */
function _clearAllEtiaState() {
    /* Clear known localStorage keys */
    var keysToRemove = [
        'etia_onboarding', 'etia_company_id', 'etia_config',
        'etia_notif_prefs', 'etia_logo_cache', 'etia_clients_cache'
    ];
    keysToRemove.forEach(function(k) { localStorage.removeItem(k); });

    /* Also clear any dynamic etia_ prefixed keys */
    var toRemove = [];
    for (var i = 0; i < localStorage.length; i++) {
        var key = localStorage.key(i);
        if (key && key.indexOf('etia_') === 0) toRemove.push(key);
    }
    toRemove.forEach(function(k) { localStorage.removeItem(k); });

    /* Clear sessionStorage */
    sessionStorage.clear();

    /* Reset in-memory ETIA state if available */
    if (typeof ETIA !== 'undefined') {
        ETIA.companyId = null;
        ETIA.company = null;
        ETIA.rejectedDocs = [];
        ETIA.txNotifications = [];
        if (ETIA.config) ETIA.config = {};
    }
}

