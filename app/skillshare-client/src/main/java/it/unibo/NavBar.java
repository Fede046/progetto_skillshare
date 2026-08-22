package it.unibo;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Barra di navigazione orizzontale condivisa dalle schermate dopo il login.
 * Le sezioni future si aggiungono con una chiamata a aggiungiSezione(),
 * senza toccare il layout.
 */
public class NavBar {

    public static final String SEZIONE_PROFILO = "Profilo";
    public static final String SEZIONE_ANNUNCI = "I miei annunci";

    private final FlowPanel barra = new FlowPanel();
    private final FlowPanel sezioni = new FlowPanel();
    private final String sezioneAttiva;

    public NavBar(UtenteDTO utente, String sezioneAttiva) {
        this.sezioneAttiva = sezioneAttiva;
        barra.addStyleName("app-navbar");

        Label brand = new Label("SkillShare");
        brand.addStyleName("app-navbar-brand");
        barra.add(brand);

        sezioni.addStyleName("app-navbar-sezioni");
        barra.add(sezioni);

        // Le sezioni future si aggiungono qui sotto
        aggiungiSezione(SEZIONE_PROFILO, () -> new ProfiloGui(utente).mostra());
        aggiungiSezione(SEZIONE_ANNUNCI, () -> new MieiAnnunciGui(utente).mostra());

        barra.add(creaAreaUtente(utente));
    }

    /**
     * Aggiunge una voce alla barra, evidenziandola se e' la sezione corrente.
     * La voce attiva non e' cliccabile: siamo gia' su quella schermata.
     */
    public void aggiungiSezione(String nome, Comando apri) {
        Label voce = new Label(nome);
        voce.addStyleName("app-nav-item");

        if (nome.equals(sezioneAttiva)) {
            voce.addStyleName("app-nav-item-attivo");
        } else {
            voce.addStyleName("app-nav-item-link");
            voce.addClickHandler(event -> apri.esegui());
        }

        sezioni.add(voce);
    }

    /**
     * Azione da eseguire al click su una voce della barra.
     */
    public interface Comando {
        void esegui();
    }

    // Nome dell'utente loggato e pulsante di logout, allineati a destra
    private Widget creaAreaUtente(UtenteDTO utente) {
        FlowPanel area = new FlowPanel();
        area.addStyleName("app-navbar-utente");

        if (utente != null) {
            area.add(new Label(nomeVisualizzato(utente)));
        }

        Button btnLogout = new Button("Logout");
        btnLogout.addStyleName("btn-navbar");
        btnLogout.addClickHandler(event -> new WelcomeGui().mostra());
        area.add(btnLogout);

        return area;
    }

    // Nome e cognome, con fallback sull'email se non valorizzati
    private String nomeVisualizzato(UtenteDTO utente) {
        String nome = utente.getNome() != null ? utente.getNome().trim() : "";
        String cognome = utente.getCognome() != null ? utente.getCognome().trim() : "";
        String completo = (nome + " " + cognome).trim();
        if (!completo.isEmpty()) {
            return completo;
        }
        return utente.getEmail() != null ? utente.getEmail() : "";
    }

    public Widget getWidget() {
        return barra;
    }
}
