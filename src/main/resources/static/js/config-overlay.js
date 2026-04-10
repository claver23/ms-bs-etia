/* ═══════════════════════════════════════════════
   ⚙️  ETIA — Config Overlay
   ═══════════════════════════════════════════════ */

var cfgMap = null;
var cfgMapMarker = null;

function openConfigOverlay() {
    document.getElementById('cfg-overlay').classList.add('show');
    loadConfigValues();
    switchConfigTab('empresa');
}

function closeConfigOverlay() {
    document.getElementById('cfg-overlay').classList.remove('show');
}

function switchConfigTab(tab) {
    /* Sidebar items */
    document.querySelectorAll('.cfg-sidebar-item').forEach(function(el) {
        el.classList.toggle('active', el.dataset.tab === tab);
    });
    /* Panels */
    document.querySelectorAll('.cfg-panel').forEach(function(el) {
        el.classList.toggle('active', el.id === 'cfg-panel-' + tab);
    });
    /* Init map when Sede tab is shown (Leaflet needs visible container) */
    if (tab === 'sede') {
        setTimeout(initSedeMap, 150);
    }
    /* Populate series panel with active sede info */
    if (tab === 'series') {
        _renderSeriesPanel();
    }
}

/* ─── Render active sede + series in config panel ─── */
function _renderSeriesPanel() {
    var sede = ETIA.activeSede;
    var heroEl = document.getElementById('cfg-sede-hero');
    var tableEl = document.getElementById('cfg-sede-series-table');
    var manualEl = document.getElementById('cfg-series-manual');

    if (!sede) {
        /* No active sede — show manual inputs */
        if (heroEl) heroEl.style.display = 'none';
        if (tableEl) tableEl.style.display = 'none';
        if (manualEl) manualEl.style.display = '';
        return;
    }

    /* Show hero */
    if (heroEl) heroEl.style.display = '';
    if (manualEl) manualEl.style.display = 'none';

    var nameEl = document.getElementById('cfg-sede-hero-name');
    var codeEl = document.getElementById('cfg-sede-hero-code');
    var addrEl = document.getElementById('cfg-sede-hero-addr');
    if (nameEl) nameEl.textContent = sede.nombre || '—';
    if (codeEl) codeEl.textContent = sede.codigo || '—';
    if (addrEl) addrEl.textContent = (sede.direccion || '') + ' · ' + (sede.distrito || '') + ' · Ubigeo ' + (sede.codigoUbigeo || '');

    /* Show series table */
    if (tableEl) {
        if (sede.series && sede.series.length > 0) {
            var html = '<div class="tbl-wrap"><table class="tbl"><thead><tr>' +
                '<th>Serie</th><th>Tipo Documento</th><th>Descripción</th><th>Default</th><th>Activa</th>' +
                '</tr></thead><tbody>';
            sede.series.forEach(function(sr) {
                var tdoc = {FACTURA:'🧾 Factura', BOLETA:'🎫 Boleta', NOTA_CREDITO:'📉 Nota Crédito', NOTA_DEBITO:'📈 Nota Débito'}[sr.tipoDocumento] || sr.tipoDocumento;
                html += '<tr>' +
                    '<td><span class="badge badge-blue" style="font-family:var(--font-mono);font-weight:700;">' + sr.serie + '</span></td>' +
                    '<td>' + tdoc + '</td>' +
                    '<td style="font-size:0.75rem;color:var(--text-secondary);">' + (sr.descripcion || '—') + '</td>' +
                    '<td>' + (sr.esDefault ? '✅' : '—') + '</td>' +
                    '<td>' + (sr.activa ? '✅' : '❌') + '</td>' +
                '</tr>';
            });
            html += '</tbody></table></div>';
            tableEl.innerHTML = html;
            tableEl.style.display = '';
        } else {
            tableEl.innerHTML = '<p class="pm-empty" style="margin:1rem 0;">Esta sede no tiene series asignadas. Ve a <strong>Mis Sedes</strong> para generarlas.</p>';
            tableEl.style.display = '';
        }
    }
}

/* ─── Load current config into form ─── */
function loadConfigValues() {
    var c = ETIA.config;

    /* ── Populate company info from ETIA.company (loaded by ETIA.init) ── */
    var co = ETIA.company;
    if (co) {
        var el;
        el = document.getElementById('cfg-co-razon');    if (el) el.textContent = co.razonSocial || '';
        el = document.getElementById('cfg-co-ruc');       if (el) el.textContent = 'RUC: ' + (co.ruc || '---');
        el = document.getElementById('cfg-co-nombre');    if (el) el.value = co.nombreComercial || '';
        el = document.getElementById('cfg-co-regimen');   if (el) el.value = co.regimen || '';
        el = document.getElementById('cfg-co-direccion'); if (el) el.value = co.direccion || '';
        el = document.getElementById('cfg-co-email');     if (el) el.value = co.emailContacto || '';
        el = document.getElementById('cfg-co-telefono');  if (el) el.value = co.telefono || '';

        /* Dynamic estado badge */
        var badge = document.getElementById('cfg-co-badge');
        if (badge) {
            var estado = (co.estado || '').toUpperCase();
            if (estado === 'ACTIVO') {
                badge.textContent = '🟢 Activa';
                badge.style.color = 'var(--accent)';
            } else if (estado === 'INACTIVO') {
                badge.textContent = '🔴 Inactiva';
                badge.style.color = 'var(--danger)';
            } else {
                badge.textContent = '⚪ Sin verificar';
                badge.style.color = 'var(--text-muted)';
            }
        }

        /* Logo */
        var logoImg = document.getElementById('cfg-co-logo-img');
        var logoPlaceholder = document.getElementById('cfg-co-logo-placeholder');
        var logoWrap = document.getElementById('cfg-co-logo-wrap');
        var logoFmt = (co.logoFormat || 'png').toLowerCase();
        if (co.tieneLogo && logoImg) {
            logoImg.src = ApiClient.getLogoUrl(ETIA.companyId) + '&t=' + Date.now();
            logoImg.style.display = 'block';
            logoImg.style.objectFit = (logoFmt === 'svg') ? 'scale-down' : 'contain';
            if (logoPlaceholder) logoPlaceholder.style.display = 'none';
            if (logoWrap) logoWrap.style.borderStyle = 'solid';
        } else if (logoImg) {
            logoImg.style.display = 'none';
            if (logoPlaceholder) logoPlaceholder.style.display = '';
            if (logoWrap) logoWrap.style.borderStyle = 'dashed';
        }

        /* Pre-select logo type — removed (auto-detect now) */

        /* Credenciales SUNAT */
        el = document.getElementById('cfg-cred-usuario-sol'); if (el) el.value = co.usuarioSol || '';
        el = document.getElementById('cfg-cred-clave-sol');   if (el) el.value = co.tieneClaveSol ? '••••••••' : '';
        el = document.getElementById('cfg-cred-cert-password'); if (el) el.value = co.tieneCertificado ? '••••••••' : '';

        /* Status indicators */
        var solInd = document.getElementById('cfg-cred-sol-indicator');
        if (solInd) {
            if (co.tieneUsuarioSol && co.tieneClaveSol) {
                solInd.innerHTML = '<span style="color:var(--accent);">✅ Configurado</span>';
            } else {
                solInd.innerHTML = '<span style="color:var(--danger);">❌ No configurado</span>';
            }
        }
        var certInd = document.getElementById('cfg-cred-cert-indicator');
        if (certInd) {
            if (co.tieneCertificado) {
                certInd.innerHTML = '<span style="color:var(--accent);">✅ Configurado</span>';
            } else {
                certInd.innerHTML = '<span style="color:var(--danger);">❌ No configurado</span>';
            }
        }
    }

    /* Preferencias */
    var currSel = document.getElementById('cfg-currency');
    if (currSel) currSel.value = c.currency;

    var itemTypeSel = document.getElementById('cfg-item-type');
    if (itemTypeSel) itemTypeSel.value = c.defaultItemType;

    /* Theme toggle */
    document.querySelectorAll('.cfg-theme-option').forEach(function(el) {
        el.classList.toggle('active', el.dataset.theme === c.theme);
    });

    /* Tipo de Cambio fuente */
    var tcFuenteSel = document.getElementById('cfg-tc-fuente-select');
    if (tcFuenteSel) tcFuenteSel.value = c.tipoCambioFuente || 'SBS';

    /* Emisión en Envío */
    var emisionChk = document.getElementById('cfg-emision-envio');
    if (emisionChk) {
        emisionChk.checked = c.emisionEnEnvio !== false;
        _updateEmisionEnvioUI(emisionChk.checked);
        emisionChk.addEventListener('change', function() {
            _updateEmisionEnvioUI(emisionChk.checked);
        });
    }

    /* Cuotas de Crédito */
    var defCuotasEl = document.getElementById('cfg-default-cuotas');
    if (defCuotasEl) defCuotasEl.value = c.defaultCuotas || '1';
    var cuotaDistEl = document.getElementById('cfg-cuota-distribucion');
    if (cuotaDistEl) cuotaDistEl.value = c.cuotaDistribucion || 'FIJA_30D';

    /* Document Format */
    var docFmtEl = document.getElementById('cfg-document-format');
    if (docFmtEl) docFmtEl.value = c.documentFormat || 'standard';

    /* Series */
    var serieFac = document.getElementById('cfg-serie-fac');
    var serieBol = document.getElementById('cfg-serie-bol');
    var serieNC  = document.getElementById('cfg-serie-nc');
    var serieND  = document.getElementById('cfg-serie-nd');
    if (serieFac) serieFac.value = c.serieFac;
    if (serieBol) serieBol.value = c.serieBol;
    if (serieNC)  serieNC.value  = c.serieNC;
    if (serieND)  serieND.value  = c.serieND;

    /* Sede */
    var sedeDir = document.getElementById('cfg-sede-dir');
    if (sedeDir && c.sedeDireccion) sedeDir.value = c.sedeDireccion;
    else if (sedeDir && co && co.direccion) sedeDir.value = co.direccion;

    var sedeLat = document.getElementById('cfg-sede-lat');
    var sedeLng = document.getElementById('cfg-sede-lng');
    if (sedeLat && c.sedeLat) sedeLat.value = c.sedeLat;
    if (sedeLng && c.sedeLng) sedeLng.value = c.sedeLng;

    /* ── Load ubigeos dynamically from API ── */
    var sedeSel = document.getElementById('cfg-sede-select');
    if (sedeSel && sedeSel.options.length <= 1) {
        ApiClient.listUbigeos().then(function(ubigeos) {
            ubigeos.forEach(function(u) {
                var opt = document.createElement('option');
                opt.value = u.codigo;
                opt.textContent = u.distrito + ' (' + u.codigo + ')';
                sedeSel.appendChild(opt);
            });
            if (c.sede) sedeSel.value = c.sede;
        }).catch(function(err) { console.error('Error cargando ubigeos:', err); });
    } else if (sedeSel && c.sede) {
        sedeSel.value = c.sede;
    }

    /* Reset map so it re-inits with fresh data when Sede tab opens */
    cfgMap = null;
    cfgMapMarker = null;

    /* Toast / Error config (PASO 8) */
    var toastErrDur = document.getElementById('cfg-toast-error-duration');
    if (toastErrDur) toastErrDur.value = c.toastDurationError || 12;
    var toastSuccDur = document.getElementById('cfg-toast-success-duration');
    if (toastSuccDur) toastSuccDur.value = c.toastDurationSuccess || 4;
    var errMode = document.getElementById('cfg-error-display-mode');
    if (errMode) errMode.value = c.errorDisplayMode || 'toast';

    /* Notification preferences (PASO 9) */
    _loadNotifPreferences();
}

/* ─── Theme selection ─── */
function selectTheme(theme) {
    ETIA.saveConfig('theme', theme);
    if (theme === 'light') {
        document.documentElement.setAttribute('data-theme', 'light');
    } else {
        document.documentElement.removeAttribute('data-theme');
    }
    document.querySelectorAll('.cfg-theme-option').forEach(function(el) {
        el.classList.toggle('active', el.dataset.theme === theme);
    });
}

/* ─── Emisión en Envío UI ─── */
function _updateEmisionEnvioUI(checked) {
    var label = document.getElementById('cfg-emision-envio-label');
    var hint = document.getElementById('cfg-emision-envio-hint');
    var warn = document.getElementById('cfg-emision-envio-warn');
    if (label) label.textContent = checked ? 'Activado' : 'Desactivado';
    if (label) label.style.color = checked ? 'var(--accent)' : 'var(--danger)';
    if (hint) hint.textContent = checked
        ? 'Al crear un documento, se generará el XML UBL 2.1 automáticamente y se notificará la emisión.'
        : 'Los documentos se guardarán como BORRADOR. Deberás emitirlos manualmente.';
    if (warn) warn.style.display = checked ? 'none' : 'block';
}

/* ─── Save all config ─── */
function saveConfig() {
    /* Preferencias */
    var currSel = document.getElementById('cfg-currency');
    if (currSel) ETIA.saveConfig('currency', currSel.value);

    var itemTypeSel = document.getElementById('cfg-item-type');
    if (itemTypeSel) ETIA.saveConfig('defaultItemType', itemTypeSel.value);

    /* Tipo de Cambio fuente */
    var tcFuenteSel = document.getElementById('cfg-tc-fuente-select');
    if (tcFuenteSel) ETIA.saveConfig('tipoCambioFuente', tcFuenteSel.value);

    /* Emisión en Envío */
    var emisionChk = document.getElementById('cfg-emision-envio');
    if (emisionChk) ETIA.saveConfig('emisionEnEnvio', emisionChk.checked);

    /* Cuotas de Crédito */
    var defCuotasEl = document.getElementById('cfg-default-cuotas');
    if (defCuotasEl) ETIA.saveConfig('defaultCuotas', defCuotasEl.value);
    var cuotaDistEl = document.getElementById('cfg-cuota-distribucion');
    if (cuotaDistEl) ETIA.saveConfig('cuotaDistribucion', cuotaDistEl.value);

    /* Document Format */
    var docFmtEl = document.getElementById('cfg-document-format');
    if (docFmtEl) ETIA.saveConfig('documentFormat', docFmtEl.value);

    /* Toast / Error config (PASO 8) */
    var toastErrDur = document.getElementById('cfg-toast-error-duration');
    if (toastErrDur) ETIA.saveConfig('toastDurationError', parseInt(toastErrDur.value) || 12);
    var toastSuccDur = document.getElementById('cfg-toast-success-duration');
    if (toastSuccDur) ETIA.saveConfig('toastDurationSuccess', parseInt(toastSuccDur.value) || 4);
    var errMode = document.getElementById('cfg-error-display-mode');
    if (errMode) ETIA.saveConfig('errorDisplayMode', errMode.value || 'toast');

    /* Sede */
    var serieFac = document.getElementById('cfg-serie-fac');
    var serieBol = document.getElementById('cfg-serie-bol');
    var serieNC  = document.getElementById('cfg-serie-nc');
    var serieND  = document.getElementById('cfg-serie-nd');
    if (serieFac) ETIA.saveConfig('serieFac', serieFac.value);
    if (serieBol) ETIA.saveConfig('serieBol', serieBol.value);
    if (serieNC)  ETIA.saveConfig('serieNC',  serieNC.value);
    if (serieND)  ETIA.saveConfig('serieND',  serieND.value);

    /* Sede */
    var sedeSel = document.getElementById('cfg-sede-select');
    if (sedeSel) ETIA.saveConfig('sede', sedeSel.value);
    var sedeDir = document.getElementById('cfg-sede-dir');
    if (sedeDir) ETIA.saveConfig('sedeDireccion', sedeDir.value);

    /* Sede coords */
    var sedeLat = document.getElementById('cfg-sede-lat');
    var sedeLng = document.getElementById('cfg-sede-lng');
    if (sedeLat && sedeLat.value) ETIA.saveConfig('sedeLat', sedeLat.value);
    if (sedeLng && sedeLng.value) ETIA.saveConfig('sedeLng', sedeLng.value);

    closeConfigOverlay();

    /* ── Sync preferences to backend (write-through) ── */
    if (ETIA.companyId) {
        var prefsToSync = ['currency', 'defaultItemType', 'tipoCambioFuente', 'emisionEnEnvio',
                           'defaultCuotas', 'cuotaDistribucion', 'documentFormat',
                           'toastDurationError', 'toastDurationSuccess', 'errorDisplayMode'];
        prefsToSync.forEach(function(key) {
            var val = String(ETIA.config[key]);
            ApiClient.savePreference(ETIA.companyId, key, val).catch(function(err) {
                console.warn('Error syncing preference ' + key + ':', err);
            });
        });
    }

    /* Show success toast */
    showConfigToast('✅ Configuración guardada');
}

/* ─── Toast notification ─── */
function showConfigToast(msg) {
    var toast = document.createElement('div');
    toast.style.cssText = 'position:fixed;bottom:2rem;right:2rem;background:var(--accent);color:var(--bg-primary);padding:0.7rem 1.4rem;border-radius:var(--radius-sm);font-weight:600;font-size:0.88rem;z-index:300;animation:screenIn 0.3s ease;box-shadow:0 4px 20px rgba(0,255,136,0.3);';
    toast.textContent = msg;
    document.body.appendChild(toast);
    setTimeout(function(){ toast.style.opacity = '0'; toast.style.transition = '0.4s'; }, 2000);
    setTimeout(function(){ toast.remove(); }, 2500);
}

/* ─── Save company contact (email + telefono) ─── */
function saveCompanyContact() {
    var emailEl = document.getElementById('cfg-co-email');
    var telEl = document.getElementById('cfg-co-telefono');
    var payload = {};
    if (emailEl) payload.emailContacto = emailEl.value.trim();
    if (telEl) payload.telefono = telEl.value.trim();

    /* Client-side validation */
    if (payload.emailContacto && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(payload.emailContacto)) {
        showConfigToast('⚠️ El formato del email no es válido');
        if (emailEl) emailEl.focus();
        return;
    }
    if (payload.telefono && payload.telefono.length < 7) {
        showConfigToast('⚠️ El teléfono debe tener al menos 7 caracteres');
        if (telEl) telEl.focus();
        return;
    }

    ApiClient.updateCompany(ETIA.companyId, payload).then(function(res) {
        /* Update local state */
        if (ETIA.company) {
            if (payload.emailContacto !== undefined) ETIA.company.emailContacto = payload.emailContacto;
            if (payload.telefono !== undefined) ETIA.company.telefono = payload.telefono;
        }
        showConfigToast('✅ Datos de contacto guardados');
    }).catch(function(err) {
        showConfigToast('❌ Error al guardar: ' + (err.message || 'desconocido'));
    });
}

/* ─── Logo type selector ─── */
var _logoTypeAcceptMap = {
    png: '.png',
    jpg: '.jpg,.jpeg',
    svg: '.svg'
};

function onLogoTypeChange(type) {
    var input = document.getElementById('cfg-logo-file-input');
    if (input) {
        input.accept = _logoTypeAcceptMap[type] || '.png';
        input.value = ''; // reset file selection
    }
}

/* ─── Magic-byte validation ─── */
function _validateLogoMagicBytes(file, expectedType) {
    return new Promise(function(resolve) {
        if (expectedType === 'svg') {
            /* SVG: read first 1KB as text, check for <svg */
            var textReader = new FileReader();
            textReader.onload = function() {
                var text = (textReader.result || '').toLowerCase();
                resolve(text.indexOf('<svg') >= 0);
            };
            textReader.onerror = function() { resolve(false); };
            textReader.readAsText(file.slice(0, 1024));
            return;
        }
        /* PNG/JPG: read first 4 bytes */
        var reader = new FileReader();
        reader.onload = function() {
            var arr = new Uint8Array(reader.result);
            if (expectedType === 'png') {
                // PNG magic: 89 50 4E 47
                resolve(arr.length >= 4 && arr[0] === 0x89 && arr[1] === 0x50 && arr[2] === 0x4E && arr[3] === 0x47);
            } else if (expectedType === 'jpg') {
                // JPEG magic: FF D8 FF
                resolve(arr.length >= 3 && arr[0] === 0xFF && arr[1] === 0xD8 && arr[2] === 0xFF);
            } else {
                resolve(true);
            }
        };
        reader.onerror = function() { resolve(false); };
        reader.readAsArrayBuffer(file.slice(0, 8));
    });
}

/* ─── Logo upload handler (auto-detect type) ─── */
function _detectImageType(file) {
    return new Promise(function(resolve) {
        var tr = new FileReader();
        tr.onload = function() {
            if ((tr.result || '').toLowerCase().indexOf('<svg') >= 0) { resolve('svg'); return; }
            var br = new FileReader();
            br.onload = function() {
                var a = new Uint8Array(br.result);
                if (a.length >= 4 && a[0]===0x89 && a[1]===0x50 && a[2]===0x4E && a[3]===0x47) { resolve('png'); return; }
                if (a.length >= 3 && a[0]===0xFF && a[1]===0xD8 && a[2]===0xFF) { resolve('jpg'); return; }
                resolve(null);
            };
            br.onerror = function() { resolve(null); };
            br.readAsArrayBuffer(file.slice(0, 8));
        };
        tr.onerror = function() { resolve(null); };
        tr.readAsText(file.slice(0, 1024));
    });
}

function handleLogoFileSelect(input) {
    if (!input.files || input.files.length === 0) return;
    var file = input.files[0];

    if (file.size > 10 * 1024 * 1024) {
        showConfigToast('⚠️ El logo excede el tamaño máximo de 10 MB');
        return;
    }

    var resultDiv = document.getElementById('cfg-logo-upload-result');

    _detectImageType(file).then(function(detectedType) {
        if (!detectedType) {
            showConfigToast('⚠️ Formato no reconocido. Usa PNG, JPG o SVG.');
            input.value = '';
            return;
        }

        /* SVG: skip dimension validation, upload directly */
        if (detectedType === 'svg') {
            _doLogoUpload(file, resultDiv, detectedType);
            return;
        }

        /* PNG/JPG: validate dimensions before upload */
        _validateLogoDimensions(file).then(function(dimResult) {
            if (dimResult.tooSmall) {
                showConfigToast('⚠️ Imagen muy pequeña (' + dimResult.width + '×' + dimResult.height + '). Mínimo: 50×50');
                return;
            }
            if (dimResult.tooLarge) {
                if (confirm('⚠️ Imagen muy grande (' + dimResult.width + '×' + dimResult.height + '). ¿Deseas redimensionar automáticamente a máx 2000px?')) {
                    _resizeLogoFile(file, 2000, 2000).then(function(resized) {
                        _doLogoUpload(resized, resultDiv, detectedType);
                    });
                } else {
                    _doLogoUpload(file, resultDiv, detectedType);
                }
                return;
            }
            _doLogoUpload(file, resultDiv, detectedType);
        });
    });
}

/* Legacy stub — no-op since selector was removed */
function onLogoTypeChange() {}

function _validateLogoDimensions(file) {
    return new Promise(function(resolve) {
        var img = new Image();
        img.onload = function() {
            var w = img.naturalWidth, h = img.naturalHeight;
            resolve({
                valid: w >= 50 && h >= 50 && w <= 2000 && h <= 2000,
                tooSmall: w < 50 || h < 50,
                tooLarge: w > 2000 || h > 2000,
                width: w, height: h
            });
            URL.revokeObjectURL(img.src);
        };
        img.onerror = function() { resolve({ valid: true, width: 0, height: 0 }); };
        img.src = URL.createObjectURL(file);
    });
}

function _resizeLogoFile(file, maxW, maxH) {
    return new Promise(function(resolve) {
        var img = new Image();
        img.onload = function() {
            var ratio = Math.min(maxW / img.naturalWidth, maxH / img.naturalHeight);
            var canvas = document.createElement('canvas');
            canvas.width = Math.round(img.naturalWidth * ratio);
            canvas.height = Math.round(img.naturalHeight * ratio);
            var ctx = canvas.getContext('2d');
            ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
            canvas.toBlob(function(blob) {
                var resized = new File([blob], file.name, { type: file.type });
                resolve(resized);
            }, file.type);
            URL.revokeObjectURL(img.src);
        };
        img.src = URL.createObjectURL(file);
    });
}

function _doLogoUpload(file, resultDiv, logoType) {
    /* Upload immediately */
    ApiClient.uploadLogo(ETIA.companyId, file).then(function(res) {
        /* Update preview with format-aware rendering */
        var logoImg = document.getElementById('cfg-co-logo-img');
        var logoPlaceholder = document.getElementById('cfg-co-logo-placeholder');
        var logoWrap = document.getElementById('cfg-co-logo-wrap');
        if (logoImg) {
            logoImg.src = URL.createObjectURL(file);
            logoImg.style.display = 'block';
            /* SVG: render as scalable vector; rasters: contain */
            logoImg.style.objectFit = (logoType === 'svg') ? 'scale-down' : 'contain';
        }
        if (logoPlaceholder) logoPlaceholder.style.display = 'none';
        if (logoWrap) logoWrap.style.borderStyle = 'solid';
        if (ETIA.company) {
            ETIA.company.tieneLogo = true;
            ETIA.company.logoFormat = logoType || 'png';
        }

        /* Invalidate logo base64 cache so PDF templates re-fetch the new logo */
        if (ETIA.invalidateLogoCache) ETIA.invalidateLogoCache();

        if (resultDiv) {
            resultDiv.style.display = 'block';
            resultDiv.style.background = 'rgba(0,255,136,0.06)';
            resultDiv.style.border = '1px solid rgba(0,255,136,0.2)';
            resultDiv.style.color = 'var(--accent)';
            resultDiv.textContent = '✅ Logo subido exitosamente';
            setTimeout(function() { resultDiv.style.display = 'none'; }, 3000);
        }
        showConfigToast('✅ Logo de empresa actualizado');
    }).catch(function(err) {
        if (resultDiv) {
            resultDiv.style.display = 'block';
            resultDiv.style.background = 'rgba(248,81,73,0.06)';
            resultDiv.style.border = '1px solid rgba(248,81,73,0.2)';
            resultDiv.style.color = 'var(--danger)';
            resultDiv.textContent = '❌ Error: ' + (err.message || 'desconocido');
        }
        showConfigToast('❌ Error al subir logo');
    });
}

/* ─── Save SUNAT credentials ─── */
function saveCredentials() {
    var userEl = document.getElementById('cfg-cred-usuario-sol');
    var passEl = document.getElementById('cfg-cred-clave-sol');
    var certPassEl = document.getElementById('cfg-cred-cert-password');

    var payload = {};
    if (userEl && userEl.value.trim()) payload.usuarioSol = userEl.value.trim();
    if (passEl && passEl.value && passEl.value !== '••••••••') payload.claveSol = passEl.value;
    if (certPassEl && certPassEl.value && certPassEl.value !== '••••••••') payload.certificadoPassword = certPassEl.value;

    if (Object.keys(payload).length === 0) {
        showConfigToast('⚠️ No hay cambios para guardar');
        return;
    }

    ApiClient.updateCompany(ETIA.companyId, payload).then(function(res) {
        /* Update local state */
        if (ETIA.company) {
            if (payload.usuarioSol) ETIA.company.usuarioSol = payload.usuarioSol;
            if (payload.claveSol) ETIA.company.claveSol = '***';
            if (payload.certificadoPassword) ETIA.company.certificadoPassword = '***';
        }
        /* Update indicators */
        var solInd = document.getElementById('cfg-cred-sol-indicator');
        if (solInd && payload.usuarioSol) {
            solInd.innerHTML = '<span style="color:var(--accent);">✅ Configurado</span>';
        }
        /* Reset password fields to masked */
        if (passEl && payload.claveSol) passEl.value = '••••••••';
        if (certPassEl && payload.certificadoPassword) certPassEl.value = '••••••••';
        showConfigToast('✅ Credenciales SUNAT guardadas');
    }).catch(function(err) {
        showConfigToast('❌ Error al guardar credenciales: ' + (err.message || 'desconocido'));
    });
}

/* ─── Test SUNAT connection (via backend API) ─── */
function testSunatConnection() {
    var btn = document.getElementById('cfg-cred-test-btn');
    var resultDiv = document.getElementById('cfg-cred-test-result');
    if (btn) { btn.disabled = true; btn.textContent = '⏳ Conectando...'; }
    if (resultDiv) { resultDiv.style.display = 'none'; }

    ApiClient.probarConexionSunat(ETIA.companyId).then(function(res) {
        if (btn) { btn.disabled = false; btn.textContent = '🔗 Probar Conexión SUNAT'; }
        if (resultDiv) {
            resultDiv.style.display = 'block';
            if (res.exitoso) {
                resultDiv.style.background = 'rgba(0,255,136,0.06)';
                resultDiv.style.border = '1px solid rgba(0,255,136,0.2)';
                resultDiv.style.color = 'var(--accent)';
            } else {
                resultDiv.style.background = 'rgba(248,81,73,0.06)';
                resultDiv.style.border = '1px solid rgba(248,81,73,0.2)';
                resultDiv.style.color = 'var(--danger)';
            }
            resultDiv.innerHTML = '<strong>' + (res.exitoso ? '✅' : '❌') + '</strong> ' + (res.mensaje || 'Sin mensaje')
                + '<br/><span style="font-size:0.72rem;color:var(--text-secondary);">Modo: ' + (res.modo || '—')
                + ' · SOL: ' + (res.tieneUsuarioSol && res.tieneClaveSol ? '✅' : '❌')
                + ' · Certificado: ' + (res.tieneCertificado ? '✅' : '❌') + '</span>';
        }
    }).catch(function(err) {
        if (btn) { btn.disabled = false; btn.textContent = '🔗 Probar Conexión SUNAT'; }
        if (resultDiv) {
            resultDiv.style.display = 'block';
            resultDiv.style.background = 'rgba(248,81,73,0.06)';
            resultDiv.style.border = '1px solid rgba(248,81,73,0.2)';
            resultDiv.style.color = 'var(--danger)';
            resultDiv.innerHTML = '❌ <strong>Error de conexión:</strong> ' + (err.message || 'desconocido');
        }
    });
}

/* ─── Certificate file handling ─── */
var _selectedCertFile = null;

function handleCertFileSelect(input) {
    if (input.files && input.files.length > 0) {
        _setCertFile(input.files[0]);
    }
}

function handleCertFileDrop(event) {
    var files = event.dataTransfer ? event.dataTransfer.files : [];
    if (files.length > 0) {
        _setCertFile(files[0]);
    }
}

function _setCertFile(file) {
    var name = file.name.toLowerCase();
    if (!name.endsWith('.p12') && !name.endsWith('.pfx')) {
        showConfigToast('⚠️ Solo se permiten archivos .p12 o .pfx');
        return;
    }
    if (file.size > 10 * 1024 * 1024) {
        showConfigToast('⚠️ El archivo excede el tamaño máximo de 10 MB');
        return;
    }
    _selectedCertFile = file;

    /* Update UI */
    var iconEl = document.getElementById('cfg-cert-upload-icon');
    var textEl = document.getElementById('cfg-cert-upload-text');
    var fnameEl = document.getElementById('cfg-cert-upload-filename');
    var barEl = document.getElementById('cfg-cert-upload-bar');
    if (iconEl) iconEl.textContent = '✅';
    if (textEl) textEl.style.display = 'none';
    if (fnameEl) { fnameEl.textContent = '📜 ' + file.name + ' (' + (file.size / 1024).toFixed(1) + ' KB)'; fnameEl.style.display = 'block'; }
    if (barEl) barEl.style.display = 'block';

    /* Style the dropzone */
    var dropzone = document.getElementById('cfg-cert-dropzone');
    if (dropzone) {
        dropzone.style.borderColor = 'var(--accent)';
        dropzone.style.background = 'rgba(0,255,136,0.04)';
    }
}

function uploadCertificado() {
    if (!_selectedCertFile) {
        showConfigToast('⚠️ Selecciona un archivo primero');
        return;
    }
    var btn = document.getElementById('cfg-cert-upload-btn');
    var progressDiv = document.getElementById('cfg-cert-upload-progress');
    var resultDiv = document.getElementById('cfg-cert-upload-result');
    if (btn) { btn.disabled = true; btn.textContent = '⏳ Subiendo...'; }
    if (progressDiv) progressDiv.style.display = 'block';
    if (resultDiv) resultDiv.style.display = 'none';

    /* Animate progress bar */
    var bar = document.getElementById('cfg-cert-upload-progress-bar');
    if (bar) { bar.style.width = '30%'; }

    ApiClient.uploadCertificado(ETIA.companyId, _selectedCertFile).then(function(res) {
        if (bar) bar.style.width = '100%';
        if (btn) { btn.disabled = false; btn.textContent = '📤 Subir Certificado'; }

        /* Update local state */
        if (ETIA.company) {
            ETIA.company.tieneCertificado = true;
            if (res.data) ETIA.company.certificadoPath = res.data.certificadoPath;
        }

        /* Update indicator */
        var certInd = document.getElementById('cfg-cred-cert-indicator');
        if (certInd) certInd.innerHTML = '<span style="color:var(--accent);">✅ Configurado</span>';

        /* Show success */
        if (resultDiv) {
            resultDiv.style.display = 'block';
            resultDiv.style.background = 'rgba(0,255,136,0.06)';
            resultDiv.style.border = '1px solid rgba(0,255,136,0.2)';
            resultDiv.style.color = 'var(--accent)';
            resultDiv.innerHTML = '✅ <strong>Certificado subido exitosamente.</strong> Recuerda configurar la contraseña del certificado.';
        }
        showConfigToast('✅ Certificado digital subido');

        /* Reset file selection */
        _selectedCertFile = null;
        setTimeout(function() { if (progressDiv) progressDiv.style.display = 'none'; }, 1000);

    }).catch(function(err) {
        if (bar) bar.style.width = '0%';
        if (btn) { btn.disabled = false; btn.textContent = '📤 Subir Certificado'; }
        if (progressDiv) progressDiv.style.display = 'none';

        if (resultDiv) {
            resultDiv.style.display = 'block';
            resultDiv.style.background = 'rgba(248,81,73,0.06)';
            resultDiv.style.border = '1px solid rgba(248,81,73,0.2)';
            resultDiv.style.color = 'var(--danger)';
            resultDiv.innerHTML = '❌ <strong>Error al subir certificado:</strong> ' + (err.message || 'desconocido');
        }
        showConfigToast('❌ Error al subir certificado');
    });
}

/* ═══════════════════════════════════════════════
   🗺️  LEAFLET MAP — Sede Location
   ═══════════════════════════════════════════════ */

/* Default: Lima, Peru */
var DEFAULT_LAT = -12.0464;
var DEFAULT_LNG = -77.0428;
var DEFAULT_ZOOM = 13;

function initSedeMap() {
    var container = document.getElementById('cfg-map');
    if (!container || typeof L === 'undefined') return;

    /* If map already exists and is valid, just resize */
    if (cfgMap) {
        cfgMap.invalidateSize();
        return;
    }

    /* Clean up any previous map instance (Leaflet internal state) */
    if (container._leaflet_id) {
        container._leaflet_id = null;
        container.innerHTML = '';
    }

    /* Saved coords or default */
    var lat = parseFloat(localStorage.getItem('etia_sedeLat')) || DEFAULT_LAT;
    var lng = parseFloat(localStorage.getItem('etia_sedeLng')) || DEFAULT_LNG;
    var zoom = (lat !== DEFAULT_LAT) ? 16 : DEFAULT_ZOOM;

    cfgMap = L.map('cfg-map').setView([lat, lng], zoom);

    /* Dark-friendly tile layer */
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OSM</a>',
        maxZoom: 19
    }).addTo(cfgMap);

    /* Place marker if we have saved coords */
    if (lat !== DEFAULT_LAT || lng !== DEFAULT_LNG) {
        placeMapMarker(lat, lng);
        updateCoordFields(lat, lng);
    }

    /* Click on map → place marker + reverse geocode */
    cfgMap.on('click', function(e) {
        placeMapMarker(e.latlng.lat, e.latlng.lng);
        updateCoordFields(e.latlng.lat, e.latlng.lng);
        reverseGeocode(e.latlng.lat, e.latlng.lng);
    });
}

function placeMapMarker(lat, lng) {
    if (cfgMapMarker) {
        cfgMapMarker.setLatLng([lat, lng]);
    } else {
        cfgMapMarker = L.marker([lat, lng], {
            draggable: true
        }).addTo(cfgMap);

        /* Drag marker → update coords + reverse geocode */
        cfgMapMarker.on('dragend', function() {
            var pos = cfgMapMarker.getLatLng();
            updateCoordFields(pos.lat, pos.lng);
            reverseGeocode(pos.lat, pos.lng);
        });
    }
    cfgMap.setView([lat, lng], Math.max(cfgMap.getZoom(), 15));
}

function updateCoordFields(lat, lng) {
    var latEl = document.getElementById('cfg-sede-lat');
    var lngEl = document.getElementById('cfg-sede-lng');
    if (latEl) latEl.value = lat.toFixed(6);
    if (lngEl) lngEl.value = lng.toFixed(6);
}

/* ─── Search address → Nominatim (free, no API key) ─── */
function searchAddressOnMap() {
    var dirInput = document.getElementById('cfg-sede-dir');
    var query = dirInput ? dirInput.value.trim() : '';
    if (!query) {
        alert('Escribe una dirección primero.');
        dirInput.focus();
        return;
    }

    /* Append Peru context for better results */
    var searchQuery = query + (query.toLowerCase().includes('peru') ? '' : ', Peru');

    fetch('https://nominatim.openstreetmap.org/search?format=json&q=' + encodeURIComponent(searchQuery) + '&limit=1&addressdetails=1', {
        headers: { 'Accept-Language': 'es' }
    })
    .then(function(r) { return r.json(); })
    .then(function(results) {
        if (!results || results.length === 0) {
            showConfigToast('⚠️ No se encontró la dirección. Intenta ser más específico.');
            return;
        }
        var place = results[0];
        var lat = parseFloat(place.lat);
        var lng = parseFloat(place.lon);

        if (!cfgMap) initSedeMap();
        placeMapMarker(lat, lng);
        updateCoordFields(lat, lng);
        cfgMap.setView([lat, lng], 17);

        /* Update address with the found display name if it's more complete */
        if (place.display_name && place.display_name.length > query.length) {
            dirInput.value = place.display_name.split(',').slice(0, 4).join(',').trim();
        }
    })
    .catch(function(err) {
        showConfigToast('❌ Error buscando dirección: ' + err.message);
    });
}

/* ─── Reverse geocode: click on map → get address ─── */
function reverseGeocode(lat, lng) {
    fetch('https://nominatim.openstreetmap.org/reverse?format=json&lat=' + lat + '&lon=' + lng + '&addressdetails=1', {
        headers: { 'Accept-Language': 'es' }
    })
    .then(function(r) { return r.json(); })
    .then(function(result) {
        if (result && result.display_name) {
            var dirInput = document.getElementById('cfg-sede-dir');
            if (dirInput) {
                dirInput.value = result.display_name.split(',').slice(0, 4).join(',').trim();
            }
            /* Try to auto-select ubigeo if district matches */
            if (result.address && result.address.suburb) {
                autoSelectUbigeo(result.address.suburb);
            } else if (result.address && result.address.city_district) {
                autoSelectUbigeo(result.address.city_district);
            }
        }
    })
    .catch(function() { /* Silently fail — coords are still set */ });
}

/* ─── Auto-match district to ubigeo select ─── */
function autoSelectUbigeo(districtName) {
    var sel = document.getElementById('cfg-sede-select');
    if (!sel) return;
    var normalizedDistrict = districtName.toUpperCase().trim();
    for (var i = 0; i < sel.options.length; i++) {
        if (sel.options[i].text.toUpperCase().includes(normalizedDistrict)) {
            sel.selectedIndex = i;
            return;
        }
    }
}

/* ─── Init: close on overlay click ─── */
document.addEventListener('DOMContentLoaded', function() {
    var overlay = document.getElementById('cfg-overlay');
    if (overlay) {
        overlay.addEventListener('click', function(e) {
            if (e.target === overlay) closeConfigOverlay();
        });
    }
});

/* ═══════════════════════════════════════════════
   🔔  Notification Preferences (PASO 9)
   ═══════════════════════════════════════════════ */

function toggleNotifEmail(on) {
    var onEl = document.getElementById('cfg-notif-email-on');
    var offEl = document.getElementById('cfg-notif-email-off');
    if (onEl) onEl.classList.toggle('active', on);
    if (offEl) offEl.classList.toggle('active', !on);
}

function toggleNotifSms(on) {
    var onEl = document.getElementById('cfg-notif-sms-on');
    var offEl = document.getElementById('cfg-notif-sms-off');
    if (onEl) onEl.classList.toggle('active', on);
    if (offEl) offEl.classList.toggle('active', !on);
}

function _loadNotifPreferences() {
    var prefs = JSON.parse(localStorage.getItem('etia_notif_prefs') || '{}');

    /* If localStorage is empty, try loading from backend */
    if (Object.keys(prefs).length === 0 && ETIA.companyId) {
        var backendPrefs = ETIA._prefCache && ETIA._prefCache['notif_prefs'];
        if (backendPrefs) {
            try { prefs = JSON.parse(backendPrefs); } catch(e) { prefs = {}; }
            localStorage.setItem('etia_notif_prefs', JSON.stringify(prefs));
        }
    }

    /* Email toggle */
    toggleNotifEmail(!!prefs.emailEnabled);
    var emailEl = document.getElementById('cfg-notif-email');
    if (emailEl) emailEl.value = prefs.email || '';

    /* SMS toggle */
    toggleNotifSms(!!prefs.smsEnabled);
    var phoneEl = document.getElementById('cfg-notif-phone');
    if (phoneEl) phoneEl.value = prefs.phone || '';

    /* Checkboxes */
    var chk;
    chk = document.getElementById('cfg-notif-aceptada');    if (chk) chk.checked = prefs.aceptada !== false;
    chk = document.getElementById('cfg-notif-rechazada');   if (chk) chk.checked = prefs.rechazada !== false;
    chk = document.getElementById('cfg-notif-resumen');     if (chk) chk.checked = prefs.resumen !== false;
    chk = document.getElementById('cfg-notif-cert-expiry'); if (chk) chk.checked = !!prefs.certExpiry;
}

function saveNotifPreferences() {
    var emailOn = document.getElementById('cfg-notif-email-on');
    var smsOn = document.getElementById('cfg-notif-sms-on');

    var prefs = {
        emailEnabled: emailOn ? emailOn.classList.contains('active') : false,
        email: (document.getElementById('cfg-notif-email') || {}).value || '',
        smsEnabled: smsOn ? smsOn.classList.contains('active') : false,
        phone: (document.getElementById('cfg-notif-phone') || {}).value || '',
        aceptada: (document.getElementById('cfg-notif-aceptada') || {}).checked !== false,
        rechazada: (document.getElementById('cfg-notif-rechazada') || {}).checked !== false,
        resumen: (document.getElementById('cfg-notif-resumen') || {}).checked !== false,
        certExpiry: !!(document.getElementById('cfg-notif-cert-expiry') || {}).checked
    };

    localStorage.setItem('etia_notif_prefs', JSON.stringify(prefs));

    /* Sync to backend (write-through) */
    if (ETIA.companyId) {
        ApiClient.savePreference(ETIA.companyId, 'notif_prefs', JSON.stringify(prefs)).catch(function(err) {
            console.warn('Error syncing notif_prefs:', err);
        });
    }

    showConfigToast('✅ Preferencias de notificación guardadas');
}

