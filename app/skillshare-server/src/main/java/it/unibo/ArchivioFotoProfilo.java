package it.unibo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

/**
 * Salva e rilegge le foto profilo sul filesystem del server.
 *
 * La cartella è "foto-profilo" dentro la directory indicata dalla variabile
 * d'ambiente DATA_DIR, la stessa già usata da {@link DatabaseCore} per il file
 * del database: così il volume Docker che rende persistente il database rende
 * persistenti anche le immagini, senza configurazione aggiuntiva.
 * Senza DATA_DIR si ricade su una cartella locale, come fa il database.
 */
public final class ArchivioFotoProfilo {

    /** Sottocartella dedicata alle immagini dei profili. */
    public static final String NOME_CARTELLA = "foto-profilo";

    /** Percorso pubblico da cui le immagini vengono servite ai browser. */
    public static final String PERCORSO_PUBBLICO = "app/fotoProfilo/";

    private ArchivioFotoProfilo() {
        // Classe di sole utilità
    }

    /**
     * Directory in cui vivono le immagini, creata se non esiste.
     */
    public static File cartella() {
        String dataDir = System.getenv("DATA_DIR");
        File base;
        if (dataDir != null && !dataDir.trim().isEmpty()) {
            base = new File(dataDir.trim());
        } else {
            // Stesso fallback locale del database
            base = new File(".");
        }

        File cartella = new File(base, NOME_CARTELLA);
        if (!cartella.exists()) {
            cartella.mkdirs();
        }
        return cartella;
    }

    /**
     * Scrive l'immagine caricata, sostituendo l'eventuale foto precedente
     * dello stesso utente.
     *
     * @param email      Email dell'utente, usata per costruire il nome del file.
     * @param estensione Estensione già validata (jpg, jpeg o png).
     * @param contenuto  Flusso del file caricato.
     * @return Il percorso pubblico da salvare in UtenteDTO.photoUrl.
     */
    public static String salva(String email, String estensione, InputStream contenuto) throws IOException {
        String nomeFile = nomeFileDi(email, estensione);
        Path destinazione = cartella().toPath().resolve(nomeFile);

        // Una sola foto per utente: la nuova sostituisce la precedente,
        // così non si accumulano file orfani a ogni cambio
        rimuoviFotoPrecedenti(email, estensione);
        Files.copy(contenuto, destinazione, StandardCopyOption.REPLACE_EXISTING);

        // Marcatore temporale: evita che il browser mostri la vecchia immagine
        // dalla cache dopo un cambio foto
        return PERCORSO_PUBBLICO + nomeFile + "?v=" + System.currentTimeMillis();
    }

    /**
     * Restituisce il file di una foto a partire dal nome richiesto dal browser,
     * oppure null se non esiste.
     *
     * @param nomeRichiesto Nome del file, così come compare nell'URL.
     */
    public static File trova(String nomeRichiesto) {
        if (nomeRichiesto == null || nomeRichiesto.trim().isEmpty()) {
            return null;
        }

        // Difesa da path traversal: si accetta solo un nome di file semplice
        String nome = nomeRichiesto.trim();
        if (nome.contains("/") || nome.contains("\\") || nome.contains("..")) {
            return null;
        }

        File file = new File(cartella(), nome);
        return file.isFile() ? file : null;
    }

    /** Content-type da dichiarare quando si serve l'immagine. */
    public static String contentTypeDi(String nomeFile) {
        String estensione = ValidatoreFotoProfilo.estensioneDi(nomeFile);
        if ("png".equals(estensione)) {
            return "image/png";
        }
        return "image/jpeg";
    }

    /**
     * Nome del file di un utente: l'email normalizzata, ripulita dai caratteri
     * che non possono comparire in un nome di file.
     */
    public static String nomeFileDi(String email, String estensione) {
        String base = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        base = base.replaceAll("[^a-z0-9._-]", "_");
        return base + "." + estensione;
    }

    /** Cancella le foto dell'utente con estensione diversa da quella nuova. */
    private static void rimuoviFotoPrecedenti(String email, String estensioneNuova) {
        for (String estensione : new String[] { "jpg", "jpeg", "png" }) {
            if (estensione.equals(estensioneNuova)) {
                continue;
            }
            File vecchia = new File(cartella(), nomeFileDi(email, estensione));
            if (vecchia.isFile()) {
                vecchia.delete();
            }
        }
    }
}
