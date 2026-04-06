/* ═══════ ETIA — Client Search Modal ═══════ */
var cmSearchTimeout = null;
var _allClients = null; /* Cache */

/* ─── Tab toggle ─── */
function cmShowTab(tab) {
    var searchPanel = document.getElementById('cm-panel-search');
    var registerPanel = document.getElementById('cm-panel-register');
    var footerSearch = document.getElementById('cm-footer-search');
    var tabSearch = document.getElementById('cm-tab-search');
    var tabRegister = document.getElementById('cm-tab-register');
    if (tab === 'register') {
        searchPanel.style.display = 'none';
        registerPanel.style.display = 'block';
        footerSearch.style.display = 'none';
        tabSearch.className = 'btn btn-ghost';
        tabRegister.className = 'btn btn-accent';
    } else {
        searchPanel.style.display = 'block';
        registerPanel.style.display = 'none';
        footerSearch.style.display = '';
        tabSearch.className = 'btn btn-accent';
        tabRegister.className = 'btn btn-ghost';
    }
}

function openClientModal() {
    document.getElementById('client-modal').classList.add('show');
    cmShowTab('search');
    var input = document.getElementById('cm-search-input');
    input.value = '';
    document.getElementById('cm-results').innerHTML = '<div class="pm-empty">Cargando clientes...</div>';
    setTimeout(function(){ input.focus(); }, 100);

    /* Filter register tab tipo select based on allowed types */
    _filterRegisterTipoOptions();

    /* Pre-load all clients */
    if (_allClients) {
        renderClientResults(_allClients, '');
    } else {
        ApiClient.listClients(ETIA.companyId).then(function(clients) {
            _allClients = clients;
            renderClientResults(clients, '');
        }).catch(function() {
            document.getElementById('cm-results').innerHTML = '<div class="pm-empty" style="color:var(--danger);">Error cargando clientes</div>';
        });
    }
}

/* ─── Filter register tipo options based on SUNAT rules ─── */
function _filterRegisterTipoOptions() {
    var regTipo = document.getElementById('cm-reg-tipo');
    if (!regTipo || !ETIA.allowedClientDocTypes) return;
    var allowed = ETIA.allowedClientDocTypes;
    /* Map code → enum name used in register select */
    var codeToEnum = { '1':'DNI', '4':'CE', '6':'RUC', '7':'PASAPORTE' };
    var allowedEnums = allowed.map(function(c) { return codeToEnum[c] || c; });
    var opts = regTipo.querySelectorAll('option');
    var firstVisible = null;
    opts.forEach(function(opt) {
        var show = (allowedEnums.indexOf(opt.value) !== -1);
        opt.style.display = show ? '' : 'none';
        if (show && !firstVisible) firstVisible = opt.value;
    });
    if (firstVisible) regTipo.value = firstVisible;
}

function closeClientModal() {
    document.getElementById('client-modal').classList.remove('show');
}

function onModalClientSearch(term) {
    clearTimeout(cmSearchTimeout);
    var container = document.getElementById('cm-results');
    if (!_allClients) {
        container.innerHTML = '<div class="pm-spinner">⏳ Cargando...</div>';
        ApiClient.listClients(ETIA.companyId).then(function(clients) {
            _allClients = clients;
            renderClientResults(clients, term);
        });
        return;
    }
    cmSearchTimeout = setTimeout(function() {
        renderClientResults(_allClients, term);
    }, 150);
}

function renderClientResults(clients, term) {
    var container = document.getElementById('cm-results');
    var filtered = clients;

    /* ── Filter by allowed document types (SUNAT rules) ── */
    if (ETIA.allowedClientDocTypes && ETIA.allowedClientDocTypes.length > 0) {
        var allowed = ETIA.allowedClientDocTypes;
        /* Map enum names to SUNAT codes for comparison */
        var enumToCode = { 'DNI':'1', 'RUC':'6', 'CE':'4', 'PASAPORTE':'7' };
        filtered = filtered.filter(function(c) {
            var code = enumToCode[c.tipoDocumento] || c.tipoDocumento || '';
            return allowed.indexOf(code) !== -1;
        });
    }

    if (term && term.trim().length > 0) {
        var t = term.toLowerCase();
        filtered = filtered.filter(function(c) {
            return (c.denominacion && c.denominacion.toLowerCase().includes(t)) ||
                   (c.numeroDocumento && c.numeroDocumento.toLowerCase().includes(t)) ||
                   (c.email && c.email.toLowerCase().includes(t));
        });
    }
    if (!filtered || filtered.length === 0) {
        var docTypeMsg = '';
        if (ETIA.allowedClientDocTypes) {
            var labels = { '6':'RUC', '1':'DNI', '4':'C.E.', '7':'Pasaporte' };
            docTypeMsg = ' (solo ' + ETIA.allowedClientDocTypes.map(function(c) { return labels[c]||c; }).join(', ') + ')';
        }
        container.innerHTML = '<div class="pm-empty">No se encontraron clientes' + docTypeMsg + (term ? ' para "' + term + '"' : '') + '</div>';
        return;
    }
    var tipoLabels = { '1':'DNI', '6':'RUC', '4':'C.E.', '7':'Pasaporte', 'DNI':'DNI', 'RUC':'RUC', 'CE':'C.E.', 'PASAPORTE':'Pasaporte' };
    container.innerHTML = filtered.map(function(c) {
        var tipoRaw = c.tipoDocumento || '';
        /* Handle both SUNAT codes and enum names */
        var tipoLabel = tipoLabels[tipoRaw] || tipoRaw;
        var safe = function(s) { return (s || '').replace(/'/g, "\\'").replace(/"/g, '&quot;'); };
        return '<div class="pm-item" onclick="selectClientFromModal(\'' + safe(c.id) + '\',\'' + safe(tipoRaw) + '\',\'' + safe(c.numeroDocumento) + '\',\'' + safe(c.denominacion) + '\',\'' + safe(c.direccion) + '\',\'' + safe(c.email) + '\',\'' + safe(c.telefono) + '\')">' +
            '<div class="pm-item-info">' +
                '<h4>' + (c.denominacion || 'Sin nombre') + '</h4>' +
                '<span><strong>' + tipoLabel + ':</strong> ' + (c.numeroDocumento || '—') + '</span>' +
            '</div>' +
            '<div class="pm-item-price">' +
                '<div class="price" style="font-size:0.75rem;color:var(--text-secondary);">' + (c.email || '') + '</div>' +
                '<div class="unit">' + (c.telefono || '') + '</div>' +
            '</div>' +
        '</div>';
    }).join('');
}

function selectClientFromModal(id, tipo, numDoc, denom, dir, email, tel) {
    ETIA.clientId = id;
    /* Map enum names back to SUNAT codes for the select */
    var codeMap = { 'DNI':'1', 'RUC':'6', 'CE':'4', 'PASAPORTE':'7' };
    var tipoCode = codeMap[tipo] || tipo;
    document.getElementById('cl-tipo').value = tipoCode;
    document.getElementById('cl-numdoc').value = numDoc || '';
    document.getElementById('cl-denom').value = denom || '';
    document.getElementById('cl-dir').value = dir || '';
    document.getElementById('cl-email').value = email || '';
    document.getElementById('cl-tel').value = tel || '';
    closeClientModal();
}

/* ─── RENIEC / SUNAT Consultation ─── */
function queryReniec() {
    var tipo = document.getElementById('cm-reg-tipo').value;
    var numDoc = document.getElementById('cm-reg-numdoc').value.trim();
    var statusEl = document.getElementById('cm-reniec-status');
    var btn = document.getElementById('cm-btn-reniec');

    if (!numDoc) {
        statusEl.style.display = 'block';
        statusEl.style.background = 'var(--warning-bg)';
        statusEl.style.color = 'var(--warning)';
        statusEl.textContent = '⚠️ Ingresa un número de documento';
        return;
    }
    if (tipo === 'DNI' && numDoc.length !== 8) {
        statusEl.style.display = 'block';
        statusEl.style.background = 'var(--warning-bg)';
        statusEl.style.color = 'var(--warning)';
        statusEl.textContent = '⚠️ El DNI debe tener 8 dígitos';
        return;
    }
    if (tipo === 'RUC' && numDoc.length !== 11) {
        statusEl.style.display = 'block';
        statusEl.style.background = 'var(--warning-bg)';
        statusEl.style.color = 'var(--warning)';
        statusEl.textContent = '⚠️ El RUC debe tener 11 dígitos';
        return;
    }

    btn.disabled = true;
    btn.textContent = '⏳ Consultando...';
    statusEl.style.display = 'block';
    statusEl.style.background = 'var(--accent-glow)';
    statusEl.style.color = 'var(--accent)';
    statusEl.textContent = '🔄 Consultando RENIEC/SUNAT...';

    ApiClient.consultarReniec(tipo, numDoc).then(function(data) {
        btn.disabled = false;
        btn.textContent = '🔎 Consultar';
        if (data && (data.denominacion || data.nombre)) {
            var nombre = data.denominacion || ((data.nombre || '') + ' ' + (data.apellidoPaterno || '') + ' ' + (data.apellidoMaterno || '')).trim();
            document.getElementById('cm-reg-denom').value = nombre;
            document.getElementById('cm-reg-dir').value = data.direccion || '';
            statusEl.style.background = 'rgba(0,255,136,0.1)';
            statusEl.style.color = 'var(--accent)';
            statusEl.textContent = '✅ Datos encontrados — ' + nombre;
        } else {
            statusEl.style.background = 'var(--warning-bg)';
            statusEl.style.color = 'var(--warning)';
            statusEl.textContent = '⚠️ No se encontraron datos para este documento. Completa manualmente.';
        }
    }).catch(function(err) {
        btn.disabled = false;
        btn.textContent = '🔎 Consultar';
        statusEl.style.background = 'rgba(248,81,73,0.1)';
        statusEl.style.color = 'var(--danger)';
        statusEl.textContent = '❌ Error consultando: ' + (err.message || err);
    });
}

/* ─── Register new client and select it ─── */
function submitNewClient() {
    var tipo = document.getElementById('cm-reg-tipo').value;
    var numDoc = document.getElementById('cm-reg-numdoc').value.trim();
    var denom = document.getElementById('cm-reg-denom').value.trim();
    var dir = document.getElementById('cm-reg-dir').value.trim();
    var email = document.getElementById('cm-reg-email').value.trim();
    var tel = document.getElementById('cm-reg-tel').value.trim();

    if (!numDoc || !denom) {
        var statusEl = document.getElementById('cm-reniec-status');
        statusEl.style.display = 'block';
        statusEl.style.background = 'var(--warning-bg)';
        statusEl.style.color = 'var(--warning)';
        statusEl.textContent = '⚠️ Documento y Denominación son obligatorios';
        return;
    }

    var payload = {
        companyId: ETIA.companyId,
        tipoDocumento: tipo,
        numeroDocumento: numDoc,
        denominacion: denom,
        direccion: dir || null,
        email: email || null,
        telefono: tel || null
    };

    ApiClient.registerClient(payload).then(function(res) {
        var newClient = res.data || res;
        var newId = newClient.id || '';
        /* Invalidate cache */
        _allClients = null;
        /* Select newly created client */
        selectClientFromModal(newId, tipo, numDoc, denom, dir, email, tel);
    }).catch(function(err) {
        var statusEl = document.getElementById('cm-reniec-status');
        statusEl.style.display = 'block';
        statusEl.style.background = 'rgba(248,81,73,0.1)';
        statusEl.style.color = 'var(--danger)';
        statusEl.textContent = '❌ Error registrando cliente: ' + (err.message || err);
    });
}

