/* ═══════════════════════════════════════════════
   🔌  ETIA — API Client (Centralized REST layer)
   Todas las vistas consumen datos SOLO a través de este módulo.
   ═══════════════════════════════════════════════ */

var ApiClient = (function() {
    'use strict';

    var BASE = '/api/v1';

    /* ─── Helper: handle response with error toast ─── */
    function _handleResponse(r, url) {
        if (!r.ok) {
            return r.text().then(function(body) {
                var detail = { status: r.status, message: r.statusText, url: url, body: body };
                if (ETIA.showError) ETIA.showError(detail);
                throw new Error('HTTP ' + r.status + ': ' + r.statusText);
            });
        }
        return r.json();
    }

    function _get(path) {
        var url = BASE + path;
        return fetch(url).then(function(r) { return _handleResponse(r, url); });
    }

    function _post(path, body) {
        var url = BASE + path;
        return fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        }).then(function(r) { return _handleResponse(r, url); });
    }

    function _put(path, body) {
        var url = BASE + path;
        return fetch(url, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        }).then(function(r) { return _handleResponse(r, url); });
    }

    function _delete(path) {
        var url = BASE + path;
        return fetch(url, { method: 'DELETE' })
            .then(function(r) { return _handleResponse(r, url); });
    }

    /* ═══════ COMPANY ═══════ */
    function listCompanies() {
        return _get('/company').then(function(res) {
            return (res.data && res.data.content) ? res.data.content : [];
        });
    }

    /* ═══════ CLIENT ═══════ */
    function listClients(companyId) {
        return _get('/client?companyId=' + encodeURIComponent(companyId)).then(function(res) {
            return (res.data && res.data.content) ? res.data.content : [];
        });
    }

    /* ═══════ EMPLOYEE ═══════ */
    function listEmployees(companyId, estado) {
        var url = '/employee?companyId=' + encodeURIComponent(companyId);
        if (estado) url += '&estado=' + encodeURIComponent(estado);
        return _get(url).then(function(res) {
            return res.data || [];
        });
    }

    /* ═══════ PRODUCT ═══════ */
    function searchProducts(companyId, term, page, size) {
        var url = '/product?companyId=' + encodeURIComponent(companyId);
        if (term) url += '&term=' + encodeURIComponent(term);
        if (page !== undefined) url += '&page=' + page;
        if (size !== undefined) url += '&size=' + size;
        return _get(url).then(function(res) {
            var items, pag;
            if (res.data && Array.isArray(res.data.content)) {
                items = res.data.content;
                pag = res.data.pagination || {};
            } else if (Array.isArray(res.data)) {
                items = res.data;
                pag = {};
            } else {
                items = [];
                pag = {};
            }
            return { content: items, pagination: pag };
        });
    }

    function createProduct(product) {
        return _post('/product', product);
    }

    function getProduct(productId) {
        return _get('/product/' + encodeURIComponent(productId)).then(function(res) {
            return res.data || null;
        });
    }

    function updateProduct(productId, product) {
        return _put('/product?productId=' + encodeURIComponent(productId), product);
    }

    function deleteProduct(productId, softDelete) {
        var url = '/product?productId=' + encodeURIComponent(productId);
        if (softDelete !== undefined) url += '&softDelete=' + softDelete;
        return _delete(url);
    }

    /* ═══════ DOCUMENT DETAIL (get by ID) ═══════ */
    function getFactura(facturaId) {
        return _get('/factura/' + encodeURIComponent(facturaId)).then(function(res) {
            return res.data || null;
        });
    }

    function getBoleta(boletaId) {
        return _get('/boleta/' + encodeURIComponent(boletaId)).then(function(res) {
            return res.data || null;
        });
    }

    function getNotaCredito(notaCreditoId) {
        return _get('/nota_credito/' + encodeURIComponent(notaCreditoId)).then(function(res) {
            return res.data || null;
        });
    }

    function getNotaDebito(notaDebitoId) {
        return _get('/nota_debito/' + encodeURIComponent(notaDebitoId)).then(function(res) {
            return res.data || null;
        });
    }

    /* ═══════ UBIGEO ═══════ */
    function listUbigeos() {
        return _get('/ubigeo');
    }

    /* ═══════ UNIDAD MEDIDA ═══════ */
    function listUnidadesMedida() {
        return _get('/unidad_medida');
    }

    /* ═══════ SERIE CONFIG ═══════ */
    function listSeriesConfig(companyId, tipoDocumento) {
        var url = '/serie_config?companyId=' + encodeURIComponent(companyId);
        if (tipoDocumento) url += '&tipoDocumento=' + encodeURIComponent(tipoDocumento);
        return _get(url);
    }

    /* ═══════ USER PREFERENCE ═══════ */
    function listPreferences(companyId) {
        return _get('/user_preference?companyId=' + encodeURIComponent(companyId));
    }

    function savePreference(companyId, clave, valor) {
        return _put('/user_preference', { companyId: companyId, clave: clave, valor: valor });
    }

    /* ═══════ FACTURA ═══════ */
    function emitirFactura(payload) {
        return _post('/factura', payload);
    }

    function listarFacturas(companyId) {
        return _get('/factura?companyId=' + encodeURIComponent(companyId)).then(function(res) {
            return (res.data && res.data.content) ? res.data.content : [];
        });
    }

    /* ═══════ BOLETA ═══════ */
    function emitirBoleta(payload) {
        return _post('/boleta', payload);
    }

    function listarBoletas(companyId) {
        return _get('/boleta?companyId=' + encodeURIComponent(companyId)).then(function(res) {
            return (res.data && res.data.content) ? res.data.content : [];
        });
    }

    /* ═══════ NOTA CREDITO ═══════ */
    function emitirNotaCredito(payload) {
        return _post('/nota_credito', payload);
    }

    function listarNotasCredito(companyId) {
        return _get('/nota_credito?companyId=' + encodeURIComponent(companyId)).then(function(res) {
            return (res.data && res.data.content) ? res.data.content : [];
        });
    }

    /* ═══════ NOTA DEBITO ═══════ */
    function emitirNotaDebito(payload) {
        return _post('/nota_debito', payload);
    }

    function listarNotasDebito(companyId) {
        return _get('/nota_debito?companyId=' + encodeURIComponent(companyId)).then(function(res) {
            return (res.data && res.data.content) ? res.data.content : [];
        });
    }

    /* ═══════ PUBLIC API ═══════ */
    return {
        listCompanies:      listCompanies,
        listClients:        listClients,
        listEmployees:      listEmployees,
        searchProducts:     searchProducts,
        createProduct:      createProduct,
        getProduct:         getProduct,
        updateProduct:      updateProduct,
        deleteProduct:      deleteProduct,
        listUbigeos:        listUbigeos,
        listUnidadesMedida: listUnidadesMedida,
        listSeriesConfig:   listSeriesConfig,
        listPreferences:    listPreferences,
        savePreference:     savePreference,
        emitirFactura:      emitirFactura,
        listarFacturas:     listarFacturas,
        getFactura:         getFactura,
        emitirBoleta:       emitirBoleta,
        listarBoletas:      listarBoletas,
        getBoleta:          getBoleta,
        emitirNotaCredito:  emitirNotaCredito,
        listarNotasCredito: listarNotasCredito,
        getNotaCredito:     getNotaCredito,
        emitirNotaDebito:   emitirNotaDebito,
        listarNotasDebito:  listarNotasDebito,
        getNotaDebito:      getNotaDebito
    };
})();
