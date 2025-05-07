document.addEventListener('DOMContentLoaded', () => {
    // Arreglo con las imágenes disponibles para la Alianza
    const alianzaImgs = ['alianza1.png', 'alianza2.png', 'alianza3.png'];

    // Arreglo con las imágenes disponibles para la Horda
    const hordaImgs = ['horda1.png', 'horda2.png', 'horda3.png'];

    // Función para elegir un elemento aleatorio de una lista
    function elegirAleatorio(lista) {
        const i = Math.floor(Math.random() * lista.length);
        return lista[i];
    }

    // Seleccionar una imagen aleatoria para la Alianza
    const imgAlianza = elegirAleatorio(alianzaImgs);

    // Seleccionar una imagen aleatoria para la Horda
    const imgHorda = elegirAleatorio(hordaImgs);

    // Asignar la imagen seleccionada de la Alianza al elemento correspondiente
    document.getElementById('imagenPersonaje1').src = `/assets/characters/${imgAlianza}`;

    // Asignar la imagen seleccionada de la Horda al elemento correspondiente
    document.getElementById('imagenPersonaje2').src = `/assets/characters/${imgHorda}`;
});
