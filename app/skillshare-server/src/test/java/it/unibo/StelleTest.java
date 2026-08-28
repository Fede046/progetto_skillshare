package it.unibo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class StelleTest {

    @Test
    void testVotoIntermedioMostraPieneEVuote() {
        assertEquals("★★★★☆", Stelle.perVoto(4));
        assertEquals("★★★☆☆", Stelle.perVoto(3));
        assertEquals("★☆☆☆☆", Stelle.perVoto(1));
    }

    @Test
    void testVotoMassimoTutteStellePiene() {
        assertEquals("★★★★★", Stelle.perVoto(5));
    }

    @Test
    void testVotoFuoriIntervalloRiportatoNeiLimiti() {
        // La striscia resta sempre lunga 5, cosi' le righe della lista si allineano
        assertEquals("☆☆☆☆☆", Stelle.perVoto(0));
        assertEquals("☆☆☆☆☆", Stelle.perVoto(-3));
        assertEquals("★★★★★", Stelle.perVoto(6));
    }

    @Test
    void testStrisciaSempreLunga5() {
        for (int voto = -1; voto <= 6; voto++) {
            assertEquals(Stelle.VOTO_MASSIMO, Stelle.perVoto(voto).length(),
                    "La striscia deve avere sempre 5 caratteri, voto " + voto);
        }
    }
}
