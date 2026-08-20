package it.unibo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface RegistrazioneServiceAsync {
    
    void registraUtente(UtenteDTO utente, AsyncCallback<Boolean> callback);
    
}