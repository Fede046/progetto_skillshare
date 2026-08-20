package it.unibo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AccessoServiceAsync {
    
    void login(String email, String password, AsyncCallback<UtenteDTO> callback);
    
}