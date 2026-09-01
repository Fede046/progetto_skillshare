package it.unibo;

import com.google.gwt.user.client.rpc.AsyncCallback;

// Versione asincrona di RegistrazioneService, quella che usa il client GWT
public interface RegistrazioneServiceAsync {
    
    void registraUtente(UtenteDTO utente, AsyncCallback<Boolean> callback);
    
}