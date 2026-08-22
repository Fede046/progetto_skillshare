package it.unibo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import it.unibo.model.AnnuncioDTO;

public class AnnuncioDatabaseTest {

    private DB dbTest;
    private AnnuncioDatabase annuncioDatabase;

    // DB in memoria per non sporcare il database reale
    @BeforeEach
    void setUp() {
        dbTest = DBMaker.memoryDB().make();
        annuncioDatabase = new AnnuncioDatabase(dbTest);
    }

    @AfterEach
    void tearDown() {
        if (dbTest != null && !dbTest.isClosed()) {
            dbTest.close();
        }
    }

    // Annuncio con tutti i campi validi, da modificare nei singoli test
    private AnnuncioDTO creaAnnuncioValido() {
        AnnuncioDTO annuncio = new AnnuncioDTO();
        annuncio.setIdUtente("mario.rossi@unibo.it");
        annuncio.setTitolo("Ripetizioni di Java");
        annuncio.setDescrizione("Lezioni base e avanzate su Java e GWT");
        annuncio.setCompetenzaOfferta("Programmazione Java");
        annuncio.setDisponibilita("Lunedì e mercoledì pomeriggio");
        annuncio.setControprestazione("Lezioni di inglese");
        return annuncio;
    }

    @Test
    void testPubblicaAnnuncioValido() {
        AnnuncioDTO salvato = annuncioDatabase.pubblica(creaAnnuncioValido());

        assertNotNull(salvato.getId(), "L'id deve essere generato");
        assertTrue(salvato.getDataCreazione() > 0, "La dataCreazione deve essere valorizzata");

        // L'annuncio deve essere rileggibile dal database
        AnnuncioDTO ricaricato = annuncioDatabase.getAnnunciCollection().get(salvato.getId());
        assertNotNull(ricaricato);
        assertEquals("Ripetizioni di Java", ricaricato.getTitolo());
        assertEquals("Programmazione Java", ricaricato.getCompetenzaOfferta());
        assertEquals("Lunedì e mercoledì pomeriggio", ricaricato.getDisponibilita());
        assertEquals("Lezioni di inglese", ricaricato.getControprestazione());
        assertEquals("mario.rossi@unibo.it", ricaricato.getIdUtente());
    }

    @Test
    void testPubblicaSenzaTitolo() {
        AnnuncioDTO annuncio = creaAnnuncioValido();
        annuncio.setTitolo(null);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            annuncioDatabase.pubblica(annuncio);
        });
        assertEquals("Il campo 'titolo' è obbligatorio", ex.getMessage());
        assertTrue(annuncioDatabase.getAnnunciCollection().isEmpty(), "Non deve salvare nulla");
    }

    @Test
    void testPubblicaSenzaCompetenzaOfferta() {
        AnnuncioDTO annuncio = creaAnnuncioValido();
        annuncio.setCompetenzaOfferta("   "); // solo spazi

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            annuncioDatabase.pubblica(annuncio);
        });
        assertEquals("Il campo 'competenza offerta' è obbligatorio", ex.getMessage());
        assertTrue(annuncioDatabase.getAnnunciCollection().isEmpty(), "Non deve salvare nulla");
    }

    @Test
    void testPubblicaSenzaDisponibilita() {
        AnnuncioDTO annuncio = creaAnnuncioValido();
        annuncio.setDisponibilita(null);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            annuncioDatabase.pubblica(annuncio);
        });
        assertEquals("Il campo 'disponibilità' è obbligatorio", ex.getMessage());
        assertTrue(annuncioDatabase.getAnnunciCollection().isEmpty(), "Non deve salvare nulla");
    }

    @Test
    void testPubblicaSenzaControprestazione() {
        AnnuncioDTO annuncio = creaAnnuncioValido();
        annuncio.setControprestazione("");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            annuncioDatabase.pubblica(annuncio);
        });
        assertEquals("Il campo 'controprestazione' è obbligatorio", ex.getMessage());
        assertTrue(annuncioDatabase.getAnnunciCollection().isEmpty(), "Non deve salvare nulla");
    }
}
