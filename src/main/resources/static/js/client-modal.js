/* ═══════ ETIA — Client Search Modal ═══════ */
var cmSearchTimeout = null;
var _allClients = null; /* Cache */

function openClientModal() {
    document.getElementById('client-modal').classList.add('show');
    var input = document.getElementById('cm-search-input');
    input.value = '';
    document.getElementById('cm-results').innerHTML = '<div class="pm-empty">Cargando clientes...</div>';
    setTimeout(function(){ input.focus(); }, 100);

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
    if (term && term.trim().length > 0) {
        var t = term.toLowerCase();
        filtered = clients.filter(function(c) {
            return (c.denominacion && c.denominacion.toLowerCase().includes(t)) ||
                   (c.numeroDocumento && c.numeroDocumento.toLowerCase().includes(t)) ||
                   (c.email && c.email.toLowerCase().includes(t));
        });
    }
    if (!filtered || filtered.length === 0) {
        container.innerHTML = '<div class="pm-empty">No se encontraron clientes' + (term ? ' para "' + term + '"' : '') + '</div>';
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

