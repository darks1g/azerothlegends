package com.wowapp.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.OptionalInt;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class ModelViewerAssetControllerTest {

    @Test
    void aceptaSoloRecursosDelVisorConExtensionesPermitidas() {
        assertTrue(ModelViewerAssetController.isSafeAssetPath(
                "mists/deployment/viewer/c3f890f/viewer.min.js"));
        assertTrue(ModelViewerAssetController.isSafeAssetPath("meta/charactercustomization/15.json"));
        assertTrue(ModelViewerAssetController.isSafeAssetPath("mo3/123.mo3"));
        assertTrue(ModelViewerAssetController.isSafeAssetPath("anim/123.anim"));
        assertTrue(ModelViewerAssetController.isSafeAssetPath("bone/123.bone"));
        assertTrue(ModelViewerAssetController.isSafeAssetPath("m2/122414.m2"));
        assertTrue(ModelViewerAssetController.isSafeAssetPath("skel/123.skel"));
        assertTrue(ModelViewerAssetController.isSafeAssetPath("skin/122414.skin"));
    }

    @Test
    void rechazaRutasTraversalYExtensionesFueraDeLaLista() {
        assertFalse(ModelViewerAssetController.isSafeAssetPath("../application.properties"));
        assertFalse(ModelViewerAssetController.isSafeAssetPath("meta/../../secrets.json"));
        assertFalse(ModelViewerAssetController.isSafeAssetPath("anything.php"));
        assertFalse(ModelViewerAssetController.isSafeAssetPath("meta//15.json"));
        assertFalse(ModelViewerAssetController.isSafeAssetPath("meta/%2e%2e/15.json"));
    }

    @Test
    void extraeDisplayIdYTipoDeInventarioDeWowhead() {
        ModelViewerAssetController.ItemModelData modelData =
                ModelViewerAssetController.parseItemModelData(
                        "<inventorySlot id=\"5\">Chest</inventorySlot>"
                                + "<json><![CDATA[\"displayid\":78292]]></json>");

        assertEquals(78292, modelData.displayId());
        assertEquals(5, modelData.inventoryType());
    }

    @Test
    void rechazaObjetosSinModeloVisible() {
        assertThrows(ResponseStatusException.class,
                () -> ModelViewerAssetController.parseItemModelData(
                        "<inventorySlot id=\"11\">Finger</inventorySlot>"));
    }

    @Test
    void seleccionaElRepositorioDelModeloSegunElTipoDeInventario() {
        assertEquals("meta/armor/20/78292.json", ModelViewerAssetController.metadataPath(20, 78292));
        assertEquals("meta/item/68391.json", ModelViewerAssetController.metadataPath(17, 68391));
    }

    @Test
    void leeElDisplayIdDeLaTablaDeApariencias() {
        String appearances = "\uFEFFID,ItemID,ItemAppearanceID\n"
                + "220712,224951,94547\n"
                + "20712,42768,9718\n";

        assertEquals(OptionalInt.of(42768),
                ModelViewerAssetController.parseCsvInteger(appearances, 20712, "ItemID"));
        assertEquals(OptionalInt.of(689129),
                ModelViewerAssetController.parseCsvInteger(
                        "ID,DisplayType,ItemDisplayInfoID,DefaultIconFileDataID\n"
                                + "9718,6,689129,132551\n",
                        9718, "ItemDisplayInfoID"));
        assertEquals(OptionalInt.empty(),
                ModelViewerAssetController.parseCsvInteger(
                        "ID,ItemID\n220712,224951\n", 20712, "ItemID"));
    }

    @Test
    void classicEraUsaRecursosDelVisorModernoCompatibleConModelosM2() {
        assertEquals("live", ModelViewerAssetController.upstreamFlavorFor("retail"));
        assertEquals("mists", ModelViewerAssetController.upstreamFlavorFor("classic"));
        assertEquals("mists", ModelViewerAssetController.upstreamFlavorFor("classic_era"));
    }
}
