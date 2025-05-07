document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('verificacionForm');
    const errorField = document.getElementById('verificationError');
  
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
  
      const codigo = document.getElementById('codigo').value.trim();
  
      const formData = new FormData();
      formData.append('codigo', codigo);
  
      try {
        const res = await fetch('/php/verificar-codigo.php', {
          method: 'POST',
          body: formData,
          credentials: 'same-origin'
        });
  
        const data = await res.json();
  
        if (data.success) {
          sessionStorage.removeItem('origenVerificacion');
          window.location.href = '/';
        } else {
          errorField.textContent = data.error || 'Código incorrecto.';
          errorField.style.display = 'block';
        }
      } catch (err) {
        errorField.textContent = 'Error al verificar el código.';
        errorField.style.display = 'block';
      }
    });
  });
  