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
        //renderPersonaje(data);
    } catch (err) {
        console.error('Error cargando personaje:', err);
        document.body.innerHTML = `<p>Error al cargar personaje: ${err.message}</p>`;
    }
});
