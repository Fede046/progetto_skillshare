package it.unibo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.Serializer;

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

    /**
     * Modifica un annuncio esistente, consentita solo al proprietario.
     *
     * @param idAnnuncio          L'id dell'annuncio da modificare.
     * @param idUtenteRichiedente L'id dell'utente che richiede la modifica.
     * @param annuncioAggiornato  L'annuncio con i dati aggiornati.
     * @return L'annuncio aggiornato e salvato.
     * @throws IllegalArgumentException Se l'annuncio non esiste, se il richiedente
     *                                  non e' il proprietario o se manca un campo obbligatorio.
     */
    public AnnuncioDTO modifica(String idAnnuncio, String idUtenteRichiedente, AnnuncioDTO annuncioAggiornato) throws IllegalArgumentException {
        if (idAnnuncio == null || idAnnuncio.trim().isEmpty()) {
            throw new IllegalArgumentException("Dati non validi");
        }
        if (idUtenteRichiedente == null || idUtenteRichiedente.trim().isEmpty()) {
            throw new IllegalArgumentException("Dati non validi");
        }
        if (annuncioAggiornato == null) {
            throw new IllegalArgumentException("Dati non validi");
        }

        AnnuncioDTO esistente = annunciCollection.get(idAnnuncio);
        if (esistente == null) {
            throw new IllegalArgumentException("Annuncio non trovato");
        }

        // Controllo proprietario: solo chi ha pubblicato l'annuncio puo' modificarlo
        if (!idUtenteRichiedente.equals(esistente.getIdUtente())) {
            throw new IllegalArgumentException("Non autorizzato: l'annuncio appartiene a un altro utente");
        }

        // Validazione dei campi obbligatori
        validaCampoObbligatorio(annuncioAggiornato.getTitolo(), "titolo");
        validaCampoObbligatorio(annuncioAggiornato.getCompetenzaOfferta(), "competenza offerta");
        validaCampoObbligatorio(annuncioAggiornato.getDisponibilita(), "disponibilità");
        validaCampoObbligatorio(annuncioAggiornato.getControprestazione(), "controprestazione");

        // Preservo id, dataCreazione e proprietario originali, applico le modifiche
        annuncioAggiornato.setId(esistente.getId());
        annuncioAggiornato.setDataCreazione(esistente.getDataCreazione());
        annuncioAggiornato.setIdUtente(esistente.getIdUtente());

        // Salvataggio e persistenza
        annunciCollection.put(annuncioAggiornato.getId(), annuncioAggiornato);
        DatabaseCore.commit();

        return annuncioAggiornato;
    }

    /**
     * Rimuove un annuncio esistente, consentita solo al proprietario.
     *
     * @param idAnnuncio          L'id dell'annuncio da rimuovere.
     * @param idUtenteRichiedente L'id dell'utente che richiede la rimozione.
     * @throws IllegalArgumentException Se l'annuncio non esiste o se il richiedente
     *                                  non e' il proprietario.
     */
    public void rimuovi(String idAnnuncio, String idUtenteRichiedente) throws IllegalArgumentException {
        if (idAnnuncio == null || idAnnuncio.trim().isEmpty()) {
            throw new IllegalArgumentException("Dati non validi");
        }
        if (idUtenteRichiedente == null || idUtenteRichiedente.trim().isEmpty()) {
            throw new IllegalArgumentException("Dati non validi");
        }

        AnnuncioDTO esistente = annunciCollection.get(idAnnuncio);
        if (esistente == null) {
            throw new IllegalArgumentException("Annuncio non trovato");
        }

        // Controllo proprietario: solo chi ha pubblicato l'annuncio puo' rimuoverlo
        if (!idUtenteRichiedente.equals(esistente.getIdUtente())) {
            throw new IllegalArgumentException("Non autorizzato: l'annuncio appartiene a un altro utente");
        }

        // Rimozione e persistenza
        annunciCollection.remove(idAnnuncio);
        DatabaseCore.commit();
    }

    /**
     * Annunci pubblicati da un utente, dal piu' recente al piu' vecchio.
     * Restituisce lista vuota se l'utente non ne ha o se l'id non e' valorizzato.
     */
    public List<AnnuncioDTO> annunciDiUtente(String idUtente) {
        List<AnnuncioDTO> risultato = new ArrayList<>();

        if (idUtente == null || idUtente.trim().isEmpty()) {
            return risultato;
        }

        String id = idUtente.trim();
        for (AnnuncioDTO annuncio : annunciCollection.values()) {
            if (id.equals(annuncio.getIdUtente())) {
                risultato.add(annuncio);
            }
        }

        // Ordine decrescente: il piu' recente in cima
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
