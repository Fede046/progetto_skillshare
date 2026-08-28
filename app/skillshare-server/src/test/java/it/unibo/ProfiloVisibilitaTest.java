package it.unibo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ProfiloVisibilitaTest {

    private static final String MARIO = "mario.rossi@unibo.it";
    private static final String LUIGI = "luigi.verdi@unibo.it";

    @Test
    void testProprioProfiloNonEInSolaLettura() {
        assertFalse(ProfiloVisibilita.soloLettura(MARIO, MARIO),
                "Sul proprio profilo le azioni personali restano disponibili");
    }

    @Test
    void testProfiloDiUnAltroUtenteEInSolaLettura() {
        assertTrue(ProfiloVisibilita.soloLettura(MARIO, LUIGI),
                "Sul profilo di un altro utente le azioni personali spariscono");
    }

    @Test
    void testIdConfrontatiDopoIlTrim() {
        // UtenteDatabase usa l'email trimmata come chiave: qui vale lo stesso
        assertFalse(ProfiloVisibilita.soloLettura("  " + MARIO + "  ", MARIO));
        assertFalse(ProfiloVisibilita.soloLettura(MARIO, MARIO + " "));
    }

    @Test
    void testIdMancanteRestaInSolaLettura() {
        // Nel dubbio si nasconde: mostrare "Modifica Profilo" sul profilo di un
        // altro e' un danno maggiore che nasconderlo sul proprio
        assertTrue(ProfiloVisibilita.soloLettura(null, MARIO));
        assertTrue(ProfiloVisibilita.soloLettura(MARIO, null));
        assertTrue(ProfiloVisibilita.soloLettura(null, null));
        assertTrue(ProfiloVisibilita.soloLettura("", MARIO));
        assertTrue(ProfiloVisibilita.soloLettura(MARIO, "   "));
    }

    @Test
    void testEmailDiverseSoloPerMaiuscoleRestanoProfiliDistinti() {
        // UtenteDatabase non normalizza le maiuscole: sono chiavi diverse,
        // quindi vanno trattate come utenti diversi anche qui
        assertTrue(ProfiloVisibilita.soloLettura(MARIO, MARIO.toUpperCase()));
    }
}
