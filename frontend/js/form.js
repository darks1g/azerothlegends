document.addEventListener('DOMContentLoaded', () => {
    const regionSelect = document.getElementById('regionSelect');
    const versionSelect = document.querySelector('select[name="version"]');
    const reinoSelect = document.getElementById('reinoSelect');

    // Función para cargar los reinos según la región seleccionada
    async function cargarReinos(region) {
        try {
            // Realiza una petición a la API para obtener los reinos de la región
            const res = await fetch(`/api/reinos?region=${region}`);
            const reinos = await res.json();

            // Limpia las opciones actuales y agrega una opción por defecto
            reinoSelect.innerHTML = '<option value="" disabled selected="selected">Selecciona un reino</option>';

            // Ordena los reinos alfabéticamente y los agrega al select
            reinos.sort((a, b) => a.nombre.localeCompare(b.nombre))
                .forEach(reino => {
                    const option = document.createElement('option');
                    option.value = reino.slug;
                    option.textContent = reino.nombre;
                    option.dataset.version = reino.versionJuego;
                    reinoSelect.appendChild(option);
                });

        } catch (err) {
            // Muestra un mensaje de error en caso de fallo al cargar los reinos
            reinoSelect.innerHTML = '<option value="">Error al cargar</option>';
            console.error('Error cargando reinos:', err);
        }
    }

    // Evento para cargar los reinos cuando se selecciona una región
    regionSelect.addEventListener('change', () => {
        const region = regionSelect.value;
        if (region) cargarReinos(region);
    });

    // Carga inicial de los reinos al cargar la página
    cargarReinos(regionSelect.value);

    // Maneja el envío del formulario de búsqueda
    document.getElementById('buscarForm').addEventListener('submit', async (e) => {
        e.preventDefault(); // Previene el comportamiento por defecto del formulario
        const formData = new FormData(e.target); // Obtiene los datos del formulario
        const data = Object.fromEntries(formData.entries()); // Convierte los datos a un objeto

        try {
            // Realiza una petición POST a la API para buscar personajes
            const res = await fetch('/api/personajes/buscar', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            const json = await res.json();
            // Aquí se puede manejar la respuesta de la API
        } catch (err) {
            // Maneja errores en la petición
            console.error(err);
        }
    });
});
