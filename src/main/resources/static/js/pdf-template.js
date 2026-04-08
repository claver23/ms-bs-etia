/* ═══════════════════════════════════════════════
   📄  ETIA — PDF Template Engine
   Extensible template system for document PDFs.
   Reads ETIA.config.documentFormat and applies
   the corresponding layout.
   ═══════════════════════════════════════════════ */

var PdfTemplate = (function() {
    'use strict';

    /* ─── Template Registry ─── */
    var _templates = {};

    /**
     * Register a template by id.
     * @param {string} id - e.g. 'standard', 'compact', 'detailed'
     * @param {function} renderFn - function(data) → HTML string
     */
    function register(id, renderFn) {
        _templates[id] = renderFn;
    }

    /**
     * Render a document to printable HTML using the active template.
     *
     * @param {object} opts
     * @param {object} opts.doc          - Document data (FacturaData, BoletaData, etc.)
     * @param {object} opts.company      - CompanyData (emisor)
     * @param {object} opts.client       - ClientData (adquiriente) — optional
     * @param {Array}  opts.items        - Parsed detalle items array
     * @param {object} opts.totals       - { gravadas, exoneradas, inafectas, igv, totalCargos, totalDescuentos, total }
     * @param {string} opts.logoBase64   - Base64 data URL of company logo — optional
     * @param {string} opts.templateId   - Override template id — optional (defaults to config)
     * @param {string} opts.docTypeName  - e.g. 'Factura Electrónica', 'Boleta de Venta'
     * @param {string} opts.docTypeIcon  - e.g. '🧾', '🎫'
     * @returns {string} Full HTML document string ready for window.open() + print
     */
    function render(opts) {
        var templateId = opts.templateId || (window.ETIA && ETIA.config && ETIA.config.documentFormat) || 'standard';
        var renderFn = _templates[templateId] || _templates['standard'];
        if (!renderFn) {
            return '<html><body><p>Template "' + templateId + '" not found.</p></body></html>';
        }
        return renderFn(opts);
    }

    /**
     * Open a print window with the rendered template.
     */
    function print(opts) {
        var html = render(opts);
        var win = window.open('', '_blank', 'width=800,height=600');
        if (win) {
            win.document.write(html);
            win.document.close();
            win.focus();
            setTimeout(function() { win.print(); }, 400);
        }
    }

    /* ─── Helpers shared across templates ─── */
    function money(val) { return (parseFloat(val) || 0).toFixed(2); }

    function formatDate(dateStr) {
        if (!dateStr) return '—';
        var parts = String(dateStr).substring(0, 10).split('-');
        if (parts.length !== 3) return dateStr;
        return parts[2] + '/' + parts[1] + '/' + parts[0];
    }

    function formatNumero(serie, numero) {
        var s = serie || '';
        var n = String(numero || 0);
        while (n.length < 8) n = '0' + n;
        return s + '-' + n;
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    /* ════════════════════════════════════════════════
       📄  STANDARD TEMPLATE — Classic Peruvian Invoice
       ════════════════════════════════════════════════ */
    register('standard', function(opts) {
        var doc      = opts.doc || {};
        var company  = opts.company || {};
        var client   = opts.client || {};
        var items    = opts.items || [];
        var totals   = opts.totals || {};
        var logo     = opts.logoBase64 || '';
        var typeName = opts.docTypeName || 'Documento';
        var typeIcon = opts.docTypeIcon || '📄';
        var serie    = formatNumero(doc.serie, doc.numero);
        var monSym   = (doc.moneda === 'USD') ? 'US$ ' : 'S/ ';
        var estado   = doc.estado || '';

        var css =
            'body{font-family:Arial,Helvetica,sans-serif;color:#1a1a1a;margin:0;padding:0;font-size:11px;}' +
            '.page{max-width:780px;margin:0 auto;padding:2rem;}' +
            /* Header */
            '.hdr{display:flex;align-items:flex-start;gap:1rem;margin-bottom:1.2rem;}' +
            '.hdr-logo{width:80px;height:80px;object-fit:contain;border-radius:4px;}' +
            '.hdr-logo-placeholder{width:80px;height:80px;background:#f0f0f0;border:1px dashed #ccc;border-radius:4px;display:flex;align-items:center;justify-content:center;font-size:2rem;color:#ccc;}' +
            '.hdr-company{flex:1;}' +
            '.hdr-company h1{font-size:1rem;margin:0 0 0.15rem;color:#1a1a1a;}' +
            '.hdr-company p{font-size:0.75rem;color:#555;margin:0.1rem 0;line-height:1.4;}' +
            '.hdr-docbox{border:2px solid #00994D;border-radius:6px;padding:0.6rem 1rem;text-align:center;min-width:160px;}' +
            '.hdr-docbox .doc-type{font-size:0.85rem;font-weight:700;color:#00994D;margin-bottom:0.15rem;}' +
            '.hdr-docbox .doc-ruc{font-size:0.75rem;font-weight:600;color:#333;}' +
            '.hdr-docbox .doc-num{font-size:0.9rem;font-weight:800;color:#00994D;font-family:monospace;letter-spacing:1px;}' +
            /* Client */
            '.client-box{border:1px solid #ddd;border-radius:4px;padding:0.6rem 0.8rem;margin-bottom:1rem;}' +
            '.client-box .cb-row{display:flex;gap:0.5rem;margin-bottom:0.15rem;font-size:0.78rem;}' +
            '.client-box .cb-label{color:#888;min-width:100px;font-weight:600;}' +
            '.client-box .cb-value{color:#333;}' +
            /* Section */
            '.section{margin-bottom:1rem;}' +
            '.section-title{font-weight:700;font-size:0.82rem;margin-bottom:0.4rem;padding-bottom:0.2rem;border-bottom:2px solid #e0e0e0;color:#333;}' +
            /* Table */
            '.tbl{width:100%;border-collapse:collapse;font-size:0.78rem;}' +
            '.tbl th{background:#f6f8fa;text-align:left;padding:0.35rem 0.5rem;border:1px solid #e0e0e0;font-size:0.7rem;font-weight:700;color:#555;}' +
            '.tbl td{padding:0.3rem 0.5rem;border:1px solid #eee;}' +
            '.tbl .r{text-align:right;}' +
            '.tbl .mono{font-family:monospace;}' +
            /* Totals */
            '.total-row{display:flex;justify-content:space-between;padding:0.2rem 0;font-size:0.82rem;}' +
            '.total-row.grand{font-weight:700;font-size:1rem;border-top:2px solid #333;padding-top:0.4rem;margin-top:0.2rem;}' +
            /* Leyenda */
            '.leyenda{font-style:italic;font-size:0.78rem;color:#555;margin-bottom:0.6rem;padding:0.4rem 0.6rem;background:#f9f9f9;border-radius:4px;}' +
            /* Footer */
            '.footer{margin-top:1.5rem;text-align:center;font-size:0.6rem;color:#aaa;border-top:1px solid #eee;padding-top:0.5rem;}' +
            /* Badge */
            '.badge{display:inline-block;padding:0.15rem 0.5rem;border-radius:12px;font-size:0.65rem;font-weight:700;}' +
            '.badge-green{background:#e6f9ed;color:#1a7f37;}' +
            '.badge-blue{background:#ddf4ff;color:#0969da;}' +
            '.badge-red{background:#ffebe9;color:#cf222e;}' +
            '.badge-yellow{background:#fff8c5;color:#9a6700;}' +
            '.badge-gray{background:#f0f0f0;color:#656d76;}' +
            '@media print{body{padding:0;}.page{padding:1rem;}}';

        var h = '<!DOCTYPE html><html><head><meta charset="utf-8"><title>' + escapeHtml(typeName) + ' ' + serie + '</title>' +
            '<style>' + css + '</style></head><body><div class="page">';

        /* ── Header: Logo + Company + DocBox ── */
        h += '<div class="hdr">';
        if (logo) {
            h += '<img class="hdr-logo" src="' + logo + '" alt="Logo"/>';
        } else {
            h += '<div class="hdr-logo-placeholder">🏢</div>';
        }
        h += '<div class="hdr-company">';
        h += '<h1>' + escapeHtml(company.razonSocial || 'Empresa') + '</h1>';
        if (company.nombreComercial) h += '<p><strong>' + escapeHtml(company.nombreComercial) + '</strong></p>';
        if (company.direccion) h += '<p>' + escapeHtml(company.direccion) + '</p>';
        var ubicacion = [company.distrito, company.provincia, company.departamento].filter(Boolean).join(' - ');
        if (ubicacion) h += '<p>' + escapeHtml(ubicacion) + '</p>';
        if (company.telefono) h += '<p>📞 ' + escapeHtml(company.telefono) + '</p>';
        if (company.emailContacto) h += '<p>📧 ' + escapeHtml(company.emailContacto) + '</p>';
        h += '</div>';
        h += '<div class="hdr-docbox">';
        h += '<div class="doc-type">' + escapeHtml(typeName) + '</div>';
        h += '<div class="doc-ruc">RUC: ' + escapeHtml(company.ruc || '—') + '</div>';
        h += '<div class="doc-num">' + serie + '</div>';
        h += '</div></div>';

        /* ── Client Box ── */
        h += '<div class="client-box">';
        h += '<div class="cb-row"><span class="cb-label">Adquiriente:</span><span class="cb-value">' + escapeHtml(client.denominacion || client.razonSocial || '—') + '</span></div>';
        if (client.tipoDocumento || client.numeroDocumento) {
            var tipoDocLabel = { '6':'RUC', '1':'DNI', '7':'Pasaporte', '4':'CE', 'A':'Cédula Diplomática' }[client.tipoDocumento] || client.tipoDocumento || '';
            h += '<div class="cb-row"><span class="cb-label">' + tipoDocLabel + ':</span><span class="cb-value">' + escapeHtml(client.numeroDocumento || '—') + '</span></div>';
        }
        if (client.direccion) {
            h += '<div class="cb-row"><span class="cb-label">Dirección:</span><span class="cb-value">' + escapeHtml(client.direccion) + '</span></div>';
        }
        h += '<div class="cb-row"><span class="cb-label">Fecha Emisión:</span><span class="cb-value">' + formatDate(doc.fechaEmision) + '</span></div>';
        h += '<div class="cb-row"><span class="cb-label">Moneda:</span><span class="cb-value">' + (doc.moneda || 'PEN') + '</span></div>';
        h += '<div class="cb-row"><span class="cb-label">Forma Pago:</span><span class="cb-value">' + (doc.formaPago || 'CONTADO') + '</span></div>';
        if (doc.fechaVencimiento && doc.formaPago === 'CREDITO') {
            h += '<div class="cb-row"><span class="cb-label">Fecha Venc.:</span><span class="cb-value">' + formatDate(doc.fechaVencimiento) + '</span></div>';
        }
        h += '</div>';

        /* ── Items Table ── */
        if (items.length > 0) {
            h += '<div class="section"><div class="section-title">Detalle</div>';
            h += '<table class="tbl"><thead><tr>' +
                '<th style="width:5%;">#</th><th>Descripción</th><th style="width:8%;">U.M.</th>' +
                '<th class="r" style="width:8%;">Cant.</th><th class="r" style="width:12%;">V.Unit.</th>' +
                '<th class="r" style="width:10%;">IGV</th><th class="r" style="width:12%;">Subtotal</th>' +
                '</tr></thead><tbody>';
            items.forEach(function(it, idx) {
                h += '<tr>' +
                    '<td>' + (idx + 1) + '</td>' +
                    '<td>' + escapeHtml(it.descripcion || '—') + '</td>' +
                    '<td>' + (it.unidadMedida || 'ZZ') + '</td>' +
                    '<td class="r">' + (it.cantidad || 1) + '</td>' +
                    '<td class="r mono">' + money(it.valorUnitario) + '</td>' +
                    '<td class="r mono">' + money(it.igv || it.montoImpuesto) + '</td>' +
                    '<td class="r mono" style="font-weight:600;">' + money(it.subtotal || (it.cantidad * it.valorUnitario)) + '</td>' +
                    '</tr>';
            });
            h += '</tbody></table></div>';
        }

        /* ── Cargos / Descuentos ── */
        var acItems = doc.cargosDescuentos || [];
        if (acItems.length > 0) {
            h += '<div class="section"><div class="section-title">Cargos / Descuentos Globales</div>';
            h += '<table class="tbl"><thead><tr><th>Tipo</th><th>Código</th><th>Descripción</th><th class="r">Factor</th><th class="r">Base</th><th class="r">Monto</th></tr></thead><tbody>';
            acItems.forEach(function(ac) {
                var tipo = ac.chargeIndicator ? 'Cargo' : 'Descuento';
                var color = ac.chargeIndicator ? '#9a6700' : '#cf222e';
                h += '<tr>' +
                    '<td style="color:' + color + ';font-weight:600;">' + tipo + '</td>' +
                    '<td>' + (ac.reasonCode || '—') + '</td>' +
                    '<td>' + escapeHtml(ac.reasonDescription || '—') + '</td>' +
                    '<td class="r">' + money(ac.multiplierFactor) + '</td>' +
                    '<td class="r">' + money(ac.baseAmount) + '</td>' +
                    '<td class="r" style="color:' + color + ';font-weight:600;">' + money(ac.amount) + '</td>' +
                    '</tr>';
            });
            h += '</tbody></table></div>';
        }

        /* ── Cuotas ── */
        var cuotaItems = doc.cuotas || [];
        if (cuotaItems.length > 0) {
            h += '<div class="section"><div class="section-title">Cuotas de Crédito</div>';
            h += '<table class="tbl"><thead><tr><th># Cuota</th><th class="r">Monto</th><th>Fecha Venc.</th><th>Moneda</th></tr></thead><tbody>';
            cuotaItems.forEach(function(c) {
                h += '<tr>' +
                    '<td>' + (c.numeroCuota || '—') + '</td>' +
                    '<td class="r mono">' + money(c.monto) + '</td>' +
                    '<td>' + formatDate(c.fechaVencimiento) + '</td>' +
                    '<td>' + (c.moneda || doc.moneda || 'PEN') + '</td>' +
                    '</tr>';
            });
            h += '</tbody></table></div>';
        }

        /* ── Totals ── */
        h += '<div class="section" style="max-width:320px;margin-left:auto;">';
        var g = totals.gravadas !== undefined ? totals.gravadas : doc.gravadas;
        var e = totals.exoneradas !== undefined ? totals.exoneradas : doc.exoneradas;
        var i = totals.inafectas !== undefined ? totals.inafectas : doc.inafectas;
        var igv = totals.igv !== undefined ? totals.igv : doc.igv;
        var tc = totals.totalCargos !== undefined ? totals.totalCargos : doc.totalCargos;
        var td = totals.totalDescuentos !== undefined ? totals.totalDescuentos : doc.totalDescuentos;
        var total = totals.total !== undefined ? totals.total : doc.total;

        if (g !== undefined) h += '<div class="total-row"><span>Op. Gravadas</span><span>' + monSym + money(g) + '</span></div>';
        if (e && parseFloat(e) > 0) h += '<div class="total-row"><span>Op. Exoneradas</span><span>' + monSym + money(e) + '</span></div>';
        if (i && parseFloat(i) > 0) h += '<div class="total-row"><span>Op. Inafectas</span><span>' + monSym + money(i) + '</span></div>';
        if (igv !== undefined) h += '<div class="total-row"><span>IGV (18%)</span><span>' + monSym + money(igv) + '</span></div>';
        if (tc && parseFloat(tc) > 0) h += '<div class="total-row"><span>+ Cargos</span><span style="color:#9a6700;">' + monSym + money(tc) + '</span></div>';
        if (td && parseFloat(td) > 0) h += '<div class="total-row"><span>− Descuentos</span><span style="color:#cf222e;">−' + monSym + money(td) + '</span></div>';
        h += '<div class="total-row grand"><span>TOTAL A PAGAR</span><span>' + monSym + money(total) + '</span></div>';
        h += '</div>';

        /* ── Leyenda ── */
        if (doc.leyenda) {
            h += '<div class="leyenda">SON: ' + escapeHtml(doc.leyenda) + '</div>';
        }

        /* ── Footer ── */
        h += '<div class="footer">';
        h += 'Representación impresa de ' + escapeHtml(typeName) + ' Electrónica<br/>';
        h += 'Generado por ETIA — ' + new Date().toLocaleString('es-PE');
        h += '</div>';

        h += '</div></body></html>';
        return h;
    });

    /* ════════════════════════════════════════════════
       Public API
       ════════════════════════════════════════════════ */
    return {
        register: register,
        render:   render,
        print:    print,
        /* Expose helpers for custom templates */
        helpers: {
            money:       money,
            formatDate:  formatDate,
            formatNumero:formatNumero,
            escapeHtml:  escapeHtml
        }
    };
})();

