package it.unibo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UtenteDTO implements Serializable {

    /**
     * Fissato esplicitamente: i record su MapDB sono scritti con Serializer.JAVA,
     * quindi un UID calcolato dal compilatore cambierebbe a ogni modifica dei campi
     * rendendo illeggibili i dati gia' persistiti.
     */
    private static final long serialVersionUID = 1L;

    private String email;
    private String password;
    private String nome;
    private String cognome;
    private String bio;
    private String photoUrl;
    private List<String> tagCompetenza;

    // Costruttore vuoto OBBLIGATORIO per GWT
    public UtenteDTO() {
        this.tagCompetenza = new ArrayList<>();
    }

    public UtenteDTO(String email, String password, String nome, String cognome) {
        this.email = email;
        this.password = password;
        this.nome = nome;
        this.cognome = cognome;
        this.bio = "";
        this.photoUrl = "";
        this.tagCompetenza = new ArrayList<>();
    }

    public UtenteDTO(String email, String password, String nome, String cognome, String bio, String photoUrl, List<String> tagCompetenza) {
        this.email = email;
        this.password = password;
        this.nome = nome;
        this.cognome = cognome;
        this.bio = bio != null ? bio : "";
        this.photoUrl = photoUrl != null ? photoUrl : "";
        this.tagCompetenza = tagCompetenza != null ? tagCompetenza : new ArrayList<>();
    }

    //Getters e Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }
    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public List<String> getTagCompetenza() {
        return tagCompetenza;
    }

    public void setTagCompetenza(List<String> tagCompetenza) {
        this.tagCompetenza = tagCompetenza != null ? tagCompetenza : new ArrayList<>();
    }
}