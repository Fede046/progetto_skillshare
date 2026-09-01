package it.unibo;

import java.io.Serializable;

// Oggetto di trasferimento dati che rappresenta una richiesta di scambio inviata da un utente
// verso un annuncio pubblicato da un altro utente.
public class RichiestaScambioDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String idAnnuncio;
    private String idRichiedente;
    private String idCreatoreAnnuncio;
    private String messaggio;
    private StatoRichiesta stato;
    private long dataCreazione;

    // Costruttore vuoto OBBLIGATORIO per GWT
    public RichiestaScambioDTO() {
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdAnnuncio() {
        return idAnnuncio;
    }

    public void setIdAnnuncio(String idAnnuncio) {
        this.idAnnuncio = idAnnuncio;
    }

    public String getIdRichiedente() {
        return idRichiedente;
    }

    public void setIdRichiedente(String idRichiedente) {
        this.idRichiedente = idRichiedente;
    }

    public String getIdCreatoreAnnuncio() {
        return idCreatoreAnnuncio;
    }

    public void setIdCreatoreAnnuncio(String idCreatoreAnnuncio) {
        this.idCreatoreAnnuncio = idCreatoreAnnuncio;
    }

    public String getMessaggio() {
        return messaggio;
    }

    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }

    public StatoRichiesta getStato() {
        return stato;
    }

    public void setStato(StatoRichiesta stato) {
        this.stato = stato;
    }

    public long getDataCreazione() {
        return dataCreazione;
    }

    public void setDataCreazione(long dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    // Indica se questo utente puo' segnare lo scambio come completato: lo scambio dev'essere stato
    // accettato e l'utente dev'essere uno dei due partecipanti.
    public boolean completabileDa(String emailUtente) {
        if (emailUtente == null || emailUtente.trim().isEmpty()) {
            return false;
        }
        if (stato != StatoRichiesta.ACCEPTED) {
            return false;
        }

        String utente = emailUtente.trim();
        return utente.equals(idRichiedente) || utente.equals(idCreatoreAnnuncio);
    }
}
