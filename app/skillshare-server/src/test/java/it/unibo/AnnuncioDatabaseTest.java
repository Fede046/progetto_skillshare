package it.unibo;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;

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

    @Test
    void testAnnunciDiUtenteSoloSuoiEOrdinatiPerDataDecrescente() {
        // Due annunci di Mario e uno di Luigi, pubblicati in sequenza
        AnnuncioDTO primoMario = creaAnnuncioValido();
        primoMario.setIdUtente("mario.rossi@unibo.it");
        primoMario.setTitolo("Ripetizioni di Java");
        annuncioDatabase.pubblica(primoMario);

        AnnuncioDTO diLuigi = creaAnnuncioValido();
        diLuigi.setIdUtente("luigi.verdi@unibo.it");
        diLuigi.setTitolo("Lezioni di chitarra");
        annuncioDatabase.pubblica(diLuigi);

        AnnuncioDTO secondoMario = creaAnnuncioValido();
        secondoMario.setIdUtente("mario.rossi@unibo.it");
        secondoMario.setTitolo("Ripetizioni di SQL");
        // pubblica() usa System.currentTimeMillis(): forziamo una data piu' recente
        // per rendere l'ordinamento verificabile a prescindere dalla velocita' del test
        annuncioDatabase.pubblica(secondoMario);
        secondoMario.setDataCreazione(primoMario.getDataCreazione() + 1000);
        annuncioDatabase.getAnnunciCollection().put(secondoMario.getId(), secondoMario);

        List<AnnuncioDTO> risultato = annuncioDatabase.annunciDiUtente("mario.rossi@unibo.it");

        // Solo i suoi due annunci, quello di Luigi resta fuori
        assertEquals(2, risultato.size(), "Deve restituire solo gli annunci di Mario");
        assertEquals("Ripetizioni di SQL", risultato.get(0).getTitolo(), "Il piu' recente va per primo");
        assertEquals("Ripetizioni di Java", risultato.get(1).getTitolo());
    }

    @Test
    void testAnnunciDiUtenteSenzaAnnunciRestituisceListaVuota() {
        AnnuncioDTO annuncio = creaAnnuncioValido();
        annuncio.setIdUtente("mario.rossi@unibo.it");
        annuncioDatabase.pubblica(annuncio);

        List<AnnuncioDTO> risultato = annuncioDatabase.annunciDiUtente("nessuno@unibo.it");

        assertNotNull(risultato, "Deve restituire una lista, non null");
        assertTrue(risultato.isEmpty(), "Un utente senza annunci ha lista vuota");
    }

    @Test
    void testTuttiGliAnnunciOrdinatiPerDataDecrescente() {
        // Tre annunci pubblicati in sequenza
        AnnuncioDTO primo = creaAnnuncioValido();
        primo.setTitolo("Ripetizioni di Java");
        annuncioDatabase.pubblica(primo);

        AnnuncioDTO secondo = creaAnnuncioValido();
        secondo.setTitolo("Lezioni di chitarra");
        annuncioDatabase.pubblica(secondo);

        AnnuncioDTO terzo = creaAnnuncioValido();
        terzo.setTitolo("Ripetizioni di SQL");
        // pubblica() usa System.currentTimeMillis(): forziamo date distinte
        // per rendere l'ordinamento verificabile a prescindere dalla velocita' del test
        annuncioDatabase.pubblica(terzo);
        secondo.setDataCreazione(primo.getDataCreazione() + 1000);
        annuncioDatabase.getAnnunciCollection().put(secondo.getId(), secondo);
        terzo.setDataCreazione(primo.getDataCreazione() + 2000);
        annuncioDatabase.getAnnunciCollection().put(terzo.getId(), terzo);

        List<AnnuncioDTO> risultato = annuncioDatabase.tuttiGliAnnunci();

        assertEquals(3, risultato.size(), "Deve restituire tutti gli annunci");
        assertEquals("Ripetizioni di SQL", risultato.get(0).getTitolo(), "Il piu' recente va per primo");
        assertEquals("Lezioni di chitarra", risultato.get(1).getTitolo());
        assertEquals("Ripetizioni di Java", risultato.get(2).getTitolo(), "Il piu' vecchio va per ultimo");
    }

    @Test
    void testTuttiGliAnnunciSenzaAnnunciRestituisceListaVuota() {
        List<AnnuncioDTO> risultato = annuncioDatabase.tuttiGliAnnunci();

        assertNotNull(risultato, "Deve restituire una lista, non null");
        assertTrue(risultato.isEmpty(), "Senza annunci la lista deve essere vuota");
    }

    @Test
    void testFiltraPerCompetenzaCaseInsensitive() {
        AnnuncioDTO a1 = creaAnnuncioValido();
        a1.setTitolo("Ripetizioni Java e OOP");
        a1.setCompetenzaOfferta("Programmazione Java");
        annuncioDatabase.pubblica(a1);

        AnnuncioDTO a2 = creaAnnuncioValido();
        a2.setTitolo("Corso Base Python");
        a2.setCompetenzaOfferta("Data Science in Python");
        annuncioDatabase.pubblica(a2);

        // Ricerca parziale maiuscola ("JAVA")
        List<AnnuncioDTO> resJava = annuncioDatabase.filtraPerCompetenza("JAVA");
        assertEquals(1, resJava.size());
        assertEquals("Ripetizioni Java e OOP", resJava.get(0).getTitolo());

        // Ricerca parziale minuscola ("python")
        List<AnnuncioDTO> resPython = annuncioDatabase.filtraPerCompetenza("python");
        assertEquals(1, resPython.size());
        assertEquals("Corso Base Python", resPython.get(0).getTitolo());
    }

    @Test
    void testFiltraConStringaVuotaOBlankRestituisceTutti() {
        AnnuncioDTO a1 = creaAnnuncioValido();
        annuncioDatabase.pubblica(a1);

        List<AnnuncioDTO> resNull = annuncioDatabase.filtraPerCompetenza(null);
        List<AnnuncioDTO> resBlank = annuncioDatabase.filtraPerCompetenza("   ");

        assertEquals(1, resNull.size());
        assertEquals(1, resBlank.size());
    }

    @Test
    void testFiltraSenzaCorrispondenzeRestituisceListaVuota() {
        AnnuncioDTO a1 = creaAnnuncioValido();
        a1.setCompetenzaOfferta("Design Grafico");
        annuncioDatabase.pubblica(a1);

        List<AnnuncioDTO> res = annuncioDatabase.filtraPerCompetenza("InesistenteXYZ");
        assertNotNull(res);
        assertTrue(res.isEmpty(), "Una ricerca senza corrispondenze deve restituire lista vuota");
    }

    @Test
    void testOrdinaPerTitoloAlfabetico() {
        AnnuncioDTO a1 = creaAnnuncioValido();
        a1.setTitolo("Zebra: Lezioni Grafica");

        AnnuncioDTO a2 = creaAnnuncioValido();
        a2.setTitolo("Algoritmi e Strutture Dati");

        AnnuncioDTO a3 = creaAnnuncioValido();
        a3.setTitolo("Basi di Dati e SQL");

        List<AnnuncioDTO> lista = List.of(a1, a2, a3);
        List<AnnuncioDTO> ordinata = annuncioDatabase.ordinaPerTitolo(lista);

        assertEquals("Algoritmi e Strutture Dati", ordinata.get(0).getTitolo());
        assertEquals("Basi di Dati e SQL", ordinata.get(1).getTitolo());
        assertEquals("Zebra: Lezioni Grafica", ordinata.get(2).getTitolo());
    }   
}
