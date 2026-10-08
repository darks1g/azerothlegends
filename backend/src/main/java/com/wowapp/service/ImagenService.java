package com.wowapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Descarga el render del personaje desde Blizzard y le recorta el margen transparente.
// Blizzard sirve el render en un lienzo de 1600x1200 con el personaje pequeño en el centro, y
// su servidor no permite leerlo desde el navegador (CORS), por eso se hace aquí, en el servidor.
@Service
public class ImagenService {

    private static final Logger log = LoggerFactory.getLogger(ImagenService.class);

    // Solo se descargan imágenes del servidor de renders de Blizzard
    private static final String HOST_PERMITIDO = "render.worldofwarcraft.com";
    private static final int MAX_EN_CACHE = 300;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final Map<String, byte[]> cache = new ConcurrentHashMap<>();

    public boolean urlPermitida(String url) {
        try {
            URI uri = URI.create(url);
            return "https".equals(uri.getScheme()) && HOST_PERMITIDO.equals(uri.getHost());
        } catch (Exception e) {
            return false;
        }
    }

    // Devuelve el PNG recortado, o null si no se pudo descargar
    public byte[] obtenerRecortada(String url) {
        if (url == null || !urlPermitida(url)) {
            return null;
        }
        byte[] enCache = cache.get(url);
        if (enCache != null) {
            return enCache;
        }

        try {
            HttpRequest peticion = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();
            HttpResponse<byte[]> respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofByteArray());
            if (respuesta.statusCode() != 200) {
                log.warn("Blizzard respondió {} al pedir la imagen {}", respuesta.statusCode(), url);
                return null;
            }

            byte[] png = recortar(respuesta.body());
            if (cache.size() >= MAX_EN_CACHE) {
                cache.clear();
            }
            cache.put(url, png);
            return png;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            log.warn("No se pudo obtener la imagen {}: {}", url, e.getMessage());
            return null;
        }
    }

    // Quita el margen transparente alrededor del personaje (deja un pequeño borde)
    byte[] recortar(byte[] original) throws IOException {
        BufferedImage imagen = ImageIO.read(new ByteArrayInputStream(original));
        if (imagen == null || !imagen.getColorModel().hasAlpha()) {
            return original;
        }

        int ancho = imagen.getWidth();
        int alto = imagen.getHeight();
        int[] pixeles = imagen.getRGB(0, 0, ancho, alto, null, 0, ancho);

        int minX = ancho, minY = alto, maxX = -1, maxY = -1;
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                int alfa = pixeles[y * ancho + x] >>> 24;
                if (alfa > 24) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }
        if (maxX < 0) {
            return original; // imagen totalmente transparente
        }

        int margen = Math.max(ancho, alto) / 50;
        int sx = Math.max(minX - margen, 0);
        int sy = Math.max(minY - margen, 0);
        int sw = Math.min(maxX + margen, ancho - 1) - sx + 1;
        int sh = Math.min(maxY + margen, alto - 1) - sy + 1;

        // Si apenas hay margen que quitar, se deja como está
        if (sw >= ancho * 0.95 && sh >= alto * 0.95) {
            return original;
        }

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(imagen.getSubimage(sx, sy, sw, sh), "png", salida);
        return salida.toByteArray();
    }
}
