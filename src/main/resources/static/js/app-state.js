/* ═══════════════════════════════════════════════
   📦  ETIA — Global App State
   ═══════════════════════════════════════════════ */
window.ETIA = window.ETIA || {};

/* Defaults — populated dynamically from API */
ETIA.companyId = '';
ETIA.clientId  = '';
ETIA.company   = null;
ETIA.client    = null;
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
    tipoCambioFuente: localStorage.getItem('etia_tipoCambioFuente') || 'SBS'
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

        /* Tipo de cambio — for header badge */
        var loadTc = _loadTcBadge();

        return Promise.all([loadClients, loadSedes, loadNotifs, loadTxNotifs, loadTc]).then(function() { return company; });
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
}

function _applyClientsData(clients) {
    if (clients && clients.length > 0) {
        ETIA.client   = clients[0];
        ETIA.clientId = clients[0].id;
    }
}

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
        if (list) list.innerHTML = '<p class="g-notif-empty" style="padding:0.6rem;text-align:center;color:var(--text-muted);font-size:0.8rem;">✅ No hay comprobantes rechazados</p>';
    } else if (list) {
        list.innerHTML = docs.map(function(d) {
            var label = { FACTURA:'Factura', BOLETA:'Boleta', NOTA_CREDITO:'Nota Crédito', NOTA_DEBITO:'Nota Débito' }[d.tipo] || d.tipo;
            return '<div class="g-notif-item">' +
                '<div class="g-notif-item-info">' +
                    '<strong>' + label + ' ' + (d.serie || '') + '-' + (d.numero || '') + '</strong>' +
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
    var msg = 'Anulación de comprobante rechazado por SUNAT';
    ApiClient.annulDocument(id, tipo, msg)
        .then(function() {
            ETIA.rejectedDocs = ETIA.rejectedDocs.filter(function(d) { return d.id !== id; });
            _renderNotifBadge(ETIA.rejectedDocs);
            if (ETIA.showSuccess) ETIA.showSuccess({ message: 'Comprobante anulado exitosamente' });
            if (ETIA.refreshNotifications) ETIA.refreshNotifications();
        })
        .catch(function(err) {
            console.error('Error anulando documento:', err);
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
        list.innerHTML = '<p class="g-notif-empty" style="padding:0.6rem;text-align:center;color:var(--text-muted);font-size:0.8rem;">Sin transmisiones recientes</p>';
        return;
    }
    var icons = { FACTURA:'📄', BOLETA:'🧾', NOTA_CREDITO:'📋', NOTA_DEBITO:'📋' };
    var acciones = { EMITIDA:'✅', ANULADA:'🚫' };
    list.innerHTML = notifs.slice(0, 20).map(function(n) {
        var icon = icons[n.tipoDocumento] || '📄';
        var accionIcon = acciones[n.accion] || '';
        var time = n.timestamp ? new Date(n.timestamp).toLocaleTimeString('es-PE', {hour:'2-digit', minute:'2-digit'}) : '';
        var readClass = n.leida ? ' g-notif-item-read' : '';
        return '<div class="g-notif-item' + readClass + '">' +
            '<div class="g-notif-item-info">' +
                '<strong>' + icon + ' ' + accionIcon + ' ' + (n.mensaje || '') + '</strong>' +
                '<span>' + (n.serie || '') + ' · ' + time + '</span>' +
            '</div>' +
        '</div>';
    }).join('');
}

function _updateGlobalBadge() {
    var count = document.getElementById('hdr-notif-count');
    if (!count) return;
    var rejectedCount = (ETIA.rejectedDocs || []).length;
    var unreadTx = (ETIA.txNotifications || []).filter(function(n) { return !n.leida; }).length;
    var total = rejectedCount + unreadTx;
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
    }
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

