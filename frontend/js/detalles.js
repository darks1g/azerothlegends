// ---- Constantes ----

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
const PRIMARIOS = ['Fuerza', 'Agilidad', 'Intelecto'];
const COLOR_STAT = {
    'Vida': '#22c55e', 'Fuerza': '#c69b6d', 'Agilidad': '#facc15', 'Intelecto': '#3fc7eb',
    'Aguante': '#fb923c', 'Golpe Crítico': '#ef4444', 'Celeridad': '#14b8a6',
    'Maestría': '#a855f7', 'Versatilidad': '#d4d4d8'
};
const ICONO_URL = 'https://render.worldofwarcraft.com/eu/icons/56/';
const ICONO_VACIO = 'inv_misc_questionmark';

let parametros = null;
let datosActuales = null;
let tabActiva = 0;

// ---- Enlaces a Wowhead (para los tooltips al pasar el ratón) ----

function baseWowhead(version) {
    if (version === 'retail') return 'https://www.wowhead.com/es';
    if (version === 'classic_era') return 'https://www.wowhead.com/classic/es';
    return 'https://www.wowhead.com/mop-classic/es';
}

function urlItem(item, version) {
    if (!item.itemId) return null;
    let url = `${baseWowhead(version)}/item=${item.itemId}`;
    if (version === 'retail') {
        const params = [];
        if (item.bonus) params.push(`bonus=${item.bonus}`);
        if (item.ilvl) params.push(`ilvl=${item.ilvl}`);
        if (params.length) url += '?' + params.join('&');
    }
    return url;
}

function urlHechizo(talento, version) {
    return talento.spellId ? `${baseWowhead(version)}/spell=${talento.spellId}` : null;
}

function refrescarTooltips() {
    try {
        if (window.WH && WH.Tooltips && WH.Tooltips.refreshLinks) WH.Tooltips.refreshLinks();
        else if (window.$WowheadPower && $WowheadPower.refreshLinks) $WowheadPower.refreshLinks();
    } catch (e) { /* los tooltips son un extra: si fallan, la ficha sigue funcionando */ }
}

// ---- Utilidades visuales ----

function reducirMovimiento() {
    return window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
}

// Cuenta desde 0 hasta el valor final (si el usuario prefiere menos movimiento, lo pone directo)
function animarNumero(el, destino, sufijo) {
    const formato = n => Math.round(n).toLocaleString('es-ES') + sufijo;
    if (reducirMovimiento() || destino === 0) {
        el.textContent = formato(destino);
        return;
    }
    const duracion = 800;
    const inicio = performance.now();
    function paso(ahora) {
        const t = Math.min((ahora - inicio) / duracion, 1);
        const suave = 1 - Math.pow(1 - t, 3);
        el.textContent = formato(destino * suave);
        if (t < 1) requestAnimationFrame(paso);
    }
    requestAnimationFrame(paso);
}

function mostrarToast(texto) {
    const toast = document.getElementById('toast');
    toast.textContent = texto;
    toast.hidden = false;
    clearTimeout(mostrarToast.temporizador);
    mostrarToast.temporizador = setTimeout(() => { toast.hidden = true; }, 2200);
}

// ---- Pintado ----

function htmlIcono(icono, titulo) {
    const nombre = icono || ICONO_VACIO;
    return `<img src="${ICONO_URL}${encodeURIComponent(nombre)}.jpg" alt="${esc(titulo)}" loading="lazy"
        onerror="this.onerror=null;this.src='${ICONO_URL}${ICONO_VACIO}.jpg'">`;
}

function htmlItem(item, slot, version, indice) {
    const etiquetaSlot = NOMBRE_SLOT[slot] || slot;
    const estilo = `style="--i:${indice}"`;
    if (!item) {
        return `<div class="item-slot item-vacio" ${estilo}><span class="item-slot-nombre">${esc(etiquetaSlot)}</span></div>`;
    }
    const calidad = 'q-' + String(item.calidad || 'COMMON').toLowerCase();
    const ilvl = item.ilvl ? ` · iLvl ${esc(item.ilvl)}` : '';
    const contenido = `${htmlIcono(item.icono, item.nombreItem)}
        <div class="item-info">
            <span class="item-nombre">${esc(item.nombreItem)}</span>
            <span class="item-slot-nombre">${esc(etiquetaSlot)}${ilvl}</span>
        </div>`;
    const url = urlItem(item, version);
    return url
        ? `<a class="item-slot ${calidad}" ${estilo} href="${esc(url)}" target="_blank" rel="noopener">${contenido}</a>`
        : `<div class="item-slot ${calidad}" ${estilo}>${contenido}</div>`;
}

// Nivel de objeto medio (como en la web oficial: sin camisa ni tabardo; a dos manos cuenta doble)
function ilvlMedio(equipo) {
    const piezas = (equipo || []).filter(i => i.ilvl && !['SHIRT', 'TABARD'].includes(i.slot));
    if (!piezas.length) return null;
    let suma = piezas.reduce((s, i) => s + i.ilvl, 0);
    let cuenta = piezas.length;
    const principal = piezas.find(i => i.slot === 'MAIN_HAND');
    if (principal && !piezas.some(i => i.slot === 'OFF_HAND')) {
        suma += principal.ilvl;
        cuenta += 1;
    }
    return Math.round(suma / cuenta);
}

function pintarEquipo(equipo, version) {
    const porSlot = {};
    (equipo || []).forEach(i => { porSlot[i.slot] = i; });

    // Los huecos de armas y distancia solo se muestran si hay objeto
    const opcionales = ['MAIN_HAND', 'OFF_HAND', 'RANGED'];
    const extra = Object.keys(porSlot).filter(s => !SLOTS_IZQUIERDA.includes(s) && !SLOTS_DERECHA.includes(s));

    const visibles = lista => lista.filter(s => porSlot[s] || !opcionales.includes(s));

    document.getElementById('equipamientoIzquierdo').innerHTML = visibles(SLOTS_IZQUIERDA)
        .map((s, i) => htmlItem(porSlot[s], s, version, i)).join('');

    document.getElementById('equipamientoDerecho').innerHTML = visibles([...SLOTS_DERECHA, ...extra])
        .map((s, i) => htmlItem(porSlot[s], s, version, i)).join('');

    const medio = ilvlMedio(equipo);
    document.getElementById('ilvlMedio').textContent = medio ? `Nivel de objeto ${medio}` : '';
}

// ---- Talentos (en pestañas) ----

function sinTalentosVacios(lista) {
    // Los talentos sin nombre ni hechizo (los que Blizzard manda por defecto) no se muestran
    return (lista || []).filter(t => !(t.icono === ICONO_VACIO && /^Talento /.test(t.nombre)));
}

function htmlTalento(t, version, indice) {
    const rango = t.rango && t.rango > 1 ? `<span class="talento-rango">${esc(t.rango)}</span>` : '';
    const contenido = `<span class="talento-icono">${htmlIcono(t.icono, t.nombre)}${rango}</span><small>${esc(t.nombre)}</small>`;
    const url = urlHechizo(t, version);
    return url
        ? `<a class="talento-item" style="--i:${indice}" href="${esc(url)}" target="_blank" rel="noopener">${contenido}</a>`
        : `<div class="talento-item" style="--i:${indice}">${contenido}</div>`;
}

function gruposDeTalentos(data) {
    if (data.versionJuego === 'retail') {
        return [
            { titulo: 'Clase', talentos: sinTalentosVacios(data.talentosClase) },
            { titulo: data.especializacion ? `Especialización · ${data.especializacion}` : 'Especialización', talentos: sinTalentosVacios(data.talentosSpec) },
            { titulo: data.heroe ? `Héroe · ${data.heroe}` : 'Héroe', talentos: sinTalentosVacios(data.talentosHero) }
        ].filter(g => g.talentos.length);
    }

    // Classic: un grupo por árbol de talentos
    const arboles = new Map();
    sinTalentosVacios(data.talentos).forEach(t => {
        const arbol = t.tipo || 'Talentos';
        if (!arboles.has(arbol)) arboles.set(arbol, []);
        arboles.get(arbol).push(t);
    });
    return [...arboles.entries()].map(([arbol, lista]) => {
        const puntos = lista.reduce((suma, t) => suma + (t.rango || 0), 0);
        return { titulo: `${arbol} (${puntos})`, talentos: lista };
    });
}

function pintarTalentos(data) {
    const grupos = gruposDeTalentos(data);
    const tabs = document.getElementById('tabs');
    const panel = document.getElementById('panelTalentos');

    if (!grupos.length) {
        tabs.innerHTML = '';
        panel.innerHTML = '<p class="sin-datos">Sin datos de talentos para este personaje.</p>';
        return;
    }

    tabActiva = Math.min(tabActiva, grupos.length - 1);

    function seleccionar(i) {
        tabActiva = i;
        tabs.querySelectorAll('.tab').forEach((boton, j) => boton.setAttribute('aria-selected', String(i === j)));
        panel.innerHTML = `<div class="talento-grid">${grupos[i].talentos.map((t, n) => htmlTalento(t, data.versionJuego, n)).join('')}</div>`;
        refrescarTooltips();
    }

    tabs.innerHTML = grupos.map((g, i) =>
        `<button type="button" class="tab" role="tab" aria-selected="${i === tabActiva}" data-i="${i}">${esc(g.titulo)}</button>`).join('');
    tabs.querySelectorAll('.tab').forEach(boton => {
        boton.addEventListener('click', () => seleccionar(Number(boton.dataset.i)));
    });
    seleccionar(tabActiva);
}

// ---- Estadísticas ----

// Como en la web oficial: de Fuerza/Agilidad/Intelecto solo se muestra el principal de la clase
function filtrarEstadisticas(lista) {
    const primarios = lista.filter(e => PRIMARIOS.includes(e.nombre));
    const principal = primarios.reduce((a, b) => (b.valor > a.valor ? b : a), primarios[0]);
    return lista.filter(e => e.valor > 0 && (!PRIMARIOS.includes(e.nombre) || e === principal));
}

function pintarEstadisticas(estadisticas) {
    const cont = document.getElementById('estadisticas');
    const lista = filtrarEstadisticas(estadisticas || []);
    if (!lista.length) {
        cont.innerHTML = '<p class="sin-datos">Sin estadísticas</p>';
        return;
    }

    cont.innerHTML = lista.map((e, i) => `
        <div class="stat" style="--c:${COLOR_STAT[e.nombre] || '#aaa'}; --i:${i}">
            <span class="stat-valor" data-valor="${Number(e.valor)}" data-sufijo="${PORCENTAJE.includes(e.nombre) ? ' %' : ''}">0</span>
            <span class="stat-nombre">${esc(e.nombre)}</span>
        </div>`).join('');

    cont.querySelectorAll('.stat-valor').forEach(el => {
        animarNumero(el, Number(el.dataset.valor), el.dataset.sufijo);
    });
}

// ---- Cabecera ----

function textoHace(iso) {
    if (!iso) return '';
    const minutos = Math.round((Date.now() - new Date(iso).getTime()) / 60000);
    if (isNaN(minutos)) return '';
    if (minutos < 1) return 'Actualizado ahora mismo';
    if (minutos < 60) return `Actualizado hace ${minutos} min`;
    const horas = Math.round(minutos / 60);
    if (horas < 24) return `Actualizado hace ${horas} h`;
    return `Actualizado hace ${Math.round(horas / 24)} días`;
}

function pintarCabecera(data) {
    document.documentElement.style.setProperty('--clase', colorClase(data.clase));
    document.title = `${data.nombre} - Azeroth Legends`;
    document.getElementById('nombrePersonaje').textContent = data.nombre;

    const subtitulo = [data.especializacion, data.heroe].filter(Boolean).join(' · ');
    const elSub = document.getElementById('subtitulo');
    elSub.textContent = subtitulo;
    elSub.hidden = !subtitulo;

    const chips = [
        { texto: `Nivel ${data.nivel ?? '?'}` },
        { texto: data.raza },
        { texto: data.clase, clase: 'chip-clase' },
        { texto: `${nombreReino(data.reino)} (${String(data.region).toUpperCase()})` },
        { texto: etiquetaVersion(data.versionJuego), clase: `chip-${data.versionJuego}` }
    ].filter(c => c.texto);
    document.getElementById('chips').innerHTML = chips
        .map((c, i) => `<span class="chip ${c.clase || ''}" style="--i:${i}">${esc(c.texto)}</span>`).join('');

    document.getElementById('textoActualizado').textContent = textoHace(data.actualizado);

    const caja = document.getElementById('renderBox');
    const imagen = document.getElementById('imagenPersonaje');
    if (data.imagen) {
        // La imagen ya llega recortada desde el servidor
        imagen.onerror = () => { caja.hidden = true; };
        imagen.src = data.imagen;
        caja.hidden = false;
    } else {
        caja.hidden = true;
    }
}

function renderPersonaje(data) {
    datosActuales = data;
    pintarCabecera(data);
    pintarEstadisticas(data.estadisticas);
    pintarEquipo(data.equipo, data.versionJuego);
    pintarTalentos(data);

    document.getElementById('esqueleto').hidden = true;
    document.getElementById('ficha').hidden = false;
    refrescarTooltips();
}

// ---- Carga ----

function mostrarEstado(texto, esError) {
    const e = document.getElementById('estado');
    e.hidden = !texto;
    e.innerHTML = texto || '';
    e.classList.toggle('estado-error', !!esError);
}

async function cargarPersonaje() {
    const { nombre, reino, region, version } = parametros;
    const boton = document.getElementById('botonActualizar');
    const primeraVez = !datosActuales;

    boton.disabled = true;
    boton.textContent = '⟳ Actualizando…';
    if (primeraVez) {
        document.getElementById('esqueleto').hidden = false;
        mostrarEstado('Cargando personaje… (la primera vez puede tardar unos segundos)');
    }

    try {
        const query = new URLSearchParams({ nombre, reino, region, version });
        const res = await fetch(`/api/personajes/detalles?${query}`);

        let cuerpo = {};
        try { cuerpo = await res.json(); } catch (e) { /* sin JSON */ }

        if (!res.ok) {
            document.getElementById('esqueleto').hidden = true;
            mostrarEstado(
                `${esc(cuerpo.error || 'No se pudo cargar el personaje.')} <a href="/index">Volver al buscador</a>`,
                true);
            return;
        }

        mostrarEstado('');
        renderPersonaje(cuerpo);
        if (!primeraVez) mostrarToast('Datos actualizados');
    } catch (err) {
        console.error('Error cargando personaje:', err);
        document.getElementById('esqueleto').hidden = true;
        mostrarEstado('No se pudo conectar con el servidor. <a href="/index">Volver al buscador</a>', true);
    } finally {
        boton.disabled = false;
        boton.textContent = '⟳ Actualizar datos';
    }
}

async function copiarEnlace() {
    try {
        await navigator.clipboard.writeText(window.location.href);
        mostrarToast('Enlace copiado');
    } catch (e) {
        mostrarToast('No se pudo copiar el enlace');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(window.location.search);
    parametros = {
        nombre: params.get('nombre'),
        reino: params.get('reino'),
        region: params.get('region'),
        version: params.get('version')
    };

    if (!parametros.nombre || !parametros.reino || !parametros.region || !parametros.version) {
        document.getElementById('esqueleto').hidden = true;
        mostrarEstado('Falta información del personaje. <a href="/index">Volver al buscador</a>', true);
        return;
    }

    document.getElementById('botonActualizar').addEventListener('click', cargarPersonaje);
    document.getElementById('botonCompartir').addEventListener('click', copiarEnlace);
    cargarPersonaje();
});
