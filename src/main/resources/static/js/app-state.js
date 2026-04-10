/* ═══════════════════════════════════════════════
   📦  ETIA — Global App State
   ═══════════════════════════════════════════════ */
window.ETIA = window.ETIA || {};

/* Defaults — populated dynamically from API */
ETIA.companyId = '';
ETIA.clientId  = '';
ETIA.company   = null;
ETIA.client    = null;
ETIA.clients   = [];   /* All clients for lookup */
ETIA.activeSede = null;
ETIA.sedes      = [];
ETIA.rejectedDocs = [];

/* ─── Config from localStorage ─── */
ETIA.config = {
    theme:          localStorage.getItem('etia_theme')          || 'dark',
    currency:       localStorage.getItem('etia_currency')       || 'PEN',
    defaultItemType:localStorage.getItem('etia_defaultItemType')|| 'servicio',
    serieFac:       localStorage.getItem('etia_serieFac')       || 'F001',
    serieBol:       localStorage.getItem('etia_serieBol')       || 'B001',
    serieNC:        localStorage.getItem('etia_serieNC')        || 'FC01',
    serieND:        localStorage.getItem('etia_serieND')        || 'FD01',
    activeSedeId:   localStorage.getItem('etia_activeSedeId')   || '',
    sede:           localStorage.getItem('etia_sede')           || '',
    sedeDireccion:  localStorage.getItem('etia_sedeDireccion')  || '',
    sedeLat:        localStorage.getItem('etia_sedeLat')        || '',
    sedeLng:        localStorage.getItem('etia_sedeLng')        || '',
    tipoCambioFuente: localStorage.getItem('etia_tipoCambioFuente') || 'SBS',
    emisionEnEnvio: localStorage.getItem('etia_emisionEnEnvio') !== 'false',
    defaultCuotas:    localStorage.getItem('etia_defaultCuotas')    || '1',
    cuotaDistribucion:localStorage.getItem('etia_cuotaDistribucion')|| 'FIJA_30D',
    documentFormat:   localStorage.getItem('etia_documentFormat')   || 'standard',
    toastDurationError:   parseInt(localStorage.getItem('etia_toastDurationError') || '12'),
    toastDurationSuccess: parseInt(localStorage.getItem('etia_toastDurationSuccess') || '4'),
    errorDisplayMode:     localStorage.getItem('etia_errorDisplayMode') || 'toast'
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

/* ─── Cache helpers (sessionStorage + TTL) ─── */
var CACHE_TTL = 5 * 60 * 1000; /* 5 minutes */

function _cacheSet(key, data) {
    try {
        sessionStorage.setItem('etia_cache_' + key, JSON.stringify({ ts: Date.now(), data: data }));
    } catch (e) { /* quota exceeded — ignore */ }
}

function _cacheGet(key) {
    try {
        var raw = sessionStorage.getItem('etia_cache_' + key);
        if (!raw) return null;
        var parsed = JSON.parse(raw);
        if (Date.now() - parsed.ts > CACHE_TTL) {
            sessionStorage.removeItem('etia_cache_' + key);
            return null;
        }
        return parsed.data;
    } catch (e) { return null; }
}

function _cacheInvalidate(key) {
    sessionStorage.removeItem('etia_cache_' + key);
}

/* ─── Bootstrap: load company + client + sedes + notifications from API ─── */
ETIA.init = function() {
    /* Auth check — redirect to login if not authenticated */
    if (!ApiClient.isAuthenticated()) {
        window.location.href = '/login';
        return Promise.resolve(null);
    }

    /* Try cache first for company/clients/sedes */
    var cachedCompany = _cacheGet('company');
    var cachedClients = _cacheGet('clients');
    var cachedSedes   = _cacheGet('sedes');

    var companyPromise;
    if (cachedCompany) {
        /* Apply cached data immediately */
        _applyCompanyData(cachedCompany);
        companyPromise = Promise.resolve(cachedCompany);
        /* Revalidate company in background (silent) */
        ApiClient.listCompanies().then(function(companies) {
            if (companies && companies.length > 0) _cacheSet('company', companies[0]);
        }).catch(function() {});
    } else {
        companyPromise = ApiClient.listCompanies().then(function(companies) {
            if (companies && companies.length > 0) {
                var c = companies[0];
                _applyCompanyData(c);
                _cacheSet('company', c);
                return c;
            }
            return null;
        });
    }

    return companyPromise.then(function(c) {
        if (!c && !ETIA.company) return null;
        var company = c || ETIA.company;
        var cid = company.id;

        /* Clients — use cache or fetch */
        var loadClients;
        if (cachedClients) {
            _applyClientsData(cachedClients);
            loadClients = Promise.resolve();
            /* Background revalidation */
            ApiClient.listClients(cid).then(function(cl) { _cacheSet('clients', cl); }).catch(function() {});
        } else {
            loadClients = ApiClient.listClients(cid).then(function(clients) {
                _applyClientsData(clients);
                _cacheSet('clients', clients);
            }).catch(function(err) { console.warn('ETIA.init: Error loading clients:', err); });
        }

        /* Sedes — use cache or fetch */
        var loadSedes;
        if (cachedSedes) {
            ETIA.sedes = cachedSedes;
            _populateSedeSelector(cachedSedes);
            loadSedes = Promise.resolve();
            /* Background revalidation */
            ApiClient.listSedes(cid).then(function(s) { _cacheSet('sedes', s); }).catch(function() {});
        } else {
            loadSedes = ApiClient.listSedes(cid).then(function(sedes) {
                ETIA.sedes = sedes || [];
                _populateSedeSelector(ETIA.sedes);
                _cacheSet('sedes', sedes);
            }).catch(function(err) { console.warn('ETIA.init: Error loading sedes:', err); });
        }

        /* Notifications — always fresh (no cache) */
        var loadNotifs = _loadRejectedDocs(cid);
        var loadTxNotifs = _loadTransmissionNotifs(cid);

        /* Config notifications — check what's missing */
        _checkConfigNotifications(company);

        /* Preferences — load from backend and sync to localStorage */
        var loadPrefs = _loadPreferencesFromBackend(cid);

        /* Tipo de cambio — for header badge */
        var loadTc = _loadTcBadge();

        return Promise.all([loadClients, loadSedes, loadNotifs, loadTxNotifs, loadTc, loadPrefs]).then(function() { return company; });
    }).catch(function(err) {
        console.error('ETIA.init error:', err);
        return null;
    });
};

/* ─── Apply data helpers ─── */
function _applyCompanyData(c) {
    ETIA.company   = c;
    ETIA.companyId = c.id;
    var hdrCompany = document.getElementById('hdr-company');
    if (hdrCompany) hdrCompany.textContent = c.razonSocial || '';

    /* ── Dynamic logo in header (format-aware) ── */
    var hdrLogoImg = document.getElementById('hdr-logo-img');
    var hdrLogoEmoji = document.getElementById('hdr-logo-emoji');
    var logoFormat = (c.logoFormat || 'png').toLowerCase();
    if (hdrLogoImg && c.tieneLogo && c.id) {
        hdrLogoImg.src = ApiClient.getLogoUrl(c.id) + '&t=' + Date.now();
        hdrLogoImg.style.display = 'block';
        /* SVG: scale-down preserves vector quality; rasters: contain for bitmap */
        hdrLogoImg.style.objectFit = (logoFormat === 'svg') ? 'scale-down' : 'contain';
        if (hdrLogoEmoji) hdrLogoEmoji.style.display = 'none';
    } else if (hdrLogoImg) {
        hdrLogoImg.style.display = 'none';
        if (hdrLogoEmoji) hdrLogoEmoji.style.display = '';
    }

    /* Logo type selector removed — auto-detect now */
}

function _applyClientsData(clients) {
    ETIA.clients = clients || [];
    /* Do NOT auto-select first client — user must choose explicitly via search modal or edit mode */
}

/**
 * Look up a client by ID from the cached clients list.
 * Returns the client object or null.
 */
ETIA.findClientById = function(clientId) {
    if (!clientId || !ETIA.clients) return null;
    return ETIA.clients.find(function(c) { return c.id === clientId; }) || null;
};

/* ─── Tipo de Cambio — Header Live Badge ─── */
ETIA.tipoCambio = null;

function _loadTcBadge() {
    var badge = document.getElementById('hdr-tc-badge');
    if (!badge) return Promise.resolve();

    var today = new Date().toISOString().split('T')[0];
    var fuente = ETIA.config.tipoCambioFuente || 'SBS';

    /* Try cache first */
    var cachedTc = _cacheGet('tc_today');
    if (cachedTc) {
        ETIA.tipoCambio = cachedTc;
        _renderTcBadge(cachedTc);
        /* Background revalidation */
        ApiClient.getTipoCambio(today, 'USD', fuente).then(function(data) {
            var tc = data && data.tasaVenta ? data : (data && data.data ? data.data : null);
            if (tc) { _cacheSet('tc_today', tc); ETIA.tipoCambio = tc; _renderTcBadge(tc); }
        }).catch(function() {});
        return Promise.resolve();
    }

    return ApiClient.getTipoCambio(today, 'USD', fuente).then(function(data) {
        var tc = data && data.tasaVenta ? data : (data && data.data ? data.data : null);
        if (tc) {
            ETIA.tipoCambio = tc;
            _cacheSet('tc_today', tc);
            _renderTcBadge(tc);
        } else {
            badge.style.display = 'none';
        }
    }).catch(function() {
        badge.style.display = 'none';
    });
}

function _renderTcBadge(tc) {
    var badge = document.getElementById('hdr-tc-badge');
    if (!badge || !tc) return;
    var venta = parseFloat(tc.tasaVenta).toFixed(4);
    badge.querySelector('.tc-badge-rate').textContent = venta;
    badge.style.display = '';

    /* Populate popup */
    var popup = document.getElementById('hdr-tc-popup');
    if (popup) {
        var compra = parseFloat(tc.tasaCompra).toFixed(4);
        popup.querySelector('.tc-popup-compra').textContent = compra;
        popup.querySelector('.tc-popup-venta').textContent = venta;
        popup.querySelector('.tc-popup-fuente').textContent = (tc.fuente || 'SBS') + ' · ' + ETIA.formatDate(tc.fecha);
    }

    /* Also populate menu widget if present */
    _renderTcMenuWidget(tc);
}

function _renderTcMenuWidget(tc) {
    var widget = document.getElementById('tc-menu-widget');
    if (!widget || !tc) return;
    widget.style.display = '';
    var compra = parseFloat(tc.tasaCompra).toFixed(4);
    var venta = parseFloat(tc.tasaVenta).toFixed(4);
    widget.querySelector('.tcw-compra-val').textContent = 'S/ ' + compra;
    widget.querySelector('.tcw-venta-val').textContent = 'S/ ' + venta;
    widget.querySelector('.tcw-fuente').textContent = 'Fuente: ' + (tc.fuente || 'SBS') + ' · ' + ETIA.formatDate(tc.fecha);
}

function toggleTcPopup() {
    var popup = document.getElementById('hdr-tc-popup');
    if (popup) popup.style.display = popup.style.display === 'none' ? '' : 'none';
}

function refreshTcBadge() {
    _cacheInvalidate('tc_today');
    _loadTcBadge();
}

/* ─── Sede Selector Logic ─── */
function _populateSedeSelector(sedes) {
    var sel = document.getElementById('hdr-sede-select');
    if (!sel || !sedes.length) return;
    sel.innerHTML = '';
    sedes.forEach(function(s) {
        var opt = document.createElement('option');
        opt.value = s.id;
        var label = '📍 ' + (s.nombre || 'Sede');
        if (s.distrito) label += ' · ' + s.distrito;
        if (s.codigoUbigeo) label += ' (' + s.codigoUbigeo + ')';
        opt.textContent = label;
        sel.appendChild(opt);
    });
    /* Restore saved sede or pick default */
    var saved = ETIA.config.activeSedeId;
    var match = sedes.find(function(s) { return s.id === saved; });
    if (!match) match = sedes.find(function(s) { return s.esDefault; }) || sedes[0];
    if (match) {
        sel.value = match.id;
        _applyActiveSede(match);
    }
    sel.addEventListener('change', function() {
        var s = sedes.find(function(x) { return x.id === sel.value; });
        if (s) _applyActiveSede(s);
    });
}

function _applyActiveSede(sede) {
    ETIA.activeSede = sede;
    ETIA.saveConfig('activeSedeId', sede.id);
    /* Update series config from sede's series */
    if (sede.series && sede.series.length) {
        sede.series.forEach(function(sr) {
            if (sr.tipoDocumento === 'FACTURA')      ETIA.saveConfig('serieFac', sr.serie);
            if (sr.tipoDocumento === 'BOLETA')        ETIA.saveConfig('serieBol', sr.serie);
            if (sr.tipoDocumento === 'NOTA_CREDITO')  ETIA.saveConfig('serieNC', sr.serie);
            if (sr.tipoDocumento === 'NOTA_DEBITO')   ETIA.saveConfig('serieND', sr.serie);
        });
    }
}

/* ─── Notification (Rejected Docs) Logic ─── */
function _loadRejectedDocs(companyId) {
    /* Check all doc types for RECHAZADA status */
    var results = [];
    return Promise.all([
        ApiClient.listarFacturas(companyId).then(function(docs) {
            (docs || []).forEach(function(d) {
                if (d.estado === 'RECHAZADA' || d.estado === 'RECHAZADO') {
                    results.push({ tipo: 'FACTURA', id: d.id, serie: d.serie, numero: d.numero, total: d.total, fecha: d.fechaEmision, estado: d.estado });
                }
            });
        }).catch(function() {}),
        ApiClient.listarBoletas(companyId).then(function(docs) {
            (docs || []).forEach(function(d) {
                if (d.estado === 'RECHAZADA' || d.estado === 'RECHAZADO') {
                    results.push({ tipo: 'BOLETA', id: d.id, serie: d.serie, numero: d.numero, total: d.total, fecha: d.fechaEmision, estado: d.estado });
                }
            });
        }).catch(function() {}),
        ApiClient.listarNotasCredito(companyId).then(function(docs) {
            (docs || []).forEach(function(d) {
                if (d.estado === 'RECHAZADA' || d.estado === 'RECHAZADO') {
                    results.push({ tipo: 'NOTA_CREDITO', id: d.id, serie: d.serie, numero: d.numero, total: d.montoDescuento, fecha: d.fechaEmision, estado: d.estado });
                }
            });
        }).catch(function() {}),
        ApiClient.listarNotasDebito(companyId).then(function(docs) {
            (docs || []).forEach(function(d) {
                if (d.estado === 'RECHAZADA' || d.estado === 'RECHAZADO') {
                    results.push({ tipo: 'NOTA_DEBITO', id: d.id, serie: d.serie, numero: d.numero, total: d.montoIncremento, fecha: d.fechaEmision, estado: d.estado });
                }
            });
        }).catch(function() {})
    ]).then(function() {
        ETIA.rejectedDocs = results;
        _renderNotifBadge(results);
    });
}

function _renderNotifBadge(docs) {
    var list = document.getElementById('hdr-notif-list');
    if (docs.length === 0) {
        if (list) list.innerHTML = '<div class="g-notif-empty-state">' +
            '<div class="g-notif-empty-icon">✅</div>' +
            '<p class="g-notif-empty-title">Sin comprobantes rechazados</p>' +
            '<p class="g-notif-empty-hint">Los documentos rechazados por SUNAT aparecerán aquí para que puedas anularlos</p>' +
        '</div>';
    } else if (list) {
        list.innerHTML = docs.map(function(d) {
            var label = { FACTURA:'Factura', BOLETA:'Boleta', NOTA_CREDITO:'Nota Crédito', NOTA_DEBITO:'Nota Débito' }[d.tipo] || d.tipo;
            var sn = ETIA.formatNumero(d.serie, d.numero);
            return '<div class="g-notif-item">' +
                '<div class="g-notif-item-info">' +
                    '<strong>' + label + ' ' + sn + '</strong>' +
                    '<span>' + ETIA.formatDate(d.fecha) + ' · S/ ' + (d.total != null ? d.total.toFixed(2) : '—') + ' · ' + d.estado + '</span>' +
                '</div>' +
                '<div class="g-notif-item-actions">' +
                    '<button class="g-notif-btn-anular" onclick="anularDocumento(\'' + d.tipo + '\',\'' + d.id + '\')">✕ Anular</button>' +
                '</div>' +
            '</div>';
        }).join('');
    }
    _updateGlobalBadge();
}

function toggleNotifPanel() {
    var panel = document.getElementById('hdr-notif-panel');
    if (panel) panel.style.display = panel.style.display === 'none' ? '' : 'none';
}

function anularDocumento(tipo, id) {
    if (!confirm('¿Estás seguro de anular este comprobante? El correlativo quedará inutilizado.')) return;
    var payload = { motivoAnulacion: 'Anulación de comprobante rechazado por SUNAT' };

    /* Dispatch to the correct endpoint based on document type */
    var anulFn;
    if (tipo === 'FACTURA')      anulFn = ApiClient.anularFactura(id, payload);
    else if (tipo === 'BOLETA')  anulFn = ApiClient.anularBoleta(id, payload);
    else {
        console.warn('anularDocumento: tipo no soportado:', tipo);
        return;
    }

    anulFn
        .then(function() {
            ETIA.rejectedDocs = ETIA.rejectedDocs.filter(function(d) { return d.id !== id; });
            _renderNotifBadge(ETIA.rejectedDocs);
            if (ETIA.showSuccess) ETIA.showSuccess({ message: 'Comprobante anulado exitosamente' });
            if (ETIA.refreshNotifications) ETIA.refreshNotifications();
        })
        .catch(function(err) {
            console.error('Error anulando documento:', err);
            if (ETIA.showError) ETIA.showError({ message: 'Error al anular: ' + (err.message || 'desconocido') });
        });
}

/* ─── Transmission Notifications ─── */
ETIA.txNotifications = [];

function _loadTransmissionNotifs(companyId) {
    return ApiClient.listNotifications(companyId).then(function(resp) {
        ETIA.txNotifications = resp.notifications || [];
        _renderTxNotifs(ETIA.txNotifications);
        _updateGlobalBadge();
    }).catch(function(err) {
        console.warn('Error loading transmission notifications:', err);
    });
}

function _renderTxNotifs(notifs) {
    var list = document.getElementById('hdr-notif-tx-list');
    if (!list) return;
    if (!notifs || notifs.length === 0) {
        list.innerHTML = '<div class="g-notif-empty-state">' +
            '<div class="g-notif-empty-icon">📭</div>' +
            '<p class="g-notif-empty-title">Sin transmisiones recientes</p>' +
            '<p class="g-notif-empty-hint">Las emisiones y anulaciones aparecerán aquí automáticamente</p>' +
        '</div>';
        return;
    }
    var icons = { FACTURA:'📄', BOLETA:'🧾', NOTA_CREDITO:'📋', NOTA_DEBITO:'📋' };
    var acciones = { EMITIDA:'✅', ANULADA:'🚫' };
    var navUrls = { FACTURA:'/facturas', BOLETA:'/boletas', NOTA_CREDITO:'/notas-credito', NOTA_DEBITO:'/notas-debito' };
    list.innerHTML = notifs.slice(0, 20).map(function(n) {
        var icon = icons[n.tipoDocumento] || '📄';
        var accionIcon = acciones[n.accion] || '';
        var time = n.timestamp ? new Date(n.timestamp).toLocaleTimeString('es-PE', {hour:'2-digit', minute:'2-digit'}) : '';
        var readClass = n.leida ? ' g-notif-item-read' : '';
        var navUrl = navUrls[n.tipoDocumento] || '/menu';
        var clickAttr = ' onclick="_handleTxNotifClick(\'' + (n.id || '') + '\',\'' + navUrl + '\')" style="cursor:pointer;"';
        return '<div class="g-notif-item g-notif-item-clickable' + readClass + '"' + clickAttr + '>' +
            '<div class="g-notif-item-info">' +
                '<strong>' + icon + ' ' + accionIcon + ' ' + (n.mensaje || '') + '</strong>' +
                '<span>' + (n.serie || '') + ' · ' + time + '</span>' +
            '</div>' +
            '<span class="g-notif-item-go">→</span>' +
        '</div>';
    }).join('');
}

/* ─── Handle TX notification click → mark read + navigate ─── */
function _handleTxNotifClick(notifId, navUrl) {
    if (notifId) {
        ApiClient.markNotifRead(notifId).then(function() {
            var n = (ETIA.txNotifications || []).find(function(x) { return x.id === notifId; });
            if (n) n.leida = true;
            _renderTxNotifs(ETIA.txNotifications);
            _updateGlobalBadge();
        }).catch(function() {});
    }
    toggleNotifPanel();
    window.location.href = navUrl;
}

function _updateGlobalBadge() {
    var count = document.getElementById('hdr-notif-count');
    if (!count) return;
    var rejectedCount = (ETIA.rejectedDocs || []).length;
    var unreadTx = (ETIA.txNotifications || []).filter(function(n) { return !n.leida; }).length;
    var configCount = (ETIA.configNotifications || []).length;
    var total = rejectedCount + unreadTx + configCount;
    if (total > 0) {
        count.style.display = '';
        count.textContent = total;
    } else {
        count.style.display = 'none';
    }
}

/* Refresh all notifications (call after emitting/annulling a doc) */
ETIA.refreshNotifications = function() {
    if (ETIA.companyId) {
        _loadRejectedDocs(ETIA.companyId);
        _loadTransmissionNotifs(ETIA.companyId);
        if (ETIA.company) _checkConfigNotifications(ETIA.company);
    }
};

/* ─── Config Notifications (missing configuration checks) ─── */
ETIA.configNotifications = [];

function _checkConfigNotifications(company) {
    if (!company) return;
    var items = [];

    if (!company.tieneLogo) {
        items.push({ icon: '🖼️', title: 'Sube el logo de tu empresa', hint: 'Personaliza tus comprobantes y PDFs', tab: 'empresa' });
    }
    if (!company.tieneUsuarioSol || !company.tieneClaveSol) {
        items.push({ icon: '🔐', title: 'Configura tus credenciales SOL', hint: 'Necesarias para conectar con SUNAT', tab: 'credenciales' });
    }
    if (!company.tieneCertificado) {
        items.push({ icon: '📜', title: 'Sube tu certificado digital', hint: 'Archivo .p12/.pfx para firma electrónica', tab: 'credenciales' });
    }
    if (!company.emailContacto) {
        items.push({ icon: '📧', title: 'Agrega un email de contacto', hint: 'Correo para notificaciones de facturación', tab: 'empresa' });
    }
    if (!company.telefono) {
        items.push({ icon: '📱', title: 'Agrega un teléfono de contacto', hint: 'Teléfono de la empresa', tab: 'empresa' });
    }

    ETIA.configNotifications = items;
    _renderConfigNotifs(items);
    _updateGlobalBadge();
}

function _renderConfigNotifs(items) {
    var section = document.getElementById('hdr-notif-config');
    var list = document.getElementById('hdr-notif-config-list');
    if (!section || !list) return;

    if (items.length === 0) {
        section.style.display = 'none';
        return;
    }
    section.style.display = '';
    list.innerHTML = items.map(function(item) {
        return '<div class="g-notif-config-item" onclick="toggleNotifPanel();openConfigOverlay();switchConfigTab(\'' + item.tab + '\')">' +
            '<span class="g-nci-icon">' + item.icon + '</span>' +
            '<div class="g-nci-text">' +
                '<strong>' + item.title + '</strong>' +
                '<span>' + item.hint + '</span>' +
            '</div>' +
            '<span class="g-nci-arrow">→</span>' +
        '</div>';
    }).join('');
}

/* ─── Mark All Notifications Read ─── */
function markAllNotifsRead() {
    if (!ETIA.companyId) return;
    ApiClient.markAllNotifRead(ETIA.companyId).then(function() {
        /* Update local state */
        (ETIA.txNotifications || []).forEach(function(n) { n.leida = true; });
        _renderTxNotifs(ETIA.txNotifications);
        _updateGlobalBadge();
    }).catch(function(err) {
        console.error('Error marking notifications as read:', err);
    });
}

/* ─── Clear All Notifications ─── */
function clearAllNotifs() {
    if (!ETIA.companyId) return;
    if (!confirm('¿Limpiar todas las notificaciones de transmisión?')) return;
    ApiClient.clearNotifications(ETIA.companyId).then(function() {
        ETIA.txNotifications = [];
        _renderTxNotifs([]);
        _updateGlobalBadge();
    }).catch(function(err) {
        console.error('Error clearing notifications:', err);
    });
}

/* ─── Load Preferences from Backend (sync to localStorage) ─── */
function _loadPreferencesFromBackend(companyId) {
    return ApiClient.listPreferences(companyId).then(function(res) {
        var prefs = res.data || res;
        if (Array.isArray(prefs)) {
            prefs.forEach(function(p) {
                if (p.clave && p.valor != null) {
                    var current = localStorage.getItem('etia_' + p.clave);
                    /* Only sync from backend if localStorage has no value (first visit / new browser) */
                    if (!current || current === 'undefined') {
                        localStorage.setItem('etia_' + p.clave, p.valor);
                        ETIA.config[p.clave] = p.valor;
                    }
                }
            });
        }
    }).catch(function(err) {
        console.warn('ETIA: Error loading preferences from backend:', err);
    });
}

/* ─── Cambiar Empresa (with proper cleanup) ─── */
ETIA.logout = function() {
    ApiClient.logout();
};

ETIA.cambiarEmpresa = function() {
    if (!confirm('¿Cerrar sesión y cambiar de empresa? Se perderá la configuración local.')) return;
    /* Keep theme preference across company switch */
    var savedTheme = localStorage.getItem('etia_theme');
    /* Use shared cleanup if available (navigation.js), otherwise inline */
    if (typeof _clearAllEtiaState === 'function') {
        _clearAllEtiaState();
    } else {
        var keysToRemove = [];
        for (var i = 0; i < localStorage.length; i++) {
            var key = localStorage.key(i);
            if (key && key.indexOf('etia_') === 0) keysToRemove.push(key);
        }
        keysToRemove.forEach(function(k) { localStorage.removeItem(k); });
        sessionStorage.clear();
    }
    /* Restore theme */
    if (savedTheme) localStorage.setItem('etia_theme', savedTheme);
    /* Redirect */
    window.location.href = '/';
};

/* ─── Numero Formatting Utility ─── */
/**
 * Formats a factura serie + numero to SUNAT standard: F001-00000011
 * @param {string} serie - e.g. 'F001'
 * @param {number|string} numero - e.g. 11
 * @returns {string} e.g. 'F001-00000011'
 */
ETIA.formatNumero = function(serie, numero) {
    var s = serie || '';
    var n = String(numero || 0);
    while (n.length < 8) n = '0' + n;
    return s + '-' + n;
};

/* ─── Date Formatting Utility ─── */
/**
 * Formats an ISO date string (yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss) to dd/MM/yyyy.
 * Returns '—' if input is falsy or unparseable.
 */
ETIA.formatDate = function(dateStr) {
    if (!dateStr) return '—';
    /* Handle both "yyyy-MM-dd" and "yyyy-MM-ddTHH:mm:ss..." */
    var parts = String(dateStr).substring(0, 10).split('-');
    if (parts.length !== 3) return dateStr;
    return parts[2] + '/' + parts[1] + '/' + parts[0];
};

/**
 * Formats an ISO datetime string to dd/MM/yyyy HH:mm.
 */
ETIA.formatDateTime = function(dateStr) {
    if (!dateStr) return '—';
    var d = ETIA.formatDate(dateStr);
    if (d === '—') return '—';
    var timePart = String(dateStr).substring(11, 16);
    return timePart ? (d + ' ' + timePart) : d;
};

/* ─── Logo as Base64 Data URL (shared helper for PDF templates) ─── */
ETIA._logoBase64Cache = null;
ETIA._logoBase64Promise = null;

/**
 * Fetch the company logo and return as a base64 data URL.
 * Caches the result so subsequent calls are instant.
 * Returns '' if no logo is configured or fetch fails.
 * @returns {Promise<string>} data:image/...;base64,... or ''
 */
ETIA.getLogoBase64 = function() {
    if (ETIA._logoBase64Cache !== null) return Promise.resolve(ETIA._logoBase64Cache);
    if (ETIA._logoBase64Promise) return ETIA._logoBase64Promise;
    if (!ETIA.companyId) return Promise.resolve('');

    ETIA._logoBase64Promise = fetch(ApiClient.getLogoUrl(ETIA.companyId))
        .then(function(resp) {
            if (!resp.ok) return '';
            return resp.blob();
        })
        .then(function(blob) {
            if (!blob || blob.size === 0) return '';
            return new Promise(function(resolve) {
                var reader = new FileReader();
                reader.onloadend = function() { resolve(reader.result || ''); };
                reader.onerror = function() { resolve(''); };
                reader.readAsDataURL(blob);
            });
        })
        .then(function(dataUrl) {
            ETIA._logoBase64Cache = dataUrl;
            ETIA._logoBase64Promise = null;
            return dataUrl;
        })
        .catch(function() {
            ETIA._logoBase64Cache = '';
            ETIA._logoBase64Promise = null;
            return '';
        });

    return ETIA._logoBase64Promise;
};

/** Invalidate logo cache (e.g. after uploading a new logo) */
ETIA.invalidateLogoCache = function() {
    ETIA._logoBase64Cache = null;
    ETIA._logoBase64Promise = null;
    sessionStorage.removeItem('etia_company'); // force re-fetch of company data
};

/* ─── Close TC popup on outside click ─── */
document.addEventListener('click', function(e) {
    var popup = document.getElementById('hdr-tc-popup');
    var badge = document.getElementById('hdr-tc-badge');
    if (popup && popup.style.display !== 'none') {
        if (!popup.contains(e.target) && (!badge || !badge.contains(e.target))) {
            popup.style.display = 'none';
        }
    }
});

