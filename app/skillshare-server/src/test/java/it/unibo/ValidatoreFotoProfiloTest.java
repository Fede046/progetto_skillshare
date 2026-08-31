package it.unibo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Regole di accettazione delle foto profilo: formato e dimensione.
 */
public class ValidatoreFotoProfiloTest {

    private static final long UN_MEGABYTE = 1024L * 1024;

    // --- Formati accettati entro il limite ---

    @Test
    void testJpgValidoAccettato() {
        assertDoesNotThrow(() -> ValidatoreFotoProfilo.valida("foto.jpg", "image/jpeg", UN_MEGABYTE));
    }

    @Test
    void testJpegValidoAccettato() {
        assertDoesNotThrow(() -> ValidatoreFotoProfilo.valida("foto.jpeg", "image/jpeg", UN_MEGABYTE));
    }

    @Test
    void testPngValidoAccettato() {
        assertDoesNotThrow(() -> ValidatoreFotoProfilo.valida("avatar.png", "image/png", UN_MEGABYTE));
    }

    @Test
    void testEstensioneMaiuscolaAccettata() {
        // I file scelti da Windows arrivano spesso con l'estensione in maiuscolo
        assertDoesNotThrow(() -> ValidatoreFotoProfilo.valida("FOTO.JPG", "image/jpeg", UN_MEGABYTE));
    }

    @Test
    void testContentTypeGenericoNonBloccaUnFileValido() {
        // Alcuni browser mandano un content-type generico: decide l'estensione
        assertDoesNotThrow(
                () -> ValidatoreFotoProfilo.valida("foto.png", "application/octet-stream", UN_MEGABYTE));
        assertDoesNotThrow(() -> ValidatoreFotoProfilo.valida("foto.png", null, UN_MEGABYTE));
    }

    @Test
    void testDimensioneEsattamenteAlLimiteAccettata() {
        assertDoesNotThrow(() -> ValidatoreFotoProfilo.valida(
                "foto.jpg", "image/jpeg", ValidatoreFotoProfilo.DIMENSIONE_MASSIMA_BYTE));
    }

    // --- Formati non supportati ---

    @Test
    void testGifRifiutata() {
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> ValidatoreFotoProfilo.valida("animazione.gif", "image/gif", UN_MEGABYTE));
        assertEquals("Formato non supportato: sono accettate solo immagini JPG e PNG.", ex.getMessage());
    }

    @Test
    void testTxtRifiutato() {
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> ValidatoreFotoProfilo.valida("note.txt", "text/plain", 500));
        assertEquals("Formato non supportato: sono accettate solo immagini JPG e PNG.", ex.getMessage());
    }

    @Test
    void testFileSenzaEstensioneRifiutato() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidatoreFotoProfilo.valida("immagine", "image/jpeg", UN_MEGABYTE));
    }

    @Test
    void testEstensioneIngannevoleRifiutata() {
        // Nome che finisce per .png ma contenuto dichiarato come GIF
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> ValidatoreFotoProfilo.valida("finta.png", "image/gif", UN_MEGABYTE));
        assertTrue(ex.getMessage().contains("non corrisponde all'estensione"),
                "Il messaggio deve spiegare l'incoerenza fra contenuto ed estensione");
    }

    // --- Limite di dimensione ---

    @Test
    void testFileTroppoGrandeRifiutato() {
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> ValidatoreFotoProfilo.valida("foto.jpg", "image/jpeg",
                        ValidatoreFotoProfilo.DIMENSIONE_MASSIMA_BYTE + 1));
        assertEquals("L'immagine supera il limite di 2 MB. Scegli un file più piccolo.", ex.getMessage());
    }

    @Test
    void testFileMoltoGrandeRifiutato() {
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> ValidatoreFotoProfilo.valida("foto.png", "image/png", 10L * 1024 * 1024));
        assertTrue(ex.getMessage().contains("2 MB"));
    }

    @Test
    void testFileVuotoRifiutato() {
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> ValidatoreFotoProfilo.valida("foto.jpg", "image/jpeg", 0));
        assertEquals("Il file selezionato è vuoto", ex.getMessage());
    }

    @Test
    void testNessunFileSelezionato() {
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> ValidatoreFotoProfilo.valida(null, "image/jpeg", UN_MEGABYTE));
        assertEquals("Nessun file selezionato", ex.getMessage());

        assertThrows(IllegalArgumentException.class,
                () -> ValidatoreFotoProfilo.valida("   ", "image/jpeg", UN_MEGABYTE));
    }

    // --- Estrazione dell'estensione ---

    @Test
    void testEstensioneEstrattaInMinuscolo() {
        assertEquals("jpg", ValidatoreFotoProfilo.estensioneDi("Foto.JPG"));
        assertEquals("png", ValidatoreFotoProfilo.estensioneDi("mia.foto.PNG"));
        assertEquals("", ValidatoreFotoProfilo.estensioneDi("senzaestensione"));
        assertEquals("", ValidatoreFotoProfilo.estensioneDi(null));
    }
}
