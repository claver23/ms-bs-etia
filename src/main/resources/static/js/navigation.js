/* ═══════════════════════════════════════════════
   🧭  ETIA — Navigation Helpers
   ═══════════════════════════════════════════════ */

/* Server-side navigation — each page is a real URL */
function navigateTo(url) {
    window.location.href = url;
}

/* Logout: go back to welcome */
function logout() {
    if (!confirm('¿Cerrar sesión? Volverás a la pantalla de configuración inicial.')) return;
    localStorage.removeItem('etia_onboarding');
    window.location.href = '/';
}

