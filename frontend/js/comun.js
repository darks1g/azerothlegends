// Utilidades compartidas por las páginas de la web

// Escapa texto para insertarlo de forma segura en HTML
function esc(texto) {
    const d = document.createElement('div');
    d.textContent = texto == null ? '' : String(texto);
    return d.innerHTML;
}

const COLORES_CLASE = [
    ['caballer', '#C41E3A'],        // Caballero/a de la Muerte
    ['cazador de demonios', '#A330C9'],
    ['cazadora de demonios', '#A330C9'],
    ['cazador', '#AAD372'],
    ['cazadora', '#AAD372'],
    ['guerrer', '#C69B6D'],
    ['paladin', '#F48CBA'],
    ['picar', '#FFF468'],
    ['sacerdot', '#FFFFFF'],
    ['cham', '#0070DD'],
    ['mag', '#3FC7EB'],
    ['bruj', '#8788EE'],
    ['monje', '#00FF98'],
    ['druida', '#FF7C0A'],
    ['evocador', '#33937F']
];

// Color oficial de la clase a partir de su nombre en español (sin importar tildes ni género)
function colorClase(nombre) {
    if (!nombre) return '#ffffff';
    const n = nombre.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
    const coincidencia = COLORES_CLASE.find(([clave]) => n.startsWith(clave));
    return coincidencia ? coincidencia[1] : '#ffffff';
}

const ETIQUETAS_VERSION = {
    retail: 'Retail',
    classic_era: 'Classic Era',
    classic: 'Classic'
};

function etiquetaVersion(version) {
    return ETIQUETAS_VERSION[version] || version;
}

// "azjol-nerub" -> "Azjol Nerub"
function nombreReino(slug) {
    return String(slug || '')
        .split('-')
        .map(p => p.charAt(0).toUpperCase() + p.slice(1))
        .join(' ');
}

function urlFicha(p) {
    return `/detalles.html?nombre=${encodeURIComponent(p.nombre)}&reino=${encodeURIComponent(p.reino)}`
        + `&region=${encodeURIComponent(p.region)}&version=${encodeURIComponent(p.versionJuego || p.version)}`;
}
