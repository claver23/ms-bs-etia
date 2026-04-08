/* ═══════════════════════════════════════════════
   ❌  ETIA — Error Handler (Global Toast + Screenshot)
   ═══════════════════════════════════════════════ */

(function() {
    'use strict';

    var toastContainer = null;

    function ensureContainer() {
        if (!toastContainer) {
            toastContainer = document.createElement('div');
            toastContainer.className = 'etia-toast-container';
            document.body.appendChild(toastContainer);
        }
        return toastContainer;
    }

    /**
     * Show an error toast with details and screenshot button.
     * @param {Object} detail - { status, message, url, body }
     */
    ETIA.showError = function(detail) {
        var mode = (ETIA.config && ETIA.config.errorDisplayMode) || 'toast';

        if (mode === 'toast' || mode === 'both') {
            _showErrorToast(detail);
        }
        if (mode === 'banner' || mode === 'both') {
            _showPersistentBanner(detail);
        }
    };

    function _showErrorToast(detail) {
        var container = ensureContainer();
        var toast = document.createElement('div');
        toast.className = 'etia-toast etia-toast-error etia-toast-enter';

        var statusText = detail.status ? ('Error ' + detail.status) : 'Error';
        var messageText = detail.message || 'Ha ocurrido un error inesperado';
        var bodyText = '';
        if (detail.body) {
            try {
                var parsed = typeof detail.body === 'string' ? JSON.parse(detail.body) : detail.body;
                bodyText = parsed.message || parsed.error || JSON.stringify(parsed, null, 2);
            } catch(e) {
                bodyText = String(detail.body);
            }
        }

        toast.innerHTML =
            '<div class="etia-toast-header">' +
                '<span class="etia-toast-icon">❌</span>' +
                '<strong class="etia-toast-title">' + statusText + '</strong>' +
                '<button class="etia-toast-close" onclick="this.closest(\'.etia-toast\').remove()">✕</button>' +
            '</div>' +
            '<div class="etia-toast-body">' +
                '<p>' + escapeHtml(messageText) + '</p>' +
                (bodyText ? '<pre class="etia-toast-detail">' + escapeHtml(bodyText).substring(0, 300) + '</pre>' : '') +
                (detail.url ? '<span class="etia-toast-url">' + escapeHtml(detail.url) + '</span>' : '') +
            '</div>' +
            '<div class="etia-toast-actions">' +
                '<div class="etia-toast-note">' +
                    '💡 <strong>Estimado usuario:</strong> Tome una captura de pantalla rápidamente y envíela a su proveedor de TI. ¡Ellos se encargarán!' +
                '</div>' +
                '<div class="etia-toast-btn-row">' +
                    '<button class="etia-toast-btn etia-toast-screenshot-btn" onclick="ETIA.screenshotError(this)">📸 Capturar pantalla</button>' +
                    '<button class="etia-toast-btn etia-toast-email-btn" onclick="ETIA.emailError(this)">📧 Enviar por email</button>' +
                '</div>' +
            '</div>' +
            '<div class="etia-toast-progress"><div class="etia-toast-progress-bar"></div></div>';

        container.appendChild(toast);

        /* Auto-dismiss — configurable duration */
        var duration = (ETIA.config && ETIA.config.toastDurationError)
            ? parseInt(ETIA.config.toastDurationError) * 1000
            : 12000;
        if (duration > 0) {
            setTimeout(function() {
                toast.classList.add('etia-toast-exit');
                setTimeout(function() { if (toast.parentNode) toast.remove(); }, 400);
            }, duration);
        }
    }

    function _showPersistentBanner(detail) {
        var statusText = detail.status ? ('Error ' + detail.status) : 'Error';
        var messageText = detail.message || 'Ha ocurrido un error inesperado';
        var bodyText = '';
        if (detail.body) {
            try {
                var parsed = typeof detail.body === 'string' ? JSON.parse(detail.body) : detail.body;
                bodyText = parsed.message || parsed.error || JSON.stringify(parsed, null, 2);
            } catch(e) {
                bodyText = String(detail.body);
            }
        }

        /* Remove existing banner if any */
        var existing = document.getElementById('etia-persistent-error-banner');
        if (existing) existing.remove();

        var banner = document.createElement('div');
        banner.id = 'etia-persistent-error-banner';
        banner.style.cssText = 'position:fixed;top:0;left:0;right:0;z-index:200;background:rgba(248,81,73,0.12);border-bottom:2px solid var(--danger, #f85149);padding:0.8rem 1.5rem;display:flex;align-items:flex-start;gap:0.8rem;animation:screenIn 0.3s ease;';
        banner.innerHTML =
            '<span style="font-size:1.2rem;flex-shrink:0;">❌</span>' +
            '<div style="flex:1;min-width:0;">' +
                '<strong style="color:var(--danger, #f85149);font-size:0.85rem;">' + escapeHtml(statusText) + '</strong>' +
                '<p style="margin:0.2rem 0 0;font-size:0.8rem;color:var(--text-secondary, #8b949e);">' + escapeHtml(messageText) + '</p>' +
                (bodyText ? '<pre style="margin:0.3rem 0 0;font-size:0.72rem;color:var(--text-muted, #484f58);white-space:pre-wrap;max-height:80px;overflow:auto;">' + escapeHtml(bodyText).substring(0, 300) + '</pre>' : '') +
            '</div>' +
            '<button onclick="this.parentElement.remove()" style="background:none;border:none;color:var(--text-secondary, #8b949e);font-size:1.2rem;cursor:pointer;padding:0;line-height:1;">✕</button>';

        /* Insert at top of body */
        document.body.insertBefore(banner, document.body.firstChild);
    }

    /**
     * Show a success toast (brief).
     */
    ETIA.showSuccess = function(message) {
        var container = ensureContainer();
        var toast = document.createElement('div');
        toast.className = 'etia-toast etia-toast-success etia-toast-enter';
        toast.innerHTML =
            '<div class="etia-toast-header">' +
                '<span class="etia-toast-icon">✅</span>' +
                '<strong class="etia-toast-title">' + escapeHtml(message) + '</strong>' +
                '<button class="etia-toast-close" onclick="this.closest(\'.etia-toast\').remove()">✕</button>' +
            '</div>';
        container.appendChild(toast);
        var successDuration = (ETIA.config && ETIA.config.toastDurationSuccess)
            ? parseInt(ETIA.config.toastDurationSuccess) * 1000
            : 4000;
        setTimeout(function() {
            toast.classList.add('etia-toast-exit');
            setTimeout(function() { if (toast.parentNode) toast.remove(); }, 400);
        }, successDuration);
    };

    /**
     * Take screenshot and download as PNG, then offer email option.
     */
    ETIA.screenshotError = function(btn) {
        if (typeof html2canvas === 'undefined') {
            alert('html2canvas no disponible');
            return;
        }
        btn.textContent = '⏳ Capturando...';
        btn.disabled = true;
        html2canvas(document.body, { scale: 1, useCORS: true, logging: false }).then(function(canvas) {
            var link = document.createElement('a');
            var filename = 'ETIA-error-' + new Date().toISOString().replace(/[:.]/g, '-') + '.png';
            link.download = filename;
            link.href = canvas.toDataURL('image/png');
            link.click();
            btn.textContent = '✅ Descargado';
            btn.title = 'Captura guardada: ' + filename;
            /* Store last screenshot filename for email reference */
            ETIA._lastScreenshotFilename = filename;
            setTimeout(function() {
                btn.textContent = '📸 Capturar de nuevo';
                btn.disabled = false;
            }, 3000);
        }).catch(function() {
            btn.textContent = '❌ Error al capturar';
            btn.disabled = false;
        });
    };

    /**
     * Open email client with error details pre-filled.
     */
    ETIA.emailError = function(btn) {
        /* Find the toast context */
        var toast = btn.closest('.etia-toast');
        var statusEl = toast ? toast.querySelector('.etia-toast-title') : null;
        var messageEl = toast ? toast.querySelector('.etia-toast-body p') : null;
        var urlEl = toast ? toast.querySelector('.etia-toast-url') : null;
        var detailEl = toast ? toast.querySelector('.etia-toast-detail') : null;

        var status = statusEl ? statusEl.textContent : 'Error desconocido';
        var message = messageEl ? messageEl.textContent : '';
        var url = urlEl ? urlEl.textContent : window.location.href;
        var detail = detailEl ? detailEl.textContent : '';

        var subject = encodeURIComponent('[ETIA] Reporte de Error — ' + status);
        var body = encodeURIComponent(
            'Hola equipo de soporte,\n\n' +
            'Se presentó el siguiente error en ETIA:\n\n' +
            '━━━━━━━━━━━━━━━━━━━━━━━━━━━\n' +
            '🔴 Estado: ' + status + '\n' +
            '📝 Mensaje: ' + message + '\n' +
            '🔗 URL: ' + url + '\n' +
            (detail ? '📋 Detalle: ' + detail.substring(0, 500) + '\n' : '') +
            '🕐 Fecha/Hora: ' + new Date().toLocaleString('es-PE') + '\n' +
            '━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n' +
            (ETIA._lastScreenshotFilename
                ? '📸 IMPORTANTE: Adjunte la captura de pantalla descargada (' + ETIA._lastScreenshotFilename + ') a este email.\n\n'
                : '💡 TIP: Use primero el botón "📸 Capturar pantalla" y adjunte la imagen descargada a este email.\n\n') +
            'Gracias,\n' +
            (ETIA.company ? ETIA.company.razonSocial || '' : 'Usuario ETIA')
        );

        window.open('mailto:?subject=' + subject + '&body=' + body, '_self');
        btn.textContent = '✅ Email abierto';
        setTimeout(function() { btn.textContent = '📧 Enviar por email'; }, 2000);
    };

    function escapeHtml(str) {
        var div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }
})();

