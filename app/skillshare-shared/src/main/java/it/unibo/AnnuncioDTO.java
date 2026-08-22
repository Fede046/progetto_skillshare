package it.unibo.model;

import java.io.Serializable;

/**
 * Oggetto di trasferimento dati che rappresenta un annuncio pubblicato da un utente.
 * Implementa Serializable per poter viaggiare sulle chiamate RPC di GWT
 * ed essere persistito su MapDB tramite Serializer.JAVA.
 */
public class AnnuncioDTO implements Serializable {

    private String id;
    private String idUtente;
    private String titolo;
    private String descrizione;
    private String competenzaOfferta;
    private String disponibilita;
    private String controprestazione;
    private long dataCreazione;

    // Costruttore vuoto OBBLIGATORIO per GWT
    public AnnuncioDTO() {
    }

    //Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(String idUtente) {
        this.idUtente = idUtente;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getCompetenzaOfferta() {
        return competenzaOfferta;
    }

    public void setCompetenzaOfferta(String competenzaOfferta) {
        this.competenzaOfferta = competenzaOfferta;
    }

    public String getDisponibilita() {
        return disponibilita;
    }

    public void setDisponibilita(String disponibilita) {
        this.disponibilita = disponibilita;
    }

    public String getControprestazione() {
        return controprestazione;
    }

    public void setControprestazione(String controprestazione) {
        this.controprestazione = controprestazione;
    }

    public long getDataCreazione() {
        return dataCreazione;
    }

    public void setDataCreazione(long dataCreazione) {
        this.dataCreazione = dataCreazione;
    }
}
