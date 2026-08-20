package it.unibo;

import org.mapdb.DB;
import org.mapdb.Serializer;
import java.util.concurrent.ConcurrentMap;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class UtenteDatabase {

    private static final DB db = DatabaseCore.getDB();
    
    // Mappa per memorizzare gli utenti: Chiave = Email (String), Valore = UtenteDTO
    private static final ConcurrentMap<String, UtenteDTO> utentiCollection = db.hashMap(
            "utenti",
            Serializer.STRING,
            Serializer.JAVA
    ).createOrOpen();

    /**
     * Registra un nuovo utente applicando l'hashing SHA-256 alla password.
     */
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

        // 3. Controllo lunghezza password 
        if (utente.getPassword().length() < 8) {
            throw new IllegalArgumentException("La password deve essere di almeno 8 caratteri.");
        }

        // 4. Controllo email duplicata 
        if (utentiCollection.containsKey(email)) {
            return false; // Email già esistente, blocca il salvataggio
        }

        // 5. Cifratura della password con SHA-256 
        String passwordCifrata = hashPassword(utente.getPassword());
        utente.setPassword(passwordCifrata);

        // 6. Salvataggio nel database MapDB e commit
        utentiCollection.put(email, utente);
        DatabaseCore.commit(); 

        return true;
    }

    /**
     * Metodo di supporto per generare l'hash SHA-256 di una stringa.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Errore durante l'hashing della password", e);
        }
    }
}