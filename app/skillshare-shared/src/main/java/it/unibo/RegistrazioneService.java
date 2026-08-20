package it.unibo;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

// Il nome tra parentesi definisce l'URL (endpoint) verso cui il Client farà la richiesta
@RemoteServiceRelativePath("registrazione")
public interface RegistrazioneService extends RemoteService {
    
    // Firma del metodo: riceve un UtenteDTO e restituisce un booleano (es. true se successo, false se l'utente esiste già)
    boolean registraUtente(UtenteDTO utente) throws IllegalArgumentException;
    
}