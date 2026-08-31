package it.unibo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AnnuncioServiceImplTest {

    @BeforeAll
    static void setUpDatabase() {
        // Database in memoria per i test: non tocca il file progetto_sweng.db
        DatabaseCore.enableTestMode();
    }

    @AfterAll
    static void tearDownDatabase() {
        DatabaseCore.disableTestMode();
    }

    // Annuncio valido, da modificare nei singoli test
    private AnnuncioDTO creaAnnuncioValido() {
        AnnuncioDTO annuncio = new AnnuncioDTO();
        annuncio.setIdUtente("rpc.test_" + System.currentTimeMillis() + "@unibo.it");
        annuncio.setTitolo("Ripetizioni di Java");
        annuncio.setDescrizione("Lezioni base e avanzate su Java e GWT");
        annuncio.setCompetenzaOfferta("Programmazione Java");
        annuncio.setDisponibilita("Lunedì e mercoledì pomeriggio");
        annuncio.setControprestazione("Lezioni di inglese");
        return annuncio;
    }

    @Test
    void testPubblicaRestituisceAnnuncioSalvato() {
        AnnuncioServiceImpl service = new AnnuncioServiceImpl();
        AnnuncioDTO annuncio = creaAnnuncioValido();

        AnnuncioDTO salvato = service.pubblica(annuncio);

        // Id e dataCreazione li valorizza AnnuncioDatabase: se ci sono, la delega ha funzionato
        assertNotNull(salvato);
        assertNotNull(salvato.getId(), "L'id deve essere generato");
        assertTrue(salvato.getDataCreazione() > 0, "La dataCreazione deve essere valorizzata");
        assertEquals("Ripetizioni di Java", salvato.getTitolo());

        // L'annuncio deve essere finito davvero nella collection di AnnuncioDatabase
        AnnuncioDTO ricaricato = new AnnuncioDatabase().getAnnunciCollection().get(salvato.getId());
        assertNotNull(ricaricato, "L'annuncio deve essere recuperabile dal database");
        assertEquals(annuncio.getIdUtente(), ricaricato.getIdUtente());
        assertEquals("Programmazione Java", ricaricato.getCompetenzaOfferta());
    }

    @Test
    void testPubblicaCampoMancantePropagaEccezione() {
        AnnuncioServiceImpl service = new AnnuncioServiceImpl();
        AnnuncioDTO annuncio = creaAnnuncioValido();
        annuncio.setTitolo(null);

        // Il messaggio arriva da AnnuncioDatabase: la servlet non valida in proprio
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            service.pubblica(annuncio);
        });
        assertEquals("Il campo 'titolo' è obbligatorio", ex.getMessage());
    }

    @Test
    void testPubblicaAnnuncioNullPropagaEccezione() {
        AnnuncioServiceImpl service = new AnnuncioServiceImpl();

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            service.pubblica(null);
        });
        assertEquals("Dati non validi", ex.getMessage());
    }
}
