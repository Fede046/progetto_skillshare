package it.unibo;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

// Servizio RPC per leggere e aggiornare il profilo di un utente
@RemoteServiceRelativePath("profile")
public interface ProfileService extends RemoteService {
    // Recupera le informazioni del profilo per l'utente specificato dall'email.
    UtenteDTO getProfilo(String email) throws IllegalArgumentException;
    // Aggiorna i dati del profilo (bio, foto, competenze) di un utente.
    UtenteDTO updateProfile(UtenteDTO utente) throws IllegalArgumentException;
}