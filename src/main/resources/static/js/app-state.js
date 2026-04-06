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

/* ─── Bootstrap: load company + client + sedes + notifications from API ─── */
ETIA.init = function() {
    return ApiClient.listCompanies().then(function(companies) {
        if (companies && companies.length > 0) {
            var c = companies[0];
            ETIA.company   = c;
            ETIA.companyId = c.id;
            /* Populate header company name */
            var hdrCompany = document.getElementById('hdr-company');
            if (hdrCompany) hdrCompany.textContent = c.razonSocial || '';

            /* Load in parallel: clients, sedes, rejected docs */
            var loadClients = ApiClient.listClients(c.id).then(function(clients) {
                if (clients && clients.length > 0) {
                    ETIA.client   = clients[0];
                    ETIA.clientId = clients[0].id;
                }
            }).catch(function(err) { console.warn('ETIA.init: Error loading clients:', err); });

            var loadSedes = ApiClient.listSedes(c.id).then(function(sedes) {
                ETIA.sedes = sedes || [];
                _populateSedeSelector(ETIA.sedes);
            }).catch(function(err) { console.warn('ETIA.init: Error loading sedes:', err); });

            var loadNotifs = _loadRejectedDocs(c.id);
            var loadTxNotifs = _loadTransmissionNotifs(c.id);

            return Promise.all([loadClients, loadSedes, loadNotifs, loadTxNotifs]).then(function() { return c; });
        }
        return null;
    }).catch(function(err) {
        console.error('ETIA.init error:', err);
        return null;
    });
};

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
                    '<span>' + (d.fecha || '') + ' · S/ ' + (d.total != null ? d.total.toFixed(2) : '—') + ' · ' + d.estado + '</span>' +
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

