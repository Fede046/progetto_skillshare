package it.unibo;

import java.util.List;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

public class RecensioneServiceImpl extends RemoteServiceServlet implements RecensioneService {

    private static final long serialVersionUID = 1L;

    private final RecensioneDatabase recensioneDatabase;

    // Costruttore usato dal container (web.xml): delega al database reale su DatabaseCore
    public RecensioneServiceImpl() {
        this(new RecensioneDatabase());
    }

    // Costruttore usato dai test per iniettare un DB in memoria senza toccare DatabaseCore
    public RecensioneServiceImpl(RecensioneDatabase recensioneDatabase) {
        this.recensioneDatabase = recensioneDatabase;
    }

    @Override
    public RecensioneDTO lascia(RecensioneDTO recensione) throws IllegalArgumentException {
        // Validazioni e persistenza vivono in RecensioneDatabase
        return recensioneDatabase.lascia(recensione);
    }

    @Override
    public List<RecensioneDTO> recensioniPerAnnuncio(String idAnnuncio) {
        return recensioneDatabase.recensioniPerAnnuncio(idAnnuncio);
    }
}
