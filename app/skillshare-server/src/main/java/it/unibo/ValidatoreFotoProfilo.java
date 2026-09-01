package it.unibo;

import java.util.Locale;

// Regole di accettazione per le foto profilo caricate dagli utenti. È una classe di sola logica,
// senza dipendenze da servlet o filesystem, così le regole restano verificabili con test unitari.
public final class ValidatoreFotoProfilo {

    /** Limite di dimensione: 2 MB, condiviso con il client. */
    public static final long DIMENSIONE_MASSIMA_BYTE = ProtocolloUploadFoto.DIMENSIONE_MASSIMA_BYTE;

    private static final String ESTENSIONE_JPG = "jpg";
    private static final String ESTENSIONE_JPEG = "jpeg";
    private static final String ESTENSIONE_PNG = "png";

    private ValidatoreFotoProfilo() {
        // Classe di sole utilità
    }

    // Verifica nome, content-type e dimensione del file caricato.
    public static void valida(String nomeFile, String contentType, long dimensione)
            throws IllegalArgumentException {
        if (nomeFile == null || nomeFile.trim().isEmpty()) {
            throw new IllegalArgumentException("Nessun file selezionato");
        }
        if (dimensione <= 0) {
            throw new IllegalArgumentException("Il file selezionato è vuoto");
        }
        if (dimensione > DIMENSIONE_MASSIMA_BYTE) {
            throw new IllegalArgumentException(
                    "L'immagine supera il limite di 2 MB. Scegli un file più piccolo.");
        }

        String estensione = estensioneDi(nomeFile);
        if (!estensioneAmmessa(estensione)) {
            throw new IllegalArgumentException(
                    "Formato non supportato: sono accettate solo immagini JPG e PNG.");
        }

        // Il content-type è un controllo aggiuntivo: i browser non sempre lo
        // valorizzano in modo utile, quindi lo si verifica solo quando è
        // riconoscibile come tipo immagine specifico.
        if (contentTypeRiconoscibile(contentType) && !contentTypeCoerente(contentType, estensione)) {
            throw new IllegalArgumentException(
                    "Il contenuto del file non corrisponde all'estensione: carica una vera immagine JPG o PNG.");
        }
    }

    /** Estensione dell'immagine da usare per il file salvato, senza il punto. */
    public static String estensioneDi(String nomeFile) {
        if (nomeFile == null) {
            return "";
        }
        int punto = nomeFile.lastIndexOf('.');
        if (punto < 0 || punto == nomeFile.length() - 1) {
            return "";
        }
        return nomeFile.substring(punto + 1).trim().toLowerCase(Locale.ROOT);
    }

    private static boolean estensioneAmmessa(String estensione) {
        return ESTENSIONE_JPG.equals(estensione)
                || ESTENSIONE_JPEG.equals(estensione)
                || ESTENSIONE_PNG.equals(estensione);
    }

    /** Un content-type generico non dice nulla di utile: si ignora. */
    private static boolean contentTypeRiconoscibile(String contentType) {
        if (contentType == null || contentType.trim().isEmpty()) {
            return false;
        }
        String tipo = contentType.trim().toLowerCase(Locale.ROOT);
        return tipo.startsWith("image/");
    }

    private static boolean contentTypeCoerente(String contentType, String estensione) {
        String tipo = contentType.trim().toLowerCase(Locale.ROOT);
        if (ESTENSIONE_PNG.equals(estensione)) {
            return tipo.startsWith("image/png");
        }
        // jpg e jpeg condividono lo stesso tipo MIME
        return tipo.startsWith("image/jpeg") || tipo.startsWith("image/jpg");
    }
}
