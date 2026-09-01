package it.unibo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.mapdb.DB;
import org.mapdb.Serializer;

// Gestisce la collection MapDB "annunci"
public class AnnuncioDatabase {

    private final DB db;

    // Chiave = id (String), Valore = AnnuncioDTO
    private final ConcurrentMap<String, AnnuncioDTO> annunciCollection;

    // Serve a sapere se un annuncio ha uno scambio in corso: condivide lo stesso
    // DB, cosi' i test con database in memoria restano isolati
    private final RichiestaScambioDatabase richiesteDatabase;

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
        this.richiesteDatabase = new RichiestaScambioDatabase(db);
    }

    public ConcurrentMap<String, AnnuncioDTO> getAnnunciCollection() {
        return annunciCollection;
    }

    // Salva un nuovo annuncio su MapDB. L'idUtente si assume già valorizzato dal livello RPC.
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

    // Modifica un annuncio esistente, consentita solo al proprietario.
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

        // Con uno scambio gia' accettato l'annuncio non e' piu' disponibile:
        // cambiarne i termini in corsa penalizzerebbe chi lo ha ottenuto
        if (richiesteDatabase.esisteScambioInCorso(idAnnuncio)) {
            throw new IllegalArgumentException(
                    "Non puoi modificare un annuncio con uno scambio in corso");
        }

        // Validazione dei campi obbligatori
        validaCampoObbligatorio(annuncioAggiornato.getTitolo(), "titolo");
        validaCampoObbligatorio(annuncioAggiornato.getCompetenzaOfferta(), "competenza offerta");
        validaCampoObbligatorio(annuncioAggiornato.getDisponibilita(), "disponibilità");
        validaCampoObbligatorio(annuncioAggiornato.getControprestazione(), "controprestazione");

        // Preservo id, dataCreazione, proprietario e stato di
        // sospensione originali, applico le modifiche
        annuncioAggiornato.setId(esistente.getId());
        annuncioAggiornato.setDataCreazione(esistente.getDataCreazione());
        annuncioAggiornato.setIdUtente(esistente.getIdUtente());
        annuncioAggiornato.setSospeso(esistente.isSospeso());

        // Salvataggio e persistenza
        annunciCollection.put(annuncioAggiornato.getId(), annuncioAggiornato);
        DatabaseCore.commit();

        return annuncioAggiornato;
    }

    // Rimuove un annuncio esistente, consentita solo al proprietario.
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

        // Eliminarlo lascerebbe orfana la richiesta accettata: finche'
        // lo scambio e' in corso l'annuncio resta
        if (richiesteDatabase.esisteScambioInCorso(idAnnuncio)) {
            throw new IllegalArgumentException(
                    "Non puoi rimuovere un annuncio con uno scambio in corso");
        }

        // Rimozione e persistenza
        annunciCollection.remove(idAnnuncio);
        DatabaseCore.commit();
    }

    // Sospende o riattiva un annuncio, consentito solo al proprietario. Un annuncio sospeso sparisce
    // dal marketplace ma resta fra quelli dell'utente, pronto a essere riattivato.
    public AnnuncioDTO cambiaDisponibilita(String idAnnuncio, String idUtenteRichiedente, boolean sospeso)
            throws IllegalArgumentException {
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

        // Controllo proprietario: solo chi ha pubblicato l'annuncio puo' sospenderlo
        if (!idUtenteRichiedente.equals(esistente.getIdUtente())) {
            throw new IllegalArgumentException("Non autorizzato: l'annuncio appartiene a un altro utente");
        }

        // Stesso criterio di modifica e rimozione: con uno scambio accettato in
        // corso la disponibilita' non si tocca
        if (richiesteDatabase.esisteScambioInCorso(idAnnuncio)) {
            throw new IllegalArgumentException(
                    "Non puoi cambiare la disponibilità di un annuncio con uno scambio in corso");
        }

        esistente.setSospeso(sospeso);
        annunciCollection.put(esistente.getId(), esistente);
        DatabaseCore.commit();

        return esistente;
    }

    // Annunci pubblicati da un utente, dal piu' recente al piu' vecchio. Restituisce lista vuota se
    // l'utente non ne ha o se l'id non e' valorizzato.
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

    // Tutti gli annunci pubblicati, dal piu' recente al piu' vecchio.
    // Restituisce lista vuota se non ci sono annunci.
    public List<AnnuncioDTO> tuttiGliAnnunci() {
        List<AnnuncioDTO> risultato = new ArrayList<>(annunciCollection.values());

        // Ordine decrescente: il piu' recente in cima
        risultato.sort((a, b) -> Long.compare(b.getDataCreazione(), a.getDataCreazione()));

        return risultato;
    }

    // Filtra gli annunci in base alla competenza offerta (case-insensitive). Se la stringa di ricerca
    // è nulla, vuota o composta solo da spazi, restituisce l'intera lista.
    public List<AnnuncioDTO> filtraPerCompetenza(String skillQuery) {
        List<AnnuncioDTO> tutti = tuttiGliAnnunci();

        if (skillQuery == null || skillQuery.trim().isEmpty()) {
            return tutti;
        }

        String queryNormalizzata = skillQuery.trim().toLowerCase();

        return tutti.stream()
                .filter(a -> a.getCompetenzaOfferta() != null &&
                        a.getCompetenzaOfferta().toLowerCase().contains(queryNormalizzata))
                .collect(Collectors.toList());
    }

    // Restituisce una lista di annunci ordinata alfabeticamente in ordine crescente per titolo (A-Z).
    public List<AnnuncioDTO> ordinaPerTitolo(List<AnnuncioDTO> listaAnnunci) {
        if (listaAnnunci == null || listaAnnunci.isEmpty()) {
            return new ArrayList<>();
        }

        return listaAnnunci.stream()
                .sorted(Comparator.comparing(
                        a -> a.getTitolo() != null ? a.getTitolo() : "",
                        String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    // Lancia eccezione se il campo è null o vuoto dopo il trim
    private void validaCampoObbligatorio(String valore, String nomeCampo) throws IllegalArgumentException {
        if (valore == null || valore.trim().isEmpty()) {
            throw new IllegalArgumentException("Il campo '" + nomeCampo + "' è obbligatorio");
        }
    }
    // Ordina la lista degli annunci in base alla valutazione media dell'autore in ordine decrescente.
    // Gli autori senza recensioni (valutazione null) sono posizionati in fondo alla lista.
    public List ordinaPerRatingAutoreDesc(List<AnnuncioDTO> listaAnnunci) {
        if (listaAnnunci == null || listaAnnunci.isEmpty()) {
            return new ArrayList<>();
        }

        return listaAnnunci.stream()
                .sorted((a1, a2) -> {
                    Double r1 = a1.getValutazioneAutore();
                    Double r2 = a2.getValutazioneAutore();

                    if (r1 == null && r2 == null) return 0;
                    if (r1 == null) return 1;  // r1 va in fondo
                    if (r2 == null) return -1; // r2 va in fondo

                    return Double.compare(r2, r1); // Ordine decrescente (dal più alto al più basso)
                })
                .collect(Collectors.toList());
    }
    
}
