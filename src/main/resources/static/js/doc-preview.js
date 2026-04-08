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
        'VALIDADO':   'badge-blue',
        'CON_ERROR':  'badge-red',
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
        title.innerHTML = meta.icon + ' Cargando...';
        sub.textContent = '';
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
        var ids = ['dp-edit-btn', 'dp-emit-btn', 'dp-annul-btn', 'dp-xml-btn', 'dp-delete-btn',
                   'dp-json-btn', 'dp-pdf-btn', 'dp-validate-btn'];
        ids.forEach(function(id) {
            var el = document.getElementById(id);
            if (el) { el.style.display = 'none'; el.disabled = false; }
        });
    }

    function _renderDoc(docType, doc) {
        var body    = document.getElementById('dp-body');
        var footer  = document.getElementById('dp-footer');
        var title   = document.getElementById('dp-title');
        var sub     = document.getElementById('dp-subtitle');
        var banner  = document.getElementById('dp-status-banner');
        var editBtn  = document.getElementById('dp-edit-btn');
        var emitBtn  = document.getElementById('dp-emit-btn');
        var annulBtn = document.getElementById('dp-annul-btn');

        /* ─── Header: serie+numero + estado badge ─── */
        var estado = doc.estado || '—';
        var badgeCls = _badgeMap[estado] || 'badge-gray';
        var estadoLabel = estado === 'CON_ERROR' ? '⚠ CON ERROR' : estado === 'VALIDADO' ? '✅ VALIDADO' : estado;
        var meta = _labels[docType] || { icon: '📄', name: 'Documento' };
        if (doc.serie && doc.numero != null) {
            title.innerHTML = meta.icon + ' ' + ETIA.formatNumero(doc.serie, doc.numero);
        } else {
            title.innerHTML = meta.icon + ' ' + meta.name;
        }
        sub.innerHTML = '<span class="badge ' + badgeCls + '" style="font-size:0.7rem;">' + estadoLabel + '</span>';

        /* ─── Status banner ─── */
        if (banner) {
            if (estado === 'CON_ERROR') {
                banner.style.display = 'block';
                var errores = [];
                if (doc.erroresValidacion) {
                    try { errores = JSON.parse(doc.erroresValidacion); } catch(e) { errores = [doc.erroresValidacion]; }
                }
                var errHtml = '<div style="background:rgba(248,81,73,0.08);border:1px solid rgba(248,81,73,0.3);border-radius:var(--radius-sm);padding:1rem 1.5rem;margin:0;">' +
                    '<div style="display:flex;align-items:flex-start;gap:0.6rem;">' +
                        '<span style="font-size:1.3rem;flex-shrink:0;">⚠️</span>' +
                        '<div style="flex:1;">' +
                            '<strong style="color:var(--danger);font-size:0.85rem;display:block;margin-bottom:0.3rem;">Errores de Validación (' + errores.length + ')</strong>' +
                            '<p style="color:var(--text-secondary);font-size:0.78rem;margin:0.3rem 0 0.5rem;line-height:1.5;">Este documento tiene errores que deben corregirse antes de emitir.</p>';
                if (errores.length > 0) {
                    errHtml += '<ul style="margin:0;padding-left:1.2rem;color:var(--danger);font-size:0.78rem;line-height:1.7;">';
                    errores.forEach(function(err) { errHtml += '<li>' + err + '</li>'; });
                    errHtml += '</ul>';
                }
                errHtml += '<p style="color:var(--accent);font-size:0.78rem;margin:0.6rem 0 0;">💡 Corrige los datos y vuelve a validar o emitir el documento.</p>' +
                        '</div></div></div>';
                banner.innerHTML = errHtml;
            } else if (estado === 'VALIDADO') {
                banner.style.display = 'block';
                banner.innerHTML =
                    '<div style="background:rgba(88,166,255,0.06);border:1px solid rgba(88,166,255,0.2);border-radius:var(--radius-sm);padding:0.8rem 1.5rem;margin:0;">' +
                        '<span style="font-size:0.82rem;color:var(--info);">✅ Documento validado — Listo para emitir. Todos los campos cumplen con las reglas SUNAT.</span>' +
                    '</div>';
            } else if (estado === 'RECHAZADA' && doc.descripcionRespuestaSunat) {
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
            html += _field('Serie / Número', ETIA.formatNumero(doc.serie, doc.numero));
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

        /* ─── Cargos / Descuentos Globales ─── */
        var acItems = doc.cargosDescuentos || [];
        if (acItems && acItems.length > 0) {
            html += '<div class="dp-section">';
            html += '<div class="dp-section-title">📋 Cargos / Descuentos Globales</div>';
            html += '<div class="tbl-wrap"><table class="tbl"><thead><tr>' +
                '<th>Tipo</th><th>Código</th><th>Descripción</th><th>Factor</th><th>Base</th><th>Monto</th>' +
                '</tr></thead><tbody>';
            acItems.forEach(function(ac) {
                var tipo = ac.chargeIndicator ? '⬆ Cargo' : '⬇ Descuento';
                var color = ac.chargeIndicator ? 'var(--warning)' : 'var(--danger)';
                html += '<tr>' +
                    '<td style="color:' + color + ';font-weight:600;">' + tipo + '</td>' +
                    '<td>' + (ac.reasonCode || '—') + '</td>' +
                    '<td>' + (ac.reasonDescription || '—') + '</td>' +
                    '<td style="font-family:var(--font-mono);">' + _money(ac.multiplierFactor) + '</td>' +
                    '<td style="font-family:var(--font-mono);">' + _money(ac.baseAmount) + '</td>' +
                    '<td style="font-family:var(--font-mono);color:' + color + ';font-weight:600;">' + _money(ac.amount) + '</td>' +
                    '</tr>';
            });
            html += '</tbody></table></div></div>';
        }

        /* ─── Cuotas de Crédito ─── */
        var cuotaItems = doc.cuotas || [];
        if (cuotaItems && cuotaItems.length > 0) {
            html += '<div class="dp-section">';
            html += '<div class="dp-section-title">💳 Cuotas de Crédito</div>';
            html += '<div class="tbl-wrap"><table class="tbl"><thead><tr>' +
                '<th># Cuota</th><th>Monto</th><th>Fecha Vencimiento</th><th>Moneda</th>' +
                '</tr></thead><tbody>';
            cuotaItems.forEach(function(c) {
                html += '<tr>' +
                    '<td><strong>' + (c.numeroCuota || '—') + '</strong></td>' +
                    '<td style="font-family:var(--font-mono);">' + _money(c.monto) + '</td>' +
                    '<td>' + (c.fechaVencimiento ? ETIA.formatDate(c.fechaVencimiento) : '—') + '</td>' +
                    '<td>' + (c.moneda || doc.moneda || 'PEN') + '</td>' +
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
            if (doc.exoneradas && parseFloat(doc.exoneradas) > 0) html += _totalRow('Op. Exoneradas', monSym + _num(doc.exoneradas));
            if (doc.inafectas && parseFloat(doc.inafectas) > 0) html += _totalRow('Op. Inafectas', monSym + _num(doc.inafectas));
            if (doc.igv !== undefined) html += _totalRow('IGV (18%)', monSym + _num(doc.igv));
            if (doc.totalCargos && parseFloat(doc.totalCargos) > 0) html += _totalRow('+ Cargos', '<span style="color:var(--warning);">' + monSym + _num(doc.totalCargos) + '</span>');
            if (doc.totalDescuentos && parseFloat(doc.totalDescuentos) > 0) html += _totalRow('− Descuentos', '<span style="color:var(--danger);">−' + monSym + _num(doc.totalDescuentos) + '</span>');
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

        var xmlBtn      = document.getElementById('dp-xml-btn');
        var deleteBtn   = document.getElementById('dp-delete-btn');
        var jsonBtn     = document.getElementById('dp-json-btn');
        var pdfBtn      = document.getElementById('dp-pdf-btn');
        var validateBtn = document.getElementById('dp-validate-btn');

        /* JSON export: available for ALL states */
        if (jsonBtn) jsonBtn.style.display = 'inline-flex';

        /* XML + PDF: only for transmitted docs with XML (ENVIADA, ACEPTADA, RECHAZADA) */
        var hasXml = (estado === 'ENVIADA' || estado === 'ACEPTADA' || estado === 'RECHAZADA');

        if (docType === 'factura') {
            if (hasXml) {
                if (xmlBtn) xmlBtn.style.display = 'inline-flex';
                if (pdfBtn) pdfBtn.style.display = 'inline-flex';
            }
            switch (estado) {
                case 'BORRADOR':
                    editBtn.style.display = 'inline-flex';
                    editBtn.textContent = '✏️ Editar';
                    editBtn.href = '/wizard?type=FACTURA&id=' + encodeURIComponent(_currentDocId);
                    if (validateBtn) validateBtn.style.display = 'inline-flex';
                    emitBtn.style.display = 'inline-flex';
                    if (deleteBtn) deleteBtn.style.display = 'inline-flex';
                    break;
                case 'VALIDADO':
                    editBtn.style.display = 'inline-flex';
                    editBtn.textContent = '✏️ Editar';
                    editBtn.href = '/wizard?type=FACTURA&id=' + encodeURIComponent(_currentDocId);
                    emitBtn.style.display = 'inline-flex';
                    if (deleteBtn) deleteBtn.style.display = 'inline-flex';
                    break;
                case 'CON_ERROR':
                    editBtn.style.display = 'inline-flex';
                    editBtn.textContent = '✏️ Corregir';
                    editBtn.href = '/wizard?type=FACTURA&id=' + encodeURIComponent(_currentDocId);
                    if (validateBtn) { validateBtn.style.display = 'inline-flex'; validateBtn.textContent = '🔍 Re-validar'; }
                    if (deleteBtn) deleteBtn.style.display = 'inline-flex';
                    break;
                case 'ENVIADA':
                    annulBtn.style.display = 'inline-flex';
                    break;
                case 'ACEPTADA':
                    annulBtn.style.display = 'inline-flex';
                    break;
                case 'RECHAZADA':
                    editBtn.style.display = 'inline-flex';
                    editBtn.textContent = '🔧 Corregir y Re-emitir';
                    editBtn.href = '/wizard?type=FACTURA&id=' + encodeURIComponent(_currentDocId);
                    break;
                case 'ANULADA':
                    /* Read-only — estado terminal */
                    break;
            }
        } else if (docType === 'boleta') {
            switch (estado) {
                case 'BORRADOR':
                    editBtn.style.display = 'inline-flex';
                    editBtn.textContent = '✏️ Editar';
                    editBtn.href = '/wizard?type=BOLETA&id=' + encodeURIComponent(_currentDocId);
                    if (validateBtn) validateBtn.style.display = 'inline-flex';
                    break;
                case 'VALIDADO':
                    editBtn.style.display = 'inline-flex';
                    editBtn.textContent = '✏️ Editar';
                    editBtn.href = '/wizard?type=BOLETA&id=' + encodeURIComponent(_currentDocId);
                    break;
                case 'CON_ERROR':
                    editBtn.style.display = 'inline-flex';
                    editBtn.textContent = '✏️ Corregir';
                    editBtn.href = '/wizard?type=BOLETA&id=' + encodeURIComponent(_currentDocId);
                    if (validateBtn) { validateBtn.style.display = 'inline-flex'; validateBtn.textContent = '🔍 Re-validar'; }
                    break;
            }
        } else {
            if (estado === 'BORRADOR' || estado === 'CREADO') {
                editBtn.style.display = 'inline-flex';
                editBtn.textContent = '✏️ Editar';
                editBtn.href = '/wizard-nota?type=' + (docType === 'nota_credito' ? 'NOTA_CREDITO' : 'NOTA_DEBITO') + '&id=' + encodeURIComponent(_currentDocId);
            }
        }
    }

    /* ─── Actions ─── */
    function emitir() {
        if (!_currentDocId || !_currentDoc) return;
        var emitBtn = document.getElementById('dp-emit-btn');
        if (emitBtn) { emitBtn.disabled = true; emitBtn.textContent = '⏳ Emitiendo...'; }

        /* Use emitirFacturaBorrador to transition BORRADOR → EMITIDA with XML generation */
        ApiClient.emitirFacturaBorrador(_currentDocId).then(function() {
            close();
            if (typeof loadDocs === 'function') loadDocs();
            if (ETIA.showSuccess) ETIA.showSuccess('Factura emitida exitosamente');
        }).catch(function() {
            if (emitBtn) { emitBtn.disabled = false; emitBtn.textContent = '📤 Emitir'; }
        });
    }

    function anular() {
        if (!_currentDocId || !_currentDoc) return;
        var serie = ETIA.formatNumero(_currentDoc.serie, _currentDoc.numero);
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

    function descargarXml() {
        if (!_currentDocId) return;
        var xmlBtn = document.getElementById('dp-xml-btn');
        if (xmlBtn) { xmlBtn.disabled = true; xmlBtn.textContent = '⏳ Descargando...'; }

        ApiClient.descargarFacturaXml(_currentDocId).then(function(result) {
            if (!result) {
                if (ETIA.showError) ETIA.showError({ message: 'Esta factura aún no tiene XML generado.' });
                if (xmlBtn) { xmlBtn.disabled = false; xmlBtn.textContent = '📥 XML'; }
                return;
            }
            var a = document.createElement('a');
            a.href = URL.createObjectURL(result.blob);
            a.download = result.filename;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            URL.revokeObjectURL(a.href);
            if (xmlBtn) { xmlBtn.disabled = false; xmlBtn.textContent = '📥 XML'; }
            if (ETIA.showSuccess) ETIA.showSuccess('XML descargado: ' + result.filename);
        }).catch(function() {
            if (xmlBtn) { xmlBtn.disabled = false; xmlBtn.textContent = '📥 XML'; }
        });
    }

    function eliminar() {
        if (!_currentDocId || !_currentDoc) return;
        var serie = ETIA.formatNumero(_currentDoc.serie, _currentDoc.numero);
        if (!confirm('¿Eliminar el borrador ' + serie + '? Esta acción no se puede deshacer.')) return;

        var deleteBtn = document.getElementById('dp-delete-btn');
        if (deleteBtn) { deleteBtn.disabled = true; deleteBtn.textContent = '⏳ Eliminando...'; }

        ApiClient.eliminarFactura(_currentDocId).then(function() {
            close();
            if (typeof loadDocs === 'function') loadDocs();
            if (ETIA.showSuccess) ETIA.showSuccess('Borrador ' + serie + ' eliminado');
        }).catch(function() {
            if (deleteBtn) { deleteBtn.disabled = false; deleteBtn.textContent = '🗑️ Eliminar'; }
        });
    }

    function validar() {
        if (!_currentDocId || !_currentDocType) return;
        var validateBtn = document.getElementById('dp-validate-btn');
        if (validateBtn) { validateBtn.disabled = true; validateBtn.textContent = '⏳ Validando...'; }

        var apiCall = _currentDocType === 'boleta'
            ? ApiClient.validarBoleta(_currentDocId)
            : ApiClient.validarFactura(_currentDocId);

        apiCall.then(function(result) {
            if (result && result.data) result = result.data;
            if (result && result.valid) {
                if (ETIA.showSuccess) ETIA.showSuccess('✅ Documento validado correctamente — Listo para emitir');
            } else {
                var errCount = (result && result.errores) ? result.errores.length : 0;
                if (ETIA.showError) ETIA.showError({ message: '⚠ Validación encontró ' + errCount + ' error(es). Revisa el documento.' });
            }
            /* Refresh preview with updated state */
            open(_currentDocType, _currentDocId);
            if (typeof loadDocs === 'function') loadDocs();
        }).catch(function() {
            if (validateBtn) { validateBtn.disabled = false; validateBtn.textContent = '🔍 Validar'; }
        });
    }

    /* ─── Export functions ─── */
    function exportJson() {
        if (!_currentDoc) return;
        var jsonBtn = document.getElementById('dp-json-btn');
        var json = JSON.stringify(_currentDoc, null, 2);

        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(json).then(function() {
                if (jsonBtn) { jsonBtn.textContent = '✅ Copiado'; setTimeout(function() { jsonBtn.textContent = '📋 JSON'; }, 1500); }
                if (ETIA.showSuccess) ETIA.showSuccess('JSON copiado al portapapeles');
            }).catch(function() {
                _fallbackCopyJson(json, jsonBtn);
            });
        } else {
            _fallbackCopyJson(json, jsonBtn);
        }
    }

    function _fallbackCopyJson(json, btn) {
        var ta = document.createElement('textarea');
        ta.value = json;
        ta.style.cssText = 'position:fixed;left:-9999px;';
        document.body.appendChild(ta);
        ta.select();
        try {
            document.execCommand('copy');
            if (btn) { btn.textContent = '✅ Copiado'; setTimeout(function() { btn.textContent = '📋 JSON'; }, 1500); }
            if (ETIA.showSuccess) ETIA.showSuccess('JSON copiado al portapapeles');
        } catch(e) {
            /* Last resort: open in new tab */
            var blob = new Blob([json], { type: 'application/json' });
            window.open(URL.createObjectURL(blob));
        }
        document.body.removeChild(ta);
    }

    function exportPdf() {
        if (!_currentDoc) return;
        var doc = _currentDoc;
        var meta = _labels[_currentDocType] || { icon: '📄', name: 'Documento' };

        /* Parse items */
        var items = doc.detalle || doc.detalleJson || [];
        if (typeof items === 'string') {
            try { items = JSON.parse(items); } catch(e) { items = []; }
        }

        /* Resolve client from cache */
        var client = {};
        if (doc.clientId) {
            client = ETIA.findClientById(doc.clientId) || {};
        }

        /* Totals */
        var totals = {
            gravadas: doc.gravadas,
            exoneradas: doc.exoneradas,
            inafectas: doc.inafectas,
            igv: doc.igv,
            totalCargos: doc.totalCargos,
            totalDescuentos: doc.totalDescuentos,
            total: doc.total
        };

        /* Fetch logo (cached) then print via PdfTemplate */
        ETIA.getLogoBase64().then(function(logoBase64) {
            if (typeof PdfTemplate !== 'undefined') {
                PdfTemplate.print({
                    doc: doc,
                    company: ETIA.company || {},
                    client: client,
                    items: items,
                    totals: totals,
                    logoBase64: logoBase64,
                    docTypeName: meta.name,
                    docTypeIcon: meta.icon
                });
            } else {
                /* Fallback: PdfTemplate not loaded */
                console.warn('PdfTemplate not available, cannot export PDF');
            }
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

    return { open: open, close: close, emitir: emitir, anular: anular, validar: validar, descargarXml: descargarXml, eliminar: eliminar, exportJson: exportJson, exportPdf: exportPdf };
})();

/* Global shortcuts */
function openDocPreview(type, id) { DocPreview.open(type, id); }
function closeDocPreview() { DocPreview.close(); }
function dpEmitirFactura() { DocPreview.emitir(); }
function dpAnularFactura() { DocPreview.anular(); }
function dpValidarDoc() { DocPreview.validar(); }
function dpDescargarXml() { DocPreview.descargarXml(); }
function dpEliminarFactura() { DocPreview.eliminar(); }
function dpExportJson() { DocPreview.exportJson(); }
function dpExportPdf() { DocPreview.exportPdf(); }
