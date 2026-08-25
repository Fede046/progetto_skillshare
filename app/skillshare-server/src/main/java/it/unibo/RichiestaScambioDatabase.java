package it.unibo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.Serializer;

/**
 * Gestisce la collection MapDB "richiesteScambio".
 */
public class RichiestaScambioDatabase {

    private final DB db;

    // Chiave = id (String), Valore = RichiestaScambioDTO
    private final ConcurrentMap<String, RichiestaScambioDTO> richiesteCollection;

    public RichiestaScambioDatabase() {
        this(DatabaseCore.getDB());
    }

    // Costruttore usato dai test per passare un DB in memoria
    public RichiestaScambioDatabase(DB db) {
        this.db = db;
        this.richiesteCollection = db.hashMap(
                "richiesteScambio",
                Serializer.STRING,
                Serializer.JAVA).createOrOpen();
    }

    public ConcurrentMap<String, RichiestaScambioDTO> getRichiesteCollection() {
        return richiesteCollection;
    }

    /**
     * Salva una nuova richiesta di scambio su MapDB con stato PENDING.
     * L'idRichiedente e l'idCreatoreAnnuncio si assumono già valorizzati dal livello RPC.
     *
     * @return La richiesta salvata (con id, stato PENDING e dataCreazione valorizzati).
     * @throws IllegalArgumentException Se la richiesta è null o manca un campo obbligatorio.
     */
    public RichiestaScambioDTO salva(RichiestaScambioDTO richiesta) throws IllegalArgumentException {
        if (richiesta == null) {
            throw new IllegalArgumentException("Dati non validi");
        }

        // 1. Controllo dei campi obbligatori
        validaCampoObbligatorio(richiesta.getIdAnnuncio(), "id annuncio");
        validaCampoObbligatorio(richiesta.getIdRichiedente(), "id richiedente");
        validaCampoObbligatorio(richiesta.getIdCreatoreAnnuncio(), "id creatore annuncio");

        // 2. Genero l'id solo se non è già stato impostato
        if (richiesta.getId() == null || richiesta.getId().trim().isEmpty()) {
            richiesta.setId(UUID.randomUUID().toString());
        }

        // 3. Le nuove richieste partono sempre con stato PENDING
        richiesta.setStato(StatoRichiesta.PENDING);

        richiesta.setDataCreazione(System.currentTimeMillis());

        // 4. Salvataggio e persistenza
        richiesteCollection.put(richiesta.getId(), richiesta);
        DatabaseCore.commit();

        return richiesta;
    }

    /**
     * Richieste di scambio ricevute da un creatore di annunci,
     * dalla più recente alla più vecchia.
     * Restituisce lista vuota se l'id è null/vuoto o non ci sono richieste.
     */
    public List<RichiestaScambioDTO> richiesteRicevuteDaCreatore(String idCreatore) {
        List<RichiestaScambioDTO> risultato = new ArrayList<>();

        if (idCreatore == null || idCreatore.trim().isEmpty()) {
            return risultato;
        }

        String id = idCreatore.trim();
        for (RichiestaScambioDTO richiesta : richiesteCollection.values()) {
            if (id.equals(richiesta.getIdCreatoreAnnuncio())) {
                risultato.add(richiesta);
            }
        }

        // Ordine decrescente: la più recente in cima
        risultato.sort((a, b) -> Long.compare(b.getDataCreazione(), a.getDataCreazione()));

        return risultato;
    }

    /**
     * Richieste di scambio inviate da un utente richiedente,
     * dalla più recente alla più vecchia.
     * Restituisce lista vuota se l'id è null/vuoto o non ci sono richieste.
     */
    public List<RichiestaScambioDTO> richiesteInviateDaRichiedente(String idRichiedente) {
        List<RichiestaScambioDTO> risultato = new ArrayList<>();

        if (idRichiedente == null || idRichiedente.trim().isEmpty()) {
            return risultato;
        }

        String id = idRichiedente.trim();
        for (RichiestaScambioDTO richiesta : richiesteCollection.values()) {
            if (id.equals(richiesta.getIdRichiedente())) {
                risultato.add(richiesta);
            }
        }

        // Ordine decrescente: la più recente in cima
        risultato.sort((a, b) -> Long.compare(b.getDataCreazione(), a.getDataCreazione()));

        return risultato;
    }

    // Lancia eccezione se il campo è null o vuoto dopo il trim
    private void validaCampoObbligatorio(String valore, String nomeCampo) throws IllegalArgumentException {
        if (valore == null || valore.trim().isEmpty()) {
            throw new IllegalArgumentException("Il campo '" + nomeCampo + "' è obbligatorio");
        }
    }
}
