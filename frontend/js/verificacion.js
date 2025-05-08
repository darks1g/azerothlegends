document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('verificacionForm');
  const errorField = document.getElementById('verificationError');

  form.addEventListener('submit', async (e) => {
      e.preventDefault();

      const codigo = document.getElementById('codigo').value.trim();

      const formData = new FormData();
      formData.append('codigo', codigo);

      try {
          // Verifica el código
          const res = await fetch('/php/verificar-codigo.php', {
              method: 'POST',
              body: formData,
              credentials: 'same-origin'
          });

          const data = await res.json();

          if (!data.success) {
              errorField.textContent = data.error || 'Código incorrecto.';
              errorField.style.display = 'block';
              return;
          }

          // Si el código fue correcto, intenta iniciar sesión en Java
          const loginRes = await fetch('/api/login', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({
                  email: data.email,
                  password: data.password
              }),
              credentials: 'include'
          });

          if (loginRes.ok) {
              sessionStorage.removeItem('origenVerificacion');
              window.location.href = '/';
          } else {
              const errorText = await loginRes.text();
              errorField.textContent = 'Verificado, pero no se pudo iniciar sesión: ' + errorText;
              errorField.style.display = 'block';
          }
      } catch (err) {
          errorField.textContent = 'Error al verificar el código o iniciar sesión.';
          errorField.style.display = 'block';
      }
  });
});
