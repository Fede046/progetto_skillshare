package it.unibo;

import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.Serializer;

/* Gestisce la collection MapDB "messaggi" */
public class MessaggioDatabase {

    private final DB db;

    // Chiave = id (String), Valore = MessaggioDTO
    private final ConcurrentMap messaggiCollection;

    public MessaggioDatabase() {
        this(DatabaseCore.getDB());
    }

    // Costruttore usato dai test per passare un DB in memoria
    public MessaggioDatabase(DB db) {
        this.db = db;
        this.messaggiCollection = db.hashMap(
                "messaggi",
                Serializer.STRING,
                Serializer.JAVA).createOrOpen();
    }

    public ConcurrentMap getMessaggiCollection() {
        return messaggiCollection;
    }

    /**
     * Salva un nuovo messaggio su MapDB.
     */
    public MessaggioDTO salva(MessaggioDTO messaggio) throws IllegalArgumentException {
        if (messaggio == null) {
            throw new IllegalArgumentException("Dati non validi");
        }

        // Genera un id se non è presente
        if (messaggio.getId() == null || messaggio.getId().trim().isEmpty()) {
            messaggio.setId(java.util.UUID.randomUUID().toString());
        }

        // Valorizza il timestamp se non è già impostato
        if (messaggio.getTimestamp() <= 0) {
            messaggio.setTimestamp(System.currentTimeMillis());
        }

        messaggiCollection.put(messaggio.getId(), messaggio);
        DatabaseCore.commit();

        return messaggio;
    }
}