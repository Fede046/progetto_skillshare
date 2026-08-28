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

    // --- rating medio mostrato sul profilo pubblico (US-14) ---

    @Test
    void testMediaInteraMostraLoStessoNumeroDiStellePiene() {
        assertEquals("★★★★☆", Stelle.perMedia(4.0));
        assertEquals("★★★★★", Stelle.perMedia(5.0));
    }

    @Test
    void testMediaArrotondataAllaStellaInteraPiuVicina() {
        assertEquals("★★★★☆", Stelle.perMedia(4.4), "4.4 resta a 4 stelle");
        assertEquals("★★★★★", Stelle.perMedia(4.5), "4.5 sale a 5 stelle");
        assertEquals("★★★☆☆", Stelle.perMedia(3.2), "3.2 resta a 3 stelle");
        assertEquals("★★★★☆", Stelle.perMedia(3.6), "3.6 sale a 4 stelle");
    }

    @Test
    void testStrisciaDellaMediaSempreLunga5() {
        assertEquals(Stelle.VOTO_MASSIMO, Stelle.perMedia(1.0).length());
        assertEquals(Stelle.VOTO_MASSIMO, Stelle.perMedia(4.5).length());
        assertEquals(Stelle.VOTO_MASSIMO, Stelle.perMedia(5.0).length());
    }

    @Test
    void testMediaFormattataConUnaCifraDecimale() {
        assertEquals("4,5", Stelle.mediaFormattata(4.5));
        assertEquals("3,3", Stelle.mediaFormattata(10.0 / 3.0), "3.333... si ferma al primo decimale");
        assertEquals("4,7", Stelle.mediaFormattata(14.0 / 3.0), "4.666... arrotonda a 4,7");
    }

    @Test
    void testMediaInteraMantieneLoZeroDecimale() {
        // Il decimale va mostrato anche quando e' zero: "4" da' l'idea di un
        // valore troncato, "4,0" di una media esatta
        assertEquals("4,0", Stelle.mediaFormattata(4.0));
        assertEquals("5,0", Stelle.mediaFormattata(5.0));
    }
}
