package it.unibo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Nomi dei file salvati e difese sulla lettura delle foto profilo.
 */
public class ArchivioFotoProfiloTest {

    @Test
    void testNomeFileDerivatoDallEmail() {
        assertEquals("mario.rossi_unibo.it.jpg",
                ArchivioFotoProfilo.nomeFileDi("mario.rossi@unibo.it", "jpg"));
    }

    @Test
    void testNomeFileNormalizzaMaiuscoleESpazi() {
        assertEquals("mario.rossi_unibo.it.png",
                ArchivioFotoProfilo.nomeFileDi("  Mario.Rossi@UNIBO.it  ", "png"));
    }

    @Test
    void testNomeFileNeutralizzaCaratteriPericolosi() {
        // Nessun separatore di percorso deve sopravvivere nel nome del file
        String nome = ArchivioFotoProfilo.nomeFileDi("../../etc/passwd@unibo.it", "jpg");
        assertTrue(nome.indexOf('/') < 0, "Il nome non deve contenere separatori di percorso");
        assertTrue(nome.indexOf('\\') < 0, "Il nome non deve contenere separatori di percorso");
    }

    @Test
    void testTrovaRifiutaPercorsiRelativi() {
        // Difesa da path traversal: si accetta solo un nome di file semplice
        assertNull(ArchivioFotoProfilo.trova("../progetto_sweng.db"));
        assertNull(ArchivioFotoProfilo.trova("../../etc/passwd"));
        assertNull(ArchivioFotoProfilo.trova("cartella/foto.jpg"));
        assertNull(ArchivioFotoProfilo.trova("cartella\\foto.jpg"));
    }

    @Test
    void testTrovaRifiutaNomeVuoto() {
        assertNull(ArchivioFotoProfilo.trova(null));
        assertNull(ArchivioFotoProfilo.trova("   "));
    }

    @Test
    void testTrovaRestituisceNullSeIlFileNonEsiste() {
        assertNull(ArchivioFotoProfilo.trova("utente.inesistente_unibo.it.jpg"));
    }

    @Test
    void testContentTypeCoerenteConLEstensione() {
        assertEquals("image/png", ArchivioFotoProfilo.contentTypeDi("avatar.png"));
        assertEquals("image/jpeg", ArchivioFotoProfilo.contentTypeDi("avatar.jpg"));
        assertEquals("image/jpeg", ArchivioFotoProfilo.contentTypeDi("avatar.jpeg"));
    }

    @Test
    void testPercorsoPubblicoCoerenteConLaServlet() {
        // Se cambia il mapping in web.xml, questo test lo segnala
        assertEquals("app/fotoProfilo/", ArchivioFotoProfilo.PERCORSO_PUBBLICO);
    }
}
