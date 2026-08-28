package it.unibo;

import java.io.Serializable;

/**
 * Oggetto di trasferimento dati che rappresenta la recensione lasciata da un
 * utente al termine di uno scambio completato.
 * Implementa Serializable per poter viaggiare sulle chiamate RPC di GWT
 * ed essere persistito su MapDB tramite Serializer.JAVA.
 */
public class RecensioneDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String idRichiestaScambio;

    // Copiato dalla richiesta collegata: serve a raccogliere le recensioni per annuncio
    private String idAnnuncio;

    private String idAutore;

    // Risolto dal server alla lettura: il client non deve mostrare un'email
    private String nomeAutore;

    private String idDestinatario;
    private int voto;

    // Facoltativo: può restare null o vuoto
    private String commento;

    private long dataCreazione;

    // Costruttore vuoto OBBLIGATORIO per GWT
    public RecensioneDTO() {
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdRichiestaScambio() {
        return idRichiestaScambio;
    }

    public void setIdRichiestaScambio(String idRichiestaScambio) {
        this.idRichiestaScambio = idRichiestaScambio;
    }

    public String getIdAnnuncio() {
        return idAnnuncio;
    }

    public void setIdAnnuncio(String idAnnuncio) {
        this.idAnnuncio = idAnnuncio;
    }

    public String getIdAutore() {
        return idAutore;
    }

    public void setIdAutore(String idAutore) {
        this.idAutore = idAutore;
    }

    public String getNomeAutore() {
        return nomeAutore;
    }

    public void setNomeAutore(String nomeAutore) {
        this.nomeAutore = nomeAutore;
    }

    public String getIdDestinatario() {
        return idDestinatario;
    }

    public void setIdDestinatario(String idDestinatario) {
        this.idDestinatario = idDestinatario;
    }

    public int getVoto() {
        return voto;
    }

    public void setVoto(int voto) {
        this.voto = voto;
    }

    public String getCommento() {
        return commento;
    }

    public void setCommento(String commento) {
        this.commento = commento;
    }

    public long getDataCreazione() {
        return dataCreazione;
    }

    public void setDataCreazione(long dataCreazione) {
        this.dataCreazione = dataCreazione;
    }
}
