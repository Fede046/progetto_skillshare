package it.unibo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.Serializer;

// Gestisce la collection MapDB "utenti": registrazione, login e profilo.
// I metodi sono statici perche' l'utente serve un po' ovunque nel server
public class UtenteDatabase {

    private static DB dbAttuale;
    private static ConcurrentMap<String, UtenteDTO> utentiCollection;

    // Risolve la collection sul database attualmente attivo. Se il DB cambia (es. passaggio da file a
    // memoria nei test), la riapre sulla nuova istanza.
    private static ConcurrentMap<String, UtenteDTO> collection() {
        DB dbCorrente = DatabaseCore.getDB();
        if (utentiCollection == null || dbAttuale != dbCorrente) {
            dbAttuale = dbCorrente;
            utentiCollection = dbCorrente.hashMap(
                    "utenti",
                    Serializer.STRING,
                    Serializer.JAVA).createOrOpen();

            // Se la collezione è vuota, registriamo un utente di test valido
            if (utentiCollection.isEmpty()) {
                try {
                    UtenteDTO defaultUser = new UtenteDTO();
                    defaultUser.setEmail("test@unibo.it");
                    defaultUser.setPassword("Password123!");
                    defaultUser.setNome("Mario");
                    defaultUser.setCognome("Rossi");

                    registra(defaultUser);
                } catch (Exception e) {
                    // Gestione silenziosa in fase di avvio
                }
            }
        }
        return utentiCollection;
    }

    // Registra un nuovo utente applicando l'hashing SHA-256 alla password.
    public static boolean registra(UtenteDTO utente) throws IllegalArgumentException {
        // 1. Controlli preliminari sui dati nulli
        if (utente == null || utente.getEmail() == null || utente.getPassword() == null) {
            throw new IllegalArgumentException("Dati non validi");
        }

        String email = utente.getEmail().trim();

        // 2. Controllo formato email
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            throw new IllegalArgumentException("Formato email non valido.");
        }

        // 3. Controllo password
        String regexPassword = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$";

        if (!utente.getPassword().matches(regexPassword)) {
            throw new IllegalArgumentException(
                    "La password deve avere almeno 8 caratteri, una maiuscola, un numero e un simbolo speciale.");
        }

        // 4. Controllo email duplicata
        if (collection().containsKey(email)) {
            return false; // Email già esistente, blocca il salvataggio
        }

        // 5. Cifratura della password con SHA-256
        String passwordCifrata = hashPassword(utente.getPassword());
        utente.setPassword(passwordCifrata);

        // 6. Salvataggio nel database MapDB e commit
        collection().put(email, utente);
        DatabaseCore.commit();

        return true;
    }

    // Metodo di supporto per generare l'hash SHA-256 di una stringa.
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Errore durante l'hashing della password", e);
        }
    }

    // Verifica le credenziali dell'utente per il login. Cifra la password
    // inserita con SHA-256 e la confronta con quella salvata.
    public static UtenteDTO verificaCredenziali(String email, String password) throws IllegalArgumentException {
        if (email == null || password == null) {
            throw new IllegalArgumentException("Dati non validi");
        }

        String emailTrimmed = email.trim();

        // 1. Controlla se l'utente esiste
        if (!collection().containsKey(emailTrimmed)) {
            throw new IllegalArgumentException("User not found");
        }

        // 2. Recupera l'utente dal database
        UtenteDTO utenteRegistrato = collection().get(emailTrimmed);

        // 3. Cifra la password inserita per il confronto
        String passwordCifrata = hashPassword(password);

        // 4. Confronta l'hash calcolato con quello salvato
        if (!passwordCifrata.equals(utenteRegistrato.getPassword())) {
            throw new IllegalArgumentException("Wrong password");
        }

        // 5. Restituisce direttamente l'utente trovato!
        return utenteRegistrato;
    }
    // Recupera un utente dal database tramite email per la visualizzazione del profilo.
    public static UtenteDTO getProfilo(String email) throws IllegalArgumentException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Dati non validi");
        }

        String emailTrimmed = email.trim();

        if (!collection().containsKey(emailTrimmed)) {
            throw new IllegalArgumentException("User not found");
        }

        return collection().get(emailTrimmed);
    }
    // Aggiorna bio, photoUrl e tagCompetenza di un utente
    // preservandone credenziali e dati anagrafici.
    public static UtenteDTO aggiornaProfilo(UtenteDTO utenteAggiornato) throws IllegalArgumentException {
        if (utenteAggiornato == null || utenteAggiornato.getEmail() == null || utenteAggiornato.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Dati non validi");
        }

        String email = utenteAggiornato.getEmail().trim();

        if (!collection().containsKey(email)) {
            throw new IllegalArgumentException("User not found");
        }

        // Recupera l'utente esistente per preservare password, nome e cognome
        UtenteDTO utenteEsistente = collection().get(email);

        utenteEsistente.setBio(utenteAggiornato.getBio() != null ? utenteAggiornato.getBio() : "");
        utenteEsistente.setPhotoUrl(utenteAggiornato.getPhotoUrl() != null ? utenteAggiornato.getPhotoUrl() : "");
        utenteEsistente.setTagCompetenza(
                utenteAggiornato.getTagCompetenza() != null ? new ArrayList<>(utenteAggiornato.getTagCompetenza()) : new ArrayList<>());

        // Salvataggio e persistenza
        collection().put(email, utenteEsistente);
        DatabaseCore.commit();

        return utenteEsistente;
    }
}