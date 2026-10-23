(() => {
    const CONTENT_BASE = '/api/model-viewer/assets';
    const VIEWER_SCRIPTS = {
        retail: 'viewer/viewer.min.js',
        classic: 'deployment/viewer/c3f890f/viewer.min.js',
        classic_era: 'deployment/viewer/c3f890f/viewer.min.js'
    };
    const RACE_IDS = {
        humano: 1, human: 1,
        orco: 2, orc: 2,
        enano: 3, dwarf: 3,
        'elfo de la noche': 4, 'night elf': 4,
        'no muerto': 5, undead: 5,
        tauren: 6,
        gnomo: 7, gnome: 7,
        trol: 8, troll: 8,
        goblin: 9,
        'elfo de sangre': 10, 'blood elf': 10,
        draenei: 11,
        huargen: 22, worgen: 22,
        pandaren: 24,
        'elfo del vacio': 29, 'void elf': 29,
        'tauren monte alto': 28, 'highmountain tauren': 28,
        'draenei forjado por la luz': 30, 'lightforged draenei': 30,
        'trol zandalari': 31, 'zandalari troll': 31,
        'humano de kul tiras': 32, 'kul tiran': 32,
        'enano hierro negro': 34, 'dark iron dwarf': 34,
        vulpera: 35,
        'orco mag har': 36, 'mag har orc': 36,
        mecagnomo: 37, mechagnome: 37,
        dracthyr: 52,
        terraneo: 70, earthen: 70
    };
    const NON_VISUAL_SLOTS = new Set(['NECK', 'FINGER_1', 'FINGER_2', 'TRINKET_1', 'TRINKET_2']);
    const scriptPromises = new Map();

    let viewer;
    let generation = 0;

    function normalizeRace(race) {
        return String(race || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '')
            .toLowerCase().replace(/[-']/g, ' ').replace(/\s+/g, ' ').trim();
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
            INVENTORY_TYPE_TABARD: 19,
            INVENTORY_TYPE_ROBE: 20,
            INVENTORY_TYPE_MAIN_HAND: 21,
            INVENTORY_TYPE_OFF_HAND: 22
        };
    }

    async function resolveEquipment(flavor, equipment) {
        const visibleItems = (equipment || []).filter(item =>
            item.itemId && item.slot && !NON_VISUAL_SLOTS.has(item.slot));
        const results = await Promise.allSettled(visibleItems.map(async item => {
            const itemUrl = `${CONTENT_BASE}/${flavor}/items/${item.itemId}`;
            let response;
            let transmogFallback = false;
            if (item.transmogAppearanceId) {
                try {
                    response = await fetch(`${itemUrl}?appearanceId=${item.transmogAppearanceId}`);
                } catch {
                    transmogFallback = true;
                }
                if (!response || !response.ok) {
                    console.warn(`No se pudo resolver la transfiguración de ${item.nombreItem || item.itemId}; se usará el aspecto original.`);
                    response = await fetch(itemUrl);
                    transmogFallback = true;
                }
            } else {
                response = await fetch(itemUrl);
            }
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }
            const model = await response.json();
            return { model, transmogFallback };
        }));

        const items = [];
        const failures = [];
        const transmogFallbacks = [];
        results.forEach((result, index) => {
            if (result.status === 'fulfilled') {
                items.push([result.value.model.inventoryType, result.value.model.displayId]);
                if (result.value.transmogFallback) {
                    transmogFallbacks.push(visibleItems[index].nombreItem || visibleItems[index].itemId);
                }
            } else {
                const item = visibleItems[index];
                failures.push(item.nombreItem || item.itemId);
                console.warn(`No se pudo resolver el modelo de ${item.nombreItem || item.itemId}:`, result.reason);
            }
        });
        return { items, failures, transmogFallbacks };
    }

    function loadViewerScript(flavor) {
        if (!scriptPromises.has(flavor)) {
            const script = document.createElement('script');
            script.src = `${CONTENT_BASE}/${flavor}/${VIEWER_SCRIPTS[flavor]}`;
            script.async = true;
            const promise = new Promise((resolve, reject) => {
                script.onload = resolve;
                script.onerror = () => {
                    script.remove();
                    scriptPromises.delete(flavor);
                    reject(new Error(`No se pudo cargar el visor ${flavor}.`));
                };
                document.head.appendChild(script);
            });
            scriptPromises.set(flavor, promise);
        }
        return scriptPromises.get(flavor);
    }

    function setStatus(message, isError = false, isLoading = false) {
        const status = document.getElementById('estadoModelo');
        status.textContent = message;
        status.classList.toggle('error', isError);
        status.classList.toggle('loading', isLoading);
    }

    function destroyViewer() {
        if (viewer) {
            viewer.destroy();
            viewer = undefined;
        }
        document.getElementById('modeloPersonaje').replaceChildren();
        document.getElementById('modeloPersonaje').hidden = true;
    }

    async function mostrar(data) {
        const currentGeneration = ++generation;
        destroyViewer();
        const modelBox = document.getElementById('modeloPersonaje');
        const imageBox = document.getElementById('renderBox');
        const image = document.getElementById('imagenPersonaje');
        const flavor = data.versionJuego;
        const race = RACE_IDS[normalizeRace(data.raza)];
        const gender = data.genero === 'FEMALE' ? 1 : data.genero === 'MALE' ? 0 : null;
        imageBox.hidden = !data.imagen;
        image.onerror = () => { imageBox.hidden = true; };
        if (data.imagen) image.src = data.imagen;

        if (!VIEWER_SCRIPTS[flavor] || !race || gender === null) {
            setStatus('No hay un modelo 3D compatible; se muestra el retrato disponible.', true);
            return;
        }

        setStatus('Cargando modelo 3D y equipo…', false, true);
        try {
            const equipment = await resolveEquipment(flavor, data.equipo);
            const contentPath = `${CONTENT_BASE}/${flavor}/`;
            initializeViewerGlobals(contentPath);
            await loadViewerScript(flavor);
            if (currentGeneration !== generation) return;
            if (typeof window.ZamModelViewer !== 'function') {
                throw new Error('El visor no inicializó ZamModelViewer.');
            }

            const classic = flavor !== 'retail';
            const modelId = race * 2 - 1 + gender;
            imageBox.hidden = true;
            modelBox.hidden = false;
            const createdViewer = await new window.ZamModelViewer({
                type: 2,
                contentPath,
                container: jQuery(modelBox),
                aspect: 1.1,
                cls: data.claseId || 0,
                background: flavor === 'retail' ? 'background-181818.png' : 'background-classic-181818.png',
                hd: !classic,
                env: classic ? 'mists' : 'live',
                dataEnv: classic ? 'mists' : 'live',
                gameDataEnv: classic ? 'mists' : 'live',
                models: { id: modelId, type: 16 },
                charCustomization: data.personalizacion?.length
                    ? { options: data.personalizacion, sheathMain: -1, sheathOff: -1 }
                    : undefined,
                items: equipment.items
            });
            if (currentGeneration !== generation) {
                createdViewer.destroy();
                return;
            }

            viewer = createdViewer;
            modelBox.hidden = false;
            const omitted = equipment.failures.length
                ? ` No se pudieron cargar ${equipment.failures.length} piezas: ${equipment.failures.join(', ')}.`
                : '';
            const transmogFallback = equipment.transmogFallbacks.length
                ? ` No se pudo aplicar la transfiguración de: ${equipment.transmogFallbacks.join(', ')}; se muestra el aspecto original.`
                : '';
            setStatus(`Modelo 3D con ${equipment.items.length} piezas de equipo.${omitted}${transmogFallback}`,
                equipment.failures.length > 0 || equipment.transmogFallbacks.length > 0);
        } catch (error) {
            if (currentGeneration !== generation) return;
            console.error('No se pudo cargar el modelo 3D del personaje:', error);
            setStatus('No se pudo cargar el modelo 3D; se muestra el retrato disponible.', true);
            modelBox.hidden = true;
            imageBox.hidden = !data.imagen;
        }
    }

    window.VisorPersonaje = { mostrar };
})();
