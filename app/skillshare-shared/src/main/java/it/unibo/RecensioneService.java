package it.unibo;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("recensione")
public interface RecensioneService extends RemoteService {
    /**
     * Registra la recensione di uno scambio concluso.
     *
     * @param recensione La recensione da salvare, con idRichiestaScambio, idAutore e voto valorizzati.
     * @return La recensione salvata, con id, idAnnuncio e dataCreazione valorizzati dal server.
     * @throws IllegalArgumentException Se lo scambio non e' completato, se il voto e' fuori
     *                                  intervallo o se l'autore ha gia' recensito lo scambio.
     */
    RecensioneDTO lascia(RecensioneDTO recensione) throws IllegalArgumentException;

    /**
     * Recensioni collegate a un annuncio, dalla piu' recente alla piu' vecchia.
     *
     * @param idAnnuncio L'id dell'annuncio di cui leggere le recensioni.
     * @return Lista delle recensioni, vuota se l'annuncio non ne ha.
     */
    List<RecensioneDTO> recensioniPerAnnuncio(String idAnnuncio);
}
