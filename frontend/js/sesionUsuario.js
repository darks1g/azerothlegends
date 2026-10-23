document.addEventListener('DOMContentLoaded', () => {
    fetch('/api/sesion-usuario', { credentials: 'same-origin' })
        .then(res => res.json())
        .then(data => {
            if (data.autenticado) {
                window.location.href = '/';
            }
        })
        .catch(err => {
            console.error('Error verificando sesión:', err);
        });
});
