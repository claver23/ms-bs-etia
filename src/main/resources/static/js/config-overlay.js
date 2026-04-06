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

    /* Series */
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

