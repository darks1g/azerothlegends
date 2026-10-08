document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('buscarForm');
    const regionSelect = document.getElementById('regionSelect');
    const versionSelect = document.getElementById('version');
    const reinoSelect = document.getElementById('reinoSelect');
    const botonBuscar = document.getElementById('botonBuscar');
    const loading = document.getElementById('loader-anim');
    const mensaje = document.getElementById('mensajeError');

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

    // Carga los reinos de la región y la versión de juego elegidas
    async function cargarReinos(reinoPreferido) {
        const region = regionSelect.value;
        const version = versionSelect.value;

        reinoSelect.disabled = true;
        reinoSelect.innerHTML = '<option value="" disabled selected>Cargando reinos…</option>';

        try {
            const res = await fetch(`/api/reinos?region=${encodeURIComponent(region)}&version=${encodeURIComponent(version)}`);
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const reinos = await res.json();

            reinos.sort((a, b) => (a.nombre || a.slug || '').localeCompare(b.nombre || b.slug || '', 'es'));

            if (reinos.length === 0) {
                reinoSelect.innerHTML = '<option value="" disabled selected>No hay reinos disponibles</option>';
                return;
            }

            reinoSelect.innerHTML = '<option value="" disabled selected>Selecciona un reino</option>';
            reinos.forEach(reino => {
                const option = document.createElement('option');
                option.value = reino.slug;
                option.textContent = reino.nombre || reino.slug;
                reinoSelect.appendChild(option);
            });

            if (reinoPreferido && reinos.some(r => r.slug === reinoPreferido)) {
                reinoSelect.value = reinoPreferido;
            }
        } catch (err) {
            console.error('Error cargando reinos:', err);
            reinoSelect.innerHTML = '<option value="" disabled selected>Error al cargar los reinos</option>';
        } finally {
            reinoSelect.disabled = false;
        }
    }

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
        if (!data.reino) {
            mostrarError('Selecciona un reino.');
            return;
        }

        loading.style.display = 'block';
        botonBuscar.disabled = true;

        try {
            const res = await fetch('/api/personajes/buscar', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
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
