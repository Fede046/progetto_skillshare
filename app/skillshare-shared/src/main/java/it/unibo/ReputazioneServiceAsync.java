package it.unibo;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ReputazioneServiceAsync {
    void ratingMedio(String idUtente, AsyncCallback<Double> callback);

    void recensioniRicevute(String idUtente, AsyncCallback<List<RecensioneDTO>> callback);
}
