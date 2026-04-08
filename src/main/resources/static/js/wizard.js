/* ═══════════════════════════════════════════════
   🧙  ETIA — Wizard Logic (Factura Emission)
   ═══════════════════════════════════════════════ */

let selectedDocType = 'FACTURA';
let wizStep = 1;
let editMode = false;
let editDocId = null;
let editDocEstado = null;

/* ═══════ INIT WIZARD ═══════ */
function initWizard(docType, editId) {
    selectedDocType = docType || 'FACTURA';
    editMode = !!editId;
    editDocId = editId || null;

    var labels = {FACTURA:'Factura Electrónica', BOLETA:'Boleta de Venta', NOTA_CREDITO:'Nota de Crédito', NOTA_DEBITO:'Nota de Débito'};
    var el = document.getElementById('wiz-detail-title');
    if (el) el.textContent = (editMode ? 'Editar — ' : 'Nuevo — ') + (labels[docType] || docType);

    /* Lock sede selector during document creation */
    var sedeSel = document.getElementById('hdr-sede-select');
    if (sedeSel) sedeSel.disabled = true;

    /* Populate serie selector from active sede */
    _populateSerieSelector(docType);

    /* Apply default currency from config */
    var monedaSelect = document.getElementById('fac-moneda');
    if (monedaSelect && ETIA.config.currency) {
        monedaSelect.value = ETIA.config.currency;
    }

    /* Set fecha emisión to today */
    var fechaInput = document.getElementById('fac-fecha');
    if (fechaInput) {
        var today = new Date().toISOString().split('T')[0];
        fechaInput.value = today;
    }

    /* Set fecha vencimiento = fecha emisión for CONTADO (default) */
    var fechaVencInput = document.getElementById('fac-fecha-venc');
    if (fechaVencInput && fechaInput) {
        fechaVencInput.value = fechaInput.value;
    }

    /* Show/hide tipo de cambio based on currency */
    onMonedaChange();

    /* Forma de pago — init */
    onFormaPagoChange();

    /* Auto-set client document type based on doc type (SUNAT rules) */
    _setupClientDocType(docType);

    /* Reset items on wizard init */
    items = [];
    itemCounter = 0;
    cuotas = [];
    cuotaCounter = 0;
    cargosDescuentos = [];
    acCounter = 0;
    renderItems();

    /* Adapt buttons based on "Emisión en Envío" config */
    _updateWizardEmisionUI();

    /* Show/hide nota-specific fields */
    _setupNotaFields(docType);

    /* Editing banner */
    var editBanner = document.getElementById('wiz-editing-banner');
    if (editBanner) editBanner.style.display = editMode ? 'block' : 'none';

    /* If edit mode → load existing document data */
    if (editMode && editDocId) {
        _loadDraftForEdit(docType, editDocId);
    }
}

/* ═══════ POPULATE SERIE SELECTOR ═══════ */
function _populateSerieSelector(docType) {
    var serieSelect = document.getElementById('fac-serie');
    if (!serieSelect) return;

    /* Determine which series to show from the active sede */
    var sede = ETIA.activeSede;
    var serieMap = { FACTURA: 'FACTURA', BOLETA: 'BOLETA', NOTA_CREDITO: 'NOTA_CREDITO', NOTA_DEBITO: 'NOTA_DEBITO' };
    var tipoDoc = serieMap[docType] || 'FACTURA';

    /* Config defaults fallback */
    var configSerieMap = { FACTURA: 'serieFac', BOLETA: 'serieBol', NOTA_CREDITO: 'serieNC', NOTA_DEBITO: 'serieND' };
    var defaultSerie = ETIA.config[configSerieMap[docType]] || 'F001';

    var seriesFromSede = [];
    if (sede && sede.series && sede.series.length) {
        seriesFromSede = sede.series.filter(function(s) { return s.tipoDocumento === tipoDoc; });
    }

    serieSelect.innerHTML = '';
    if (seriesFromSede.length > 0) {
        seriesFromSede.forEach(function(s) {
            var opt = document.createElement('option');
            opt.value = s.serie;
            opt.textContent = s.serie;
            serieSelect.appendChild(opt);
        });
        /* Select the default from config */
        var hasDefault = seriesFromSede.some(function(s) { return s.serie === defaultSerie; });
        if (hasDefault) {
            serieSelect.value = defaultSerie;
        }
    } else {
        /* Fallback: single option from config */
        var opt = document.createElement('option');
        opt.value = defaultSerie;
        opt.textContent = defaultSerie;
        serieSelect.appendChild(opt);
    }
}

/* ═══════ SETUP CLIENT DOC TYPE ═══════ */
function _setupClientDocType(docType) {
    var clTipo = document.getElementById('cl-tipo');
    if (!clTipo) return;
    var allowedTypes;
    if (docType === 'FACTURA' || docType === 'NOTA_CREDITO' || docType === 'NOTA_DEBITO') {
        allowedTypes = ['6'];
    } else if (docType === 'BOLETA') {
        allowedTypes = ['1', '4', '7'];
    } else {
        allowedTypes = ['6', '1', '4', '7', '0'];
    }
    ETIA.allowedClientDocTypes = allowedTypes;
    var clOptions = clTipo.querySelectorAll('option');
    clOptions.forEach(function(opt) {
        opt.style.display = (allowedTypes.indexOf(opt.value) !== -1) ? '' : 'none';
    });
    if (!ETIA.clientId) clTipo.value = allowedTypes[0];
    clTipo.disabled = true;
}

/* ═══════ SETUP NOTA FIELDS ═══════ */
function _setupNotaFields(docType) {
    var notaFields = document.getElementById('nota-fields');
    var notaTipoDocGroup = document.getElementById('nota-tipo-doc-group');
    if (!notaFields) return;
    var isNota = (docType === 'NOTA_CREDITO' || docType === 'NOTA_DEBITO');
    notaFields.style.display = isNota ? 'block' : 'none';
    var motivoSelect = document.getElementById('nota-motivo');
    if (motivoSelect) {
        var options = motivoSelect.querySelectorAll('option');
        options.forEach(function(opt) {
            if (opt.value === '') { opt.style.display = ''; return; }
            if (docType === 'NOTA_CREDITO') {
                opt.style.display = opt.classList.contains('opt-nc') ? '' : 'none';
            } else if (docType === 'NOTA_DEBITO') {
                opt.style.display = opt.classList.contains('opt-nd') ? '' : 'none';
            }
        });
        motivoSelect.selectedIndex = 0;
    }
    if (notaTipoDocGroup) {
        notaTipoDocGroup.style.display = (docType === 'NOTA_CREDITO') ? 'block' : 'none';
    }
}

/* ═══════ LOAD DRAFT FOR EDIT ═══════ */
function _loadDraftForEdit(docType, docId) {
    var fetchFn;
    if (docType === 'FACTURA') fetchFn = ApiClient.getFactura;
    else if (docType === 'BOLETA') fetchFn = ApiClient.getBoleta;
    else if (docType === 'NOTA_CREDITO') fetchFn = ApiClient.getNotaCredito;
    else if (docType === 'NOTA_DEBITO') fetchFn = ApiClient.getNotaDebito;
    else return;

    fetchFn(docId).then(function(doc) {
        if (!doc) { console.warn('Draft not found:', docId); return; }
        console.log('[wizard] _loadDraftForEdit doc:', JSON.stringify(doc, null, 2));
        editDocEstado = doc.estado || null;

        /* Show editing banner */
        var idEl = document.getElementById('wiz-editing-id');
        if (idEl) {
            var displayId = doc.serie && doc.numero
                ? ETIA.formatNumero(doc.serie, doc.numero)
                : docId.substring(0, 12) + '…';
            idEl.textContent = displayId;
        }

        /* ── Step 1: Document fields ── */
        if (doc.serie) {
            var serieSelect = document.getElementById('fac-serie');
            if (serieSelect) {
                /* Ensure option exists */
                var exists = Array.from(serieSelect.options).some(function(o) { return o.value === doc.serie; });
                if (!exists) {
                    var opt = document.createElement('option');
                    opt.value = doc.serie; opt.textContent = doc.serie;
                    serieSelect.appendChild(opt);
                }
                serieSelect.value = doc.serie;
            }
        }
        /* Show existing numero with padding */
        var numPreview = document.getElementById('fac-numero-preview');
        if (numPreview && doc.numero) {
            var n = String(doc.numero); while (n.length < 8) n = '0' + n;
            numPreview.value = n;
            numPreview.style.color = 'var(--text-primary)';
        }

        if (doc.fechaEmision) {
            var fechaEl = document.getElementById('fac-fecha');
            if (fechaEl) fechaEl.value = String(doc.fechaEmision).substring(0, 10);
        }
        if (doc.moneda) {
            var monedaEl = document.getElementById('fac-moneda');
            if (monedaEl) { monedaEl.value = doc.moneda; onMonedaChange(); }
        }
        if (doc.tipoCambio) {
            var tcEl = document.getElementById('fac-tipo-cambio');
            if (tcEl) tcEl.value = parseFloat(doc.tipoCambio).toFixed(4);
        }
        if (doc.formaPago) {
            var fpEl = document.getElementById('fac-forma-pago');
            if (fpEl) { fpEl.value = doc.formaPago; onFormaPagoChange(); }
        }
        if (doc.fechaVencimiento) {
            var fvEl = document.getElementById('fac-fecha-venc');
            if (fvEl) fvEl.value = String(doc.fechaVencimiento).substring(0, 10);
        }
        if (doc.tipoOperacion) {
            var toEl = document.getElementById('fac-tipo-operacion');
            if (toEl) toEl.value = doc.tipoOperacion;
        }
        if (doc.leyenda) {
            var leyEl = document.getElementById('fac-leyenda');
            if (leyEl) leyEl.value = doc.leyenda;
            var leyDisplay = document.getElementById('fac-leyenda-display');
            if (leyDisplay) leyDisplay.textContent = doc.leyenda;
        }

        /* If serie + fecha were already assigned, make them readonly in edit mode */
        if (doc.estado === 'BORRADOR' && doc.serie) {
            var sEl = document.getElementById('fac-serie');
            if (sEl) sEl.disabled = true;
            var fEl = document.getElementById('fac-fecha');
            if (fEl) fEl.readOnly = true;
        }

        /* ── Step 3: Client — look up from cached list or fetch ── */
        if (doc.clientId) {
            ETIA.clientId = doc.clientId;
            var client = ETIA.findClientById(doc.clientId);
            if (client) {
                _applyClientToWizard(client);
            } else {
                /* Client not in cache — fetch all clients and retry */
                ApiClient.listClients(ETIA.companyId).then(function(clients) {
                    ETIA.clients = clients || [];
                    var cl = ETIA.findClientById(doc.clientId);
                    if (cl) _applyClientToWizard(cl);
                }).catch(function() {
                    console.warn('Could not load client for edit:', doc.clientId);
                });
            }
        }

        /* ── Step 4: Items ── */
        var detalle = doc.detalle;
        console.log('[wizard] detalle array:', detalle);
        if (detalle && detalle.length > 0) {
            items = [];
            itemCounter = 0;
            detalle.forEach(function(d) {
                itemCounter++;
                var valorUnit = parseFloat(d.valorUnitario || 0);
                var precioUnit = parseFloat(d.precioUnitario || 0);
                var cantidad = parseFloat(d.cantidad || 1);
                var afect = d.tipoAfectacionIgv || '10';
                var igvInfo = IGV_MAP[afect] || IGV_MAP['10'];
                var baseImp = parseFloat(d.baseImponible || d.subtotal || (cantidad * valorUnit) || 0);
                var montoImp = parseFloat(d.montoImpuesto || d.igv || 0);
                items.push({
                    id: itemCounter,
                    descripcion: d.descripcion || '',
                    unidadMedida: d.unidadMedida || 'NIU',
                    cantidad: cantidad,
                    valorUnitario: valorUnit,
                    precioUnitario: precioUnit || (valorUnit * (1 + (igvInfo.porcentajeIgv / 100))),
                    tipoAfectacionIgv: afect,
                    codigoTributo: d.codigoTributo || igvInfo.codigoTributo,
                    porcentajeIgv: d.porcentajeIgv != null ? d.porcentajeIgv : igvInfo.porcentajeIgv,
                    tipoPrecio: d.tipoPrecio || '01',
                    baseImponible: baseImp,
                    montoImpuesto: montoImp,
                    igv: montoImp,
                    subtotal: baseImp
                });
            });
            console.log('[wizard] loaded items:', items.length);
            renderItems();
        } else {
            console.warn('[wizard] No detalle found in document', docId);
        }

        /* ── Cuotas ── */
        if (doc.cuotas && doc.cuotas.length > 0) {
            cuotas = [];
            cuotaCounter = 0;
            doc.cuotas.forEach(function(c) {
                cuotaCounter++;
                cuotas.push({
                    id: cuotaCounter,
                    numeroCuota: c.numeroCuota || cuotaCounter,
                    monto: parseFloat(c.monto || 0),
                    fechaVencimiento: c.fechaVencimiento ? String(c.fechaVencimiento).substring(0, 10) : ''
                });
            });
            if (typeof renderCuotas === 'function') renderCuotas();
            /* Show cuotas section */
            var cuotasSec = document.getElementById('cuotas-section');
            if (cuotasSec && doc.formaPago === 'CREDITO') cuotasSec.style.display = 'block';
        }

        /* ── Cargos/Descuentos ── */
        if (doc.cargosDescuentos && doc.cargosDescuentos.length > 0) {
            cargosDescuentos = [];
            acCounter = 0;
            doc.cargosDescuentos.forEach(function(ac) {
                acCounter++;
                cargosDescuentos.push({
                    id: acCounter,
                    chargeIndicator: ac.chargeIndicator,
                    reasonCode: ac.reasonCode || '',
                    reasonDescription: ac.reasonDescription || '',
                    multiplierFactor: parseFloat(ac.multiplierFactor || 0),
                    amount: parseFloat(ac.amount || 0),
                    baseAmount: parseFloat(ac.baseAmount || 0)
                });
            });
            if (typeof renderCargosDescuentos === 'function') renderCargosDescuentos();
        }

        /* ── Nota fields ── */
        if (docType === 'NOTA_CREDITO' || docType === 'NOTA_DEBITO') {
            if (doc.facturaReferenciaId) document.getElementById('nota-ref-id').value = doc.facturaReferenciaId;
            if (doc.motivo) document.getElementById('nota-motivo').value = doc.motivo;
            if (doc.tipoDocumento) document.getElementById('nota-tipo-doc').value = doc.tipoDocumento;
            if (doc.descripcion) document.getElementById('nota-descripcion').value = doc.descripcion;
        }

        /* In edit mode, change "Emitir" button to use emitirBorrador */
        _updateWizardEmisionUI();

    }).catch(function(err) {
        console.error('Error loading draft for edit:', err);
    });
}

/* ═══════ HELPER: Apply client data to wizard Step 3 fields ═══════ */
function _applyClientToWizard(cl) {
    if (!cl) return;
    var enumToCode = { 'DNI':'1', 'RUC':'6', 'CE':'4', 'PASAPORTE':'7' };
    var tipoCode = enumToCode[cl.tipoDocumento] || cl.tipoDocumento || '6';
    var el;
    el = document.getElementById('cl-tipo');    if (el) el.value = tipoCode;
    el = document.getElementById('cl-numdoc');  if (el) el.value = cl.numeroDocumento || '';
    el = document.getElementById('cl-denom');   if (el) el.value = cl.denominacion || '';
    el = document.getElementById('cl-dir');     if (el) el.value = cl.direccion || '';
    el = document.getElementById('cl-email');   if (el) el.value = cl.email || '';
    el = document.getElementById('cl-tel');     if (el) el.value = cl.telefono || '';
}

/* ═══════ MONEDA CHANGE — Tipo de cambio ═══════ */
function onMonedaChange() {
    recalcTotals();
    var moneda = document.getElementById('fac-moneda').value;
    var tcGroup = document.getElementById('fac-tc-group');
    if (tcGroup) {
        tcGroup.style.display = (moneda !== 'PEN') ? 'block' : 'none';
    }
    if (moneda !== 'PEN') {
        fetchTipoCambio();
    }
}

/* ═══════ FETCH TIPO DE CAMBIO ═══════ */
function fetchTipoCambio() {
    var fecha = document.getElementById('fac-fecha').value;
    var moneda = document.getElementById('fac-moneda').value;
    var tcInput = document.getElementById('fac-tipo-cambio');
    var tcFuente = document.getElementById('fac-tc-fuente');
    if (!fecha || moneda === 'PEN') return;

    tcInput.value = 'Consultando...';
    var fuente = ETIA.config.tipoCambioFuente || '';
    ApiClient.getTipoCambio(fecha, moneda, fuente).then(function(data) {
        if (data && data.tasaVenta) {
            tcInput.value = parseFloat(data.tasaVenta).toFixed(4);
            if (tcFuente) tcFuente.textContent = 'Fuente: ' + (data.fuente || 'SBS') + ' — ' + fecha;
        } else if (data && data.data && data.data.tasaVenta) {
            tcInput.value = parseFloat(data.data.tasaVenta).toFixed(4);
            if (tcFuente) tcFuente.textContent = 'Fuente: ' + (data.data.fuente || 'SBS') + ' — ' + fecha;
        } else {
            tcInput.value = '—';
            if (tcFuente) tcFuente.textContent = 'No disponible para esta fecha';
        }
    }).catch(function() {
        tcInput.value = '—';
        if (tcFuente) tcFuente.textContent = 'Error consultando tipo de cambio';
    });
}

/* ═══════ FORMA PAGO CHANGE ═══════ */
function onFormaPagoChange() {
    var forma = document.getElementById('fac-forma-pago').value;
    var fechaVenc = document.getElementById('fac-fecha-venc');
    var fechaEmision = document.getElementById('fac-fecha');
    var cuotasSection = document.getElementById('cuotas-section');
    var nroCuotasGroup = document.getElementById('fac-nro-cuotas-group');
    var nroCuotasInput = document.getElementById('fac-nro-cuotas');
    if (forma === 'CONTADO') {
        if (fechaVenc && fechaEmision) {
            fechaVenc.value = fechaEmision.value;
            fechaVenc.readOnly = true;
        }
        if (cuotasSection) cuotasSection.style.display = 'none';
        if (nroCuotasGroup) nroCuotasGroup.style.display = 'none';
    } else {
        if (fechaVenc) fechaVenc.readOnly = false;
        if (cuotasSection) cuotasSection.style.display = 'block';
        if (nroCuotasGroup) nroCuotasGroup.style.display = '';
        /* Pre-fill nro cuotas from config */
        if (nroCuotasInput && !nroCuotasInput.dataset.userSet) {
            nroCuotasInput.value = ETIA.config.defaultCuotas || '1';
        }
        /* Auto-generate cuotas if none exist */
        if (cuotas.length === 0) {
            _triggerAutoGenerateCuotas();
        }
    }
}

/** Auto-generate cuotas based on config/settings */
function _triggerAutoGenerateCuotas() {
    var nroCuotasInput = document.getElementById('fac-nro-cuotas');
    var defaultCuotas = (nroCuotasInput && parseInt(nroCuotasInput.value)) || parseInt(ETIA.config.defaultCuotas) || 1;
    var modo = ETIA.config.cuotaDistribucion || 'FIJA_30D';
    var fechaEmision = document.getElementById('fac-fecha') ? document.getElementById('fac-fecha').value : '';
    var fechaVenc = document.getElementById('fac-fecha-venc') ? document.getElementById('fac-fecha-venc').value : '';
    var total = getCurrentTotal();
    if (total <= 0) total = 0;
    autoGenerarCuotas(defaultCuotas, fechaEmision, fechaVenc, total, modo);
}

/** Called when the nro cuotas input changes in the wizard */
function onNroCuotasChange() {
    cuotas = [];
    cuotaCounter = 0;
    _triggerAutoGenerateCuotas();
}

/* ═══════ WIZARD NAV — 5 steps ═══════ */
function wizGo(step) {
    if (step === wizStep) return;

    var currentPanel = document.getElementById('wiz-' + wizStep);
    var targetPanel  = document.getElementById('wiz-' + step);
    var allSteps     = document.querySelectorAll('.wiz-step');

    /* Fade out current panel */
    if (currentPanel) {
        currentPanel.style.transition = 'opacity 0.25s cubic-bezier(0.22, 1, 0.36, 1), transform 0.25s cubic-bezier(0.22, 1, 0.36, 1)';
        currentPanel.style.opacity = '0';
        currentPanel.style.transform = 'translateY(-8px) scale(0.998)';
    }

    /* Update step indicators */
    allSteps.forEach(function(s, i) {
        var wn = s.querySelector('.wn');
        s.classList.remove('active');
        var isDone = (i + 1 < step);
        s.classList.toggle('done', isDone);
        if (wn) wn.textContent = isDone ? '✓' : (s.getAttribute('data-icon') || String(i + 1));
    });
    if (allSteps[step - 1]) allSteps[step - 1].classList.add('active');

    /* After fadeout, swap panels */
    setTimeout(function() {
        document.querySelectorAll('#wizard-panels .panel').forEach(function(p) {
            p.classList.remove('active');
            p.style.opacity = '';
            p.style.transform = '';
            p.style.transition = '';
        });

        if (targetPanel) {
            targetPanel.classList.add('active');
            /* Force reflow so the animation restarts cleanly */
            void targetPanel.offsetWidth;
            targetPanel.style.animation = 'none';
            void targetPanel.offsetWidth;
            targetPanel.style.animation = '';
        }

        wizStep = step;
        if (step === 5) populateReview();
    }, 220);
}

/* ═══════ REVIEW — Step 5 ═══════ */
function populateReview() {
    var val = function(id) { var e = document.getElementById(id); return e ? e.value : ''; };

    /* Document */
    var labels = {FACTURA:'Factura Electrónica', BOLETA:'Boleta de Venta', NOTA_CREDITO:'Nota de Crédito', NOTA_DEBITO:'Nota de Débito'};
    document.getElementById('r-tipo').textContent = labels[selectedDocType] || selectedDocType;
    document.getElementById('r-serie').textContent = val('fac-serie');
    document.getElementById('r-fecha').textContent = ETIA.formatDate(val('fac-fecha')) || '—';
    document.getElementById('r-moneda').textContent = val('fac-moneda');

    /* Tipo de cambio */
    var tcRow = document.getElementById('r-tc-row');
    var moneda = val('fac-moneda');
    if (tcRow) {
        if (moneda !== 'PEN') {
            tcRow.style.display = '';
            document.getElementById('r-tipo-cambio').textContent = val('fac-tipo-cambio') || '—';
        } else {
            tcRow.style.display = 'none';
        }
    }

    var rFormaPago = document.getElementById('r-forma-pago');
    if (rFormaPago) rFormaPago.textContent = val('fac-forma-pago') || 'CONTADO';
    document.getElementById('r-fecha-venc').textContent = ETIA.formatDate(val('fac-fecha-venc')) || '—';

    /* Ubigeo */
    var rUbigeo = document.getElementById('r-ubigeo');
    if (rUbigeo) {
        var sede = ETIA.activeSede;
        rUbigeo.textContent = sede ? ((sede.codigoUbigeo || '—') + ' · ' + (sede.distrito || '') + ', ' + (sede.provincia || '') + ', ' + (sede.departamento || '')) : '—';
    }

    /* Emisor */
    document.getElementById('r-em-ruc').textContent = val('em-ruc');
    document.getElementById('r-em-razon').textContent = val('em-razon');
    document.getElementById('r-em-dir').textContent = val('em-dir');

    /* Client */
    var tipoSel = document.getElementById('cl-tipo');
    var tipoText = (tipoSel && tipoSel.selectedIndex >= 0) ? tipoSel.options[tipoSel.selectedIndex].text : 'N/D';
    document.getElementById('r-cl-doc').textContent = tipoText + ' ' + val('cl-numdoc');
    document.getElementById('r-cl-denom').textContent = val('cl-denom');
    document.getElementById('r-cl-dir').textContent = val('cl-dir');

    /* Items count */
    var rCount = document.getElementById('r-items-count');
    if (rCount) rCount.textContent = items.length + ' ítem' + (items.length !== 1 ? 's' : '');

    /* Items table in review */
    var sym = val('fac-moneda') === 'USD' ? 'US$ ' : 'S/ ';
    var tableWrap = document.getElementById('r-items-table-wrap');
    if (tableWrap && items.length > 0) {
        var html = '<table class="tbl" style="font-size:0.78rem;">' +
            '<thead><tr><th>#</th><th>Descripción</th><th>Cant.</th><th>V.Unit.</th><th>IGV</th><th>Subtotal</th></tr></thead><tbody>';
        items.forEach(function(it, idx) {
            html += '<tr>' +
                '<td>' + (idx + 1) + '</td>' +
                '<td>' + it.descripcion + '</td>' +
                '<td style="text-align:center;">' + it.cantidad + '</td>' +
                '<td style="font-family:var(--font-mono);">' + sym + it.valorUnitario.toFixed(2) + '</td>' +
                '<td style="font-family:var(--font-mono);color:var(--warning);">' + sym + it.montoImpuesto.toFixed(2) + '</td>' +
                '<td style="font-family:var(--font-mono);color:var(--accent);font-weight:600;">' + sym + it.subtotal.toFixed(2) + '</td>' +
                '</tr>';
        });
        html += '</tbody></table>';
        tableWrap.innerHTML = html;
    } else if (tableWrap) {
        tableWrap.innerHTML = '<p style="text-align:center;color:var(--text-muted);font-size:0.8rem;">Sin ítems agregados</p>';
    }

    /* Totals */
    var g = items.reduce(function(s, i) { return s + i.subtotal; }, 0);
    var iv = items.reduce(function(s, i) { return s + (i.montoImpuesto || i.igv || 0); }, 0);
    var totalCargos = 0, totalDescuentos = 0;
    cargosDescuentos.forEach(function(ac) {
        if (ac.chargeIndicator) totalCargos += ac.amount;
        else totalDescuentos += ac.amount;
    });
    var t = g + iv + totalCargos - totalDescuentos;
    document.getElementById('r-grav').textContent = sym + g.toFixed(2);
    document.getElementById('r-igv').textContent = sym + iv.toFixed(2);
    document.getElementById('r-total').textContent = sym + t.toFixed(2);

    /* Cargos / Descuentos rows in totals */
    var rCargosRow = document.getElementById('r-cargos-row');
    var rDescRow = document.getElementById('r-desc-row');
    if (rCargosRow) {
        if (totalCargos > 0) {
            rCargosRow.style.display = '';
            document.getElementById('r-cargos').textContent = sym + totalCargos.toFixed(2);
        } else {
            rCargosRow.style.display = 'none';
        }
    }
    if (rDescRow) {
        if (totalDescuentos > 0) {
            rDescRow.style.display = '';
            document.getElementById('r-desc').textContent = '−' + sym + totalDescuentos.toFixed(2);
        } else {
            rDescRow.style.display = 'none';
        }
    }

    /* Cargos / Descuentos detail block (next to Adquiriente) */
    var acBlock = document.getElementById('r-ac-block');
    var acList = document.getElementById('r-ac-list');
    if (acBlock && acList) {
        if (cargosDescuentos.length > 0) {
            acBlock.style.display = '';
            var acHtml = '';
            cargosDescuentos.forEach(function(ac) {
                var tipo = ac.chargeIndicator ? '⬆ Cargo' : '⬇ Descuento';
                var color = ac.chargeIndicator ? 'var(--warning)' : 'var(--danger)';
                var sign = ac.chargeIndicator ? '+' : '−';
                acHtml += '<div class="sr" style="font-size:0.82rem;">' +
                    '<span class="l" style="color:' + color + ';">' + tipo +
                    (ac.reasonCode ? ' <span style="opacity:0.6;font-size:0.7rem;">(' + ac.reasonCode + ')</span>' : '') +
                    '</span>' +
                    '<span class="v" style="color:' + color + ';font-family:var(--font-mono);">' + sign + sym + ac.amount.toFixed(2) + '</span>' +
                    '</div>';
                if (ac.reasonDescription) {
                    acHtml += '<div style="font-size:0.7rem;color:var(--text-muted);margin:-0.2rem 0 0.3rem 0.5rem;">' + ac.reasonDescription + '</div>';
                }
            });
            acList.innerHTML = acHtml;
            var netoEl = document.getElementById('r-ac-neto');
            var neto = totalCargos - totalDescuentos;
            if (netoEl) netoEl.textContent = (neto >= 0 ? '+' : '−') + sym + Math.abs(neto).toFixed(2);
        } else {
            acBlock.style.display = 'none';
        }
    }

    /* ── Cuotas review (REQ-7) ── */
    var cuotasReviewEl = document.getElementById('r-cuotas-info');
    if (cuotasReviewEl) {
        if (cuotas.length > 0 && val('fac-forma-pago') === 'CREDITO') {
            cuotasReviewEl.style.display = 'block';
            var cuotaHtml = '<h3>📅 Cuotas de Crédito</h3><table class="review-table" style="width:100%;font-size:0.82rem;border-collapse:collapse;">' +
                '<thead><tr style="border-bottom:1px solid var(--border);"><th style="text-align:left;padding:4px 8px;">#</th><th style="text-align:right;padding:4px 8px;">Monto</th><th style="text-align:left;padding:4px 8px;">Vencimiento</th></tr></thead><tbody>';
            cuotas.forEach(function(c) {
                cuotaHtml += '<tr><td style="padding:3px 8px;">' + c.numeroCuota + '</td>' +
                    '<td style="text-align:right;padding:3px 8px;font-family:var(--font-mono);">' + sym + c.monto.toFixed(2) + '</td>' +
                    '<td style="padding:3px 8px;">' + (c.fechaVencimiento || '—') + '</td></tr>';
            });
            var cuotasSum = cuotas.reduce(function(s, c) { return s + c.monto; }, 0);
            cuotaHtml += '</tbody><tfoot><tr style="border-top:1px solid var(--border);font-weight:600;">' +
                '<td style="padding:4px 8px;">Total</td>' +
                '<td style="text-align:right;padding:4px 8px;font-family:var(--font-mono);">' + sym + cuotasSum.toFixed(2) + '</td>' +
                '<td></td></tr></tfoot></table>';
            cuotasReviewEl.innerHTML = cuotaHtml;
        } else {
            cuotasReviewEl.style.display = 'none';
        }
    }

    /* Nota review */
    var notaReviewEl = document.getElementById('r-nota-info');
    if (notaReviewEl) {
        if (selectedDocType === 'NOTA_CREDITO' || selectedDocType === 'NOTA_DEBITO') {
            var motivoSel = document.getElementById('nota-motivo');
            var motivoText = motivoSel ? motivoSel.options[motivoSel.selectedIndex].text : '';
            notaReviewEl.style.display = 'block';
            notaReviewEl.innerHTML =
                '<h3>📌 Datos de la Nota</h3>' +
                '<div class="sr"><span class="l">Doc. Referencia:</span><span class="v">' + (val('nota-ref-id') || '—') + '</span></div>' +
                '<div class="sr"><span class="l">Motivo:</span><span class="v">' + (motivoText || '—') + '</span></div>' +
                '<div class="sr"><span class="l">Descripción:</span><span class="v">' + (val('nota-descripcion') || '—') + '</span></div>';
        } else {
            notaReviewEl.style.display = 'none';
        }
    }
}

/* ═══════ REVIEW VIEW TOGGLE (REQ-9) ═══════ */
var _reviewViewMode = 'contable'; /* 'contable' | 'pdf' | 'json' */

function toggleReviewView(mode) {
    _reviewViewMode = mode || 'contable';
    var contableEl = document.getElementById('review-contable');
    var pdfEl = document.getElementById('review-pdf');
    var jsonEl = document.getElementById('review-json');
    var btnContable = document.getElementById('btn-view-contable');
    var btnPdf = document.getElementById('btn-view-pdf');
    var btnJson = document.getElementById('btn-view-json');

    /* Hide all */
    if (contableEl) contableEl.style.display = 'none';
    if (pdfEl) pdfEl.style.display = 'none';
    if (jsonEl) jsonEl.style.display = 'none';
    if (btnContable) btnContable.classList.remove('active');
    if (btnPdf) btnPdf.classList.remove('active');
    if (btnJson) btnJson.classList.remove('active');

    if (_reviewViewMode === 'pdf') {
        if (pdfEl) { pdfEl.style.display = 'block'; _loadPdfPreview(); }
        if (btnPdf) btnPdf.classList.add('active');
    } else if (_reviewViewMode === 'json') {
        if (jsonEl) { jsonEl.style.display = 'block'; populateJsonViewer(); }
        if (btnJson) btnJson.classList.add('active');
    } else {
        if (contableEl) contableEl.style.display = 'block';
        if (btnContable) btnContable.classList.add('active');
    }
}

function _loadPdfPreview() {
    var pdfEl = document.getElementById('review-pdf');
    if (!pdfEl) return;

    /* Show loading state */
    pdfEl.innerHTML =
        '<div style="text-align:center;padding:2rem;color:var(--text-muted);">' +
            '<p style="font-size:1.2rem;">⏳</p>' +
            '<p style="font-size:0.85rem;">Generando vista previa…</p>' +
        '</div>';

    var val = function(id) { var e = document.getElementById(id); return e ? e.value : ''; };

    /* Build doc-like object from wizard form fields */
    var doc = {
        serie: val('fac-serie'),
        numero: null, /* not yet assigned */
        fechaEmision: val('fac-fecha'),
        moneda: val('fac-moneda'),
        formaPago: val('fac-forma-pago'),
        fechaVencimiento: val('fac-fecha-venc'),
        tipoCambio: val('fac-tipo-cambio') || null,
        tipoOperacion: val('fac-tipo-operacion'),
        leyenda: val('fac-leyenda') || '',
        estado: editMode ? (editDocEstado || 'BORRADOR') : 'NUEVO',
        cargosDescuentos: cargosDescuentos || [],
        cuotas: (val('fac-forma-pago') === 'CREDITO') ? (cuotas || []) : []
    };

    /* If editing and we have a real numero, use it */
    var numPreview = document.getElementById('fac-numero-preview');
    if (editMode && numPreview && numPreview.value && numPreview.value !== '(se asigna al emitir)') {
        doc.numero = parseInt(numPreview.value, 10) || null;
    }

    /* Company data */
    var company = ETIA.company || {};

    /* Client data from wizard DOM fields (most reliable source) */
    var tipoSel = document.getElementById('cl-tipo');
    var tipoCode = tipoSel ? tipoSel.value : '6';
    var client = {
        tipoDocumento: tipoCode,
        numeroDocumento: val('cl-numdoc'),
        denominacion: val('cl-denom'),
        direccion: val('cl-dir')
    };

    /* Items */
    var previewItems = (items || []).map(function(it) {
        return {
            descripcion: it.descripcion,
            unidadMedida: it.unidadMedida,
            cantidad: it.cantidad,
            valorUnitario: it.valorUnitario,
            precioUnitario: it.precioUnitario,
            igv: it.montoImpuesto || it.igv || 0,
            montoImpuesto: it.montoImpuesto || it.igv || 0,
            subtotal: it.subtotal || (it.cantidad * it.valorUnitario),
            tipoAfectacionIgv: it.tipoAfectacionIgv || '10'
        };
    });

    /* Totals */
    var grav = 0, exo = 0, ina = 0, totalIgv = 0;
    previewItems.forEach(function(it) {
        if (it.tipoAfectacionIgv === '20') exo += it.subtotal;
        else if (it.tipoAfectacionIgv === '30') ina += it.subtotal;
        else grav += it.subtotal;
        totalIgv += it.montoImpuesto;
    });
    var totalCargos = 0, totalDescuentos = 0;
    (cargosDescuentos || []).forEach(function(ac) {
        if (ac.chargeIndicator) totalCargos += ac.amount;
        else totalDescuentos += ac.amount;
    });
    var total = grav + exo + ina + totalIgv + totalCargos - totalDescuentos;

    var totals = {
        gravadas: grav, exoneradas: exo, inafectas: ina,
        igv: totalIgv, totalCargos: totalCargos,
        totalDescuentos: totalDescuentos, total: total
    };

    /* Doc type labels */
    var typeNames = { FACTURA:'Factura Electrónica', BOLETA:'Boleta de Venta', NOTA_CREDITO:'Nota de Crédito', NOTA_DEBITO:'Nota de Débito' };
    var typeIcons = { FACTURA:'🧾', BOLETA:'🎫', NOTA_CREDITO:'📉', NOTA_DEBITO:'📈' };

    /* Fetch logo (cached) then render */
    ETIA.getLogoBase64().then(function(logoBase64) {
        if (typeof PdfTemplate === 'undefined') {
            pdfEl.innerHTML = '<div style="text-align:center;padding:2rem;color:var(--danger);"><p>Error: PdfTemplate no está disponible.</p></div>';
            return;
        }

        var html = PdfTemplate.render({
            doc: doc,
            company: company,
            client: client,
            items: previewItems,
            totals: totals,
            logoBase64: logoBase64,
            docTypeName: typeNames[selectedDocType] || 'Documento',
            docTypeIcon: typeIcons[selectedDocType] || '📄'
        });

        /* Render into iframe */
        pdfEl.innerHTML =
            '<div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:0.6rem;">' +
                '<span style="font-size:0.78rem;color:var(--text-muted);">Vista previa del comprobante tal como se imprimirá</span>' +
                '<button class="btn btn-ghost" onclick="printPdfPreview()" style="font-size:0.75rem;padding:0.3rem 0.8rem;">🖨️ Imprimir</button>' +
            '</div>' +
            '<iframe id="pdf-preview-frame" style="width:100%;height:600px;border:1px solid var(--border);border-radius:var(--radius-sm);background:#fff;" sandbox="allow-same-origin"></iframe>';

        var iframe = document.getElementById('pdf-preview-frame');
        if (iframe) {
            iframe.srcdoc = html;
        }
    });
}

/** Print the PDF preview iframe content */
function printPdfPreview() {
    var iframe = document.getElementById('pdf-preview-frame');
    if (iframe && iframe.contentWindow) {
        iframe.contentWindow.focus();
        iframe.contentWindow.print();
    }
}

/* ═══════ EMIT DOCUMENT ═══════ */
async function emitDoc() {
    await _submitDoc(true);
}

/* ═══════ SAVE AS DRAFT ═══════ */
async function saveDraft() {
    await _submitDoc(false);
}

/* ═══════ ADAPT WIZARD UI BASED ON EMISIÓN EN ENVÍO ═══════ */
function _updateWizardEmisionUI() {
    var emisionActiva = ETIA.config.emisionEnEnvio !== false;
    var btnEmit = document.getElementById('btn-emit');
    var btnDraft = document.getElementById('btn-draft');
    var emisionHint = document.getElementById('wiz-emision-hint');
    var stepLabel = document.querySelectorAll('.wiz-step')[4]; /* step 5 now */
    var stepLabelText = stepLabel ? stepLabel.querySelector('.wiz-label') : null;

    if (editMode) {
        /* In edit mode: "Emitir" uses emitirBorrador, "Guardar" re-saves draft */
        if (btnEmit) {
            btnEmit.className = 'btn btn-accent';
            btnEmit.innerHTML = '<span class="spinner" id="spinner" style="display:none;"></span> 🚀 Emitir Borrador';
            btnEmit.style.order = '2';
        }
        if (btnDraft) {
            btnDraft.className = 'btn btn-ghost';
            btnDraft.innerHTML = '<span class="spinner" id="spinner-draft" style="display:none;"></span> 💾 Guardar Cambios';
            btnDraft.style.cssText = 'border-color:var(--text-muted);order:1;';
            btnDraft.style.display = '';
        }
        if (emisionHint) emisionHint.style.display = 'none';
        if (stepLabelText) stepLabelText.textContent = 'Emitir';
    } else if (emisionActiva) {
        if (btnEmit) {
            btnEmit.className = 'btn btn-accent';
            btnEmit.innerHTML = '<span class="spinner" id="spinner" style="display:none;"></span> 🚀 Emitir Comprobante';
            btnEmit.style.order = '2';
        }
        if (btnDraft) {
            btnDraft.className = 'btn btn-ghost';
            btnDraft.innerHTML = '<span class="spinner" id="spinner-draft" style="display:none;"></span> 💾 Guardar Borrador';
            btnDraft.style.cssText = 'border-color:var(--text-muted);order:1;';
            btnDraft.style.display = '';
        }
        if (emisionHint) emisionHint.style.display = 'none';
        if (stepLabelText) stepLabelText.textContent = 'Emitir';
    } else {
        if (btnDraft) {
            btnDraft.className = 'btn btn-accent';
            btnDraft.innerHTML = '<span class="spinner" id="spinner-draft" style="display:none;"></span> 💾 Guardar Borrador';
            btnDraft.style.cssText = 'order:2;';
            btnDraft.style.display = '';
        }
        if (btnEmit) {
            btnEmit.className = 'btn btn-ghost';
            btnEmit.innerHTML = '<span class="spinner" id="spinner" style="display:none;"></span> 🚀 Emitir Comprobante';
            btnEmit.style.cssText = 'border-color:var(--accent);color:var(--accent);order:1;';
        }
        if (emisionHint) emisionHint.style.display = 'block';
        if (stepLabelText) stepLabelText.textContent = 'Guardar';
    }
}

/* ═══════ INTERNAL SUBMIT — emitir=true (emit) or emitir=false (draft) ═══════ */
async function _submitDoc(emitir) {
    var spinnerId = emitir ? 'spinner' : 'spinner-draft';
    var btnId = emitir ? 'btn-emit' : 'btn-draft';
    var spinner = document.getElementById(spinnerId);
    var btn = document.getElementById(btnId);
    if (spinner) spinner.style.display = 'inline-block';
    if (btn) btn.disabled = true;
    var otherBtn = document.getElementById(emitir ? 'btn-draft' : 'btn-emit');
    if (otherBtn) otherBtn.disabled = true;
    var val = function(id) { var e = document.getElementById(id); return e ? e.value : ''; };


    var grav = 0, exo = 0, ina = 0, totalIgv = 0;
    items.forEach(function(i) {
        if (i.tipoAfectacionIgv === '20') exo += i.subtotal;
        else if (i.tipoAfectacionIgv === '30') ina += i.subtotal;
        else grav += i.subtotal;
        totalIgv += i.montoImpuesto;
    });
    var totalCargos = 0, totalDescuentos = 0;
    cargosDescuentos.forEach(function(ac) {
        if (ac.chargeIndicator) totalCargos += ac.amount;
        else totalDescuentos += ac.amount;
    });
    var t = grav + exo + ina + totalIgv + totalCargos - totalDescuentos;

    var detalleArr = items.map(function(i) {
        return {
            descripcion: i.descripcion, unidadMedida: i.unidadMedida,
            cantidad: i.cantidad,
            valorUnitario: parseFloat(i.valorUnitario.toFixed(2)),
            precioUnitario: parseFloat(i.precioUnitario.toFixed(2)),
            igv: parseFloat(i.igv.toFixed(2)),
            subtotal: parseFloat(i.subtotal.toFixed(2)),
            tipoAfectacionIgv: i.tipoAfectacionIgv || '10',
            codigoTributo: i.codigoTributo || '1000',
            tipoPrecio: i.tipoPrecio || '01',
            porcentajeIgv: i.porcentajeIgv != null ? i.porcentajeIgv : 18.00,
            baseImponible: parseFloat((i.baseImponible || i.subtotal).toFixed(2)),
            montoImpuesto: parseFloat((i.montoImpuesto || i.igv).toFixed(2))
        };
    });

    var payload;
    var emitFn;

    if (selectedDocType === 'NOTA_CREDITO') {
        payload = {
            companyId: ETIA.companyId,
            facturaReferenciaId: val('nota-ref-id') || null,
            motivo: val('nota-motivo') || 'otro',
            montoDescuento: parseFloat(t.toFixed(2)),
            tipoDocumento: val('nota-tipo-doc') || 'factura',
            descripcion: val('nota-descripcion') || '',
            detalle: detalleArr
        };
        emitFn = ApiClient.emitirNotaCredito;
    } else if (selectedDocType === 'NOTA_DEBITO') {
        payload = {
            companyId: ETIA.companyId,
            facturaReferenciaId: val('nota-ref-id') || null,
            motivo: val('nota-motivo') || 'otros',
            montoIncremento: parseFloat(t.toFixed(2)),
            descripcion: val('nota-descripcion') || '',
            detalle: detalleArr
        };
        emitFn = ApiClient.emitirNotaDebito;
    } else {
        var fechaEmision = val('fac-fecha') || new Date().toISOString().split('T')[0];
        var formaPago = val('fac-forma-pago') || 'CONTADO';
        var fechaVenc = val('fac-fecha-venc') || fechaEmision;
        if (formaPago === 'CONTADO') fechaVenc = fechaEmision;

        var tipoCambioVal = null;
        if (val('fac-moneda') !== 'PEN') {
            var tcRaw = val('fac-tipo-cambio');
            if (tcRaw && tcRaw !== '—' && tcRaw !== 'Consultando...') tipoCambioVal = parseFloat(tcRaw);
        }

        var ubigeo = (ETIA.activeSede && ETIA.activeSede.codigoUbigeo) ? ETIA.activeSede.codigoUbigeo : null;

        payload = {
            companyId: ETIA.companyId,
            clientId: ETIA.clientId,
            serie: val('fac-serie'),
            fechaEmision: fechaEmision,
            moneda: val('fac-moneda'),
            formaPago: formaPago,
            fechaVencimiento: fechaVenc,
            tipoCambio: tipoCambioVal,
            codigoUbigeo: ubigeo,
            tipoOperacion: val('fac-tipo-operacion') || '0101',
            gravadas: parseFloat(grav.toFixed(2)),
            exoneradas: parseFloat(exo.toFixed(2)),
            inafectas: parseFloat(ina.toFixed(2)),
            igv: parseFloat(totalIgv.toFixed(2)),
            total: parseFloat(t.toFixed(2)),
            subtotal: parseFloat((grav + exo + ina).toFixed(2)),
            totalConImpuestos: parseFloat((grav + exo + ina + totalIgv).toFixed(2)),
            leyenda: val('fac-leyenda') || 'MONTO EN LETRAS',
            detalle: detalleArr,
            cuotas: (formaPago === 'CREDITO' && cuotas.length > 0)
                ? cuotas.map(function(c) {
                    return {
                        numeroCuota: c.numeroCuota,
                        monto: c.monto,
                        fechaVencimiento: c.fechaVencimiento
                    };
                }) : [],
            cargosDescuentos: cargosDescuentos.length > 0
                ? cargosDescuentos.map(function(ac) {
                    return {
                        chargeIndicator: ac.chargeIndicator,
                        reasonCode: ac.reasonCode,
                        reasonDescription: ac.reasonDescription,
                        multiplierFactor: ac.multiplierFactor,
                        amount: ac.amount,
                        baseAmount: ac.baseAmount
                    };
                }) : [],
            emitir: emitir
        };
        emitFn = (selectedDocType === 'BOLETA') ? ApiClient.emitirBoleta : ApiClient.emitirFactura;
    }

    /* ── Edit mode (FACTURA): actualizar borrador y opcionalmente emitir ── */
    if (editMode && editDocId && selectedDocType === 'FACTURA') {
        try {
            /* Siempre guardar cambios primero */
            var saveResult = await ApiClient.actualizarFacturaBorrador(editDocId, payload);
            if (emitir) {
                /* Después de guardar, emitir */
                var emitResult = await ApiClient.emitirFacturaBorrador(editDocId);
                _showResultModal(true, emitir, emitResult);
            } else {
                _showResultModal(true, emitir, saveResult);
            }
        } catch (err) {
            _showResultModal(false, emitir, err);
        } finally {
            _resetButtons(spinner, btn, otherBtn);
        }
        return;
    }

    try {
        var data = await emitFn(payload);
        _showResultModal(true, emitir, data);
    } catch (err) {
        _showResultModal(false, emitir, err);
    } finally {
        _resetButtons(spinner, btn, otherBtn);
    }
}

/* ═══════ RESULT MODAL HELPER ═══════ */
function _showResultModal(isOk, emitir, dataOrErr) {
    var card = document.getElementById('modal-card');
    var title = document.getElementById('modal-title');
    if (isOk && dataOrErr && dataOrErr.isSuccess !== false) {
        card.className = 'modal-card success';
        if (editMode && emitir) {
            title.innerHTML = '✅ <span style="color:var(--accent);">Borrador emitido exitosamente</span>';
        } else if (emitir) {
            title.innerHTML = '✅ <span style="color:var(--accent);">Comprobante emitido exitosamente</span>';
        } else {
            title.innerHTML = '💾 <span style="color:var(--accent);">Borrador guardado exitosamente</span>';
        }
        if (ETIA.refreshNotifications) ETIA.refreshNotifications();
    } else if (isOk && dataOrErr && dataOrErr.isSuccess === false) {
        card.className = 'modal-card error';
        title.innerHTML = '❌ <span style="color:var(--danger);">Error al ' + (emitir ? 'emitir' : 'guardar') + '</span>';
    } else {
        card.className = 'modal-card error';
        title.innerHTML = '❌ <span style="color:var(--danger);">Error de conexión</span>';
    }
    document.getElementById('modal-body').textContent = isOk ? JSON.stringify(dataOrErr, null, 2) : String(dataOrErr);
    document.getElementById('modal').classList.add('show');
}

function _resetButtons(spinner, btn, otherBtn) {
    if (spinner) spinner.style.display = 'none';
    if (btn) btn.disabled = false;
    if (otherBtn) otherBtn.disabled = false;
}

function resetAll() {
    document.getElementById('modal').classList.remove('show');
    items = []; itemCounter = 0; renderItems();
    cuotas = []; cuotaCounter = 0;
    cargosDescuentos = []; acCounter = 0;
    clearSelectedProduct();
    var sedeSel = document.getElementById('hdr-sede-select');
    if (sedeSel) sedeSel.disabled = false;
    window.location.href = '/documents';
}
