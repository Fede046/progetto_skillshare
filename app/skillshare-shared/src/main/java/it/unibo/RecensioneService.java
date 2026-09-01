package it.unibo;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

// Servizio RPC per lasciare e leggere le recensioni di uno scambio concluso
@RemoteServiceRelativePath("recensione")
public interface RecensioneService extends RemoteService {
    // Registra la recensione di uno scambio concluso.
    RecensioneDTO lascia(RecensioneDTO recensione) throws IllegalArgumentException;

    // Recensioni collegate a un annuncio, dalla piu' recente alla piu' vecchia.
    List<RecensioneDTO> recensioniPerAnnuncio(String idAnnuncio);
}
