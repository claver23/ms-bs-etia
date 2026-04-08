/* ═══════ ETIA — Product Search Modal ═══════ */
var selectedProduct = null;
var pmSearchTimeout = null;
var _allProducts = null; /* Cache */

function openProductModal() {
    document.getElementById('product-modal').classList.add('show');
    var input = document.getElementById('pm-search-input');
    input.value = '';
    document.getElementById('pm-results').innerHTML = '<div class="pm-empty">Cargando productos...</div>';
    setTimeout(function(){ input.focus(); }, 100);

    /* Pre-load all products */
    if (_allProducts) {
        renderProductResults(_allProducts, '');
    } else {
        ApiClient.listProducts(ETIA.companyId).then(function(products) {
            _allProducts = products;
            renderProductResults(products, '');
        }).catch(function() {
            document.getElementById('pm-results').innerHTML = '<div class="pm-empty" style="color:var(--danger);">Error cargando productos</div>';
        });
    }
}

function closeProductModal() {
    document.getElementById('product-modal').classList.remove('show');
}

function onModalProductSearch(term) {
    clearTimeout(pmSearchTimeout);
    var container = document.getElementById('pm-results');
    if (!_allProducts) {
        container.innerHTML = '<div class="pm-spinner">⏳ Cargando...</div>';
        ApiClient.listProducts(ETIA.companyId).then(function(products) {
            _allProducts = products;
            renderProductResults(products, term);
        });
        return;
    }
    pmSearchTimeout = setTimeout(function() {
        renderProductResults(_allProducts, term);
    }, 150);
}

function renderProductResults(products, term) {
    var container = document.getElementById('pm-results');
    var filtered = products;

    if (term && term.trim().length > 0) {
        var t = term.toLowerCase();
        filtered = filtered.filter(function(p) {
            return (p.descripcion && p.descripcion.toLowerCase().includes(t)) ||
                   (p.codigoProducto && p.codigoProducto.toLowerCase().includes(t)) ||
                   (p.codigoProductoSunat && p.codigoProductoSunat.toLowerCase().includes(t));
        });
    }

    if (!filtered || filtered.length === 0) {
        container.innerHTML = '<div class="pm-empty">No se encontraron productos' + (term ? ' para "' + term + '"' : '') + '</div>';
        return;
    }

    container.innerHTML = filtered.map(function(p) {
        var afectMap = {'10':'Grav.','20':'Exon.','30':'Inaf.'};
        var afect = p.tipoAfectacionIgv || '10';
        return '<div class="pm-item" onclick=\'selectProductFromModal(' + JSON.stringify({
            id: p.id, descripcion: p.descripcion||'', unidadMedida: p.unidadMedida||'ZZ',
            valorUnitario: p.valorUnitario||0, precioUnitario: p.precioUnitario||0,
            tipoAfectacionIgv: afect, codigoProductoSunat: p.codigoProductoSunat||''
        }) + ')\'>' +
            '<div class="pm-item-info"><h4>' + (p.descripcion || 'Sin nombre') + '</h4>' +
            '<span>' + (p.codigoProducto||'') + ' · ' + (p.unidadMedida||'ZZ') + (p.codigoProductoSunat ? ' · SUNAT:' + p.codigoProductoSunat : '') + ' · ' + (afectMap[afect]||afect) + '</span></div>' +
            '<div class="pm-item-price"><div class="price">S/ ' + parseFloat(p.valorUnitario||0).toFixed(2) + '</div>' +
            '<div class="unit">+ IGV</div></div></div>';
    }).join('');
}

function selectProductFromModal(prod) {
    selectedProduct = {
        id: prod.id, descripcion: prod.descripcion, unidadMedida: prod.unidadMedida,
        valorUnitario: prod.valorUnitario, precioUnitario: prod.precioUnitario,
        tipoAfectacionIgv: prod.tipoAfectacionIgv || '10',
        codigoProductoSunat: prod.codigoProductoSunat || ''
    };
    document.getElementById('sp-name').textContent = prod.descripcion;
    var afectMap = {'10':'Gravado','20':'Exonerado','30':'Inafecto'};
    document.getElementById('sp-meta').textContent = prod.unidadMedida + ' · S/ ' + parseFloat(prod.valorUnitario).toFixed(2) + ' + IGV · ' + (afectMap[prod.tipoAfectacionIgv]||'Gravado');
    document.getElementById('selected-product').style.display = 'block';
    document.getElementById('search-btn-wrap').style.display = 'none';
    var qtyInput = document.getElementById('add-qty');
    if (prod.unidadMedida === 'ZZ') { qtyInput.value = 1; qtyInput.disabled = true; }
    else { qtyInput.disabled = false; }
    closeProductModal();
    if (prod.unidadMedida !== 'ZZ') qtyInput.focus();
}

function clearSelectedProduct() {
    selectedProduct = null;
    document.getElementById('selected-product').style.display = 'none';
    document.getElementById('search-btn-wrap').style.display = 'flex';
    var qtyInput = document.getElementById('add-qty');
    qtyInput.value = 1;
    qtyInput.disabled = false;
}

