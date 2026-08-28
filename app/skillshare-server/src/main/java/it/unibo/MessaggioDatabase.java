package it.unibo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.Serializer;

/**
 * Gestisce la collection MapDB "messaggi" e la logica della chat interna.
 */
public class MessaggioDatabase {

    private final DB db;

    // Chiave = id (String), Valore = MessaggioDTO
    private final ConcurrentMap messaggiCollection;
    private final RichiestaScambioDatabase richiestaScambioDatabase;

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
        this.richiestaScambioDatabase = new RichiestaScambioDatabase(db);
    }

    public ConcurrentMap getMessaggiCollection() {
        return messaggiCollection;
    }

    /**
     * Invia un messaggio di chat verificando che:
     * - idMittente sia uno dei due partecipanti della richiesta di scambio collegata.
     * - la richiesta di scambio abbia lo stato "ACCEPTED".
     * In caso contrario, lancia un'IllegalArgumentException.
     */
    public MessaggioDTO inviaMessaggio(MessaggioDTO messaggio) throws IllegalArgumentException {
        if (messaggio == null) {
            throw new IllegalArgumentException("Dati del messaggio non validi");
        }

        validaCampoObbligatorio(messaggio.getIdRichiestaScambio(), "id richiesta scambio");
        validaCampoObbligatorio(messaggio.getIdMittente(), "id mittente");
        validaCampoObbligatorio(messaggio.getTesto(), "testo");

        // 1. Recupera la richiesta di scambio associata
        RichiestaScambioDTO richiesta = richiestaScambioDatabase.getRichiesteCollection()
                .get(messaggio.getIdRichiestaScambio());
        
        if (richiesta == null) {
            throw new IllegalArgumentException("Richiesta di scambio non trovata");
        }

        // 2. Verifica che lo stato sia ACCEPTED
        if (richiesta.getStato() != StatoRichiesta.ACCEPTED) {
            throw new IllegalArgumentException("Impossibile inviare messaggi se la richiesta non è stata accettata");
        }

        // 3. Verifica che il mittente sia uno dei due partecipanti (creatore annuncio o richiedente)
        String mittente = messaggio.getIdMittente().trim();
        boolean isPartecipante = mittente.equals(richiesta.getIdCreatoreAnnuncio()) ||
                                 mittente.equals(richiesta.getIdRichiedente());
        
        if (!isPartecipante) {
            throw new IllegalArgumentException("L'utente non è autorizzato a inviare messaggi per questa richiesta");
        }

        // 4. Generazione ID se non presente
        if (messaggio.getId() == null || messaggio.getId().trim().isEmpty()) {
            messaggio.setId(UUID.randomUUID().toString());
        }

        // 5. Valorizzazione timestamp
        messaggio.setTimestamp(System.currentTimeMillis());

        // 6. Salvataggio e persistenza
        messaggiCollection.put(messaggio.getId(), messaggio);
        DatabaseCore.commit();

        return messaggio;
    }

    /**
     * Restituisce i messaggi di una richiesta di scambio ordinati per timestamp ascendente,
     * solo se idUtenteRichiedente è uno dei due partecipanti, altrimenti lancia un'IllegalArgumentException.
     */
public List getMessaggi(String idRichiestaScambio, String idUtenteRichiedente) throws IllegalArgumentException {
        if (idRichiestaScambio == null || idRichiestaScambio.trim().isEmpty()) {
            throw new IllegalArgumentException("Id richiesta non valido");
        }
        if (idUtenteRichiedente == null || idUtenteRichiedente.trim().isEmpty()) {
            throw new IllegalArgumentException("Id utente non valido");
        }

        // 1. Recupera la richiesta di scambio
        RichiestaScambioDTO richiesta = richiestaScambioDatabase.getRichiesteCollection()
                .get(idRichiestaScambio);
        
        if (richiesta == null) {
            throw new IllegalArgumentException("Richiesta di scambio non trovata");
        }

        // 2. Verifica che l'utente sia uno dei due partecipanti
        String utente = idUtenteRichiedente.trim();
        boolean isPartecipante = utente.equals(richiesta.getIdCreatoreAnnuncio()) ||
                                 utente.equals(richiesta.getIdRichiedente());
        
        if (!isPartecipante) {
            throw new IllegalArgumentException("Non autorizzato a visualizzare i messaggi di questa chat");
        }

        // 3. Filtra i messaggi appartenenti a questa richiesta
        List risultato = new ArrayList<>();
        for (Object obj : messaggiCollection.values()) {
            if (obj instanceof MessaggioDTO) {
                MessaggioDTO m = (MessaggioDTO) obj;
                if (idRichiestaScambio.equals(m.getIdRichiestaScambio())) {
                    risultato.add(m);
                }
            }
        }

        // 4. Ordina per timestamp ascendente (dal più vecchio al più recente) usando un Comparator esplicito
        risultato.sort(java.util.Comparator.comparingLong(MessaggioDTO::getTimestamp));
        
        // (Nota: se preferisci la lambda puoi usare direttamente questa riga al posto del Comparator sopra:
        // risultato.sort((MessaggioDTO m1, MessaggioDTO m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));

        return risultato;
    }

    // Metodo di supporto per validare i campi obbligatori
    private void validaCampoObbligatorio(String valore, String nomeCampo) throws IllegalArgumentException {
        if (valore == null || valore.trim().isEmpty()) {
            throw new IllegalArgumentException("Il campo '" + nomeCampo + "' è obbligatorio");
        }
    }
}