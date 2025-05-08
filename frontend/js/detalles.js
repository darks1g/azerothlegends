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
        if (!res.ok) throw new Error('Personaje no encontrado');
        const data = await res.json();
        renderPersonaje(data);
    } catch (err) {
        console.error('Error cargando personaje:', err);
        document.body.innerHTML = `<p>Error al cargar personaje: ${err.message}</p>`;
    }
});

function renderPersonaje(data) {
    document.getElementById('nombrePersonaje').textContent = `${data.nombre} [${data.nivel}]`;
    document.getElementById('infoBasica').textContent = `${data.raza} ${data.clase} – ${data.reino} (${data.region}) – ${data.versionJuego}`;

    // Estadísticas
    const statsContainer = document.getElementById('estadisticas');
    data.estadisticas.forEach(stat => {
        const div = document.createElement('div');
        div.className = 'stat-box';
        div.innerHTML = `<strong>${stat.nombre}:</strong> ${stat.valor}`;
        statsContainer?.appendChild(div);
    });

    // Separar equipo en dos columnas
    const izquierdaSlots = ['head', 'shoulder', 'back', 'chest', 'shirt', 'tabard', 'wrist'];
    const derechaSlots = ['hands', 'waist', 'legs', 'feet', 'finger1', 'finger2', 'trinket1', 'trinket2', 'main_hand', 'off_hand'];

    const equipamientoIzq = document.getElementById('equipamientoIzquierdo');
    const equipamientoDer = document.getElementById('equipamientoDerecho');

    data.equipo.forEach(item => {
        const div = document.createElement('div');
        div.className = 'equipo-item';
        div.innerHTML = `
            <a href="https://www.wowhead.com/item=${item.itemId}" data-wowhead="item=${item.itemId}&domain=es">
                <img src="https://wow.zamimg.com/images/wow/icons/large/${item.icono}.jpg"
                     class="w-12 h-12 mx-auto mb-1" alt="${item.nombreItem}">
            </a>
            <div class="text-sm text-yellow-300">${item.nombreItem}</div>
            <div class="text-xs text-gray-400">ilvl: ${item.ilvl}</div>
        `;

        const slot = item.slot?.toLowerCase(); // ← normalizamos

        if (izquierdaSlots.includes(slot)) {
            equipamientoIzq.appendChild(div);
        } else if (derechaSlots.includes(slot)) {
            equipamientoDer.appendChild(div);
        } else {
            console.warn('Slot no reconocido:', item.slot);
        }

    });

    // Talentos
    if (data.versionJuego === 'retail') {
        mostrarTalentos(data.talentosClase, 'talentosClase');
        mostrarTalentos(data.talentosSpec, 'talentosSpec');
        mostrarTalentos(data.talentosHero, 'talentosHero');
    } else {
        mostrarTalentos(data.talentos, 'talentosClase');
    }

    // (Opcional) render 3D puede activarse aquí si usas alguna lib externa
    const canvas = document.getElementById('renderCanvas');
    const ctx = canvas.getContext('2d');
    ctx.fillStyle = '#111';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    ctx.fillStyle = '#fff';
    ctx.font = '12px Arial';
    ctx.fillText('Render 3D aquí', 10, 30);
}

function mostrarTalentos(lista, contenedorId) {
    const container = document.getElementById(contenedorId);
    if (!container) {
        console.warn(`⚠️ Contenedor ${contenedorId} no encontrado.`);
        return;
    }

    if (!Array.isArray(lista)) {
        console.warn(`⚠️ Lista de talentos (${contenedorId}) no es un array o está vacía.`);
        return;
    }

    lista.forEach(t => {
        const div = document.createElement('div');
        div.className = 'talento-item';
        div.innerHTML = `
            <a href="https://www.wowhead.com/spell=${t.spellId}" data-wowhead="spell=${t.spellId}&domain=es">
                <img src="https://wow.zamimg.com/images/wow/icons/large/${t.icono}.jpg"
                     class="w-12 h-12 mx-auto mb-1" alt="${t.nombre}">
            </a>
            <div class="text-sm text-white">${t.nombre}</div>
        `;
        container.appendChild(div);
    });
}
