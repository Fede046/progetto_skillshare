package it.unibo.repository;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.Serializer;

import it.unibo.DatabaseCore;
import it.unibo.model.AnnuncioDTO;

/**
 * Gestisce la collection MapDB "annunci".
 */
public class AnnuncioDatabase {

    private final DB db;

    // Chiave = id (String), Valore = AnnuncioDTO
    private final ConcurrentMap<String, AnnuncioDTO> annunciCollection;

    public AnnuncioDatabase() {
        this(DatabaseCore.getDB());
    }

    // Costruttore usato dai test per passare un DB in memoria
    public AnnuncioDatabase(DB db) {
        this.db = db;
        this.annunciCollection = db.hashMap(
                "annunci",
                Serializer.STRING,
                Serializer.JAVA).createOrOpen();
    }

    public ConcurrentMap<String, AnnuncioDTO> getAnnunciCollection() {
        return annunciCollection;
    }

    /**
     * Salva un nuovo annuncio su MapDB.
     * L'idUtente si assume già valorizzato dal livello RPC.
     */
    public AnnuncioDTO pubblica(AnnuncioDTO annuncio) throws IllegalArgumentException {
        if (annuncio == null) {
            throw new IllegalArgumentException("Dati non validi");
        }

        // 1. Controllo dei campi obbligatori
        validaCampoObbligatorio(annuncio.getTitolo(), "titolo");
        validaCampoObbligatorio(annuncio.getCompetenzaOfferta(), "competenza offerta");
        validaCampoObbligatorio(annuncio.getDisponibilita(), "disponibilità");
        validaCampoObbligatorio(annuncio.getControprestazione(), "controprestazione");

        // 2. Genero l'id solo se non è già stato impostato
        if (annuncio.getId() == null || annuncio.getId().trim().isEmpty()) {
            annuncio.setId(UUID.randomUUID().toString());
        }

        annuncio.setDataCreazione(System.currentTimeMillis());

        // 3. Salvataggio e persistenza
        annunciCollection.put(annuncio.getId(), annuncio);
        DatabaseCore.commit();

        return annuncio;
    }

    // Lancia eccezione se il campo è null o vuoto dopo il trim
    private void validaCampoObbligatorio(String valore, String nomeCampo) throws IllegalArgumentException {
        if (valore == null || valore.trim().isEmpty()) {
            throw new IllegalArgumentException("Il campo '" + nomeCampo + "' è obbligatorio");
        }
    }
}
