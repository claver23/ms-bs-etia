/* ═══════════════════════════════════════════════
   🧙  ETIA — Wizard Logic (Factura Emission)
   ═══════════════════════════════════════════════ */

let selectedDocType = 'FACTURA';
let wizStep = 1;

/* ═══════ INIT WIZARD ═══════ */
function initWizard(docType) {
    selectedDocType = docType || 'FACTURA';
    var labels = {FACTURA:'Factura Electrónica', BOLETA:'Boleta de Venta', NOTA_CREDITO:'Nota de Crédito', NOTA_DEBITO:'Nota de Débito'};
    var el = document.getElementById('wiz-detail-title');
    if (el) el.textContent = 'Detalle — ' + (labels[docType] || docType);

    /* Lock sede selector during document creation */
    var sedeSel = document.getElementById('hdr-sede-select');
    if (sedeSel) sedeSel.disabled = true;

    /* Apply default series from config */
    var serieMap = { FACTURA: 'serieFac', BOLETA: 'serieBol', NOTA_CREDITO: 'serieNC', NOTA_DEBITO: 'serieND' };
    var serieInput = document.getElementById('fac-serie');
    if (serieInput && ETIA.config[serieMap[docType]]) {
        serieInput.value = ETIA.config[serieMap[docType]];
    }

    /* Apply default currency from config */
    var monedaSelect = document.getElementById('fac-moneda');
    if (monedaSelect && ETIA.config.currency) {
        monedaSelect.value = ETIA.config.currency;
    }

    /* Set fecha emisión to today (date only, no time) */
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
    var clTipo = document.getElementById('cl-tipo');
    if (clTipo) {
        /* Determine allowed document types per SUNAT rules */
        var allowedTypes;
        if (docType === 'FACTURA' || docType === 'NOTA_CREDITO' || docType === 'NOTA_DEBITO') {
            allowedTypes = ['6']; /* RUC only */
        } else if (docType === 'BOLETA') {
            allowedTypes = ['1', '4', '7']; /* DNI, C.E., Pasaporte */
        } else {
            allowedTypes = ['6', '1', '4', '7', '0']; /* All */
        }

        /* Store globally so client-modal.js can use it */
        ETIA.allowedClientDocTypes = allowedTypes;

        /* Show/hide options in cl-tipo select */
        var clOptions = clTipo.querySelectorAll('option');
        clOptions.forEach(function(opt) {
            opt.style.display = (allowedTypes.indexOf(opt.value) !== -1) ? '' : 'none';
        });

        /* Set default value */
        if (!ETIA.clientId) {
            clTipo.value = allowedTypes[0];
        }

        /* Disable the select — user must use client modal to change it */
        clTipo.disabled = true;
    }

    /* Reset items on wizard init */
    items = [];
    itemCounter = 0;
    cuotas = [];
    cuotaCounter = 0;
    cargosDescuentos = [];
    acCounter = 0;
    renderItems();

    /* Show/hide nota-specific fields */
    var notaFields = document.getElementById('nota-fields');
    var notaTipoDocGroup = document.getElementById('nota-tipo-doc-group');
    if (notaFields) {
        var isNota = (docType === 'NOTA_CREDITO' || docType === 'NOTA_DEBITO');
        notaFields.style.display = isNota ? 'block' : 'none';

        /* Filter motivo options by docType */
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

        /* Hide tipoDocumento selector for nota débito (only applies to NC) */
        if (notaTipoDocGroup) {
            notaTipoDocGroup.style.display = (docType === 'NOTA_CREDITO') ? 'block' : 'none';
        }
    }
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
    if (forma === 'CONTADO') {
        if (fechaVenc && fechaEmision) {
            fechaVenc.value = fechaEmision.value;
            fechaVenc.readOnly = true;
        }
        if (cuotasSection) cuotasSection.style.display = 'none';
    } else {
        if (fechaVenc) fechaVenc.readOnly = false;
        if (cuotasSection) cuotasSection.style.display = 'block';
        // Auto-generate one default cuota if none exist
        if (cuotas.length === 0) {
            addCuota();
        }
    }
}

/* ═══════ WIZARD NAV — Smooth orchestrated transitions ═══════ */
function wizGo(step) {
    /* Skip if already on this step */
    if (step === wizStep) return;

    var currentPanel = document.getElementById('wiz-' + wizStep);
    var targetPanel  = document.getElementById('wiz-' + step);
    var allSteps     = document.querySelectorAll('.wiz-step');

    /* 1. Fade out current panel gracefully */
    if (currentPanel) {
        currentPanel.style.transition = 'opacity 0.25s cubic-bezier(0.22, 1, 0.36, 1), transform 0.25s cubic-bezier(0.22, 1, 0.36, 1)';
        currentPanel.style.opacity = '0';
        currentPanel.style.transform = 'translateY(-8px) scale(0.998)';
    }

    /* 2. Update step indicators with staggered timing */
    allSteps.forEach(function(s, i) {
        var wn = s.querySelector('.wn');
        s.classList.remove('active');
        var isDone = (i + 1 < step);
        s.classList.toggle('done', isDone);
        if (wn) wn.textContent = isDone ? '✓' : (s.getAttribute('data-icon') || String(i + 1));
    });
    if (allSteps[step - 1]) allSteps[step - 1].classList.add('active');

    /* 3. After fadeout completes, swap panels and fade in */
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
        if (step === 4) populateReview();
    }, 220);
}

/* ═══════ REVIEW ═══════ */
function populateReview() {
    var val = function(id) { var e = document.getElementById(id); return e ? e.value : ''; };
    document.getElementById('r-em-ruc').textContent = val('em-ruc');
    document.getElementById('r-em-razon').textContent = val('em-razon');
    document.getElementById('r-em-dir').textContent = val('em-dir');
    var tipoSel = document.getElementById('cl-tipo');
    var tipoText = (tipoSel && tipoSel.selectedIndex >= 0) ? tipoSel.options[tipoSel.selectedIndex].text : 'N/D';
    document.getElementById('r-cl-doc').textContent = tipoText + ' ' + val('cl-numdoc');
    document.getElementById('r-cl-denom').textContent = val('cl-denom');
    document.getElementById('r-cl-dir').textContent = val('cl-dir');
    var labels = {FACTURA:'Factura Electrónica', BOLETA:'Boleta de Venta', NOTA_CREDITO:'Nota de Crédito', NOTA_DEBITO:'Nota de Débito'};
    document.getElementById('r-tipo').textContent = labels[selectedDocType] || selectedDocType;
    document.getElementById('r-serie').textContent = val('fac-serie');
    document.getElementById('r-fecha').textContent = val('fac-fecha') || '—';
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
    document.getElementById('r-fecha-venc').textContent = val('fac-fecha-venc') || '—';

    /* Ubigeo from active sede */
    var rUbigeo = document.getElementById('r-ubigeo');
    if (rUbigeo) {
        var sede = ETIA.activeSede;
        rUbigeo.textContent = sede ? ((sede.codigoUbigeo || '—') + ' · ' + (sede.distrito || '') + ', ' + (sede.provincia || '') + ', ' + (sede.departamento || '')) : '—';
    }

    document.getElementById('r-items').textContent = items.length + ' ítem(s)';
    var g = items.reduce(function(s, i) { return s + i.subtotal; }, 0);
    var iv = items.reduce(function(s, i) { return s + i.igv; }, 0);
    var t = g + iv;
    var sym = val('fac-moneda') === 'USD' ? 'US$ ' : 'S/ ';
    document.getElementById('r-grav').textContent = sym + g.toFixed(2);
    document.getElementById('r-igv').textContent = sym + iv.toFixed(2);
    document.getElementById('r-total').textContent = sym + t.toFixed(2);

    /* Show nota-specific info in review */
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

/* ═══════ EMIT DOCUMENT ═══════ */
async function emitDoc() {
    var spinner = document.getElementById('spinner');
    var btn = document.getElementById('btn-emit');
    spinner.style.display = 'inline-block';
    btn.disabled = true;
    var val = function(id) { var e = document.getElementById(id); return e ? e.value : ''; };
    var grav = 0, exo = 0, ina = 0, totalIgv = 0;
    items.forEach(function(i) {
        if (i.tipoAfectacionIgv === '20') exo += i.subtotal;
        else if (i.tipoAfectacionIgv === '30') ina += i.subtotal;
        else grav += i.subtotal;
        totalIgv += i.montoImpuesto;
    });
    // Include cargos/descuentos in total
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
        /* If CONTADO, due date = emission date (date only) */
        if (formaPago === 'CONTADO') fechaVenc = fechaEmision;

        var tipoCambioVal = null;
        if (val('fac-moneda') !== 'PEN') {
            var tcRaw = val('fac-tipo-cambio');
            if (tcRaw && tcRaw !== '—' && tcRaw !== 'Consultando...') tipoCambioVal = parseFloat(tcRaw);
        }

        /* Ubigeo from active sede */
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
                }) : []
        };
        emitFn = (selectedDocType === 'BOLETA') ? ApiClient.emitirBoleta : ApiClient.emitirFactura;
    }

    try {
        var data = await emitFn(payload);
        var card = document.getElementById('modal-card');
        var title = document.getElementById('modal-title');
        if (data.isSuccess) {
            card.className = 'modal-card success';
            title.innerHTML = '✅ <span style="color:var(--accent);">Comprobante emitido exitosamente</span>';
            if (ETIA.refreshNotifications) ETIA.refreshNotifications();
        } else {
            card.className = 'modal-card error';
            title.innerHTML = '❌ <span style="color:var(--danger);">Error al emitir</span>';
        }
        document.getElementById('modal-body').textContent = JSON.stringify(data, null, 2);
        document.getElementById('modal').classList.add('show');
    } catch (err) {
        document.getElementById('modal-title').innerHTML = '❌ <span style="color:var(--danger);">Error de conexión</span>';
        document.getElementById('modal-body').textContent = err.toString();
        document.getElementById('modal-card').className = 'modal-card error';
        document.getElementById('modal').classList.add('show');
    } finally {
        spinner.style.display = 'none';
        btn.disabled = false;
    }
}

function resetAll() {
    document.getElementById('modal').classList.remove('show');
    items = []; itemCounter = 0; renderItems();
    cuotas = []; cuotaCounter = 0;
    cargosDescuentos = []; acCounter = 0;
    clearSelectedProduct();
    /* Unlock sede selector */
    var sedeSel = document.getElementById('hdr-sede-select');
    if (sedeSel) sedeSel.disabled = false;
    window.location.href = '/documents';
}
