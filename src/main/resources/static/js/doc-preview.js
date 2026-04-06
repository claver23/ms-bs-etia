/* ═══════════════════════════════════════════════
   🔍  ETIA — Document Preview Modal
   Shows a rich preview of any document type
   ═══════════════════════════════════════════════ */

var DocPreview = (function() {
    'use strict';

    var _fetchers = {
        'factura':       function(id) { return ApiClient.getFactura(id); },
        'boleta':        function(id) { return ApiClient.getBoleta(id); },
        'nota_credito':  function(id) { return ApiClient.getNotaCredito(id); },
        'nota_debito':   function(id) { return ApiClient.getNotaDebito(id); }
    };

    var _labels = {
        'factura':      { icon: '🧾', name: 'Factura Electrónica' },
        'boleta':       { icon: '🎫', name: 'Boleta de Venta' },
        'nota_credito': { icon: '📉', name: 'Nota de Crédito' },
        'nota_debito':  { icon: '📈', name: 'Nota de Débito' }
    };

    function open(docType, docId) {
        var overlay = document.getElementById('doc-preview-overlay');
        var body    = document.getElementById('dp-body');
        var footer  = document.getElementById('dp-footer');
        var title   = document.getElementById('dp-title');
        var sub     = document.getElementById('dp-subtitle');
        var editBtn = document.getElementById('dp-edit-btn');

        var meta = _labels[docType] || { icon: '📄', name: 'Documento' };
        title.innerHTML = meta.icon + ' ' + meta.name;
        sub.textContent = 'Cargando...';
        body.innerHTML = '<p class="pm-spinner" style="text-align:center;padding:3rem;color:var(--accent);">⏳ Cargando datos del documento...</p>';
        footer.style.display = 'none';
        editBtn.style.display = 'none';
        overlay.classList.add('show');

        var fetcher = _fetchers[docType];
        if (!fetcher) {
            body.innerHTML = '<p class="pm-empty" style="color:var(--danger);">Tipo de documento no soportado</p>';
            footer.style.display = 'flex';
            return;
        }

        fetcher(docId).then(function(doc) {
            if (!doc) {
                body.innerHTML = '<p class="pm-empty" style="color:var(--danger);">Documento no encontrado</p>';
                footer.style.display = 'flex';
                return;
            }
            _renderDoc(docType, doc);
        }).catch(function() {
            body.innerHTML = '<p class="pm-empty" style="color:var(--danger);">❌ Error al cargar el documento</p>';
            footer.style.display = 'flex';
        });
    }

    function close() {
        document.getElementById('doc-preview-overlay').classList.remove('show');
    }

    function _renderDoc(docType, doc) {
        var body    = document.getElementById('dp-body');
        var footer  = document.getElementById('dp-footer');
        var sub     = document.getElementById('dp-subtitle');
        var editBtn = document.getElementById('dp-edit-btn');

        // Subtitle
        var estado = doc.estado || '—';
        var badgeCls = estado === 'BORRADOR' ? 'badge-gray' : estado === 'ANULADA' ? 'badge-red' : estado === 'RECHAZADA' ? 'badge-red' : 'badge-green';
        sub.innerHTML = '<span class="badge ' + badgeCls + '" style="font-size:0.7rem;">' + estado + '</span>';

        var html = '';

        // Header info
        html += '<div class="dp-section">';
        html += '<div class="dp-section-title">📋 Información General</div>';
        html += '<div class="dp-grid">';

        if (docType === 'factura' || docType === 'boleta') {
            html += _field('Serie / Número', (doc.serie || '') + '-' + (doc.numero || ''));
            html += _field('Fecha Emisión', ETIA.formatDate(doc.fechaEmision || doc.fechaCreacion));
            html += _field('Moneda', doc.moneda || 'PEN');
            html += _field('Estado', estado);
        } else {
            html += _field('Tipo', docType === 'nota_credito' ? 'Nota de Crédito' : 'Nota de Débito');
            html += _field('Motivo', doc.motivo || '—');
            html += _field('Estado', estado);
            html += _field('Fecha', ETIA.formatDate(doc.fechaCreacion));
            if (doc.facturaReferenciaId) {
                html += _field('Ref. Factura', doc.facturaReferenciaId);
            }
            if (doc.descripcion) {
                html += '<div class="dp-field" style="grid-column:1/-1;"><label>Descripción</label><span>' + doc.descripcion + '</span></div>';
            }
        }
        html += '</div></div>';

        // Leyenda (factura/boleta)
        if (doc.leyenda) {
            html += '<div class="dp-section"><div class="dp-section-title">💬 Leyenda</div>';
            html += '<p style="font-size:0.82rem;color:var(--text-secondary);font-style:italic;">' + doc.leyenda + '</p></div>';
        }

        // Items detail
        var items = doc.detalle || doc.detalleJson || [];
        if (typeof items === 'string') {
            try { items = JSON.parse(items); } catch(e) { items = []; }
        }
        if (items && items.length > 0) {
            html += '<div class="dp-section">';
            html += '<div class="dp-section-title">📦 Detalle de Ítems</div>';
            html += '<div class="tbl-wrap"><table class="tbl"><thead><tr>' +
                '<th>Descripción</th><th>U.M.</th><th>Cant.</th><th>V.Unit.</th><th>IGV</th><th>Subtotal</th>' +
                '</tr></thead><tbody>';
            items.forEach(function(it) {
                html += '<tr>' +
                    '<td><strong>' + (it.descripcion || '—') + '</strong></td>' +
                    '<td>' + (it.unidadMedida || 'ZZ') + '</td>' +
                    '<td style="text-align:center;">' + (it.cantidad || 1) + '</td>' +
                    '<td style="font-family:var(--font-mono);">' + _money(it.valorUnitario) + '</td>' +
                    '<td style="font-family:var(--font-mono);">' + _money(it.igv) + '</td>' +
                    '<td style="font-family:var(--font-mono);font-weight:600;">' + _money(it.subtotal) + '</td>' +
                    '</tr>';
            });
            html += '</tbody></table></div></div>';
        }

        // Totals (factura/boleta)
        if (doc.gravadas !== undefined || doc.total !== undefined) {
            var monSym = (doc.moneda === 'USD') ? 'US$ ' : 'S/ ';
            html += '<div class="dp-section">';
            html += '<div class="dp-section-title">💰 Resumen</div>';
            if (doc.gravadas !== undefined) html += _totalRow('Op. Gravadas', monSym + _num(doc.gravadas));
            if (doc.igv !== undefined) html += _totalRow('IGV (18%)', monSym + _num(doc.igv));
            html += '<div class="dp-total-row grand"><span>TOTAL</span><span>' + monSym + _num(doc.total) + '</span></div>';
            html += '</div>';
        }

        // Notas: monto descuento/incremento
        if (doc.montoDescuento !== undefined) {
            html += '<div class="dp-section"><div class="dp-section-title">💰 Monto</div>';
            html += '<div class="dp-total-row grand"><span>Descuento</span><span style="color:var(--danger);">-S/ ' + _num(doc.montoDescuento) + '</span></div></div>';
        }
        if (doc.montoIncremento !== undefined) {
            html += '<div class="dp-section"><div class="dp-section-title">💰 Monto</div>';
            html += '<div class="dp-total-row grand"><span>Incremento</span><span style="color:var(--warning);">+S/ ' + _num(doc.montoIncremento) + '</span></div></div>';
        }

        body.innerHTML = html;
        footer.style.display = 'flex';

        // Edit button only for drafts
        if (estado === 'BORRADOR' || estado === 'CREADO') {
            editBtn.style.display = 'inline-flex';
            if (docType === 'factura' || docType === 'boleta') {
                editBtn.href = '/wizard?type=' + (docType === 'factura' ? 'FACTURA' : 'BOLETA');
            } else {
                editBtn.href = '/wizard-nota?type=' + (docType === 'nota_credito' ? 'NOTA_CREDITO' : 'NOTA_DEBITO');
            }
        } else {
            editBtn.style.display = 'none';
        }
    }

    function _field(label, value) {
        return '<div class="dp-field"><label>' + label + '</label><span>' + (value || '—') + '</span></div>';
    }

    function _money(val) {
        return (parseFloat(val) || 0).toFixed(2);
    }

    function _num(val) {
        return (parseFloat(val) || 0).toFixed(2);
    }

    function _totalRow(label, value) {
        return '<div class="dp-total-row"><span style="color:var(--text-secondary);">' + label + '</span><span style="font-family:var(--font-mono);">' + value + '</span></div>';
    }

    return { open: open, close: close };
})();

/* Global shortcuts */
function openDocPreview(type, id) { DocPreview.open(type, id); }
function closeDocPreview() { DocPreview.close(); }

