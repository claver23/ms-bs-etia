/* ═══════ ETIA — Items Table (UBL 2.1) ═══════ */
let items = [];
let itemCounter = 0;

/* Afectación IGV → auto-set tributo/porcentaje */
var IGV_MAP = {
    '10': { codigoTributo: '1000', porcentajeIgv: 18.00, label: 'Gravado' },
    '20': { codigoTributo: '9997', porcentajeIgv: 0,     label: 'Exonerado' },
    '30': { codigoTributo: '9998', porcentajeIgv: 0,     label: 'Inafecto' },
    '40': { codigoTributo: '9996', porcentajeIgv: 0,     label: 'Gratuita' }
};

function renderItems() {
    var tbody = document.getElementById('items-body');
    if (!tbody) return;
    tbody.innerHTML = '';
    items.forEach(function(item, idx) {
        var igvInfo = IGV_MAP[item.tipoAfectacionIgv] || IGV_MAP['10'];
        var tr = document.createElement('tr');
        tr.innerHTML =
            '<td>'+(idx+1)+'</td>'+
            '<td style="min-width:160px;">'+item.descripcion+'</td>'+
            '<td><span class="badge badge-blue">'+item.unidadMedida+'</span></td>'+
            '<td><input type="number" value="'+item.cantidad+'" min="1" style="width:55px;" onchange="updItem('+item.id+',\'cantidad\',this.value)"/></td>'+
            '<td><input type="number" step="0.01" value="'+item.valorUnitario.toFixed(2)+'" style="width:85px;" onchange="updItem('+item.id+',\'valorUnitario\',this.value)"/></td>'+
            '<td style="font-family:var(--font-mono);color:var(--text-secondary);">'+item.precioUnitario.toFixed(2)+'</td>'+
            '<td>'+
                '<select style="font-size:0.7rem;padding:2px 4px;width:90px;" onchange="updItemAfect('+item.id+',this.value)">'+
                    '<option value="10"'+(item.tipoAfectacionIgv==='10'?' selected':'')+'>Gravado</option>'+
                    '<option value="20"'+(item.tipoAfectacionIgv==='20'?' selected':'')+'>Exonerado</option>'+
                    '<option value="30"'+(item.tipoAfectacionIgv==='30'?' selected':'')+'>Inafecto</option>'+
                '</select>'+
            '</td>'+
            '<td style="font-family:var(--font-mono);color:var(--warning);">'+item.montoImpuesto.toFixed(2)+'</td>'+
            '<td style="font-family:var(--font-mono);color:var(--accent);font-weight:600;">'+item.subtotal.toFixed(2)+'</td>'+
            '<td><button class="btn btn-danger-sm" onclick="rmItem('+item.id+')">✕</button></td>';
        tbody.appendChild(tr);
    });
    recalcTotals();
}

function updItemAfect(id, afectacion) {
    var item = items.find(function(i){ return i.id === id; });
    if (!item) return;
    var info = IGV_MAP[afectacion] || IGV_MAP['10'];
    item.tipoAfectacionIgv = afectacion;
    item.codigoTributo = info.codigoTributo;
    item.porcentajeIgv = info.porcentajeIgv;
    recalcItem(item);
    renderItems();
}

function updItem(id, field, val) {
    var item = items.find(function(i){ return i.id === id; });
    if (!item) return;
    item[field] = parseFloat(val) || 0;
    recalcItem(item);
    renderItems();
}

function recalcItem(item) {
    var pct = item.porcentajeIgv || 0;
    item.baseImponible = item.cantidad * item.valorUnitario;
    item.subtotal = item.baseImponible;
    item.montoImpuesto = item.baseImponible * pct / 100;
    item.igv = item.montoImpuesto;
    item.precioUnitario = item.valorUnitario * (1 + pct / 100);
}

function rmItem(id) {
    items = items.filter(function(i){ return i.id !== id; });
    renderItems();
}

function addProductToItems() {
    if (!selectedProduct) {
        alert('Primero selecciona un producto del catálogo.');
        openProductModal();
        return;
    }
    var qty = parseInt(document.getElementById('add-qty').value) || 1;
    var valor = parseFloat(selectedProduct.valorUnitario) || 0;
    var base = qty * valor;
    // Usar la afectación IGV del producto registrado (default 10=Gravado)
    var afect = selectedProduct.tipoAfectacionIgv || '10';
    var igvInfo = IGV_MAP[afect] || IGV_MAP['10'];
    var impuesto = base * igvInfo.porcentajeIgv / 100;
    items.push({
        id: ++itemCounter,
        descripcion: selectedProduct.descripcion,
        unidadMedida: selectedProduct.unidadMedida,
        cantidad: qty,
        valorUnitario: valor,
        precioUnitario: valor * (1 + igvInfo.porcentajeIgv / 100),
        igv: impuesto,
        subtotal: base,
        tipoAfectacionIgv: afect,
        codigoTributo: igvInfo.codigoTributo,
        tipoPrecio: '01',
        porcentajeIgv: igvInfo.porcentajeIgv,
        baseImponible: base,
        montoImpuesto: impuesto,
        codigoProductoSunat: selectedProduct.codigoProductoSunat || ''
    });
    renderItems();
    clearSelectedProduct();
    document.getElementById('add-qty').value = '1';
}

function recalcTotals() {
    var grav = 0, exo = 0, ina = 0, totalIgv = 0;
    items.forEach(function(i) {
        if (i.tipoAfectacionIgv === '20') exo += i.subtotal;
        else if (i.tipoAfectacionIgv === '30') ina += i.subtotal;
        else grav += i.subtotal;
        totalIgv += i.montoImpuesto;
    });
    // Include cargos/descuentos globales
    var totalCargos = 0, totalDescuentos = 0;
    cargosDescuentos.forEach(function(ac) {
        if (ac.chargeIndicator) totalCargos += ac.amount;
        else totalDescuentos += ac.amount;
    });
    var t = grav + exo + ina + totalIgv + totalCargos - totalDescuentos;
    var monedaEl = document.getElementById('fac-moneda');
    var sym = (monedaEl && monedaEl.value === 'USD') ? 'US$ ' : 'S/ ';
    var el;
    el = document.getElementById('t-grav');  if (el) el.textContent = sym + grav.toFixed(2);
    el = document.getElementById('t-exo');   if (el) { el.textContent = sym + exo.toFixed(2); el.parentElement.style.display = exo > 0 ? '' : 'none'; }
    el = document.getElementById('t-ina');   if (el) { el.textContent = sym + ina.toFixed(2); el.parentElement.style.display = ina > 0 ? '' : 'none'; }
    el = document.getElementById('t-igv');   if (el) el.textContent = sym + totalIgv.toFixed(2);
    el = document.getElementById('t-cargos-row'); if (el) el.style.display = totalCargos > 0 ? '' : 'none';
    el = document.getElementById('t-cargos'); if (el) el.textContent = '+ ' + sym + totalCargos.toFixed(2);
    el = document.getElementById('t-desc-row'); if (el) el.style.display = totalDescuentos > 0 ? '' : 'none';
    el = document.getElementById('t-desc'); if (el) el.textContent = '- ' + sym + totalDescuentos.toFixed(2);
    el = document.getElementById('t-total'); if (el) el.textContent = sym + t.toFixed(2);
    // Update cuotas sum check
    validateCuotasSum(t);
}

/* ═══════ CUOTAS DE CRÉDITO ═══════ */
let cuotas = [];
let cuotaCounter = 0;

function addCuota() {
    var total = getCurrentTotal();
    var existingSum = cuotas.reduce(function(s, c) { return s + c.monto; }, 0);
    var remaining = Math.max(0, total - existingSum);
    var fechaVenc = document.getElementById('fac-fecha-venc');
    var baseDate = fechaVenc ? fechaVenc.value : new Date().toISOString().split('T')[0];
    // Next month from base date
    var nextDate = new Date(baseDate);
    nextDate.setMonth(nextDate.getMonth() + cuotas.length);
    var nextDateStr = nextDate.toISOString().split('T')[0];

    cuotas.push({
        id: ++cuotaCounter,
        numeroCuota: 'Cuota' + String(cuotas.length + 1).padStart(3, '0'),
        monto: parseFloat(remaining.toFixed(2)),
        fechaVencimiento: nextDateStr
    });
    renderCuotas();
}

function rmCuota(id) {
    cuotas = cuotas.filter(function(c) { return c.id !== id; });
    // Re-number
    cuotas.forEach(function(c, idx) {
        c.numeroCuota = 'Cuota' + String(idx + 1).padStart(3, '0');
    });
    renderCuotas();
}

function updCuota(id, field, val) {
    var cuota = cuotas.find(function(c) { return c.id === id; });
    if (!cuota) return;
    if (field === 'monto') cuota.monto = parseFloat(val) || 0;
    else if (field === 'fechaVencimiento') cuota.fechaVencimiento = val;
    renderCuotas();
}

function renderCuotas() {
    var tbody = document.getElementById('cuotas-body');
    if (!tbody) return;
    tbody.innerHTML = '';
    cuotas.forEach(function(c) {
        var tr = document.createElement('tr');
        tr.innerHTML =
            '<td>' + c.numeroCuota + '</td>' +
            '<td><input type="number" step="0.01" value="' + c.monto.toFixed(2) + '" style="width:100px;" onchange="updCuota(' + c.id + ',\'monto\',this.value)"/></td>' +
            '<td><input type="date" value="' + (c.fechaVencimiento || '') + '" onchange="updCuota(' + c.id + ',\'fechaVencimiento\',this.value)"/></td>' +
            '<td><button class="btn btn-danger-sm" onclick="rmCuota(' + c.id + ')">✕</button></td>';
        tbody.appendChild(tr);
    });
    validateCuotasSum(getCurrentTotal());
}

function validateCuotasSum(total) {
    var checkEl = document.getElementById('cuotas-sum-check');
    if (!checkEl) return;
    if (cuotas.length === 0) { checkEl.textContent = ''; return; }
    var sum = cuotas.reduce(function(s, c) { return s + c.monto; }, 0);
    var diff = Math.abs(sum - total);
    if (diff < 0.02) {
        checkEl.innerHTML = '<span style="color:var(--accent);">✓ Suma correcta: ' + sum.toFixed(2) + '</span>';
    } else {
        checkEl.innerHTML = '<span style="color:var(--danger);">✗ Suma: ' + sum.toFixed(2) + ' ≠ Total: ' + total.toFixed(2) + '</span>';
    }
}

function getCurrentTotal() {
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
    return grav + exo + ina + totalIgv + totalCargos - totalDescuentos;
}

/* ═══════ CARGOS / DESCUENTOS GLOBALES (Cat. 53 SUNAT) ═══════ */
let cargosDescuentos = [];
let acCounter = 0;

function addCargoDescuento() {
    cargosDescuentos.push({
        id: ++acCounter,
        chargeIndicator: true,
        reasonCode: '46',
        reasonDescription: 'Recargo al consumo',
        multiplierFactor: 0.10,
        amount: 0,
        baseAmount: 0
    });
    renderCargosDescuentos();
}

function rmCargoDescuento(id) {
    cargosDescuentos = cargosDescuentos.filter(function(a) { return a.id !== id; });
    renderCargosDescuentos();
    recalcTotals();
}

function updAC(id, field, val) {
    var ac = cargosDescuentos.find(function(a) { return a.id === id; });
    if (!ac) return;
    if (field === 'chargeIndicator') ac.chargeIndicator = (val === 'true');
    else if (field === 'reasonCode') ac.reasonCode = val;
    else if (field === 'reasonDescription') ac.reasonDescription = val;
    else if (field === 'multiplierFactor') {
        ac.multiplierFactor = parseFloat(val) || 0;
        ac.amount = parseFloat((ac.baseAmount * ac.multiplierFactor).toFixed(2));
    }
    else if (field === 'baseAmount') {
        ac.baseAmount = parseFloat(val) || 0;
        ac.amount = parseFloat((ac.baseAmount * ac.multiplierFactor).toFixed(2));
    }
    else if (field === 'amount') ac.amount = parseFloat(val) || 0;
    renderCargosDescuentos();
    recalcTotals();
}

function renderCargosDescuentos() {
    var tbody = document.getElementById('ac-body');
    if (!tbody) return;
    tbody.innerHTML = '';
    cargosDescuentos.forEach(function(ac) {
        var tr = document.createElement('tr');
        tr.innerHTML =
            '<td><select style="font-size:0.75rem;width:90px;" onchange="updAC(' + ac.id + ',\'chargeIndicator\',this.value)">' +
                '<option value="true"' + (ac.chargeIndicator ? ' selected' : '') + '>Cargo</option>' +
                '<option value="false"' + (!ac.chargeIndicator ? ' selected' : '') + '>Descuento</option>' +
            '</select></td>' +
            '<td><select style="font-size:0.75rem;width:70px;" onchange="updAC(' + ac.id + ',\'reasonCode\',this.value)">' +
                '<option value="00"' + (ac.reasonCode === '00' ? ' selected' : '') + '>00</option>' +
                '<option value="02"' + (ac.reasonCode === '02' ? ' selected' : '') + '>02</option>' +
                '<option value="45"' + (ac.reasonCode === '45' ? ' selected' : '') + '>45</option>' +
                '<option value="46"' + (ac.reasonCode === '46' ? ' selected' : '') + '>46</option>' +
                '<option value="47"' + (ac.reasonCode === '47' ? ' selected' : '') + '>47</option>' +
                '<option value="50"' + (ac.reasonCode === '50' ? ' selected' : '') + '>50</option>' +
            '</select></td>' +
            '<td><input value="' + (ac.reasonDescription || '') + '" style="width:120px;font-size:0.75rem;" onchange="updAC(' + ac.id + ',\'reasonDescription\',this.value)"/></td>' +
            '<td><input type="number" step="0.01" value="' + ac.multiplierFactor + '" style="width:60px;" onchange="updAC(' + ac.id + ',\'multiplierFactor\',this.value)"/></td>' +
            '<td><input type="number" step="0.01" value="' + ac.baseAmount.toFixed(2) + '" style="width:85px;" onchange="updAC(' + ac.id + ',\'baseAmount\',this.value)"/></td>' +
            '<td style="font-family:var(--font-mono);font-weight:600;' + (ac.chargeIndicator ? 'color:var(--danger);' : 'color:var(--accent);') + '">' + ac.amount.toFixed(2) + '</td>' +
            '<td><button class="btn btn-danger-sm" onclick="rmCargoDescuento(' + ac.id + ')">✕</button></td>';
        tbody.appendChild(tr);
    });
}
