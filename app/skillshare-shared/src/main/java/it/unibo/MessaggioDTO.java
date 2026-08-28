package it.unibo;

import java.io.Serializable;
import java.util.Objects;

public class MessaggioDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String idRichiestaScambio;
    private String idMittente;
    private String testo;
    private long timestamp; // oppure LocalDateTime/String a seconda delle convenzioni del progetto, long è comunemente usato con MapDB

    // Costruttore vuoto (richiesto)
    public MessaggioDTO() {
    }

    // Costruttore con campi (comodo per la creazione rapida)
    public MessaggioDTO(String id, String idRichiestaScambio, String idMittente, String testo, long timestamp) {
        this.id = id;
        this.idRichiestaScambio = idRichiestaScambio;
        this.idMittente = idMittente;
        this.testo = testo;
        this.timestamp = timestamp;
    }

    // Getter e Setter
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

    public String getIdMittente() {
        return idMittente;
    }

    public void setIdMittente(String idMittente) {
        this.idMittente = idMittente;
    }

    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessaggioDTO that = (MessaggioDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MessaggioDTO{" +
                "id='" + id + '\'' +
                ", idRichiestaScambio='" + idRichiestaScambio + '\'' +
                ", idMittente='" + idMittente + '\'' +
                ", testo='" + testo + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}