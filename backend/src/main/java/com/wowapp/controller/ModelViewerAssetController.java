package com.wowapp.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/model-viewer/assets")
public class ModelViewerAssetController {

    private static final Logger log = LoggerFactory.getLogger(ModelViewerAssetController.class);
    private static final int MAX_ASSET_BYTES = 20 * 1024 * 1024;
    private static final int MAX_CSV_BYTES = 64 * 1024;
    private static final Map<String, String> UPSTREAM_FLAVORS = Map.of(
            "retail", "live",
            "classic", "mists",
            "classic_era", "mists");
    private static final Map<String, String> WOWHEAD_FLAVORS = Map.of(
            "retail", "",
            "classic", "mop-classic/",
            "classic_era", "classic/");
    private static final Pattern DISPLAY_ID = Pattern.compile("\"displayid\"\\s*:\\s*(\\d+)");
    private static final Pattern INVENTORY_TYPE = Pattern.compile("<inventorySlot id=\"(\\d+)\"");
    private static final Set<Integer> ARMOR_INVENTORY_TYPES = Set.of(1, 3, 4, 5, 6, 7, 8, 9, 10, 16, 19, 20);
    private static final Map<String, MediaType> CONTENT_TYPES = Map.ofEntries(
            Map.entry("blp", MediaType.APPLICATION_OCTET_STREAM),
            Map.entry("anim", MediaType.APPLICATION_OCTET_STREAM),
            Map.entry("bone", MediaType.APPLICATION_OCTET_STREAM),
            Map.entry("css", MediaType.valueOf("text/css")),
            Map.entry("gif", MediaType.IMAGE_GIF),
            Map.entry("jpeg", MediaType.IMAGE_JPEG),
            Map.entry("jpg", MediaType.IMAGE_JPEG),
            Map.entry("js", MediaType.valueOf("text/javascript")),
            Map.entry("json", MediaType.APPLICATION_JSON),
            Map.entry("m2", MediaType.APPLICATION_OCTET_STREAM),
            Map.entry("mo3", MediaType.APPLICATION_OCTET_STREAM),
            Map.entry("png", MediaType.IMAGE_PNG),
            Map.entry("skin", MediaType.APPLICATION_OCTET_STREAM),
            Map.entry("skel", MediaType.APPLICATION_OCTET_STREAM),
            Map.entry("webp", MediaType.valueOf("image/webp")));

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
    private final Map<Integer, ItemModelData> transmogItemModels = new ConcurrentHashMap<>();

    @GetMapping("/{flavor}/{*assetPath}")
    public ResponseEntity<byte[]> asset(@PathVariable String flavor, @PathVariable String assetPath) {
        String upstreamFlavor = upstreamFlavorFor(flavor);
        String safePath = assetPath.startsWith("/") ? assetPath.substring(1) : assetPath;
        if (upstreamFlavor == null || !isSafeAssetPath(safePath)) {
            return ResponseEntity.notFound().build();
        }

        URI uri = URI.create("https://wow.zamimg.com/modelviewer/" + upstreamFlavor + "/" + safePath);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();
        try {
            HttpResponse<InputStream> response = http.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream body = response.body()) {
                if (response.statusCode() != 200) {
                    log.warn("Wowhead devolvió {} al pedir un recurso del visor {}", response.statusCode(), safePath);
                    HttpStatus status = response.statusCode() == 404 ? HttpStatus.NOT_FOUND : HttpStatus.BAD_GATEWAY;
                    return ResponseEntity.status(status).build();
                }
                byte[] bytes = body.readNBytes(MAX_ASSET_BYTES + 1);
                if (bytes.length > MAX_ASSET_BYTES) {
                    log.warn("El recurso del visor {} supera el tamaño permitido", safePath);
                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
                }
                String extension = safePath.substring(safePath.lastIndexOf('.') + 1).toLowerCase();
                return ResponseEntity.ok()
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePublic())
                        .contentType(CONTENT_TYPES.get(extension))
                        .body(bytes);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Se interrumpió la carga del visor.", e);
        } catch (IOException e) {
            log.warn("No se pudo obtener un recurso del visor {}: {}", safePath, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo cargar el recurso del visor.", e);
        }
    }

    @GetMapping("/{flavor}/items/{itemId}")
    public ResponseEntity<ItemModelData> itemModel(
            @PathVariable String flavor,
            @PathVariable int itemId,
            @RequestParam(required = false) Integer appearanceId) {
        String wowheadFlavor = WOWHEAD_FLAVORS.get(flavor);
        String upstreamFlavor = upstreamFlavorFor(flavor);
        if (wowheadFlavor == null || itemId < 1 || (appearanceId != null && appearanceId < 1)) {
            return ResponseEntity.notFound().build();
        }

        URI uri = URI.create("https://www.wowhead.com/" + wowheadFlavor + "item=" + itemId + "&xml");
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(12))
                .header("User-Agent", "AzerothLegends/1.0")
                .header("Accept", "application/xml")
                .GET()
                .build();
        try {
            HttpResponse<InputStream> response = http.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream body = response.body()) {
                if (response.statusCode() == 404) {
                    return ResponseEntity.notFound().build();
                }
                if (response.statusCode() != 200) {
                    log.warn("Wowhead devolvió {} al resolver el modelo del objeto {}", response.statusCode(), itemId);
                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
                }

                String xml = new String(body.readNBytes(1024 * 1024 + 1), java.nio.charset.StandardCharsets.UTF_8);
                if (xml.length() > 1024 * 1024) {
                    log.warn("La respuesta de Wowhead para el objeto {} supera el tamaño permitido", itemId);
                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
                }
                ItemModelData modelData = parseItemModelData(xml);
                int resolvedDisplayId = modelData.displayId();
                int resolvedInventoryType = modelData.inventoryType();
                if (appearanceId != null) {
                    ItemModelData transmogModel = transmogItemModels.get(appearanceId);
                    if (transmogModel == null) {
                        transmogModel = resolveTransmogModel(appearanceId, wowheadFlavor);
                        if (transmogModel == null) {
                            log.info("No se encontró la apariencia transfigurada {}", appearanceId);
                            return ResponseEntity.unprocessableEntity().build();
                        }
                        transmogItemModels.put(appearanceId, transmogModel);
                    }
                    resolvedDisplayId = transmogModel.displayId();
                    resolvedInventoryType = transmogModel.inventoryType();
                }
                if (resolvedInventoryType == 5
                        && !assetExists(upstreamFlavor, "meta/armor/5/" + resolvedDisplayId + ".json")
                        && assetExists(upstreamFlavor, "meta/armor/20/" + resolvedDisplayId + ".json")) {
                    resolvedInventoryType = 20;
                }
                String metadataPath = metadataPath(resolvedInventoryType, resolvedDisplayId);
                if (!assetExists(upstreamFlavor, metadataPath)) {
                    log.info("El objeto {} no tiene metadatos de modelo en {}: {}",
                            itemId, upstreamFlavor, metadataPath);
                    return ResponseEntity.unprocessableEntity().build();
                }

                return ResponseEntity.ok()
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePublic())
                        .body(new ItemModelData(resolvedDisplayId, resolvedInventoryType));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Se interrumpió la consulta del modelo o su apariencia.", e);
        } catch (IOException e) {
            log.warn("No se pudo consultar el modelo o la apariencia del objeto {}: {}", itemId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No se pudo consultar el modelo o su apariencia.", e);
        }
    }

    private ItemModelData resolveTransmogModel(int appearanceId, String wowheadFlavor)
            throws IOException, InterruptedException {
        OptionalInt transmogItemId = lookupCsvInteger(
                "ItemModifiedAppearance", appearanceId, "ItemID");
        if (transmogItemId.isEmpty() || transmogItemId.getAsInt() < 1) {
            return null;
        }
        return fetchWowheadItemModel(transmogItemId.getAsInt(), wowheadFlavor);
    }

    private OptionalInt lookupCsvInteger(String table, int id, String column)
            throws IOException, InterruptedException {
        URI uri = URI.create("https://wago.tools/db2/" + table
                + "/csv?filter%5BID%5D=" + id);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(12))
                .header("User-Agent", "AzerothLegends/1.0")
                .GET()
                .build();
        HttpResponse<InputStream> response = http.send(request, HttpResponse.BodyHandlers.ofInputStream());
        try (InputStream body = response.body()) {
            if (response.statusCode() != 200) {
                log.warn("Wago devolvió {} al resolver {} en {}", response.statusCode(), id, table);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "No se pudieron consultar los datos de apariencia.");
            }
            byte[] bytes = body.readNBytes(MAX_CSV_BYTES + 1);
            if (bytes.length > MAX_CSV_BYTES) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "La respuesta de datos de apariencia supera el tamaño permitido.");
            }
            return parseCsvInteger(new String(bytes, java.nio.charset.StandardCharsets.UTF_8), id, column);
        }
    }

    private ItemModelData fetchWowheadItemModel(int itemId, String wowheadFlavor)
            throws IOException, InterruptedException {
        URI uri = URI.create("https://www.wowhead.com/" + wowheadFlavor + "item=" + itemId + "&xml");
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(12))
                .header("User-Agent", "AzerothLegends/1.0")
                .header("Accept", "application/xml")
                .GET()
                .build();
        HttpResponse<InputStream> response = http.send(request, HttpResponse.BodyHandlers.ofInputStream());
        try (InputStream body = response.body()) {
            if (response.statusCode() == 404) {
                return null;
            }
            if (response.statusCode() != 200) {
                log.warn("Wowhead devolvió {} al resolver el objeto transfigurado {}", response.statusCode(), itemId);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "No se pudo consultar el modelo del objeto transfigurado.");
            }
            byte[] bytes = body.readNBytes(1024 * 1024 + 1);
            if (bytes.length > 1024 * 1024) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "La respuesta de Wowhead supera el tamaño permitido.");
            }
            return parseItemModelData(new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    static OptionalInt parseCsvInteger(String csv, int id, String column) {
        String[] lines = csv.strip().split("\\R");
        if (lines.length < 2) {
            return OptionalInt.empty();
        }
        String[] headers = lines[0].split(",", -1);
        if (headers.length > 0) {
            headers[0] = headers[0].replace("\uFEFF", "").strip();
        }
        int columnIndex = -1;
        for (int index = 0; index < headers.length; index++) {
            if (headers[index].strip().equals(column)) {
                columnIndex = index;
                break;
            }
        }
        if (columnIndex < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Wago devolvió un formato de datos de apariencia no válido.");
        }
        for (int rowIndex = 1; rowIndex < lines.length; rowIndex++) {
            String[] values = lines[rowIndex].split(",", -1);
            if (values.length == 0 || !values[0].strip().equals(Integer.toString(id))) {
                continue;
            }
            if (columnIndex >= values.length) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Wago devolvió una fila de apariencia incompleta.");
            }
            try {
                return OptionalInt.of(Integer.parseInt(values[columnIndex].strip()));
            } catch (NumberFormatException exception) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Wago devolvió un identificador de apariencia no válido.", exception);
            }
        }
        return OptionalInt.empty();
    }

    static ItemModelData parseItemModelData(String xml) {
        Matcher displayId = DISPLAY_ID.matcher(xml);
        Matcher inventoryType = INVENTORY_TYPE.matcher(xml);
        if (!displayId.find() || !inventoryType.find()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Wowhead no ofrece un modelo visible para este objeto.");
        }
        return new ItemModelData(Integer.parseInt(displayId.group(1)), Integer.parseInt(inventoryType.group(1)));
    }

    static String metadataPath(int inventoryType, int displayId) {
        return ARMOR_INVENTORY_TYPES.contains(inventoryType)
                ? "meta/armor/" + inventoryType + "/" + displayId + ".json"
                : "meta/item/" + displayId + ".json";
    }

    static String upstreamFlavorFor(String flavor) {
        return UPSTREAM_FLAVORS.get(flavor);
    }

    private boolean assetExists(String upstreamFlavor, String assetPath) throws IOException, InterruptedException {
        URI uri = URI.create("https://wow.zamimg.com/modelviewer/" + upstreamFlavor + "/" + assetPath);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(8))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();
        return http.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() == 200;
    }

    public record ItemModelData(int displayId, int inventoryType) {
    }

    static boolean isSafeAssetPath(String path) {
        if (path == null || path.isBlank() || path.length() > 240
                || !path.matches("[A-Za-z0-9._/-]+")) {
            return false;
        }
        for (String segment : path.split("/", -1)) {
            if (segment.isBlank() || segment.equals(".") || segment.equals("..")) {
                return false;
            }
        }
        String extension = path.substring(path.lastIndexOf('.') + 1).toLowerCase();
        return CONTENT_TYPES.containsKey(extension);
    }
}
