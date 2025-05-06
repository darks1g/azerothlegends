document.addEventListener('DOMContentLoaded', () => {
    fetch('/php/perfil-menu.php')
      .then(res => res.text())
      .then(html => {
        document.getElementById('perfilMenuContainer').innerHTML = html;
  
        // Desplegable básico
        const toggle = document.getElementById('perfilToggle');
        const menu = document.querySelector('.perfil-menu');
  
        toggle.addEventListener('click', (e) => {
          e.stopPropagation();
          if (menu) {
            menu.style.display = menu.style.display === 'block' ? 'none' : 'block';
          }
        });
  
        document.addEventListener('click', () => {
          if (menu) menu.style.display = 'none';
        });
      })
      .catch(err => {
        console.error('Error cargando el menú del perfil:', err);
      });
  });
  