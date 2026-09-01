package it.unibo;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

// Servizio RPC per le richieste di scambio: invio, accettazione, rifiuto e completamento
@RemoteServiceRelativePath("richiestaScambio")
public interface RichiestaScambioService extends RemoteService {
    // Invia una richiesta di scambio per un annuncio. Il creatore
    // dell'annuncio viene risolto dal server.
    RichiestaScambioDTO inviaRichiestaScambio(String idAnnuncio, String idRichiedente, String messaggio)
            throws IllegalArgumentException;

    // Richieste di scambio ricevute da un creatore di annunci, dalla più recente alla più vecchia.
    List<RichiestaScambioDTO> richiesteRicevuteDaCreatore(String idCreatore);

    // Richieste di scambio inviate da un utente richiedente, dalla più recente alla più vecchia.
    List<RichiestaScambioDTO> richiesteInviateDaRichiedente(String idRichiedente);

    // Accetta una richiesta di scambio: solo il creatore dell'annuncio può accettarla.
    RichiestaScambioDTO accetta(String idRichiesta, String idCreatore) throws IllegalArgumentException;

    // Rifiuta una richiesta di scambio: solo il creatore dell'annuncio può rifiutarla.
    RichiestaScambioDTO rifiuta(String idRichiesta, String idCreatore) throws IllegalArgumentException;

    // Segna come completato uno scambio gia' accettato. Puo' agire
    // ciascuno dei due partecipanti allo scambio.
    RichiestaScambioDTO completa(String idRichiesta, String idUtente) throws IllegalArgumentException;
}
