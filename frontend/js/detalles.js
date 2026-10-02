document.addEventListener('DOMContentLoaded', async () => {
    const params = new URLSearchParams(window.location.search);
    const nombre = params.get('nombre');
    const reino = params.get('reino');
    const region = params.get('region');
    const version = params.get('version');

    if (!nombre || !reino || !region || !version) {
        document.body.innerHTML = '<p>Error: falta información del personaje.</p>';
        return;
    }

    try {
        const res = await fetch(`/api/personajes/detalles?nombre=${nombre}&reino=${reino}&region=${region}&version=${version}`);
        const contentType = res.headers.get("content-type");

        if (!res.ok) {
            const texto = await res.text();
            throw new Error(`Error HTTP ${res.status}: ${texto}`);
        }

        if (!contentType || !contentType.includes("application/json")) {
            const texto = await res.text();
            throw new Error(`Respuesta no JSON:\n\n${texto.slice(0, 200)}`);
        }

        const data = await res.json();
        renderPersonaje(data);
    } catch (err) {
        console.error('Error cargando personaje:', err);
        document.body.innerHTML = `<p>Error al cargar personaje: ${err.message}</p>`;
    }
});


// ---- Pintado de la ficha del personaje ----

const SLOTS_IZQUIERDA = ['HEAD', 'NECK', 'SHOULDER', 'BACK', 'CHEST', 'SHIRT', 'TABARD', 'WRIST', 'MAIN_HAND'];
const SLOTS_DERECHA = ['HANDS', 'WAIST', 'LEGS', 'FEET', 'FINGER_1', 'FINGER_2', 'TRINKET_1', 'TRINKET_2', 'OFF_HAND', 'RANGED'];
const NOMBRE_SLOT = {
    HEAD: 'Cabeza', NECK: 'Cuello', SHOULDER: 'Hombros', BACK: 'Espalda', CHEST: 'Pecho',
    SHIRT: 'Camisa', TABARD: 'Tabardo', WRIST: 'Muñecas', HANDS: 'Manos', WAIST: 'Cintura',
    LEGS: 'Piernas', FEET: 'Pies', FINGER_1: 'Anillo 1', FINGER_2: 'Anillo 2',
    TRINKET_1: 'Abalorio 1', TRINKET_2: 'Abalorio 2', MAIN_HAND: 'Mano derecha',
    OFF_HAND: 'Mano izquierda', RANGED: 'A distancia'
};
const PORCENTAJE = ['Golpe Crítico', 'Celeridad', 'Maestría', 'Versatilidad'];
const ICONO_URL = 'https://render.worldofwarcraft.com/eu/icons/56/';

function esc(texto) {
    const d = document.createElement('div');
    d.textContent = texto == null ? '' : String(texto);
    return d.innerHTML;
}

function htmlIcono(icono, titulo) {
    const nombre = icono || 'inv_misc_questionmark';
    return `<img src="${ICONO_URL}${encodeURIComponent(nombre)}.jpg" alt="${esc(titulo)}" title="${esc(titulo)}"
        onerror="this.onerror=null;this.src='${ICONO_URL}inv_misc_questionmark.jpg'">`;
}

function htmlItem(item, slot) {
    if (!item) {
        return `<div class="item-slot item-vacio"><span class="item-slot-nombre">${esc(NOMBRE_SLOT[slot] || slot)}</span></div>`;
    }
    const ilvl = item.ilvl ? `<span class="item-ilvl">iLvl ${esc(item.ilvl)}</span>` : '';
    return `<div class="item-slot" title="${esc(item.nombreItem)}">
        ${htmlIcono(item.icono, item.nombreItem)}
        <div class="item-info">
            <span class="item-nombre">${esc(item.nombreItem)}</span>
            <span class="item-slot-nombre">${esc(NOMBRE_SLOT[slot] || slot)}</span>
            ${ilvl}
        </div>
    </div>`;
}

function pintarEquipo(equipo) {
    const porSlot = {};
    (equipo || []).forEach(i => { porSlot[i.slot] = i; });

    const izq = document.getElementById('equipamientoIzquierdo');
    const der = document.getElementById('equipamientoDerecho');

    izq.innerHTML = SLOTS_IZQUIERDA
        .filter(s => porSlot[s] || !['MAIN_HAND'].includes(s))
        .map(s => htmlItem(porSlot[s], s)).join('');

    // Los slots de la derecha que no tengan objeto no se muestran (RANGED, OFF_HAND...)
    const extra = Object.keys(porSlot).filter(s => !SLOTS_IZQUIERDA.includes(s) && !SLOTS_DERECHA.includes(s));
    der.innerHTML = [...SLOTS_DERECHA, ...extra]
        .filter(s => porSlot[s] || !['OFF_HAND', 'RANGED'].includes(s))
        .map(s => htmlItem(porSlot[s], s)).join('');
}

function pintarTalentos(contenedorId, lista) {
    const cont = document.getElementById(contenedorId);
    if (!cont) return;
    // Talentos que Blizzard manda sin nombre ni hechizo (los que vienen por defecto)
    lista = (lista || []).filter(t => !(t.icono === 'inv_misc_questionmark' && /^Talento /.test(t.nombre)));
    if (lista.length === 0) {
        cont.innerHTML = '<p class="sin-datos">Sin datos</p>';
        return;
    }
    cont.innerHTML = lista.map(t => `
        <div class="talento-item">
            ${htmlIcono(t.icono, t.nombre)}
            <small>${esc(t.nombre)}</small>
        </div>`).join('');
}

const PRIMARIOS = ['Fuerza', 'Agilidad', 'Intelecto'];

// Como en la web oficial: de Fuerza/Agilidad/Intelecto solo se muestra el principal de la clase
function filtrarEstadisticas(lista) {
    const primarios = lista.filter(e => PRIMARIOS.includes(e.nombre));
    const principal = primarios.reduce((a, b) => (b.valor > a.valor ? b : a), primarios[0]);
    return lista.filter(e => !PRIMARIOS.includes(e.nombre) || e === principal);
}

function renderPersonaje(data) {
    document.title = `${data.nombre} - Azeroth Legends`;
    document.getElementById('nombrePersonaje').textContent = data.nombre;
    document.getElementById('infoBasica').textContent =
        `Nivel ${data.nivel} ${data.raza || ''} ${data.clase || ''} · ${data.reino} (${String(data.region).toUpperCase()}) · ${data.versionJuego}`;

    pintarEquipo(data.equipo);

    const stats = document.getElementById('estadisticas');
    const listaStats = filtrarEstadisticas(data.estadisticas || []);
    stats.innerHTML = listaStats.length
        ? listaStats.map(e => `<div><strong>${esc(e.nombre)}</strong>: ${Number(e.valor).toLocaleString('es-ES')}${PORCENTAJE.includes(e.nombre) ? ' %' : ''}</div>`).join('')
        : '<p class="sin-datos">Sin estadísticas</p>';

    if (data.versionJuego === 'retail') {
        pintarTalentos('talentosClase', data.talentosClase);
        pintarTalentos('talentosSpec', data.talentosSpec);
        pintarTalentos('talentosHero', data.talentosHero);
    } else {
        // Classic: una sola lista de talentos
        pintarTalentos('talentosClase', data.talentos);
        document.getElementById('talentosSpec').closest('fieldset').style.display = 'none';
        document.getElementById('talentosHero').closest('fieldset').style.display = 'none';
    }
}
