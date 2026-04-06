/* ═══════ ETIA — Items Table ═══════ */
let items = [];
let itemCounter = 0;

function renderItems() {
    var tbody = document.getElementById('items-body');
    if (!tbody) return;
    tbody.innerHTML = '';
    items.forEach(function(item, idx) {
        var tr = document.createElement('tr');
        tr.innerHTML =
            '<td>'+(idx+1)+'</td>'+
            '<td style="min-width:180px;">'+item.descripcion+'</td>'+
            '<td><span class="badge badge-blue">'+item.unidadMedida+'</span></td>'+
            '<td><input type="number" value="'+item.cantidad+'" min="1" style="width:55px;" onchange="updItem('+item.id+',\'cantidad\',this.value)"/></td>'+
            '<td><input type="number" step="0.01" value="'+item.valorUnitario.toFixed(2)+'" style="width:85px;" onchange="updItem('+item.id+',\'valorUnitario\',this.value)"/></td>'+
            '<td style="font-family:var(--font-mono);color:var(--text-secondary);">'+item.precioUnitario.toFixed(2)+'</td>'+
            '<td style="font-family:var(--font-mono);color:var(--warning);">'+item.igv.toFixed(2)+'</td>'+
            '<td style="font-family:var(--font-mono);color:var(--accent);font-weight:600;">'+item.subtotal.toFixed(2)+'</td>'+
            '<td><button class="btn btn-danger-sm" onclick="rmItem('+item.id+')">✕</button></td>';
        tbody.appendChild(tr);
    });
    recalcTotals();
}

function updItem(id, field, val) {
    var item = items.find(function(i){ return i.id === id; });
    if (!item) return;
    item[field] = parseFloat(val) || 0;
    item.subtotal = item.cantidad * item.valorUnitario;
    item.igv = item.subtotal * 0.18;
    item.precioUnitario = item.valorUnitario * 1.18;
    renderItems();
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
    var subtotal = qty * valor;
    items.push({
        id: ++itemCounter,
        descripcion: selectedProduct.descripcion,
        unidadMedida: selectedProduct.unidadMedida,
        cantidad: qty,
        valorUnitario: valor,
        precioUnitario: valor * 1.18,
        igv: subtotal * 0.18,
        subtotal: subtotal
    });
    renderItems();
    clearSelectedProduct();
    document.getElementById('add-qty').value = '1';
}

function recalcTotals() {
    var g = items.reduce(function(s, i){ return s + i.subtotal; }, 0);
    var iv = items.reduce(function(s, i){ return s + i.igv; }, 0);
    var t = g + iv;
    var monedaEl = document.getElementById('fac-moneda');
    var sym = (monedaEl && monedaEl.value === 'USD') ? 'US$ ' : 'S/ ';
    var el;
    el = document.getElementById('t-grav');  if (el) el.textContent = sym + g.toFixed(2);
    el = document.getElementById('t-igv');   if (el) el.textContent = sym + iv.toFixed(2);
    el = document.getElementById('t-total'); if (el) el.textContent = sym + t.toFixed(2);
}
