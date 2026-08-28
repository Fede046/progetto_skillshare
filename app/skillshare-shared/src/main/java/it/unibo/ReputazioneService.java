package it.unibo;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("reputazione")
public interface ReputazioneService extends RemoteService {
    /**
     * Media dei voti ricevuti da un utente.
     * Restituisce null quando l'utente non ha ancora recensioni: i voti vanno
     * da 1 a 5, quindi 0.0 sarebbe indistinguibile da una media reale bassa.
     *
     * @param idUtente L'utente di cui leggere il rating.
     * @return La media dei voti, oppure null se non ci sono recensioni.
     */
    Double ratingMedio(String idUtente);

    /**
     * Recensioni ricevute da un utente, dalla piu' recente alla piu' vecchia.
     *
     * @param idUtente L'utente di cui leggere lo storico.
     * @return Lista delle recensioni ricevute, vuota se l'utente non ne ha.
     */
    List<RecensioneDTO> recensioniRicevute(String idUtente);
}
