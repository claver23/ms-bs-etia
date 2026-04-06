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

    /* Auto-set client document type based on doc type (SUNAT rules) */
    var clTipo = document.getElementById('cl-tipo');
    if (clTipo && !ETIA.clientId) {
        /* No client selected yet — apply default based on document type */
        if (docType === 'FACTURA' || docType === 'NOTA_CREDITO' || docType === 'NOTA_DEBITO') {
            clTipo.value = '6'; /* RUC — empresas */
        } else if (docType === 'BOLETA') {
            clTipo.value = '1'; /* DNI — personas naturales */
        }
    }

    /* Reset items on wizard init */
    items = [];
    itemCounter = 0;
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

/* ═══════ WIZARD NAV ═══════ */
function wizGo(step) {
    document.querySelectorAll('#wizard-panels .panel').forEach(function(p) { p.classList.remove('active'); });
    document.querySelectorAll('.wiz-step').forEach(function(s, i) {
        s.classList.remove('active');
        s.classList.toggle('done', i + 1 < step);
    });
    var target = document.getElementById('wiz-' + step);
    if (target) target.classList.add('active');
    var steps = document.querySelectorAll('.wiz-step');
    if (steps[step - 1]) steps[step - 1].classList.add('active');
    wizStep = step;
    if (step === 4) populateReview();
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
    document.getElementById('r-moneda').textContent = val('fac-moneda');
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
    var g = items.reduce(function(s, i) { return s + i.subtotal; }, 0);
    var iv = items.reduce(function(s, i) { return s + i.igv; }, 0);
    var t = g + iv;

    var detalleArr = items.map(function(i) {
        return {
            descripcion: i.descripcion, unidadMedida: i.unidadMedida,
            cantidad: i.cantidad,
            valorUnitario: parseFloat(i.valorUnitario.toFixed(2)),
            precioUnitario: parseFloat(i.precioUnitario.toFixed(2)),
            igv: parseFloat(i.igv.toFixed(2)),
            subtotal: parseFloat(i.subtotal.toFixed(2))
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
        payload = {
            companyId: ETIA.companyId,
            clientId: ETIA.clientId,
            serie: val('fac-serie'),
            fechaEmision: new Date().toISOString().split('T')[0],
            moneda: val('fac-moneda'),
            gravadas: parseFloat(g.toFixed(2)),
            igv: parseFloat(iv.toFixed(2)),
            total: parseFloat(t.toFixed(2)),
            leyenda: val('fac-leyenda') || 'MONTO EN LETRAS',
            detalle: detalleArr
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
    clearSelectedProduct();
    window.location.href = '/documents';
}
