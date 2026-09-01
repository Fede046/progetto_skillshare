package it.unibo;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

// Servizio RPC per il rating medio di un utente, calcolato dalle recensioni ricevute
@RemoteServiceRelativePath("reputazione")
public interface ReputazioneService extends RemoteService {
    // Media dei voti ricevuti da un utente. Restituisce null quando l'utente non ha ancora
    // recensioni: i voti vanno da 1 a 5, quindi 0.0 sarebbe indistinguibile da una media reale bassa.
    Double ratingMedio(String idUtente);

    // Recensioni ricevute da un utente, dalla piu' recente alla piu' vecchia.
    List<RecensioneDTO> recensioniRicevute(String idUtente);
}
