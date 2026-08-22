package it.unibo.repository;

import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.Serializer;

import it.unibo.DatabaseCore;
import it.unibo.model.AnnuncioDTO;

/**
 * Gestisce la collection MapDB "annunci".
 * In questo task si occupa solo della configurazione della collection:
 * la logica di business (pubblica/modifica/rimuovi) verrà aggiunta in seguito.
 */
public class AnnuncioDatabase {

    private final DB db;

    // Mappa per memorizzare gli annunci: Chiave = id (String), Valore = AnnuncioDTO
    private final ConcurrentMap<String, AnnuncioDTO> annunciCollection;

    /**
     * Inizializza la collection "annunci" sul database condiviso,
     * creandola se non esiste ancora.
     */
    public AnnuncioDatabase() {
        this.db = DatabaseCore.getDB();
        this.annunciCollection = db.hashMap(
                "annunci",
                Serializer.STRING,
                Serializer.JAVA).createOrOpen();
    }

    /**
     * Restituisce la collection degli annunci.
     */
    public ConcurrentMap<String, AnnuncioDTO> getAnnunciCollection() {
        return annunciCollection;
    }
}
