package it.unibo;

import java.util.List;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

public class AnnuncioServiceImpl extends RemoteServiceServlet implements AnnuncioService {

    private static final long serialVersionUID = 1L;

    @Override
    public AnnuncioDTO pubblica(AnnuncioDTO annuncio) throws IllegalArgumentException {
        // Validazioni e persistenza vivono in AnnuncioDatabase
        return new AnnuncioDatabase().pubblica(annuncio);
    }

    @Override
    public List<AnnuncioDTO> annunciDiUtente(String idUtente) {
        return new AnnuncioDatabase().annunciDiUtente(idUtente);
    }

    @Override
    public AnnuncioDTO modifica(String idAnnuncio, String idUtenteRichiedente, AnnuncioDTO annuncioAggiornato) throws IllegalArgumentException {
        // Delega la gestione e il controllo del proprietario a AnnuncioDatabase
        return new AnnuncioDatabase().modifica(idAnnuncio, idUtenteRichiedente, annuncioAggiornato);
    }

    @Override
    public void rimuovi(String idAnnuncio, String idUtenteRichiedente) throws IllegalArgumentException {
        // Delega la gestione e il controllo del proprietario a AnnuncioDatabase
        new AnnuncioDatabase().rimuovi(idAnnuncio, idUtenteRichiedente);
    }

    @Override
    public AnnuncioDTO cambiaDisponibilita(String idAnnuncio, String idUtenteRichiedente, boolean sospeso)
            throws IllegalArgumentException {
        return new AnnuncioDatabase().cambiaDisponibilita(idAnnuncio, idUtenteRichiedente, sospeso);
    }
}
