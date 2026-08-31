package it.unibo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Verifica la regola che decide se mostrare il pulsante "Segna come completato":
 * lo scambio dev'essere ACCEPTED e l'utente dev'essere uno dei due partecipanti.
 * E' la condizione usata da CompletamentoRecensioneGui per ridisegnarsi dopo
 * un'accettazione, senza attendere un reload della pagina.
 */
public class RichiestaScambioCompletabileTest {

    private static final String RICHIEDENTE = "luigi.verdi@unibo.it";
    private static final String CREATORE = "mario.rossi@unibo.it";
    private static final String ESTRANEO = "anna.bianchi@unibo.it";

    private RichiestaScambioDTO creaRichiesta(StatoRichiesta stato) {
        RichiestaScambioDTO richiesta = new RichiestaScambioDTO();
        richiesta.setId("richiesta-1");
        richiesta.setIdAnnuncio("annuncio-1");
        richiesta.setIdRichiedente(RICHIEDENTE);
        richiesta.setIdCreatoreAnnuncio(CREATORE);
        richiesta.setStato(stato);
        return richiesta;
    }

    @Test
    void testAccettataVisibileAlRichiedente() {
        assertTrue(creaRichiesta(StatoRichiesta.ACCEPTED).completabileDa(RICHIEDENTE),
                "Il richiedente partecipa allo scambio: deve poterlo completare");
    }

    @Test
    void testAccettataVisibileAlCreatore() {
        assertTrue(creaRichiesta(StatoRichiesta.ACCEPTED).completabileDa(CREATORE),
                "Anche il creatore dell'annuncio deve poter completare lo scambio");
    }

    @Test
    void testAccettataNonVisibileAEstranei() {
        assertFalse(creaRichiesta(StatoRichiesta.ACCEPTED).completabileDa(ESTRANEO),
                "Chi non partecipa allo scambio non deve vedere il pulsante");
    }

    @Test
    void testPendingNonCompletabile() {
        // E' il caso del bug: prima dell'accettazione il pulsante non c'e'
        assertFalse(creaRichiesta(StatoRichiesta.PENDING).completabileDa(CREATORE));
        assertFalse(creaRichiesta(StatoRichiesta.PENDING).completabileDa(RICHIEDENTE));
    }

    @Test
    void testRifiutataNonCompletabile() {
        assertFalse(creaRichiesta(StatoRichiesta.REJECTED).completabileDa(CREATORE));
        assertFalse(creaRichiesta(StatoRichiesta.REJECTED).completabileDa(RICHIEDENTE));
    }

    @Test
    void testGiaCompletataNonRipropone() {
        // Completato lo scambio, il blocco passa alla recensione: niente pulsante
        assertFalse(creaRichiesta(StatoRichiesta.COMPLETED).completabileDa(CREATORE));
        assertFalse(creaRichiesta(StatoRichiesta.COMPLETED).completabileDa(RICHIEDENTE));
    }

    @Test
    void testUtenteNullOVuotoNonCompletabile() {
        RichiestaScambioDTO accettata = creaRichiesta(StatoRichiesta.ACCEPTED);
        assertFalse(accettata.completabileDa(null));
        assertFalse(accettata.completabileDa("   "));
    }

    @Test
    void testEmailConSpaziVieneNormalizzata() {
        // La GUI passa l'email dell'utente loggato cosi' com'e': eventuali
        // spazi accidentali non devono far sparire il pulsante
        assertTrue(creaRichiesta(StatoRichiesta.ACCEPTED).completabileDa("  " + CREATORE + "  "));
    }

    @Test
    void testTransizioneDaPendingAdAccettataAbilitaIlPulsante() {
        // Riproduce la sequenza del bug: la stessa richiesta, prima e dopo
        // l'accettazione, senza ricostruire l'oggetto
        RichiestaScambioDTO richiesta = creaRichiesta(StatoRichiesta.PENDING);
        assertFalse(richiesta.completabileDa(CREATORE), "Prima dell'accettazione il pulsante non c'è");

        richiesta.setStato(StatoRichiesta.ACCEPTED);
        assertTrue(richiesta.completabileDa(CREATORE),
                "Subito dopo l'accettazione il pulsante deve comparire, senza reload");
    }
}
