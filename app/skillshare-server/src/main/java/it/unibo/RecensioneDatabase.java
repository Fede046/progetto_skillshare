package it.unibo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.Serializer;

// Gestisce la collection MapDB "recensioni".
public class RecensioneDatabase {

    private final DB db;

    // Chiave = id (String), Valore = RecensioneDTO
    private final ConcurrentMap<String, RecensioneDTO> recensioniCollection;

    // Serve a verificare lo scambio collegato: condivide lo stesso DB
    private final RichiestaScambioDatabase richiesteDatabase;

    public RecensioneDatabase() {
        this(DatabaseCore.getDB());
    }

    // Costruttore usato dai test per passare un DB in memoria
    public RecensioneDatabase(DB db) {
        this.db = db;
        this.recensioniCollection = db.hashMap(
                "recensioni",
                Serializer.STRING,
                Serializer.JAVA).createOrOpen();
        this.richiesteDatabase = new RichiestaScambioDatabase(db);
    }

    public ConcurrentMap<String, RecensioneDTO> getRecensioniCollection() {
        return recensioniCollection;
    }

    // Salva la recensione di uno scambio concluso. L'idAnnuncio viene sempre ricavato dalla richiesta
    // collegata, non dal DTO in ingresso: e' il server a stabilire a quale annuncio appartiene.
    public RecensioneDTO lascia(RecensioneDTO recensione) throws IllegalArgumentException {
        if (recensione == null) {
            throw new IllegalArgumentException("Dati non validi");
        }

        validaCampoObbligatorio(recensione.getIdRichiestaScambio(), "id richiesta scambio");
        validaCampoObbligatorio(recensione.getIdAutore(), "id autore");

        // 1. Si recensisce solo uno scambio davvero concluso
        RichiestaScambioDTO scambio = richiesteDatabase.getRichiesteCollection()
                .get(recensione.getIdRichiestaScambio());
        if (scambio == null || scambio.getStato() != StatoRichiesta.COMPLETED) {
            throw new IllegalArgumentException("Non è possibile recensire uno scambio non ancora completato");
        }

        // 2. Voto nell'intervallo consentito
        if (recensione.getVoto() < 1 || recensione.getVoto() > 5) {
            throw new IllegalArgumentException("Il voto deve essere compreso tra 1 e 5");
        }

        // 3. Una sola recensione per autore su ciascuno scambio
        if (haGiaRecensito(recensione.getIdRichiestaScambio(), recensione.getIdAutore())) {
            throw new IllegalArgumentException("Hai già recensito questo scambio");
        }

        // 4. Id, annuncio di riferimento e marcatura temporale
        recensione.setId(UUID.randomUUID().toString());
        recensione.setIdAnnuncio(scambio.getIdAnnuncio());
        recensione.setDataCreazione(System.currentTimeMillis());

        // 5. Salvataggio e persistenza
        recensioniCollection.put(recensione.getId(), recensione);
        DatabaseCore.commit();

        return recensione;
    }

    // Recensioni collegate a un annuncio, dalla piu' recente alla piu' vecchia. Restituisce lista
    // vuota se l'id e' null/vuoto o non ci sono recensioni.
    public List<RecensioneDTO> recensioniPerAnnuncio(String idAnnuncio) {
        List<RecensioneDTO> risultato = new ArrayList<>();

        if (idAnnuncio == null || idAnnuncio.trim().isEmpty()) {
            return risultato;
        }

        String id = idAnnuncio.trim();
        for (RecensioneDTO recensione : recensioniCollection.values()) {
            if (id.equals(recensione.getIdAnnuncio())) {
                risultato.add(recensione);
            }
        }

        // Ordine decrescente: la piu' recente in cima
        risultato.sort((a, b) -> Long.compare(b.getDataCreazione(), a.getDataCreazione()));

        return risultato;
    }

    // Recensioni ricevute da un utente, dalla piu' recente alla piu' vecchia. Restituisce lista vuota
    // se l'id e' null/vuoto o non ci sono recensioni.
    public List<RecensioneDTO> recensioniRicevute(String idUtente) {
        List<RecensioneDTO> risultato = new ArrayList<>();

        if (idUtente == null || idUtente.trim().isEmpty()) {
            return risultato;
        }

        String id = idUtente.trim();
        for (RecensioneDTO recensione : recensioniCollection.values()) {
            if (id.equals(recensione.getIdDestinatario())) {
                risultato.add(recensione);
            }
        }

        // Ordine decrescente: la piu' recente in cima
        risultato.sort((a, b) -> Long.compare(b.getDataCreazione(), a.getDataCreazione()));

        return risultato;
    }

    // Media dei voti ricevuti da un utente. Restituisce null quando l'utente non ha ancora
    // recensioni: i voti vanno da 1 a 5, quindi 0.0 sarebbe indistinguibile da una media reale bassa.
    public Double ratingMedio(String idUtente) {
        List<RecensioneDTO> ricevute = recensioniRicevute(idUtente);

        if (ricevute.isEmpty()) {
            return null;
        }

        int somma = 0;
        for (RecensioneDTO recensione : ricevute) {
            somma += recensione.getVoto();
        }

        return (double) somma / ricevute.size();
    }

    // Vero se l'autore ha gia' recensito quello scambio
    private boolean haGiaRecensito(String idRichiestaScambio, String idAutore) {
        for (RecensioneDTO esistente : recensioniCollection.values()) {
            if (idRichiestaScambio.equals(esistente.getIdRichiestaScambio())
                    && idAutore.equals(esistente.getIdAutore())) {
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
