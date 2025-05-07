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

            let datos;
            try {
                datos = await res.json();
            } catch {
                errorField.textContent = 'Error al procesar la respuesta del servidor.';
                errorField.style.display = 'block';
                return;
            }

            if (!res.ok) {
                errorField.textContent = datos.error || 'Error al iniciar sesión.';
                errorField.style.display = 'block';
                return;
            }

            // Redirige a verificacion.php con POST (crea y envía un formulario)
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = '/verificacion.php';

            const inputEmail = document.createElement('input');
            inputEmail.type = 'hidden';
            inputEmail.name = 'email';
            inputEmail.value = datos.email;

            const inputOrigen = document.createElement('input');
            inputOrigen.type = 'hidden';
            inputOrigen.name = 'origen';
            inputOrigen.value = 'login';

            form.appendChild(inputEmail);
            form.appendChild(inputOrigen);
            document.body.appendChild(form);
            form.submit();
        } catch (err) {
            errorField.textContent = 'Error de conexión con el servidor.';
            errorField.style.display = 'block';
        }
    });
});
