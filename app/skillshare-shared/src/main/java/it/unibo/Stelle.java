package it.unibo;

/**
 * Rappresentazione testuale di un voto come stelle piene e vuote.
 * Sta in shared perche' serve sia alla GUI sia ai test lato server.
 */
public final class Stelle {

    public static final int VOTO_MINIMO = 1;
    public static final int VOTO_MASSIMO = 5;

    private static final String PIENA = "★";
    private static final String VUOTA = "☆";

    // Classe di sole utilita': non va istanziata
    private Stelle() {
    }

    /**
     * Converte un voto nella sua striscia di stelle, sempre lunga VOTO_MASSIMO.
     * Un voto fuori intervallo viene riportato dentro i limiti invece di
     * produrre una striscia di lunghezza diversa dalle altre.
     *
     * @param voto Il voto da rappresentare.
     * @return Es. "★★★★☆" per un voto di 4.
     */
    public static String perVoto(int voto) {
        int piene = voto;
        if (piene < 0) {
            piene = 0;
        }
        if (piene > VOTO_MASSIMO) {
            piene = VOTO_MASSIMO;
        }

        StringBuilder striscia = new StringBuilder();
        for (int i = 0; i < VOTO_MASSIMO; i++) {
            striscia.append(i < piene ? PIENA : VUOTA);
        }
        return striscia.toString();
    }

    /**
     * Converte un rating medio nella sua striscia di stelle, arrotondando alla
     * stella intera piu' vicina. Si resta sull'intero - e non sulla mezza
     * stella - per riusare lo stesso alfabeto di perVoto(): le stelle del
     * profilo e quelle delle singole recensioni restano cosi' identiche.
     * Il valore esatto non va perso: va affiancato con mediaFormattata().
     *
     * @param media La media dei voti ricevuti.
     * @return Es. "★★★★★" per una media di 4.5.
     */
    public static String perMedia(double media) {
        return perVoto((int) Math.round(media));
    }

    /**
     * Media con una sola cifra decimale e virgola come separatore.
     * La stringa e' costruita con aritmetica intera invece che con
     * String.valueOf(double): in GWT la conversione di un double passa da
     * JavaScript e 4.0 diventerebbe "4" invece di "4,0".
     *
     * @param media La media dei voti ricevuti.
     * @return Es. "4,5" per una media di 4.47.
     */
    public static String mediaFormattata(double media) {
        long decimi = Math.round(media * 10);
        return (decimi / 10) + "," + Math.abs(decimi % 10);
    }
}
