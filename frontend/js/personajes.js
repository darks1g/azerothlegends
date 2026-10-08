const TAMANO_PAGINA = 12;

let paginaActual = 0;
let totalPaginas = 0;

function mostrarEstado(texto, esError) {
    const e = document.getElementById('estado');
    e.hidden = !texto;
    e.textContent = texto || '';
    e.classList.toggle('estado-error', !!esError);
}

function filtrosActuales() {
    return {
        nombre: document.getElementById('filtroNombre').value.trim(),
        clase: document.getElementById('filtroClase').value,
        version: document.getElementById('filtroVersion').value
    };
}

// Guarda los filtros en la URL para poder volver atrás o compartir el enlace
function guardarEnUrl(filtros, pagina) {
    const params = new URLSearchParams();
    Object.entries(filtros).forEach(([k, v]) => { if (v) params.set(k, v); });
    if (pagina > 0) params.set('pagina', pagina);
    const query = params.toString();
    history.replaceState(null, '', query ? `?${query}` : location.pathname);
}

function htmlTarjeta(p, indice) {
    const detalle = [p.especializacion, p.heroe].filter(Boolean).join(' · ');
    const color = colorClase(p.clase);
    return `<a class="card-personaje" href="${esc(urlFicha(p))}" style="--clase:${color}; --i:${indice}">
        <span class="card-nombre">${esc(p.nombre)}</span>
        <span class="card-linea">Nivel ${esc(p.nivel ?? '?')} ${esc(p.raza || '')} ${esc(p.clase || '')}</span>
        ${detalle ? `<span class="card-linea">${esc(detalle)}</span>` : ''}
        <span class="card-linea">${esc(nombreReino(p.reino))} (${esc(String(p.region).toUpperCase())})</span>
        <span class="card-etiqueta">${esc(etiquetaVersion(p.versionJuego))}</span>
    </a>`;
}

async function cargar(pagina) {
    const filtros = filtrosActuales();
    paginaActual = pagina;
    guardarEnUrl(filtros, pagina);
    mostrarEstado('Cargando…');

    try {
        const query = new URLSearchParams({ pagina, tamano: TAMANO_PAGINA });
        Object.entries(filtros).forEach(([k, v]) => { if (v) query.set(k, v); });

        const res = await fetch(`/api/personajes?${query}`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const datos = await res.json();

        totalPaginas = datos.totalPaginas;
        document.getElementById('resultados').innerHTML = datos.contenido.map((p, i) => htmlTarjeta(p, i)).join('');

        if (datos.total === 0) {
            mostrarEstado('No hay personajes con esos filtros.');
        } else {
            mostrarEstado('');
        }

        const nav = document.getElementById('paginacion');
        nav.hidden = totalPaginas <= 1;
        document.getElementById('infoPagina').textContent =
            `Página ${datos.pagina + 1} de ${Math.max(totalPaginas, 1)} · ${datos.total} personajes`;
        document.getElementById('anterior').disabled = datos.pagina <= 0;
        document.getElementById('siguiente').disabled = datos.pagina + 1 >= totalPaginas;
    } catch (err) {
        console.error(err);
        document.getElementById('resultados').innerHTML = '';
        document.getElementById('paginacion').hidden = true;
        mostrarEstado('No se pudo cargar el listado. Comprueba que el servidor está en marcha.', true);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    // Restaurar filtros desde la URL
    const params = new URLSearchParams(location.search);
    document.getElementById('filtroNombre').value = params.get('nombre') || '';
    document.getElementById('filtroClase').value = params.get('clase') || '';
    document.getElementById('filtroVersion').value = params.get('version') || '';
    const pagina = parseInt(params.get('pagina') || '0', 10);

    document.getElementById('filtros').addEventListener('submit', e => {
        e.preventDefault();
        cargar(0);
    });
    document.getElementById('filtroClase').addEventListener('change', () => cargar(0));
    document.getElementById('filtroVersion').addEventListener('change', () => cargar(0));
    document.getElementById('anterior').addEventListener('click', () => cargar(paginaActual - 1));
    document.getElementById('siguiente').addEventListener('click', () => cargar(paginaActual + 1));

    cargar(isNaN(pagina) ? 0 : Math.max(pagina, 0));
});
