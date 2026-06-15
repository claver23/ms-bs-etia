/* ═══════════════════════════════════════════════
   🔌  ETIA — API Client (Centralized REST layer)
   Todas las vistas consumen datos SOLO a través de este módulo.
   ═══════════════════════════════════════════════ */

var ApiClient = (function() {
    'use strict';

    var BASE = '/api/v1';

    /* ─── Helper: get auth headers ─── */
    function _headers(contentType) {
        var h = {};
        if (contentType) h['Content-Type'] = contentType;
        var token = localStorage.getItem('etia_token');
        if (token) h['Authorization'] = 'Bearer ' + token;
        return h;
    }

    /* ─── Helper: handle response with error toast ─── */
    function _handleResponse(r, url) {
        if (r.status === 401) {
            // Don't redirect on auth endpoints (login/register handle their own errors)
            if (url.indexOf('/auth/login') !== -1 || url.indexOf('/auth/register') !== -1) {
                return r.json();
            }
            localStorage.removeItem('etia_token');
            window.location.href = '/login';
            throw new Error('Sesion expirada');
        }
        if (!r.ok) {
            // For auth endpoints, return the JSON body so the form can show the message
            if (url.indexOf('/auth/login') !== -1 || url.indexOf('/auth/register') !== -1) {
                return r.json();
            }
            return r.text().then(function(body) {
                var detail = { status: r.status, message: r.statusText, url: url, body: body };
                if (typeof ETIA !== 'undefined' && ETIA.showError) ETIA.showError(detail);
                throw new Error('HTTP ' + r.status + ': ' + r.statusText);
            });
        }
        return r.json();
    }

    function _get(path) {
        var url = BASE + path;
        return fetch(url, { headers: _headers() }).then(function(r) { return _handleResponse(r, url); });
    }

    function _post(path, body) {
        var url = BASE + path;
        return fetch(url, {
            method: 'POST',
            headers: _headers('application/json'),
            body: JSON.stringify(body)
        }).then(function(r) { return _handleResponse(r, url); });
    }

    function _put(path, body) {
        var url = BASE + path;
        return fetch(url, {
            method: 'PUT',
            headers: _headers('application/json'),
            body: JSON.stringify(body)
        }).then(function(r) { return _handleResponse(r, url); });
    }

    function _delete(path) {
        var url = BASE + path;
        return fetch(url, { method: 'DELETE', headers: _headers() })
            .then(function(r) { return _handleResponse(r, url); });
    }

    /* ═══════ COMPANY ═══════ */
    function listCompanies() {
        return _get('/company').then(function(res) {
            return (res.data && res.data.content) ? res.data.content : [];
        });
    }

    function updateCompany(companyId, payload) {
        return _put('/company?companyId=' + encodeURIComponent(companyId), payload);
    }

    function uploadCertificado(companyId, file) {
        var url = BASE + '/company/certificado?companyId=' + encodeURIComponent(companyId);
        var formData = new FormData();
        formData.append('archivo', file);
        return fetch(url, {
            method: 'POST',
            headers: _headers(),
            body: formData
        }).then(function(r) { return _handleResponse(r, url); });
    }

    function probarConexionSunat(companyId) {
        return _post('/company/probar-conexion?companyId=' + encodeURIComponent(companyId), {});
    }

    function uploadLogo(companyId, file) {
        var url = BASE + '/company/logo?companyId=' + encodeURIComponent(companyId);
        var formData = new FormData();
        formData.append('archivo', file);
        return fetch(url, {
            method: 'POST',
            headers: _headers(),
            body: formData
        }).then(function(r) { return _handleResponse(r, url); });
    }

    function getLogoUrl(companyId) {
        return BASE + '/company/logo?companyId=' + encodeURIComponent(companyId);
    }

    /* ═══════ CLIENT ═══════ */
    function listClients(companyId) {
        return _get('/client?companyId=' + encodeURIComponent(companyId)).then(function(res) {
            return (res.data && res.data.content) ? res.data.content : [];
        });
    }

    function registerClient(payload) {
        return _post('/client', payload);
    }

    /* ═══════ RENIEC / SUNAT Document Query ═══════ */
    function consultarReniec(tipo, numero) {
        return _get('/reniec?tipo=' + encodeURIComponent(tipo) + '&numero=' + encodeURIComponent(numero))
            .then(function(res) { return res.data || res; });
    }

    /* ═══════ TIPO DE CAMBIO ═══════ */
    function getTipoCambio(fecha, moneda, fuente) {
        var url = '/tipo_cambio?fecha=' + encodeURIComponent(fecha);
        if (moneda) url += '&moneda=' + encodeURIComponent(moneda);
        if (fuente) url += '&fuente=' + encodeURIComponent(fuente);
        return _get(url).then(function(res) {
            return res.data || res;
        });
    }

    function listTiposCambio() {
        return _get('/tipo_cambio/list').then(function(res) {
            return (res.data && res.data.content) ? res.data.content : (res.data || []);
        });
    }

    /* ═══════ CLIENT (extended) ═══════ */
    function updateClient(clientId, payload) {
        return _put('/client?clientId=' + encodeURIComponent(clientId), payload);
    }

    function deleteClient(clientId) {
        return _delete('/client?clientId=' + encodeURIComponent(clientId));
    }

    /* ═══════ EMPLOYEE ═══════ */
    function listEmployees(companyId, estado) {
        var url = '/employee?companyId=' + encodeURIComponent(companyId);
        if (estado) url += '&estado=' + encodeURIComponent(estado);
        return _get(url).then(function(res) {
            return res.data || [];
        });
    }

    function registerEmployee(payload) {
        return _post('/employee', payload);
    }

    function updateEmployee(employeeId, payload) {
        return _put('/employee?employeeId=' + encodeURIComponent(employeeId), payload);
    }

    function deleteEmployee(employeeId, softDelete) {
        var url = '/employee?employeeId=' + encodeURIComponent(employeeId);
        if (softDelete !== undefined) url += '&softDelete=' + softDelete;
        return _delete(url);
    }

    /* ═══════ PRODUCT ═══════ */
    function listProducts(companyId) {
        return _get('/product?companyId=' + encodeURIComponent(companyId) + '&size=1000').then(function(res) {
            if (res.data && Array.isArray(res.data.content)) return res.data.content;
            if (Array.isArray(res.data)) return res.data;
            return [];
        });
    }

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

    function anularFactura(facturaId, payload) {
        return _put('/factura?facturaId=' + encodeURIComponent(facturaId), payload);
    }

    function eliminarFactura(facturaId) {
        return _delete('/factura?facturaId=' + encodeURIComponent(facturaId));
    }

    function emitirFacturaBorrador(facturaId) {
        return _put('/factura/' + encodeURIComponent(facturaId) + '/emitir', {});
    }

    function actualizarFacturaBorrador(facturaId, payload) {
        return _put('/factura/' + encodeURIComponent(facturaId) + '/actualizar', payload);
    }

    function validarFactura(facturaId) {
        return _post('/factura/' + encodeURIComponent(facturaId) + '/validar', {});
    }

    function descargarFacturaXml(facturaId) {
        var url = BASE + '/factura/' + encodeURIComponent(facturaId) + '/xml';
        return fetch(url).then(function(r) {
            if (r.status === 204) return null;
            if (!r.ok) throw new Error('HTTP ' + r.status);
            var cd = r.headers.get('Content-Disposition') || '';
            var match = cd.match(/filename="?([^"]+)"?/);
            var filename = match ? match[1] : (facturaId + '.xml');
            return r.blob().then(function(blob) { return { blob: blob, filename: filename }; });
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

    function validarBoleta(boletaId) {
        return _post('/boleta/' + encodeURIComponent(boletaId) + '/validar', {});
    }

    function actualizarBoletaBorrador(boletaId, payload) {
        return _put('/boleta/' + encodeURIComponent(boletaId) + '/actualizar', payload);
    }

    function eliminarBoleta(boletaId) {
        return _delete('/boleta?boletaId=' + encodeURIComponent(boletaId));
    }

    function descargarBoletaXml(boletaId) {
        var url = BASE + '/boleta/' + encodeURIComponent(boletaId) + '/xml';
        return fetch(url).then(function(r) {
            if (r.status === 204) return null;
            if (!r.ok) throw new Error('HTTP ' + r.status);
            var cd = r.headers.get('Content-Disposition') || '';
            var match = cd.match(/filename="?([^"]+)"?/);
            var filename = match ? match[1] : (boletaId + '.xml');
            return r.blob().then(function(blob) { return { blob: blob, filename: filename }; });
        });
    }

    function anularBoleta(boletaId, payload) {
        return _put('/boleta?boletaId=' + encodeURIComponent(boletaId), payload);
    }

    /* ═══════ RESUMEN DIARIO ═══════ */
    function crearResumenDiario(payload) {
        return _post('/resumen-diario', payload);
    }

    function listarResumenesDiarios(companyId) {
        return _get('/resumen-diario?companyId=' + encodeURIComponent(companyId)).then(function(res) {
            return (res.data && res.data.content) ? res.data.content : [];
        });
    }

    function obtenerResumenDiario(resumenId) {
        return _get('/resumen-diario/' + encodeURIComponent(resumenId)).then(function(res) {
            return res.data || null;
        });
    }

    function consultarEstadoResumen(resumenId) {
        return _post('/resumen-diario/' + encodeURIComponent(resumenId) + '/consultar-estado', {});
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

    function actualizarNotaCreditoBorrador(notaCreditoId, payload) {
        return _put('/nota_credito?notaCreditoId=' + encodeURIComponent(notaCreditoId), payload);
    }

    function eliminarNotaCreditoBorrador(notaCreditoId) {
        return _delete('/nota_credito?notaCreditoId=' + encodeURIComponent(notaCreditoId));
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

    function actualizarNotaDebitoBorrador(notaDebitoId, payload) {
        return _put('/nota_debito?notaDebitoId=' + encodeURIComponent(notaDebitoId), payload);
    }

    function eliminarNotaDebitoBorrador(notaDebitoId) {
        return _delete('/nota_debito?notaDebitoId=' + encodeURIComponent(notaDebitoId));
    }

    /* ═══════ LIBRO ELECTRÓNICO ═══════ */
    function generarLibroElectronico(payload) {
        return _post('/libro_electronico', payload);
    }

    function listarLibrosElectronicos(companyId, tipo, periodo, page, size) {
        var url = '/libro_electronico?companyId=' + encodeURIComponent(companyId);
        if (tipo) url += '&tipo=' + encodeURIComponent(tipo);
        if (periodo) url += '&periodo=' + encodeURIComponent(periodo);
        if (page !== undefined) url += '&page=' + page;
        if (size !== undefined) url += '&size=' + size;
        return _get(url).then(function(res) {
            return (res.data && res.data.content) ? res.data.content : [];
        });
    }

    /* ═══════ SEDE ═══════ */
    function listSedes(companyId) {
        return _get('/sede?companyId=' + encodeURIComponent(companyId)).then(function(res) {
            return res.data || [];
        });
    }

    function getSede(sedeId) {
        return _get('/sede/' + encodeURIComponent(sedeId)).then(function(res) {
            return res.data || null;
        });
    }

    function createSede(payload) {
        return _post('/sede', payload);
    }

    function updateSede(sedeId, payload) {
        return _put('/sede?sedeId=' + encodeURIComponent(sedeId), payload);
    }

    function deleteSede(sedeId) {
        return _delete('/sede?sedeId=' + encodeURIComponent(sedeId));
    }

    function generarSeriesSede(sedeId) {
        return _post('/sede/' + encodeURIComponent(sedeId) + '/generar_series', {});
    }

    /* ═══════ TABLAS SUNAT ═══════ */
    function listMenuTablasSunat() {
        return _get('/tabla_sunat/menu');
    }

    function obtenerTablaSunat(nombreTabla) {
        return _get('/tabla_sunat?nombreTabla=' + encodeURIComponent(nombreTabla))
            .then(function(res) { return res.data || res; });
    }

    /* ═══════ CATÁLOGO SUNAT ═══════ */
    function listMenuCatalogosSunat() {
        return _get('/catalogo_sunat/menu');
    }

    function obtenerCatalogoSunat(nombreCatalogo) {
        return _get('/catalogo_sunat?nombreCatalogo=' + encodeURIComponent(nombreCatalogo))
            .then(function(res) { return res.data || res; });
    }

    /* ═══════ AUTH ═══════ */
    function login(email, password) {
        return _post('/auth/login', { email: email, password: password }).then(function(res) {
            if (res.isSuccess && res.data && res.data.token) {
                localStorage.setItem('etia_token', res.data.token);
            }
            return res;
        });
    }

    function register(nombre, email, password) {
        return _post('/auth/register', { nombre: nombre, email: email, password: password }).then(function(res) {
            if (res.isSuccess && res.data && res.data.token) {
                localStorage.setItem('etia_token', res.data.token);
            }
            return res;
        });
    }

    function logout() {
        localStorage.removeItem('etia_token');
        window.location.href = '/login';
    }

    function _getTokenPayload() {
        var token = localStorage.getItem('etia_token');
        if (!token) return null;
        try {
            var parts = token.split('.');
            if (parts.length < 2) return null;
            return JSON.parse(atob(parts[1]));
        } catch (e) {
            return null;
        }
    }

    function isAuthenticated() {
        var payload = _getTokenPayload();
        if (!payload) {
            localStorage.removeItem('etia_token');
            return false;
        }
        if (payload.exp && (payload.exp * 1000) <= Date.now()) {
            localStorage.removeItem('etia_token');
            return false;
        }
        return true;
    }

    function getCompanyIdFromToken() {
        if (!isAuthenticated()) return '';
        var payload = _getTokenPayload();
        return (payload && payload.companyId) ? payload.companyId : '';
    }

    function resolveInitialRoute() {
        if (!isAuthenticated()) return '/login';
        return getCompanyIdFromToken() ? '/menu' : '/onboarding';
    }

    /* ═══════ PUBLIC API ═══════ */
    return {
        login:              login,
        register:           register,
        logout:             logout,
        isAuthenticated:    isAuthenticated,
        getCompanyIdFromToken: getCompanyIdFromToken,
        resolveInitialRoute: resolveInitialRoute,
        listCompanies:      listCompanies,
        updateCompany:      updateCompany,
        uploadCertificado:  uploadCertificado,
        probarConexionSunat: probarConexionSunat,
        uploadLogo:         uploadLogo,
        getLogoUrl:         getLogoUrl,
        listClients:        listClients,
        registerClient:     registerClient,
        updateClient:       updateClient,
        deleteClient:       deleteClient,
        consultarReniec:    consultarReniec,
        getTipoCambio:      getTipoCambio,
        listTiposCambio:    listTiposCambio,
        listEmployees:      listEmployees,
        registerEmployee:   registerEmployee,
        updateEmployee:     updateEmployee,
        deleteEmployee:     deleteEmployee,
        listProducts:       listProducts,
        searchProducts:     searchProducts,
        createProduct:      createProduct,
        getProduct:         getProduct,
        updateProduct:      updateProduct,
        deleteProduct:      deleteProduct,
        listUbigeos:        listUbigeos,
        listUnidadesMedida: listUnidadesMedida,
        listSeriesConfig:   listSeriesConfig,
        listSedes:          listSedes,
        getSede:            getSede,
        createSede:         createSede,
        updateSede:         updateSede,
        deleteSede:         deleteSede,
        generarSeriesSede:  generarSeriesSede,
        listPreferences:    listPreferences,
        savePreference:     savePreference,
        emitirFactura:      emitirFactura,
        listarFacturas:     listarFacturas,
        anularFactura:      anularFactura,
        eliminarFactura:    eliminarFactura,
        emitirFacturaBorrador: emitirFacturaBorrador,
        actualizarFacturaBorrador: actualizarFacturaBorrador,
        validarFactura:     validarFactura,
        descargarFacturaXml: descargarFacturaXml,
        getFactura:         getFactura,
        emitirBoleta:       emitirBoleta,
        listarBoletas:      listarBoletas,
        validarBoleta:      validarBoleta,
        actualizarBoletaBorrador: actualizarBoletaBorrador,
        eliminarBoleta:     eliminarBoleta,
        descargarBoletaXml: descargarBoletaXml,
        anularBoleta:       anularBoleta,
        getBoleta:          getBoleta,
        crearResumenDiario:      crearResumenDiario,
        listarResumenesDiarios:  listarResumenesDiarios,
        obtenerResumenDiario:    obtenerResumenDiario,
        consultarEstadoResumen:  consultarEstadoResumen,
        emitirNotaCredito:  emitirNotaCredito,
        listarNotasCredito: listarNotasCredito,
        getNotaCredito:     getNotaCredito,
        actualizarNotaCreditoBorrador: actualizarNotaCreditoBorrador,
        eliminarNotaCreditoBorrador: eliminarNotaCreditoBorrador,
        emitirNotaDebito:   emitirNotaDebito,
        listarNotasDebito:  listarNotasDebito,
        getNotaDebito:      getNotaDebito,
        actualizarNotaDebitoBorrador: actualizarNotaDebitoBorrador,
        eliminarNotaDebitoBorrador: eliminarNotaDebitoBorrador,
        generarLibroElectronico: generarLibroElectronico,
        listarLibrosElectronicos: listarLibrosElectronicos,
        listMenuTablasSunat:    listMenuTablasSunat,
        obtenerTablaSunat:      obtenerTablaSunat,
        listMenuCatalogosSunat: listMenuCatalogosSunat,
        obtenerCatalogoSunat:   obtenerCatalogoSunat,
        listNotifications:  function(companyId) {
            return _get('/notification?companyId=' + companyId).then(function(res) {
                return res.data || res;
            });
        },
        markAllNotifRead:   function(companyId) { return _put('/notification/read-all?companyId=' + companyId, {}); },
        markNotifRead:      function(notificationId) { return _put('/notification/' + notificationId + '/read', {}); },
        clearNotifications: function(companyId) { return _delete('/notification?companyId=' + companyId); }
    };
})();
