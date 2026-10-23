const CONTENT_BASE = '/api/model-viewer/assets';
const VIEWER_SCRIPTS = {
    retail: 'viewer/viewer.min.js',
    classic: 'deployment/viewer/c3f890f/viewer.min.js',
    classic_era: 'viewer/viewer.min.js'
};
const RACE_IDS = {
    'humano': 1,
    'orco': 2,
    'enano': 3,
    'noche elfo': 4,
    'no-muerto': 5,
    'tauren': 6,
    'gnomo': 7,
    'trol': 8,
    'trol zandalari': 31,
    'elfo de sangre': 10,
    'draenei': 11,
    'pandaren': 24
};
const NON_VISUAL_SLOTS = new Set(['NECK', 'FINGER_1', 'FINGER_2', 'TRINKET_1', 'TRINKET_2']);

let currentViewer;
let character;
let activeScript;

function setStatus(message, isError = false) {
    const status = document.getElementById('viewerStatus');
    status.textContent = message;
    status.classList.toggle('error', isError);
}

function initializeViewerGlobals(contentPath) {
    window.CONTENT_PATH = contentPath;
    window.WOTLK_TO_RETAIL_DISPLAY_ID_API = undefined;
    window.WH = window.WH || {};
    window.WH.debug = (...args) => console.debug('[model-viewer]', ...args);
    window.WH.defaultAnimation = 'Stand';
    window.WH.WebP = {
        getImageExtension() {
            return '.webp';
        }
    };
    window.WH.Wow = window.WH.Wow || {};
    window.WH.Wow.Item = {
        INVENTORY_TYPE_HEAD: 1,
        INVENTORY_TYPE_NECK: 2,
        INVENTORY_TYPE_SHOULDERS: 3,
        INVENTORY_TYPE_SHIRT: 4,
        INVENTORY_TYPE_CHEST: 5,
        INVENTORY_TYPE_WAIST: 6,
        INVENTORY_TYPE_LEGS: 7,
        INVENTORY_TYPE_FEET: 8,
        INVENTORY_TYPE_WRISTS: 9,
        INVENTORY_TYPE_HANDS: 10,
        INVENTORY_TYPE_FINGER: 11,
        INVENTORY_TYPE_TRINKET: 12,
        INVENTORY_TYPE_ONE_HAND: 13,
        INVENTORY_TYPE_SHIELD: 14,
        INVENTORY_TYPE_RANGED: 15,
        INVENTORY_TYPE_BACK: 16,
        INVENTORY_TYPE_TWO_HAND: 17,
        INVENTORY_TYPE_MAIN_HAND: 21,
        INVENTORY_TYPE_OFF_HAND: 22
    };
}

async function obtenerPersonaje() {
    const params = new URLSearchParams({
        nombre: 'Dedillos',
        reino: 'Golemagg',
        region: 'eu',
        version: 'classic'
    });
    const response = await fetch(`/api/personajes/detalles?${params}`, {
        credentials: 'same-origin'
    });
    const result = await response.json();
    if (!response.ok) {
        throw new Error(result.error || 'No se pudo cargar el personaje de prueba.');
    }

    const race = RACE_IDS[String(result.raza || '').toLocaleLowerCase('es')];
    const gender = result.genero === 'FEMALE' ? 1 : result.genero === 'MALE' ? 0 : null;
    if (!race || gender === null) {
        throw new Error(`No se puede mapear la raza o el género de ${result.nombre}.`);
    }
    return { name: result.nombre, race, gender, equipment: result.equipo || [] };
}

async function obtenerEquipoVisual(flavor, equipment) {
    const equippedItems = equipment.filter(item =>
        item.itemId && item.slot && !NON_VISUAL_SLOTS.has(item.slot));
    const results = await Promise.allSettled(equippedItems.map(async item => {
        const response = await fetch(`${CONTENT_BASE}/${flavor}/items/${item.itemId}`, {
            credentials: 'same-origin'
        });
        if (!response.ok) {
            throw new Error(`${item.nombreItem || item.itemId}: HTTP ${response.status}`);
        }
        const model = await response.json();
        return [model.inventoryType, model.displayId];
    }));

    const items = [];
    const failures = [];
    results.forEach((result, index) => {
        if (result.status === 'fulfilled') {
            items.push(result.value);
        } else {
            const item = equippedItems[index];
            failures.push(item.nombreItem || item.itemId);
            console.warn(`No se pudo resolver el modelo de ${item.nombreItem || item.itemId}:`, result.reason);
        }
    });
    return { items, failures };
}

function loadViewerScript(flavor) {
    if (activeScript) {
        activeScript.remove();
    }
    window.ZamModelViewer = undefined;
    const script = document.createElement('script');
    script.src = `${CONTENT_BASE}/${flavor}/${VIEWER_SCRIPTS[flavor]}`;
    script.async = true;
    activeScript = script;
    return new Promise((resolve, reject) => {
        script.onload = resolve;
        script.onerror = () => reject(new Error(`No se pudo cargar el visor ${flavor}.`));
        document.head.appendChild(script);
    });
}

async function loadModel() {
    const flavor = document.getElementById('gameVersion').value;
    const button = document.getElementById('loadModel');
    button.disabled = true;
    setStatus(`Cargando recursos para ${flavor}…`);

    try {
        currentViewer?.destroy();
        currentViewer = undefined;
        document.getElementById('model3d').replaceChildren();

        character ||= await obtenerPersonaje();
        const equipment = await obtenerEquipoVisual(flavor, character.equipment);
        const contentPath = `${CONTENT_BASE}/${flavor}/`;
        initializeViewerGlobals(contentPath);
        await loadViewerScript(flavor);
        if (typeof window.ZamModelViewer !== 'function') {
            throw new Error('El visor cargó, pero no inicializó ZamModelViewer.');
        }

        const classic = flavor !== 'retail';
        const modelId = character.race * 2 - 1 + character.gender;
        const options = {
            type: 2,
            contentPath,
            container: jQuery('#model3d'),
            aspect: 2,
            cls: 1,
            background: 'background-classic-181818.png',
            hd: !classic,
            env: flavor === 'classic' ? 'mists' : flavor === 'classic_era' ? 'classic' : 'live',
            dataEnv: flavor === 'classic' ? 'mists' : flavor === 'classic_era' ? 'classic' : 'live',
            gameDataEnv: flavor === 'classic' ? 'mists' : flavor === 'classic_era' ? 'classic' : 'live',
            models: { id: modelId, type: 16 },
            items: equipment.items
        };
        currentViewer = await new window.ZamModelViewer(options);
        const omitidas = equipment.failures.length
            ? ` No se pudieron resolver ${equipment.failures.length} piezas: ${equipment.failures.join(', ')}.`
            : '';
        setStatus(`${character.name}: modelo y ${equipment.items.length} piezas de equipo cargados para ${flavor}.${omitidas}`);
    } catch (error) {
        console.error('Fallo en la prueba del visor:', error);
        setStatus(error.message || 'No se pudo cargar el modelo.', true);
    } finally {
        button.disabled = false;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('loadModel').addEventListener('click', loadModel);
    loadModel();
});
