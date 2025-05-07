document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('loginForm');
    const togglePassword = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('password');
    const eyeIcon = document.getElementById('eyeIcon');
    const errorField = document.getElementById('loginError');

    // Mostrar/Ocultar contraseña
    togglePassword.addEventListener('click', () => {
        const isPasswordVisible = passwordInput.type === 'text';
        passwordInput.type = isPasswordVisible ? 'password' : 'text';
        eyeIcon.src = isPasswordVisible ? '/assets/eye2.png' : '/assets/eye1.png';
    });

    // Login con AJAX
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const email = document.getElementById('email').value.trim();
        const password = passwordInput.value;

        try {
            const res = await fetch('/api/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });

            if (res.ok) {
                window.location.href = '/';
            } else {
                const mensaje = await res.text();
                errorField.textContent = mensaje || 'Error al iniciar sesión.';
                errorField.style.display = 'block';
            }
        } catch (err) {
            errorField.textContent = 'Error de conexión con el servidor.';
            errorField.style.display = 'block';
        }
    });
});
