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
     * @throws IllegalArgumentException Se la richiesta è null, manca un campo obbligatorio
     *                                  o il richiedente ha già una richiesta non completata
     *                                  sullo stesso annuncio.
     */
    public RichiestaScambioDTO salva(RichiestaScambioDTO richiesta) throws IllegalArgumentException {
        if (richiesta == null) {
            throw new IllegalArgumentException("Dati non validi");
        }

        // 1. Controllo dei campi obbligatori
        validaCampoObbligatorio(richiesta.getIdAnnuncio(), "id annuncio");
        validaCampoObbligatorio(richiesta.getIdRichiedente(), "id richiedente");
        validaCampoObbligatorio(richiesta.getIdCreatoreAnnuncio(), "id creatore annuncio");

        // 2. Una nuova richiesta sullo stesso annuncio è ammessa solo se la precedente
        //    è stata completata: PENDING, ACCEPTED e REJECTED bloccano l'invio.
        if (esisteRichiestaNonCompletata(richiesta.getIdRichiedente(), richiesta.getIdAnnuncio())) {
            throw new IllegalArgumentException("Hai già una richiesta su questo annuncio non ancora completata");
        }

        // 3. Genero l'id solo se non è già stato impostato
        if (richiesta.getId() == null || richiesta.getId().trim().isEmpty()) {
            richiesta.setId(UUID.randomUUID().toString());
        }

        // 4. Le nuove richieste partono sempre con stato PENDING
        richiesta.setStato(StatoRichiesta.PENDING);

        richiesta.setDataCreazione(System.currentTimeMillis());

        // 5. Salvataggio e persistenza
        richiesteCollection.put(richiesta.getId(), richiesta);
        DatabaseCore.commit();

        return richiesta;
    }

    /**
     * Accetta una richiesta di scambio: solo il creatore dell'annuncio può accettarla.
     *
     * @param idRichiesta L'id della richiesta da accettare.
     * @param idCreatore  L'id dell'utente autenticato che accetta (deve essere il creatore dell'annuncio).
     * @return La richiesta aggiornata con stato ACCEPTED.
     * @throws IllegalArgumentException Se l'id è nullo/vuoto, se la richiesta non esiste
     *                                  o se l'utente non è il creatore dell'annuncio.
     */
    public RichiestaScambioDTO accetta(String idRichiesta, String idCreatore) throws IllegalArgumentException {
        return aggiornaStatoRichiesta(idRichiesta, idCreatore, StatoRichiesta.ACCEPTED, "accettare");
    }

    /**
     * Rifiuta una richiesta di scambio: solo il creatore dell'annuncio può rifiutarla.
     *
     * @param idRichiesta L'id della richiesta da rifiutare.
     * @param idCreatore  L'id dell'utente autenticato che rifiuta (deve essere il creatore dell'annuncio).
     * @return La richiesta aggiornata con stato REJECTED.
     * @throws IllegalArgumentException Se l'id è nullo/vuoto, se la richiesta non esiste
     *                                  o se l'utente non è il creatore dell'annuncio.
     */
    public RichiestaScambioDTO rifiuta(String idRichiesta, String idCreatore) throws IllegalArgumentException {
        return aggiornaStatoRichiesta(idRichiesta, idCreatore, StatoRichiesta.REJECTED, "rifiutare");
    }

    /**
     * Aggiorna lo stato di una richiesta esistente, consentito solo al creatore dell'annuncio.
     * Nessuna guardia sulle transizioni: cambia lo stato a prescindere da quello corrente
     * (la regola "solo PENDING è decidibile" appartiene al layer di orchestrazione di US-11,
     * non alla persistenza).
     *
     * @param verbo Il verbo all'infinito usato nel messaggio di non autorizzazione
     *              ("accettare" per accetta, "rifiutare" per rifiuta).
     */
    private RichiestaScambioDTO aggiornaStatoRichiesta(String idRichiesta, String idCreatore,
            StatoRichiesta nuovoStato, String verbo) throws IllegalArgumentException {
        if (idRichiesta == null || idRichiesta.trim().isEmpty()) {
            throw new IllegalArgumentException("Dati non validi");
        }
        if (idCreatore == null || idCreatore.trim().isEmpty()) {
            throw new IllegalArgumentException("Dati non validi");
        }

        RichiestaScambioDTO esistente = richiesteCollection.get(idRichiesta);
        if (esistente == null) {
            throw new IllegalArgumentException("Richiesta non trovata");
        }

        // Controllo proprietario: solo chi ha pubblicato l'annuncio puo' decidere sulla richiesta
        if (!idCreatore.equals(esistente.getIdCreatoreAnnuncio())) {
            throw new IllegalArgumentException(
                    "Non autorizzato: solo il creatore dell'annuncio può " + verbo + " la richiesta");
        }

        // Aggiornamento dello stato e persistenza
        esistente.setStato(nuovoStato);
        richiesteCollection.put(esistente.getId(), esistente);
        DatabaseCore.commit();

        return esistente;
    }

    /**
     * Segna come COMPLETED uno scambio gia' accettato.
     * A differenza di accetta/rifiuta, qui puo' agire ciascuno dei due
     * partecipanti: lo scambio si conclude per entrambi.
     *
     * @param idRichiesta          L'id della richiesta da completare.
     * @param idUtenteRichiedente  L'id dell'utente autenticato che completa
     *                             (il richiedente o il creatore dell'annuncio).
     * @return La richiesta aggiornata con stato COMPLETED.
     * @throws IllegalArgumentException Se l'id e' nullo/vuoto, se la richiesta non esiste,
     *                                  se non e' ancora accettata o se l'utente non partecipa allo scambio.
     */
    public RichiestaScambioDTO completa(String idRichiesta, String idUtenteRichiedente) throws IllegalArgumentException {
        if (idRichiesta == null || idRichiesta.trim().isEmpty()) {
            throw new IllegalArgumentException("Dati non validi");
        }
        if (idUtenteRichiedente == null || idUtenteRichiedente.trim().isEmpty()) {
            throw new IllegalArgumentException("Dati non validi");
        }

        // 1. La richiesta deve esistere
        RichiestaScambioDTO esistente = richiesteCollection.get(idRichiesta);
        if (esistente == null) {
            throw new IllegalArgumentException("Richiesta non trovata");
        }

        // 2. Si completa solo cio' che e' stato accettato
        if (esistente.getStato() != StatoRichiesta.ACCEPTED) {
            throw new IllegalArgumentException("Impossibile completare uno scambio non ancora accettato");
        }

        // 3. Deve agire uno dei due partecipanti allo scambio
        String utente = idUtenteRichiedente.trim();
        boolean partecipante = utente.equals(esistente.getIdRichiedente())
                || utente.equals(esistente.getIdCreatoreAnnuncio());
        if (!partecipante) {
            throw new IllegalArgumentException("Non sei autorizzato a completare questo scambio");
        }

        // Aggiornamento dello stato e persistenza
        esistente.setStato(StatoRichiesta.COMPLETED);
        richiesteCollection.put(esistente.getId(), esistente);
        DatabaseCore.commit();

        return esistente;
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

    /**
     * Indica se sull'annuncio c'è uno scambio in corso, cioè una richiesta già
     * accettata e non ancora completata. Finché dura, l'annuncio non è più
     * disponibile: chi ha ottenuto lo scambio conta su quei termini.
     *
     * @param idAnnuncio L'id dell'annuncio da controllare.
     * @return true se esiste almeno una richiesta in stato ACCEPTED su quell'annuncio.
     */
    public boolean esisteScambioInCorso(String idAnnuncio) {
        if (idAnnuncio == null || idAnnuncio.trim().isEmpty()) {
            return false;
        }

        String annuncio = idAnnuncio.trim();
        for (RichiestaScambioDTO esistente : richiesteCollection.values()) {
            if (annuncio.equals(esistente.getIdAnnuncio())
                    && esistente.getStato() == StatoRichiesta.ACCEPTED) {
                return true;
            }
        }

        return false;
    }

    /**
     * Indica se il richiedente ha già una richiesta ancora aperta sull'annuncio indicato.
     * Il controllo è ristretto alla coppia richiedente+annuncio: le richieste dello stesso
     * utente su annunci diversi non si bloccano a vicenda.
     *
     * @return true se esiste una richiesta in stato PENDING, ACCEPTED o REJECTED.
     */
    private boolean esisteRichiestaNonCompletata(String idRichiedente, String idAnnuncio) {
        String richiedente = idRichiedente.trim();
        String annuncio = idAnnuncio.trim();

        for (RichiestaScambioDTO esistente : richiesteCollection.values()) {
            if (richiedente.equals(esistente.getIdRichiedente())
                    && annuncio.equals(esistente.getIdAnnuncio())
                    && esistente.getStato() != StatoRichiesta.COMPLETED) {
                return true;
            }
        }

        return false;
    }

    // Lancia eccezione se il campo è null o vuoto dopo il trim
    private void validaCampoObbligatorio(String valore, String nomeCampo) throws IllegalArgumentException {
        if (valore == null || valore.trim().isEmpty()) {
            throw new IllegalArgumentException("Il campo '" + nomeCampo + "' è obbligatorio");
        }
    }
}
