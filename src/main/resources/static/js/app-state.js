/* ═══════════════════════════════════════════════
   📦  ETIA — Global App State
   ═══════════════════════════════════════════════ */
window.ETIA = window.ETIA || {};

/* Defaults — populated dynamically from API */
ETIA.companyId = '';
ETIA.clientId  = '';
ETIA.company   = null;
ETIA.client    = null;

/* ─── Config from localStorage ─── */
ETIA.config = {
    theme:          localStorage.getItem('etia_theme')          || 'dark',
    currency:       localStorage.getItem('etia_currency')       || 'PEN',
    defaultItemType:localStorage.getItem('etia_defaultItemType')|| 'servicio',
    serieFac:       localStorage.getItem('etia_serieFac')       || 'F001',
    serieBol:       localStorage.getItem('etia_serieBol')       || 'B001',
    serieNC:        localStorage.getItem('etia_serieNC')        || 'FC01',
    serieND:        localStorage.getItem('etia_serieND')        || 'FD01',
    sede:           localStorage.getItem('etia_sede')           || '',
    sedeDireccion:  localStorage.getItem('etia_sedeDireccion')  || '',
    sedeLat:        localStorage.getItem('etia_sedeLat')        || '',
    sedeLng:        localStorage.getItem('etia_sedeLng')        || ''
};

/* ─── Save a config key ─── */
ETIA.saveConfig = function(key, value) {
    ETIA.config[key] = value;
    localStorage.setItem('etia_' + key, value);
};

/* ─── Apply theme on load ─── */
(function applyTheme() {
    var theme = ETIA.config.theme;
    if (theme === 'light') {
        document.documentElement.setAttribute('data-theme', 'light');
    } else {
        document.documentElement.removeAttribute('data-theme');
    }
})();

/* ─── Bootstrap: load company + client from API ─── */
ETIA.init = function() {
    return ApiClient.listCompanies().then(function(companies) {
        if (companies && companies.length > 0) {
            var c = companies[0];
            ETIA.company   = c;
            ETIA.companyId = c.id;
            /* Populate header placeholders if present */
            var hdrCompany  = document.getElementById('hdr-company');
            var hdrLocation = document.getElementById('hdr-location');
            if (hdrCompany)  hdrCompany.textContent  = c.razonSocial || '';
            if (hdrLocation) hdrLocation.textContent  = (c.distrito || '') + ' · ' + (c.codigoUbigeo || '');
            /* Load first client */
            return ApiClient.listClients(c.id).then(function(clients) {
                if (clients && clients.length > 0) {
                    ETIA.client   = clients[0];
                    ETIA.clientId = clients[0].id;
                }
                return c;
            }).catch(function(err) {
                console.warn('ETIA.init: Error loading clients:', err);
                return c;
            });
        }
        return null;
    }).catch(function(err) {
        console.error('ETIA.init error:', err);
        return null;
    });
};
