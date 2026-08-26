package it.unibo;

import java.util.List;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

public class RichiestaScambioServiceImpl extends RemoteServiceServlet implements RichiestaScambioService {

    private static final long serialVersionUID = 1L;

    private final AnnuncioDatabase annuncioDatabase;
    private final RichiestaScambioDatabase richiesteDatabase;

    // Costruttore usato dal container (web.xml): delega ai database reali su DatabaseCore
    public RichiestaScambioServiceImpl() {
        this(new AnnuncioDatabase(), new RichiestaScambioDatabase());
    }

    // Costruttore usato dai test per iniettare DB in memoria senza toccare il singleton DatabaseCore
    public RichiestaScambioServiceImpl(AnnuncioDatabase annuncioDatabase, RichiestaScambioDatabase richiesteDatabase) {
        this.annuncioDatabase = annuncioDatabase;
        this.richiesteDatabase = richiesteDatabase;
    }

    @Override
    public RichiestaScambioDTO inviaRichiestaScambio(String idAnnuncio, String idRichiedente, String messaggio)
            throws IllegalArgumentException {
        // 1. Controllo dei dati obbligatori
        if (idAnnuncio == null || idAnnuncio.trim().isEmpty()) {
            throw new IllegalArgumentException("Dati non validi");
        }
        if (idRichiedente == null || idRichiedente.trim().isEmpty()) {
            throw new IllegalArgumentException("Dati non validi");
        }

        // 2. Carico l'annuncio target: il creatore va risolto dal server, non dal client
        AnnuncioDTO annuncio = annuncioDatabase.getAnnunciCollection().get(idAnnuncio);
        if (annuncio == null) {
            throw new IllegalArgumentException("Annuncio non trovato");
        }

        // 3. Vietato lo scambio con se stessi: non puoi richiedere il tuo stesso annuncio
        String idCreatore = annuncio.getIdUtente();
        if (idRichiedente.equals(idCreatore)) {
            throw new IllegalArgumentException("Non puoi inviare una richiesta di scambio sul tuo stesso annuncio");
        }

        // 4. Compongo la richiesta e delego la persistenza a RichiestaScambioDatabase
        RichiestaScambioDTO richiesta = new RichiestaScambioDTO();
        richiesta.setIdAnnuncio(idAnnuncio);
        richiesta.setIdRichiedente(idRichiedente);
        richiesta.setIdCreatoreAnnuncio(idCreatore);
        richiesta.setMessaggio(messaggio);

        return richiesteDatabase.salva(richiesta);
    }

    @Override
    public List<RichiestaScambioDTO> richiesteRicevuteDaCreatore(String idCreatore) {
        return richiesteDatabase.richiesteRicevuteDaCreatore(idCreatore);
    }

    @Override
    public List<RichiestaScambioDTO> richiesteInviateDaRichiedente(String idRichiedente) {
        return richiesteDatabase.richiesteInviateDaRichiedente(idRichiedente);
    }

    @Override
    public RichiestaScambioDTO accetta(String idRichiesta, String idCreatore) throws IllegalArgumentException {
        // Le eccezioni di ownership/inesistenza del Database risalgono fino al client
        return richiesteDatabase.accetta(idRichiesta, idCreatore);
    }

    @Override
    public RichiestaScambioDTO rifiuta(String idRichiesta, String idCreatore) throws IllegalArgumentException {
        // Le eccezioni di ownership/inesistenza del Database risalgono fino al client
        return richiesteDatabase.rifiuta(idRichiesta, idCreatore);
    }
}

