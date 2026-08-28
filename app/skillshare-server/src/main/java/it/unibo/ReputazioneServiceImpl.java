package it.unibo;

import java.util.List;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

public class ReputazioneServiceImpl extends RemoteServiceServlet implements ReputazioneService {

    private static final long serialVersionUID = 1L;

    private final RecensioneDatabase recensioneDatabase;

    // Costruttore usato dal container (web.xml): delega al database reale su DatabaseCore
    public ReputazioneServiceImpl() {
        this(new RecensioneDatabase());
    }

    // Costruttore usato dai test per iniettare un DB in memoria senza toccare DatabaseCore
    public ReputazioneServiceImpl(RecensioneDatabase recensioneDatabase) {
        this.recensioneDatabase = recensioneDatabase;
    }

    @Override
    public Double ratingMedio(String idUtente) {
        // Il calcolo della media vive in RecensioneDatabase
        return recensioneDatabase.ratingMedio(idUtente);
    }

    @Override
    public List<RecensioneDTO> recensioniRicevute(String idUtente) {
        // Filtro per destinatario e ordinamento vivono in RecensioneDatabase
        return recensioneDatabase.recensioniRicevute(idUtente);
    }
}
