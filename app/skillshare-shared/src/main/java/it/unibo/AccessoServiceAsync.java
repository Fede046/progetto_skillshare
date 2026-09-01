package it.unibo;

import com.google.gwt.user.client.rpc.AsyncCallback;

// Versione asincrona di AccessoService, quella che usa il client GWT
public interface AccessoServiceAsync {
    
    void login(String email, String password, AsyncCallback<UtenteDTO> callback);
    
}