document.addEventListener('DOMContentLoaded', () => {
    // Realiza una solicitud para obtener el contenido del menú de perfil
    fetch('/api/perfil-menu', { credentials: 'same-origin' })
      .then(res => res.text())
      .then(html => {
        // Inserta el contenido del menú en el contenedor correspondiente
        const container = document.getElementById('perfilMenuContainer');
        container.innerHTML = html;
        const menu = container.querySelector('.perfil-menu');
        if (menu && !menu.querySelector('a[href="/personajes"]')) {
          const item = document.createElement('li');
          const link = document.createElement('a');
          link.href = '/personajes';
          link.textContent = 'Personajes Recientes';
          item.appendChild(link);
          menu.prepend(item);
        }
  
        // Obtiene los elementos necesarios para el funcionamiento del menú desplegable
        const toggle = document.getElementById('perfilToggle');
  
        // Mostrar/Ocultar el menú
        toggle?.addEventListener('click', (e) => {
          e.stopPropagation();
          if (menu) {
            menu.style.display = menu.style.display === 'block' ? 'none' : 'block';
          }
        });
  
        document.addEventListener('click', () => {
          if (menu) menu.style.display = 'none';
        });
  
        // 🔥 Añadir evento de cerrar sesión justo después de insertar el HTML
        const cerrarSesionBtn = document.getElementById('cerrarSesion');
        if (cerrarSesionBtn) {
          cerrarSesionBtn.addEventListener('click', async (e) => {
            e.preventDefault();
            try {
              await fetch('/api/logout', { method: 'POST', credentials: 'same-origin' });
              window.location.href = '/login';
            } catch (err) {
              console.error('Error cerrando sesión:', err);
            }
          });
        }
      })
      .catch(err => {
        console.error('Error cargando el menú del perfil:', err);
      });
  });
  