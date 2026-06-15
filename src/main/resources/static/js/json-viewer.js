/* ═══════════════════════════════════════════════
   🔍  ETIA — JSON Viewer (collapsible, colored)
   Read-only tree with copy-to-clipboard button
   ═══════════════════════════════════════════════ */

/**
 * Renders a JSON value as a collapsible, syntax-highlighted HTML tree.
 * @param {*} data - The JS object/array/value to render
 * @param {string} containerId - DOM id where the tree will be injected
 */
function renderJsonViewer(data, containerId) {
    var container = document.getElementById(containerId);
    if (!container) return;
    container.innerHTML = '';
    var root = _jvBuildNode(data, '', 0, true);
    container.appendChild(root);
}

function _jvBuildNode(val, key, depth, isLast) {
    var wrap = document.createElement('div');
    wrap.className = 'jv-line';
    wrap.style.paddingLeft = (depth * 18) + 'px';

    if (val === null) {
        wrap.innerHTML = _jvKey(key) + '<span class="jv-null">null</span>' + _jvComma(isLast);
        return wrap;
    }

    var type = Array.isArray(val) ? 'array' : typeof val;

    if (type === 'object' || type === 'array') {
        var isArr = type === 'array';
        var entries = isArr ? val : Object.keys(val);
        var len = isArr ? val.length : entries.length;
        var openBracket = isArr ? '[' : '{';
        var closeBracket = isArr ? ']' : '}';

        if (len === 0) {
            wrap.innerHTML = _jvKey(key) + '<span class="jv-bracket">' + openBracket + closeBracket + '</span>' + _jvComma(isLast);
            return wrap;
        }

        /* Header line with toggle */
        var toggle = document.createElement('span');
        toggle.className = 'jv-toggle';
        toggle.textContent = '▼';
        toggle.onclick = function() {
            var collapsed = childWrap.style.display === 'none';
            childWrap.style.display = collapsed ? '' : 'none';
            closeLine.style.display = collapsed ? '' : 'none';
            toggle.textContent = collapsed ? '▼' : '▶';
            if (!collapsed) {
                /* Show inline preview */
                preview.style.display = 'inline';
            } else {
                preview.style.display = 'none';
            }
        };

        var headerHtml = _jvKey(key) + '<span class="jv-bracket">' + openBracket + '</span>';
        var headerSpan = document.createElement('span');
        headerSpan.innerHTML = headerHtml;

        var preview = document.createElement('span');
        preview.className = 'jv-preview';
        preview.style.display = 'none';
        preview.textContent = isArr
            ? ' …' + len + ' item' + (len !== 1 ? 's' : '') + ' ' + closeBracket
            : ' …' + len + ' prop' + (len !== 1 ? 's' : '') + ' ' + closeBracket;

        wrap.appendChild(toggle);
        wrap.appendChild(headerSpan);
        wrap.appendChild(preview);

        /* Children container */
        var childWrap = document.createElement('div');
        childWrap.className = 'jv-children';

        if (isArr) {
            for (var i = 0; i < val.length; i++) {
                childWrap.appendChild(_jvBuildNode(val[i], '', depth + 1, i === val.length - 1));
            }
        } else {
            for (var j = 0; j < entries.length; j++) {
                var k = entries[j];
                childWrap.appendChild(_jvBuildNode(val[k], k, depth + 1, j === entries.length - 1));
            }
        }

        /* Close bracket line */
        var closeLine = document.createElement('div');
        closeLine.className = 'jv-line';
        closeLine.style.paddingLeft = (depth * 18) + 'px';
        closeLine.innerHTML = '<span class="jv-bracket">' + closeBracket + '</span>' + _jvComma(isLast);

        var frag = document.createDocumentFragment();
        frag.appendChild(wrap);
        frag.appendChild(childWrap);
        frag.appendChild(closeLine);

        /* Wrap in a single container div */
        var block = document.createElement('div');
        block.className = 'jv-block';
        block.appendChild(wrap);
        block.appendChild(childWrap);
        block.appendChild(closeLine);
        return block;

    } else if (type === 'string') {
        wrap.innerHTML = _jvKey(key) + '<span class="jv-string">"' + _jvEscape(val) + '"</span>' + _jvComma(isLast);
    } else if (type === 'number') {
        wrap.innerHTML = _jvKey(key) + '<span class="jv-number">' + val + '</span>' + _jvComma(isLast);
    } else if (type === 'boolean') {
        wrap.innerHTML = _jvKey(key) + '<span class="jv-bool">' + val + '</span>' + _jvComma(isLast);
    } else {
        wrap.innerHTML = _jvKey(key) + '<span class="jv-null">' + String(val) + '</span>' + _jvComma(isLast);
    }

    return wrap;
}

function _jvKey(key) {
    if (!key && key !== 0) return '';
    return '<span class="jv-key">"' + _jvEscape(String(key)) + '"</span><span class="jv-colon">: </span>';
}

function _jvComma(isLast) {
    return isLast ? '' : '<span class="jv-comma">,</span>';
}

function _jvEscape(s) {
    return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

/* ═══════ COPY TO CLIPBOARD ═══════ */
var _jvPayloadCache = null;

function copyJsonToClipboard() {
    if (!_jvPayloadCache) return;
    var text = JSON.stringify(_jvPayloadCache, null, 2);
    if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).then(function() {
            _jvShowCopyFeedback(true);
        }).catch(function() {
            _jvFallbackCopy(text);
        });
    } else {
        _jvFallbackCopy(text);
    }
}

function _jvFallbackCopy(text) {
    var ta = document.createElement('textarea');
    ta.value = text;
    ta.style.position = 'fixed';
    ta.style.opacity = '0';
    document.body.appendChild(ta);
    ta.select();
    try { document.execCommand('copy'); _jvShowCopyFeedback(true); }
    catch (e) { _jvShowCopyFeedback(false); }
    document.body.removeChild(ta);
}

function _jvShowCopyFeedback(ok) {
    var btn = document.getElementById('json-copy-btn');
    if (!btn) return;
    var orig = btn.textContent;
    btn.textContent = ok ? '✓ Copiado' : '✗ Error';
    btn.classList.add(ok ? 'copied' : '');
    setTimeout(function() {
        btn.textContent = orig;
        btn.classList.remove('copied');
    }, 1500);
}

/**
 * Builds the current wizard payload and renders it in the JSON viewer.
 * Called from populateReview() when entering Step 5.
 */
function populateJsonViewer() {
    var payload = buildWizardPayload();
    _jvPayloadCache = payload;
    renderJsonViewer(payload, 'json-tree');
}

/**
 * Builds the current wizard payload object (mirrors _submitDoc logic).
 * @returns {Object} The payload that would be sent to the API
 */
function buildWizardPayload() {
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

    var detalleArr = items.map(function(i, idx) {
        return {
            numero: idx + 1,
            descripcion: i.descripcion,
            unidadMedida: i.unidadMedida,
            cantidad: i.cantidad,
            valorUnitario: parseFloat(i.valorUnitario.toFixed(2)),
            precioUnitario: parseFloat(i.precioUnitario.toFixed(2)),
            tipoAfectacionIgv: i.tipoAfectacionIgv || '10',
            codigoTributo: i.codigoTributo || '1000',
            porcentajeIgv: i.porcentajeIgv != null ? i.porcentajeIgv : 18.00,
            baseImponible: parseFloat((i.baseImponible || i.subtotal).toFixed(2)),
            montoImpuesto: parseFloat((i.montoImpuesto || i.igv).toFixed(2)),
            subtotal: parseFloat(i.subtotal.toFixed(2))
        };
    });

    if (selectedDocType === 'NOTA_CREDITO') {
        return {
            _tipo: 'NOTA_CREDITO',
            companyId: ETIA.companyId,
            facturaReferenciaId: val('nota-ref-id') || null,
            motivo: val('nota-motivo') || 'otro',
            montoDescuento: parseFloat(t.toFixed(2)),
            tipoDocumento: val('nota-tipo-doc') || 'factura',
            descripcion: val('nota-descripcion') || '',
            detalle: detalleArr
        };
    }

    if (selectedDocType === 'NOTA_DEBITO') {
        return {
            _tipo: 'NOTA_DEBITO',
            companyId: ETIA.companyId,
            facturaReferenciaId: val('nota-ref-id') || null,
            motivo: val('nota-motivo') || 'otros',
            montoIncremento: parseFloat(t.toFixed(2)),
            descripcion: val('nota-descripcion') || '',
            detalle: detalleArr
        };
    }

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

    var payload = {
        _tipo: selectedDocType,
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
        totalCargos: totalCargos > 0 ? parseFloat(totalCargos.toFixed(2)) : undefined,
        totalDescuentos: totalDescuentos > 0 ? parseFloat(totalDescuentos.toFixed(2)) : undefined,
        total: parseFloat(t.toFixed(2)),
        subtotal: parseFloat((grav + exo + ina).toFixed(2)),
        totalConImpuestos: parseFloat((grav + exo + ina + totalIgv).toFixed(2)),
        leyenda: val('fac-leyenda') || 'MONTO EN LETRAS',
        detalle: detalleArr
    };

    if (formaPago === 'CREDITO' && cuotas.length > 0) {
        payload.cuotas = cuotas.map(function(c) {
            return { numeroCuota: c.numeroCuota, monto: c.monto, fechaVencimiento: c.fechaVencimiento };
        });
    }

    if (cargosDescuentos.length > 0) {
        payload.cargosDescuentos = cargosDescuentos.map(function(ac) {
            return {
                chargeIndicator: ac.chargeIndicator,
                reasonCode: ac.reasonCode,
                reasonDescription: ac.reasonDescription,
                multiplierFactor: ac.multiplierFactor,
                amount: ac.amount,
                baseAmount: ac.baseAmount
            };
        });
    }

    /* Remove undefined keys for clean JSON */
    Object.keys(payload).forEach(function(k) {
        if (payload[k] === undefined) delete payload[k];
    });

    return payload;
}

