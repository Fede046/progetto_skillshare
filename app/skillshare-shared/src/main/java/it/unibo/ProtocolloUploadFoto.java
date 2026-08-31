package it.unibo;

/**
 * Nomi dei campi e prefissi di risposta usati nell'upload della foto profilo.
 *
 * Sta in shared perché l'upload non passa dall'RPC di GWT: client e servlet si
 * accordano su un form multipart e su una risposta testuale, e questo è il
 * punto unico in cui quel contratto è scritto.
 */
public final class ProtocolloUploadFoto {

    /** Percorso della servlet di upload, relativo alla radice dell'applicazione. */
    public static final String PERCORSO_UPLOAD = "app/uploadFotoProfilo";

    /** Nome del campo file nel form multipart. */
    public static final String CAMPO_FILE = "fotoProfilo";

    /** Nome del campo nascosto che porta l'email dell'utente. */
    public static final String CAMPO_EMAIL = "email";

    /** Prefisso della risposta in caso di successo, seguito dal percorso dell'immagine. */
    public static final String ESITO_OK = "OK|";

    /** Prefisso della risposta in caso di errore, seguito dal messaggio per l'utente. */
    public static final String ESITO_ERRORE = "ERRORE|";

    /** Limite di dimensione accettato: 2 MB. */
    public static final long DIMENSIONE_MASSIMA_BYTE = 2L * 1024 * 1024;

    private ProtocolloUploadFoto() {
        // Classe di sole costanti
    }
}
