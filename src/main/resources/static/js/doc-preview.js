/* ═══════════════════════════════════════════════
   🔍  ETIA — Document Preview Modal
   Shows a rich preview of any document type
   with state-aware actions (BORRADOR/ENVIADA/
   ACEPTADA/RECHAZADA/ANULADA)
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

    /* Badge class per estado */
    var _badgeMap = {
        'BORRADOR':   'badge-gray',
        'ENVIADA':    'badge-blue',
        'ACEPTADA':   'badge-green',
        'RECHAZADA':  'badge-red',
        'ANULADA':    'badge-yellow'
    };

    /* Currently loaded doc context */
    var _currentDocType = null;
    var _currentDocId   = null;
    var _currentDoc     = null;

    function open(docType, docId) {
        var overlay = document.getElementById('doc-preview-overlay');
        var body    = document.getElementById('dp-body');
        var footer  = document.getElementById('dp-footer');
        var title   = document.getElementById('dp-title');
        var sub     = document.getElementById('dp-subtitle');
        var banner  = document.getElementById('dp-status-banner');

        _currentDocType = docType;
        _currentDocId   = docId;
        _currentDoc     = null;

        var meta = _labels[docType] || { icon: '📄', name: 'Documento' };
        title.innerHTML = meta.icon + ' ' + meta.name;
        sub.textContent = 'Cargando...';
        body.innerHTML = '<p class="pm-spinner" style="text-align:center;padding:3rem;color:var(--accent);">⏳ Cargando datos del documento...</p>';
        footer.style.display = 'none';
        if (banner) banner.style.display = 'none';
        _hideAllActions();
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
            _currentDoc = doc;
            _renderDoc(docType, doc);
        }).catch(function() {
            body.innerHTML = '<p class="pm-empty" style="color:var(--danger);">❌ Error al cargar el documento</p>';
            footer.style.display = 'flex';
        });
    }

    function close() {
        document.getElementById('doc-preview-overlay').classList.remove('show');
        _currentDocType = null;
        _currentDocId = null;
        _currentDoc = null;
    }

    function _hideAllActions() {
        var ids = ['dp-edit-btn', 'dp-emit-btn', 'dp-annul-btn'];
        ids.forEach(function(id) {
            var el = document.getElementById(id);
            if (el) el.style.display = 'none';
        });
    }

    function _renderDoc(docType, doc) {
        var body    = document.getElementById('dp-body');
        var footer  = document.getElementById('dp-footer');
        var sub     = document.getElementById('dp-subtitle');
        var banner  = document.getElementById('dp-status-banner');
        var editBtn  = document.getElementById('dp-edit-btn');
        var emitBtn  = document.getElementById('dp-emit-btn');
        var annulBtn = document.getElementById('dp-annul-btn');

        /* ─── Estado badge ─── */
        var estado = doc.estado || '—';
        var badgeCls = _badgeMap[estado] || 'badge-gray';
        sub.innerHTML = '<span class="badge ' + badgeCls + '" style="font-size:0.7rem;">' + estado + '</span>';

        /* ─── Status banner ─── */
        if (banner) {
            if (estado === 'RECHAZADA' && doc.descripcionRespuestaSunat) {
                banner.style.display = 'block';
                banner.innerHTML =
                    '<div style="background:rgba(248,81,73,0.08);border:1px solid rgba(248,81,73,0.3);border-radius:var(--radius-sm);padding:1rem 1.5rem;margin:0;">' +
                        '<div style="display:flex;align-items:flex-start;gap:0.6rem;">' +
                            '<span style="font-size:1.3rem;flex-shrink:0;">⚠️</span>' +
                            '<div>' +
                                '<strong style="color:var(--danger);font-size:0.85rem;display:block;margin-bottom:0.3rem;">Documento Rechazado por SUNAT</strong>' +
                                (doc.codigoRespuestaSunat ? '<span class="badge badge-red" style="font-size:0.6rem;margin-bottom:0.4rem;">Código ' + doc.codigoRespuestaSunat + '</span> ' : '') +
                                '<p style="color:var(--text-secondary);font-size:0.8rem;margin:0.3rem 0 0.6rem;line-height:1.5;">' + doc.descripcionRespuestaSunat + '</p>' +
                                '<p style="color:var(--accent);font-size:0.78rem;margin:0;">💡 Corrige los datos y vuelve a emitir el documento desde el editor.</p>' +
                            '</div>' +
                        '</div>' +
                    '</div>';
            } else if (estado === 'ANULADA') {
                banner.style.display = 'block';
                banner.innerHTML =
                    '<div style="background:var(--warning-bg);border:1px solid rgba(210,153,34,0.3);border-radius:var(--radius-sm);padding:0.8rem 1.5rem;margin:0;">' +
                        '<span style="font-size:0.82rem;color:var(--warning);">🚫 Este documento fue anulado y no puede modificarse.</span>' +
                    '</div>';
            } else if (estado === 'ACEPTADA') {
                banner.style.display = 'block';
                banner.innerHTML =
                    '<div style="background:rgba(0,255,136,0.05);border:1px solid rgba(0,255,136,0.2);border-radius:var(--radius-sm);padding:0.8rem 1.5rem;margin:0;">' +
                        '<span style="font-size:0.82rem;color:var(--accent);">✅ Aceptada por SUNAT — Documento válido y registrado.</span>' +
                    '</div>';
            } else if (estado === 'ENVIADA') {
                banner.style.display = 'block';
                banner.innerHTML =
                    '<div style="background:rgba(88,166,255,0.06);border:1px solid rgba(88,166,255,0.2);border-radius:var(--radius-sm);padding:0.8rem 1.5rem;margin:0;">' +
                        '<span style="font-size:0.82rem;color:var(--info);">📤 Enviada a SUNAT — Pendiente de respuesta.</span>' +
                    '</div>';
            } else {
                banner.style.display = 'none';
            }
        }

        var html = '';

        /* ─── General Info ─── */
        html += '<div class="dp-section">';
        html += '<div class="dp-section-title">📋 Información General</div>';
        html += '<div class="dp-grid">';

        if (docType === 'factura' || docType === 'boleta') {
            html += _field('Serie / Número', (doc.serie || '') + '-' + (doc.numero || ''));
            html += _field('Fecha Emisión', ETIA.formatDate(doc.fechaEmision || doc.fechaCreacion));
            html += _field('Moneda', doc.moneda || 'PEN');
            html += _field('Forma de Pago', doc.formaPago || 'CONTADO');
            if (doc.fechaVencimiento && doc.formaPago === 'CREDITO') {
                html += _field('Fecha Vencimiento', ETIA.formatDate(doc.fechaVencimiento));
            }
            if (doc.hashCdr) {
                html += _field('Hash CDR', '<span style="font-family:var(--font-mono);font-size:0.72rem;word-break:break-all;">' + doc.hashCdr + '</span>');
            }
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

        /* ─── Leyenda ─── */
        if (doc.leyenda) {
            html += '<div class="dp-section"><div class="dp-section-title">💬 Leyenda</div>';
            html += '<p style="font-size:0.82rem;color:var(--text-secondary);font-style:italic;">' + doc.leyenda + '</p></div>';
        }

        /* ─── Items detail ─── */
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

        /* ─── Totals ─── */
        if (doc.gravadas !== undefined || doc.total !== undefined) {
            var monSym = (doc.moneda === 'USD') ? 'US$ ' : 'S/ ';
            html += '<div class="dp-section">';
            html += '<div class="dp-section-title">💰 Resumen</div>';
            if (doc.gravadas !== undefined) html += _totalRow('Op. Gravadas', monSym + _num(doc.gravadas));
            if (doc.igv !== undefined) html += _totalRow('IGV (18%)', monSym + _num(doc.igv));
            html += '<div class="dp-total-row grand"><span>TOTAL</span><span>' + monSym + _num(doc.total) + '</span></div>';
            html += '</div>';
        }

        /* Notas: monto descuento/incremento */
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

        /* ═══════ State-driven action buttons ═══════ */
        _hideAllActions();

        if (docType === 'factura') {
            switch (estado) {
                case 'BORRADOR':
                    editBtn.style.display = 'inline-flex';
                    editBtn.textContent = '✏️ Editar';
                    editBtn.href = '/wizard?type=FACTURA';
                    emitBtn.style.display = 'inline-flex';
                    break;
                case 'ENVIADA':
                    annulBtn.style.display = 'inline-flex';
                    break;
                case 'ACEPTADA':
                    /* Read-only */
                    break;
                case 'RECHAZADA':
                    editBtn.style.display = 'inline-flex';
                    editBtn.textContent = '🔧 Corregir y Re-emitir';
                    editBtn.href = '/wizard?type=FACTURA';
                    break;
                case 'ANULADA':
                    /* Read-only */
                    break;
            }
        } else if (docType === 'boleta') {
            if (estado === 'BORRADOR' || estado === 'CREADO') {
                editBtn.style.display = 'inline-flex';
                editBtn.textContent = '✏️ Editar';
                editBtn.href = '/wizard?type=BOLETA';
            }
        } else {
            if (estado === 'BORRADOR' || estado === 'CREADO') {
                editBtn.style.display = 'inline-flex';
                editBtn.textContent = '✏️ Editar';
                editBtn.href = '/wizard-nota?type=' + (docType === 'nota_credito' ? 'NOTA_CREDITO' : 'NOTA_DEBITO');
            }
        }
    }

    /* ─── Actions ─── */
    function emitir() {
        if (!_currentDocId || !_currentDoc) return;
        var emitBtn = document.getElementById('dp-emit-btn');
        if (emitBtn) { emitBtn.disabled = true; emitBtn.textContent = '⏳ Emitiendo...'; }

        /* Re-emit using the existing emitirFactura endpoint */
        var payload = {
            companyId: _currentDoc.companyId || ETIA.companyId,
            clientId: _currentDoc.clientId || null,
            serie: _currentDoc.serie || null,
            moneda: _currentDoc.moneda || 'PEN',
            formaPago: _currentDoc.formaPago || 'CONTADO',
            gravadas: _currentDoc.gravadas || 0,
            igv: _currentDoc.igv || 0,
            total: _currentDoc.total || 0,
            detalle: _currentDoc.detalle || []
        };

        ApiClient.emitirFactura(payload).then(function() {
            close();
            if (typeof loadDocs === 'function') loadDocs();
            if (ETIA.showSuccess) ETIA.showSuccess('Factura emitida exitosamente');
        }).catch(function() {
            if (emitBtn) { emitBtn.disabled = false; emitBtn.textContent = '📤 Emitir'; }
        });
    }

    function anular() {
        if (!_currentDocId || !_currentDoc) return;
        var serie = (_currentDoc.serie || '') + '-' + (_currentDoc.numero || '');
        if (!confirm('¿Anular la factura ' + serie + '? Esta acción no se puede deshacer.')) return;

        var annulBtn = document.getElementById('dp-annul-btn');
        if (annulBtn) { annulBtn.disabled = true; annulBtn.textContent = '⏳ Anulando...'; }

        ApiClient.anularFactura(_currentDocId, { motivoAnulacion: 'Anulación solicitada por el usuario' })
            .then(function() {
                close();
                if (typeof loadDocs === 'function') loadDocs();
                if (ETIA.showSuccess) ETIA.showSuccess('Factura ' + serie + ' anulada exitosamente');
            }).catch(function() {
                if (annulBtn) { annulBtn.disabled = false; annulBtn.textContent = '🚫 Anular'; }
            });
    }

    /* ─── Helpers ─── */
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

    return { open: open, close: close, emitir: emitir, anular: anular };
})();

/* Global shortcuts */
function openDocPreview(type, id) { DocPreview.open(type, id); }
function closeDocPreview() { DocPreview.close(); }
function dpEmitirFactura() { DocPreview.emitir(); }
function dpAnularFactura() { DocPreview.anular(); }
