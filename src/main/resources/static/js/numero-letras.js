/* ═══════════════════════════════════════════════
   💬  ETIA — Número a Letras (español, SUNAT)
   Formato: "CIENTO DIECIOCHO Y 00/100 SOLES"
   ═══════════════════════════════════════════════ */

var _NL_UNIDADES = ['', 'UNO', 'DOS', 'TRES', 'CUATRO', 'CINCO', 'SEIS', 'SIETE', 'OCHO', 'NUEVE',
    'DIEZ', 'ONCE', 'DOCE', 'TRECE', 'CATORCE', 'QUINCE', 'DIECISÉIS', 'DIECISIETE', 'DIECIOCHO', 'DIECINUEVE',
    'VEINTE', 'VEINTIUNO', 'VEINTIDÓS', 'VEINTITRÉS', 'VEINTICUATRO', 'VEINTICINCO', 'VEINTISÉIS', 'VEINTISIETE', 'VEINTIOCHO', 'VEINTINUEVE'];
var _NL_DECENAS = ['', 'DIEZ', 'VEINTE', 'TREINTA', 'CUARENTA', 'CINCUENTA', 'SESENTA', 'SETENTA', 'OCHENTA', 'NOVENTA'];
var _NL_CENTENAS = ['', 'CIENTO', 'DOSCIENTOS', 'TRESCIENTOS', 'CUATROCIENTOS', 'QUINIENTOS', 'SEISCIENTOS', 'SETECIENTOS', 'OCHOCIENTOS', 'NOVECIENTOS'];

function _nlCentenas(n) {
    if (n === 0) return '';
    if (n === 100) return 'CIEN';
    if (n < 30) return _NL_UNIDADES[n];
    if (n < 100) {
        var d = Math.floor(n / 10);
        var u = n % 10;
        return _NL_DECENAS[d] + (u > 0 ? ' Y ' + _NL_UNIDADES[u] : '');
    }
    var c = Math.floor(n / 100);
    var r = n % 100;
    return _NL_CENTENAS[c] + (r > 0 ? ' ' + _nlCentenas(r) : '');
}

function _nlGrupo(n) {
    if (n === 0) return '';
    return _nlCentenas(n);
}

/**
 * Converts a number to Spanish words (SUNAT format for leyenda).
 * @param {number} monto - The amount (e.g. 3186.50)
 * @param {string} moneda - 'PEN' or 'USD'
 * @returns {string} e.g. "TRES MIL CIENTO OCHENTA Y SEIS Y 50/100 SOLES"
 */
function numeroALetras(monto, moneda) {
    if (monto == null || isNaN(monto)) return 'CERO Y 00/100 SOLES';
    var negativo = monto < 0;
    monto = Math.abs(monto);
    var entero = Math.floor(monto);
    var decimales = Math.round((monto - entero) * 100);
    if (decimales >= 100) { entero++; decimales = 0; }
    var decStr = String(decimales).padStart(2, '0');

    var texto = '';
    if (entero === 0) {
        texto = 'CERO';
    } else {
        /* Split into groups: billones, millones, miles, unidades */
        var billones = Math.floor(entero / 1000000000);
        var millones = Math.floor((entero % 1000000000) / 1000000);
        var miles = Math.floor((entero % 1000000) / 1000);
        var unidades = entero % 1000;

        var partes = [];

        if (billones > 0) {
            if (billones === 1) partes.push('MIL');
            else partes.push(_nlGrupo(billones) + ' MIL');
            /* billones = miles de millones en español corto */
        }

        if (millones > 0) {
            if (millones === 1) partes.push('UN MILLÓN');
            else partes.push(_nlGrupo(millones) + ' MILLONES');
        }

        if (miles > 0) {
            if (miles === 1) partes.push('MIL');
            else partes.push(_nlGrupo(miles) + ' MIL');
        }

        if (unidades > 0) {
            partes.push(_nlGrupo(unidades));
        }

        texto = partes.join(' ');
    }

    var monedaLabel = 'SOLES';
    if (moneda === 'USD') monedaLabel = 'DÓLARES AMERICANOS';

    return (negativo ? 'MENOS ' : '') + texto + ' Y ' + decStr + '/100 ' + monedaLabel;
}

/**
 * Updates the leyenda fields in the wizard based on the current total.
 */
function updateLeyenda() {
    var total = (typeof getCurrentTotal === 'function') ? getCurrentTotal() : 0;
    var monedaEl = document.getElementById('fac-moneda');
    var moneda = monedaEl ? monedaEl.value : 'PEN';
    var texto = numeroALetras(total, moneda);

    var leyendaInput = document.getElementById('fac-leyenda');
    var leyendaDisplay = document.getElementById('fac-leyenda-display');
    if (leyendaInput) leyendaInput.value = texto;
    if (leyendaDisplay) leyendaDisplay.textContent = texto;
}

