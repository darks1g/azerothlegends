document.addEventListener('DOMContentLoaded', () => {
    // Realiza una solicitud para obtener el contenido del menú de perfil
    fetch('/api/perfil-menu')
        .then(res => res.text())
        .then(html => {
            // Inserta el contenido del menú en el contenedor correspondiente
            document.getElementById('perfilMenuContainer').innerHTML = html;

            // Obtiene los elementos necesarios para el funcionamiento del menú desplegable
            const toggle = document.getElementById('perfilToggle');
            const menu = document.querySelector('.perfil-menu');

            // Agrega un evento para mostrar u ocultar el menú al hacer clic en el botón
            toggle.addEventListener('click', (e) => {
                e.stopPropagation(); // Evita que el evento se propague
                if (menu) {
                    menu.style.display = menu.style.display === 'block' ? 'none' : 'block';
                }
            });

            // Oculta el menú si se hace clic fuera de él
            document.addEventListener('click', () => {
                if (menu) menu.style.display = 'none';
            });
        })
        .catch(err => {
            // Muestra un error en la consola si falla la carga del menú
            console.error('Error cargando el menú del perfil:', err);
        });
});