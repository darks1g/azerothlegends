document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('buscarForm');
    const regionSelect = document.getElementById('regionSelect');
    const versionSelect = document.getElementById('version');
    const reinoSelect = document.getElementById('reinoSelect');
    const reinoSearch = document.getElementById('reinoSearch');
    const reinoSuggestions = document.getElementById('reinoSuggestions');
    const reinoStatus = document.getElementById('reinoStatus');
    const botonBuscar = document.getElementById('botonBuscar');
    const loading = document.getElementById('loader-anim');
    const mensaje = document.getElementById('mensajeError');
    let reinosDisponibles = [];
    let sugerenciasActuales = [];
    let sugerenciaActiva = -1;
    document.body.appendChild(reinoSuggestions);

    function mostrarError(texto) {
        mensaje.textContent = texto || '';
        mensaje.hidden = !texto;
    }

    // Recuerda la última versión y reino elegidos (si el navegador lo permite)
    function recordar(clave, valor) {
        try { localStorage.setItem(clave, valor); } catch (e) { /* sin almacenamiento: se ignora */ }
    }
    function recuperar(clave) {
        try { return localStorage.getItem(clave); } catch (e) { return null; }
    }

    function normalizar(texto) {
        return texto
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .toLocaleLowerCase('es')
            .replace(/[^\p{L}\p{N}]+/gu, ' ')
            .trim();
    }

    function cerrarSugerencias() {
        reinoSuggestions.hidden = true;
        reinoSearch.setAttribute('aria-expanded', 'false');
        reinoSearch.removeAttribute('aria-activedescendant');
        sugerenciaActiva = -1;
    }

    function posicionarSugerencias() {
        if (reinoSuggestions.hidden) return;

        const campo = reinoSearch.getBoundingClientRect();
        const margen = 8;
        const espacioAbajo = window.innerHeight - campo.bottom - margen;
        const espacioArriba = campo.top - margen;
        const abrirArriba = espacioAbajo < 220 && espacioArriba > espacioAbajo;
        const espacioDisponible = Math.max(120, Math.min(480, abrirArriba ? espacioArriba : espacioAbajo));

        reinoSuggestions.style.left = `${Math.max(margen, campo.left)}px`;
        reinoSuggestions.style.width = `${Math.min(campo.width, window.innerWidth - margen * 2)}px`;
        reinoSuggestions.style.maxHeight = `${espacioDisponible}px`;
        reinoSuggestions.style.top = abrirArriba
            ? `${Math.max(margen, campo.top - Math.min(reinoSuggestions.scrollHeight, espacioDisponible) - 6)}px`
            : `${campo.bottom + 6}px`;
    }

    function seleccionarReino(reino) {
        reinoSelect.value = reino.slug;
        reinoSearch.value = reino.nombre || reino.slug;
        reinoStatus.textContent = `Reino seleccionado: ${reino.nombre || reino.slug}`;
        cerrarSugerencias();
        mostrarError('');
    }

    function mostrarSugerencias() {
        const consulta = normalizar(reinoSearch.value);
        const palabras = consulta.split(/\s+/).filter(Boolean);

        sugerenciasActuales = reinosDisponibles
            .map(reino => {
                const nombre = normalizar(reino.nombre || reino.slug);
                const slug = normalizar(reino.slug);
                let puntuacion = 0;

                if (!consulta) puntuacion = 1;
                else if (nombre.startsWith(consulta)) puntuacion = 4;
                else if (nombre.split(' ').some(palabra => palabra.startsWith(consulta))) puntuacion = 3;
                else if (palabras.every(palabra => nombre.includes(palabra))) puntuacion = 2;
                else if (slug.includes(consulta)) puntuacion = 1;

                return { reino, puntuacion };
            })
            .filter(resultado => resultado.puntuacion > 0)
            .sort((a, b) => b.puntuacion - a.puntuacion
                || (a.reino.nombre || a.reino.slug).localeCompare(b.reino.nombre || b.reino.slug, 'es'))
            .map(resultado => resultado.reino);

        reinoSuggestions.replaceChildren();
        sugerenciaActiva = -1;

        if (sugerenciasActuales.length === 0) {
            cerrarSugerencias();
            reinoStatus.textContent = consulta
                ? 'No hay reinos que coincidan. Prueba con el nombre o el slug.'
                : 'No hay reinos disponibles.';
            return;
        }

        sugerenciasActuales.forEach((reino, indice) => {
            const opcion = document.createElement('button');
            opcion.type = 'button';
            opcion.id = `reino-option-${indice}`;
            opcion.className = 'realm-option';
            opcion.setAttribute('role', 'option');
            opcion.setAttribute('aria-selected', 'false');

            const nombre = document.createElement('span');
            nombre.className = 'realm-option-name';
            nombre.textContent = reino.nombre || reino.slug;
            opcion.appendChild(nombre);

            if (reino.nombre && reino.slug && normalizar(reino.nombre) !== normalizar(reino.slug)) {
                const slug = document.createElement('span');
                slug.className = 'realm-option-slug';
                slug.textContent = reino.slug;
                opcion.appendChild(slug);
            }

            opcion.addEventListener('mousedown', evento => evento.preventDefault());
            opcion.addEventListener('click', () => seleccionarReino(reino));
            reinoSuggestions.appendChild(opcion);
        });

        reinoStatus.textContent = consulta
            ? `${sugerenciasActuales.length} sugerencia${sugerenciasActuales.length === 1 ? '' : 's'} de ${reinosDisponibles.length} reinos.`
            : `${reinosDisponibles.length} reinos disponibles. Escribe para filtrar.`;
        reinoSuggestions.hidden = false;
        reinoSearch.setAttribute('aria-expanded', 'true');
        posicionarSugerencias();
    }

    function activarSugerencia(indice) {
        const opciones = reinoSuggestions.querySelectorAll('.realm-option');
        if (!opciones.length) return;

        sugerenciaActiva = ((indice % opciones.length) + opciones.length) % opciones.length;
        opciones.forEach((opcion, i) => {
            const activa = i === sugerenciaActiva;
            opcion.setAttribute('aria-selected', String(activa));
            if (activa) {
                reinoSearch.setAttribute('aria-activedescendant', opcion.id);
                const opcionArriba = opcion.offsetTop;
                const opcionAbajo = opcionArriba + opcion.offsetHeight;
                if (opcionArriba < reinoSuggestions.scrollTop) {
                    reinoSuggestions.scrollTop = opcionArriba;
                } else if (opcionAbajo > reinoSuggestions.scrollTop + reinoSuggestions.clientHeight) {
                    reinoSuggestions.scrollTop = opcionAbajo - reinoSuggestions.clientHeight;
                }
            }
        });
    }

    // Carga los reinos de la región y la versión de juego elegidas
    async function cargarReinos(reinoPreferido) {
        const region = regionSelect.value;
        const version = versionSelect.value;

        reinoSelect.disabled = true;
        reinoSearch.disabled = true;
        reinoSearch.value = '';
        reinoSelect.value = '';
        reinosDisponibles = [];
        cerrarSugerencias();
        reinoStatus.textContent = 'Cargando reinos…';

        try {
            const res = await fetch(`/api/reinos?region=${encodeURIComponent(region)}&version=${encodeURIComponent(version)}`, {
                credentials: 'same-origin'
            });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const reinos = await res.json();

            reinos.sort((a, b) => (a.nombre || a.slug || '').localeCompare(b.nombre || b.slug || '', 'es'));
            reinosDisponibles = reinos.filter(reino =>
                typeof reino.slug === 'string'
                && reino.slug.length > 0
                && !/^\s*(?:eu\d+|eu[\s_-]+|rdb[\s_-]+)/i.test(reino.nombre || '')
                && !/^\s*(?:eu\d+|eu[\s_-]+|rdb[\s_-]+)/i.test(reino.slug)
            );

            if (reinosDisponibles.length === 0) {
                reinoStatus.textContent = 'No hay reinos disponibles para esta versión.';
                return;
            }

            reinoStatus.textContent = `${reinosDisponibles.length} reinos disponibles.`;
            if (reinoPreferido) {
                const preferido = reinosDisponibles.find(reino => reino.slug === reinoPreferido);
                if (preferido) seleccionarReino(preferido);
            }
        } catch (err) {
            console.error('Error cargando reinos:', err);
            reinoStatus.textContent = 'No se pudieron cargar los reinos. Inténtalo de nuevo más tarde.';
        } finally {
            reinoSelect.disabled = false;
            reinoSearch.disabled = false;
        }
    }

    reinoSearch.addEventListener('input', () => {
        reinoSelect.value = '';
        mostrarError('');
        mostrarSugerencias();
    });

    reinoSearch.addEventListener('focus', mostrarSugerencias);

    reinoSearch.addEventListener('keydown', evento => {
        if (evento.key === 'ArrowDown' && !reinoSuggestions.hidden) {
            evento.preventDefault();
            activarSugerencia(sugerenciaActiva + 1);
        } else if (evento.key === 'ArrowUp' && !reinoSuggestions.hidden) {
            evento.preventDefault();
            activarSugerencia(sugerenciaActiva < 0 ? -1 : sugerenciaActiva - 1);
        } else if (evento.key === 'Enter' && sugerenciaActiva >= 0 && !reinoSuggestions.hidden) {
            evento.preventDefault();
            seleccionarReino(sugerenciasActuales[sugerenciaActiva]);
        } else if (evento.key === 'Escape') {
            cerrarSugerencias();
        }
    });

    document.addEventListener('click', evento => {
        if (!reinoSuggestions.contains(evento.target) && evento.target !== reinoSearch) {
            cerrarSugerencias();
        }
    });

    window.addEventListener('resize', posicionarSugerencias);
    window.addEventListener('scroll', posicionarSugerencias, true);

    // Restaurar la última selección
    const versionGuardada = recuperar('al_version');
    if (versionGuardada && [...versionSelect.options].some(o => o.value === versionGuardada)) {
        versionSelect.value = versionGuardada;
    }
    cargarReinos(recuperar('al_reino'));

    versionSelect.addEventListener('change', () => {
        recordar('al_version', versionSelect.value);
        mostrarError('');
        cargarReinos();
    });

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        mostrarError('');

        const data = Object.fromEntries(new FormData(form).entries());
        if (!data.reino || !reinosDisponibles.some(reino => reino.slug === data.reino)) {
            mostrarError('Selecciona un reino de la lista de sugerencias.');
            return;
        }

        loading.style.display = 'block';
        botonBuscar.disabled = true;

        try {
            const res = await fetch('/api/personajes/buscar', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data),
                credentials: 'same-origin'
            });

            let cuerpo = {};
            try { cuerpo = await res.json(); } catch (e) { /* respuesta sin JSON */ }

            if (res.ok) {
                recordar('al_version', data.version);
                recordar('al_reino', data.reino);
                // Se usa el nombre oficial que devuelve el servidor (con su mayúscula correcta)
                window.location.href = urlFicha({
                    nombre: cuerpo.nombre || data.nombre,
                    reino: data.reino,
                    region: data.region,
                    version: data.version
                });
            } else {
                mostrarError(cuerpo.error || 'No se pudo buscar el personaje. Inténtalo de nuevo.');
            }
        } catch (err) {
            console.error(err);
            mostrarError('No se pudo conectar con el servidor. Comprueba que está en marcha.');
        } finally {
            loading.style.display = 'none';
            botonBuscar.disabled = false;
        }
    });
});
