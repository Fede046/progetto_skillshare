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
}
