package it.unibo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ProfileServiceAsync {
    void getProfilo(String email, AsyncCallback<UtenteDTO> callback);

    void updateProfile(UtenteDTO utente, AsyncCallback<UtenteDTO> callback);
}