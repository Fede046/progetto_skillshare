package it.unibo;

import java.util.List;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

public class MarketplaceServiceImpl extends RemoteServiceServlet implements MarketplaceService {

    private static final long serialVersionUID = 1L;

    @Override
    public List<AnnuncioDTO> listaAnnunci() {
        // Delega ad AnnuncioDatabase: tutti gli annunci, dal piu' recente al piu' vecchio
        List<AnnuncioDTO> annunci = new AnnuncioDatabase().tuttiGliAnnunci();

        // Arricchisce ogni annuncio con il nome completo dell'autore
        for (AnnuncioDTO annuncio : annunci) {
            annuncio.setNomeAutore(nomeAutore(annuncio.getIdUtente()));
        }

        return annunci;
    }

    // Nome e cognome dell'autore, con fallback sull'email se l'utente non esiste piu'
    private String nomeAutore(String idUtente) {
        try {
            UtenteDTO autore = UtenteDatabase.getProfilo(idUtente);
            String nome = autore.getNome() != null ? autore.getNome().trim() : "";
            String cognome = autore.getCognome() != null ? autore.getCognome().trim() : "";
            String completo = (nome + " " + cognome).trim();
            return completo.isEmpty() ? idUtente : completo;
        } catch (IllegalArgumentException e) {
            // Autore non piu' registrato: resta visibile la sua email
            return idUtente;
        }
    }
}
