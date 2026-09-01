package it.unibo;

// Rappresentazione testuale di un voto come stelle piene e vuote. Sta in shared perche' serve sia
// alla GUI sia ai test lato server.
public final class Stelle {

    public static final int VOTO_MINIMO = 1;
    public static final int VOTO_MASSIMO = 5;

    private static final String PIENA = "★";
    private static final String VUOTA = "☆";

    // Classe di sole utilita': non va istanziata
    private Stelle() {
    }

    // Converte un voto nella sua striscia di stelle, sempre lunga VOTO_MASSIMO.
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

    // Converte un rating medio nella sua striscia di stelle,
    // arrotondando alla stella intera piu' vicina.
    public static String perMedia(double media) {
        return perVoto((int) Math.round(media));
    }

    // Media con una sola cifra decimale e virgola come separatore.
    public static String mediaFormattata(double media) {
        long decimi = Math.round(media * 10);
        return (decimi / 10) + "," + Math.abs(decimi % 10);
    }
}
