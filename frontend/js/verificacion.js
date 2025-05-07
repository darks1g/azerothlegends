document.addEventListener('DOMContentLoaded', () => {
    // Verificamos si se ha accedido correctamente desde login o registro
    const origen = sessionStorage.getItem('origenVerificacion');
  
    if (!origen) {
      window.location.href = '/login'; // Redirige si entró directo
      return;
    }
  
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
          body: JSON.stringify({ codigo }),
          redirect: 'follow'
        });
  
        if (res.redirected) {
          // Eliminamos el marcador si ha sido verificado
          sessionStorage.removeItem('origenVerificacion');
          window.location.href = res.url;
        } else {
          const msg = await res.text();
          errorField.textContent = msg || 'Código incorrecto.';
          errorField.style.display = 'block';
        }
      } catch (err) {
        errorField.textContent = 'Error al verificar el código.';
        errorField.style.display = 'block';
      }
    });
  });
  