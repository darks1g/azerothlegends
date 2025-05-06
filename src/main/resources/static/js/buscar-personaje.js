document.addEventListener('DOMContentLoaded', () => {
    const regionSelect = document.getElementById('regionSelect');
    const versionSelect = document.querySelector('select[name="version"]');
    const reinoSelect = document.getElementById('reinoSelect');
  
    async function cargarReinos(region) {
  
      try {
        const res = await fetch(`/api/reinos?region=${region}`);
        const reinos = await res.json();
  
        reinoSelect.innerHTML = '<option value="" disabled selected="selected">Selecciona un reino</option>';
        reinos.forEach(reino => {
          const option = document.createElement('option');
          option.value = reino.slug;
          option.textContent = reino.nombre;
          option.dataset.version = reino.versionJuego;
          reinoSelect.appendChild(option);
        });
      } catch (err) {
        reinoSelect.innerHTML = '<option value="">Error al cargar</option>';
        console.error('Error cargando reinos:', err);
      }
    }
  
    regionSelect.addEventListener('change', () => {
      const region = regionSelect.value;
      if (region) cargarReinos(region);
    });
  
    // Carga inicial
    cargarReinos(regionSelect.value);
  
    document.getElementById('buscarForm').addEventListener('submit', async (e) => {
      e.preventDefault();
      const formData = new FormData(e.target);
      const data = Object.fromEntries(formData.entries());
  
      try {
        const res = await fetch('/api/personajes/buscar', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(data)
        });
  
        const json = await res.json();
        // document.getElementById('resultado').innerText = JSON.stringify(json, null, 2);
      } catch (err) {
        // document.getElementById('resultado').innerText = "❌ Error en la petición.";
        console.error(err);
      }
    });
  });
  