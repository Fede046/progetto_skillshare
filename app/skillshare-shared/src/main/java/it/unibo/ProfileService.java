package it.unibo;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("profile")
public interface ProfileService extends RemoteService {
    /**
     * Recupera le informazioni del profilo per l'utente specificato dall'email.
     * 
     * @param email L'indirizzo email dell'utente.
     * @return UtenteDTO con i dati completi del profilo.
     * @throws IllegalArgumentException Se l'email non è valida o l'utente non esiste.
     */
    UtenteDTO getProfilo(String email) throws IllegalArgumentException;
}