document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('verificacionForm');
  const errorField = document.getElementById('verificationError');

  form.addEventListener('submit', async (e) => {
      e.preventDefault();

      const codigo = document.getElementById('codigo').value.trim();

      try {
          const res = await fetch('/api/verificar-codigo', {
              method: 'POST',
              headers: {
                  'Content-Type': 'application/json'
              },
              body: JSON.stringify({ codigo })
          });

          if (res.redirected) {
              window.location.href = res.url;
              return;
          }

          const data = await res.json();
          errorField.textContent = data.error || 'Código incorrecto.';
          errorField.style.display = 'block';

      } catch (err) {
          errorField.textContent = 'Error al verificar el código.';
          errorField.style.display = 'block';
      }
  });
});
