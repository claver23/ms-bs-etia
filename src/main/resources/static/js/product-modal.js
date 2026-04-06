/* ═══════ ETIA — Product Search Modal ═══════ */
var selectedProduct = null;
var pmSearchTimeout = null;

function openProductModal() {
    document.getElementById('product-modal').classList.add('show');
    var input = document.getElementById('pm-search-input');
    input.value = '';
    document.getElementById('pm-results').innerHTML = '<div class="pm-empty">Escribe al menos 1 carácter para buscar</div>';
    setTimeout(function(){ input.focus(); }, 100);
}

function closeProductModal() {
    document.getElementById('product-modal').classList.remove('show');
}

function onModalProductSearch(term) {
    clearTimeout(pmSearchTimeout);
    var container = document.getElementById('pm-results');
    if (!term || term.length < 1) {
        container.innerHTML = '<div class="pm-empty">Escribe al menos 1 carácter para buscar</div>';
        return;
    }
    container.innerHTML = '<div class="pm-spinner">⏳ Buscando...</div>';
    pmSearchTimeout = setTimeout(function(){
        ApiClient.searchProducts(ETIA.companyId, term)
            .then(function(result){
                /* Normalize: handle every possible response shape */
                var products;
                if (Array.isArray(result)) {
                    products = result;
                } else if (result && Array.isArray(result.content)) {
                    products = result.content;
                } else if (result && result.data && Array.isArray(result.data.content)) {
                    products = result.data.content;
                } else if (result && Array.isArray(result.data)) {
                    products = result.data;
                } else {
                    products = [];
                }
                if (products.length === 0) {
                    container.innerHTML = '<div class="pm-empty">No se encontraron productos para "' + term + '"</div>';
                    return;
                }
                container.innerHTML = products.map(function(p){
                    return '<div class="pm-item" onclick="selectProductFromModal(\'' + p.id + '\',\'' + (p.descripcion||'').replace(/'/g,"\\'") + '\',\'' + (p.unidadMedida||'ZZ') + '\',' + (p.valorUnitario||0) + ',' + (p.precioUnitario||0) + ')">' +
                        '<div class="pm-item-info"><h4>' + p.descripcion + '</h4>' +
                        '<span>' + (p.codigoProducto||'') + ' · ' + (p.unidadMedida||'ZZ') + '</span></div>' +
                        '<div class="pm-item-price"><div class="price">S/ ' + parseFloat(p.valorUnitario||0).toFixed(2) + '</div>' +
                        '<div class="unit">+ IGV</div></div></div>';
                }).join('');
            })
            .catch(function(err){
                container.innerHTML = '<div class="pm-empty" style="color:var(--danger);">Error al buscar: ' + err.message + '</div>';
            });
    }, 300);
}

function selectProductFromModal(id, desc, um, valor, precio) {
    selectedProduct = { id:id, descripcion:desc, unidadMedida:um, valorUnitario:valor, precioUnitario:precio };
    document.getElementById('sp-name').textContent = desc;
    document.getElementById('sp-meta').textContent = um + ' · S/ ' + parseFloat(valor).toFixed(2) + ' + IGV';
    document.getElementById('selected-product').style.display = 'block';
    document.getElementById('search-btn-wrap').style.display = 'none';
    var qtyInput = document.getElementById('add-qty');
    if (um === 'ZZ') { qtyInput.value = 1; qtyInput.disabled = true; }
    else { qtyInput.disabled = false; }
    closeProductModal();
    if (um !== 'ZZ') qtyInput.focus();
}

function clearSelectedProduct() {
    selectedProduct = null;
    document.getElementById('selected-product').style.display = 'none';
    document.getElementById('search-btn-wrap').style.display = 'flex';
    var qtyInput = document.getElementById('add-qty');
    qtyInput.value = 1;
    qtyInput.disabled = false;
}

