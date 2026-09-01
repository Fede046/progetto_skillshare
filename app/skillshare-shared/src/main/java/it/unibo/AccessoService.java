package it.unibo;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

// Servizio RPC per il login: verifica le credenziali di chi prova ad accedere
@RemoteServiceRelativePath("accesso")
public interface AccessoService extends RemoteService {

    // Restituisce l'oggetto UtenteDTO se il login ha successo, altrimenti lancia
    // un'eccezione o restituisce null
    UtenteDTO login(String email, String password);

}