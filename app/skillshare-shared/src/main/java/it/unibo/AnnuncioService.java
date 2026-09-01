package it.unibo;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

// Servizio RPC per gli annunci: pubblicazione, modifica, rimozione e disponibilita'
@RemoteServiceRelativePath("annuncio")
public interface AnnuncioService extends RemoteService {
    // Pubblica un nuovo annuncio.
    AnnuncioDTO pubblica(AnnuncioDTO annuncio) throws IllegalArgumentException;

    // Annunci pubblicati da un utente, dal piu' recente al piu' vecchio.
    List<AnnuncioDTO> annunciDiUtente(String idUtente);

    // Modifica un annuncio esistente, consentita solo al proprietario.
    AnnuncioDTO modifica(String idAnnuncio, String idUtenteRichiedente, AnnuncioDTO annuncioAggiornato) throws IllegalArgumentException;

    // Rimuove un annuncio esistente, consentita solo al proprietario.
    void rimuovi(String idAnnuncio, String idUtenteRichiedente) throws IllegalArgumentException;

    // Sospende o riattiva un annuncio: sospeso sparisce dal marketplace
    // ma resta fra gli annunci del proprietario.
    AnnuncioDTO cambiaDisponibilita(String idAnnuncio, String idUtenteRichiedente, boolean sospeso)
            throws IllegalArgumentException;
}
