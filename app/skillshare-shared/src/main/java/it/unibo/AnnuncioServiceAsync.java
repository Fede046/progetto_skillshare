package it.unibo;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AnnuncioServiceAsync {
    void pubblica(AnnuncioDTO annuncio, AsyncCallback<AnnuncioDTO> callback);

    void annunciDiUtente(String idUtente, AsyncCallback<List<AnnuncioDTO>> callback);

    void modifica(String idAnnuncio, String idUtenteRichiedente, AnnuncioDTO annuncioAggiornato, AsyncCallback<AnnuncioDTO> callback);

    void rimuovi(String idAnnuncio, String idUtenteRichiedente, AsyncCallback<Void> callback);

    void cambiaDisponibilita(String idAnnuncio, String idUtenteRichiedente, boolean sospeso,
            AsyncCallback<AnnuncioDTO> callback);
}
