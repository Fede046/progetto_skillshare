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
        List<RecensioneDTO> recensioni = recensioneDatabase.recensioniPerAnnuncio(idAnnuncio);

        // Arricchisce ogni recensione con il nome completo di chi l'ha scritta
        for (RecensioneDTO recensione : recensioni) {
            recensione.setNomeAutore(nomeAutore(recensione.getIdAutore()));
        }

        return recensioni;
    }

    // Nome e cognome dell'autore, con fallback sull'email se l'utente non esiste piu'
    private String nomeAutore(String idAutore) {
        try {
            UtenteDTO autore = UtenteDatabase.getProfilo(idAutore);
            String nome = autore.getNome() != null ? autore.getNome().trim() : "";
            String cognome = autore.getCognome() != null ? autore.getCognome().trim() : "";
            String completo = (nome + " " + cognome).trim();
            return completo.isEmpty() ? idAutore : completo;
        } catch (IllegalArgumentException e) {
            // Autore non piu' registrato: resta visibile la sua email
            return idAutore;
        }
    }
}
