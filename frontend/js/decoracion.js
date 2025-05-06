document.addEventListener('DOMContentLoaded', () => {
  const alianzaImgs = ['alianza1.png', 'alianza2.png', 'alianza3.png'];
  const hordaImgs = ['horda1.png', 'horda2.png', 'horda3.png'];

  function elegirAleatorio(lista) {
    const i = Math.floor(Math.random() * lista.length);
    return lista[i];
  }

  const imgAlianza = elegirAleatorio(alianzaImgs);
  const imgHorda = elegirAleatorio(hordaImgs);

  document.getElementById('imagenPersonaje1').src = `/assets/characters/${imgAlianza}`;
  document.getElementById('imagenPersonaje2').src = `/assets/characters/${imgHorda}`;
});
