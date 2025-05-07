document.addEventListener('DOMContentLoaded', () => {
    const form = document.querySelector('.login-form');
    const errorField = document.getElementById('registroError');

    function validarPassword(password) {
        const requisitos = [
            { test: /.{8,}/, mensaje: "Debe tener al menos 8 caracteres." },
            { test: /[A-Z]/, mensaje: "Debe tener al menos una letra mayúscula." },
            { test: /[a-z]/, mensaje: "Debe tener al menos una letra minúscula." },
            { test: /[0-9]/, mensaje: "Debe tener al menos un número." },
            { test: /[!@#$%^&*(),.?\":{}|<>]/, mensaje: "Debe tener al menos un carácter especial." }
        ];

        for (const r of requisitos) {
            if (!r.test.test(password)) return r.mensaje;
        }

        return null; // válida
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const email = document.getElementById('email').value.trim();
        const nombre_usuario = document.getElementById('nombre_usuario').value.trim();
        const password = document.getElementById('password').value;
        const confirmar = document.getElementById('confirmar').value;

        errorField.style.display = 'none';
        errorField.textContent = '';

        if (password !== confirmar) {
            errorField.textContent = 'Las contraseñas no coinciden.';
            errorField.style.display = 'block';
            return;
        }

        const errorValidacion = validarPassword(password);
        if (errorValidacion) {
            errorField.textContent = errorValidacion;
            errorField.style.display = 'block';
            return;
        }

        const res = await fetch('/api/registro', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, nombre_usuario, password, confirmar }),
            redirect: 'follow'
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
            errorField.textContent = datos.error || 'Error inesperado al registrar.';
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
        inputOrigen.value = 'registro';

        form.appendChild(inputEmail);
        form.appendChild(inputOrigen);
        document.body.appendChild(form);
        form.submit();
    });

    const passwordField = document.getElementById('password');
    const checklist = {
        length: document.getElementById('check-length'),
        mayus: document.getElementById('check-mayus'),
        minus: document.getElementById('check-minus'),
        num: document.getElementById('check-num'),
        special: document.getElementById('check-special')
    };

    passwordField.addEventListener('input', () => {
        const val = passwordField.value;
        const validaciones = {
            length: /.{8,}/.test(val),
            mayus: /[A-Z]/.test(val),
            minus: /[a-z]/.test(val),
            num: /[0-9]/.test(val),
            special: /[!@#$%^&*(),.?":{}|<>]/.test(val)
        };

        for (const clave in validaciones) {
            if (validaciones[clave]) {
                checklist[clave].textContent = "🟢 " + checklist[clave].textContent.slice(2);
                checklist[clave].classList.add("verificado");
                checklist[clave].classList.remove("no-verificado");
            } else {
                checklist[clave].textContent = "🔴 " + checklist[clave].textContent.slice(2);
                checklist[clave].classList.add("no-verificado");
                checklist[clave].classList.remove("verificado");
            }
        }
    });

    document.querySelectorAll('.toggle-password').forEach(button => {
        button.addEventListener('click', () => {
            const inputId = button.getAttribute('data-target');
            const input = document.getElementById(inputId);
            const img = button.querySelector('img');

            const isVisible = input.type === 'text';
            input.type = isVisible ? 'password' : 'text';
            img.src = isVisible ? '/assets/eye2.png' : '/assets/eye1.png';
            img.alt = isVisible ? 'Mostrar contraseña' : 'Ocultar contraseña';
        });
    });

    document.querySelectorAll('.toggle-password2').forEach(button => {
        button.addEventListener('click', () => {
            const input = document.getElementById('confirmar');
            const img = button.querySelector('img');

            const isVisible = input.type === 'text';
            input.type = isVisible ? 'password' : 'text';
            img.src = isVisible ? '/assets/eye2.png' : '/assets/eye1.png';
            img.alt = isVisible ? 'Mostrar contraseña' : 'Ocultar contraseña';
        });
    });
});
