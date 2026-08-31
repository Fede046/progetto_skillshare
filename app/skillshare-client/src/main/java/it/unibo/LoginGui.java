package it.unibo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Schermata di accesso. Lo stile arriva dalle classi condivise in
 * skillshare.css (auth-*, btn-*, form-errore), le stesse usate da
 * WelcomeGui e RegistrazioneGui.
 */
public class LoginGui {

    private final AccessoServiceAsync accessoService = GWT.create(AccessoService.class);

    public void mostra() {
        FlowPanel sfondo = new FlowPanel();
        sfondo.addStyleName("auth-sfondo");

        FlowPanel card = new FlowPanel();
        card.addStyleName("auth-card");

        FlowPanel intestazione = new FlowPanel();
        intestazione.addStyleName("auth-intestazione");

        Label titolo = new Label("Accedi");
        titolo.addStyleName("auth-titolo");
        intestazione.add(titolo);

        Label sottotitolo = new Label("Inserisci le credenziali del tuo account.");
        sottotitolo.addStyleName("auth-sottotitolo");
        intestazione.add(sottotitolo);

        card.add(intestazione);

        // Stesso alert usato negli altri form del progetto: resta nascosto
        // finche' non c'e' davvero un errore da mostrare
        Label messaggioErrore = new Label();
        messaggioErrore.addStyleName("form-errore");
        messaggioErrore.setVisible(false);
        card.add(messaggioErrore);

        FlowPanel form = new FlowPanel();
        form.addStyleName("auth-form");

        TextBox emailBox = new TextBox();
        emailBox.getElement().setPropertyString("placeholder", "Email");
        emailBox.addStyleName("auth-campo");
        form.add(emailBox);

        PasswordTextBox passwordBox = new PasswordTextBox();
        passwordBox.getElement().setPropertyString("placeholder", "Password");
        passwordBox.addStyleName("auth-campo");
        form.add(passwordBox);

        FlowPanel azioni = new FlowPanel();
        azioni.addStyleName("auth-azioni");

        Button btnLogin = new Button("Accedi");
        btnLogin.addStyleName("btn-primary");

        Button btnBack = new Button("Indietro");
        btnBack.addStyleName("btn-secondary");
        btnBack.addClickHandler(event -> new WelcomeGui().mostra());

        azioni.add(btnLogin);
        azioni.add(btnBack);
        form.add(azioni);
        card.add(form);

        btnLogin.addClickHandler(event -> {
            String email = emailBox.getText().trim();
            String password = passwordBox.getText();

            // Pulisce eventuali errori precedenti
            nascondiErrore(messaggioErrore);

            if (email.isEmpty() || password.isEmpty()) {
                mostraErrore(messaggioErrore, "Compila tutti i campi.");
                return;
            }

            accessoService.login(email, password, new AsyncCallback<UtenteDTO>() {
                @Override
                public void onFailure(Throwable caught) {
                    // Questo ora scatta solo se cade la rete
                    mostraErrore(messaggioErrore, "Errore di connessione al server. Riprova più tardi.");
                }

                @Override
                public void onSuccess(UtenteDTO utente) {
                    if ("User not found".equals(utente.getNome())) {
                        mostraErrore(messaggioErrore, "Nessun account registrato con questa email.");
                    } else if ("Wrong password".equals(utente.getNome())) {
                        mostraErrore(messaggioErrore, "Password errata. Riprova.");
                    } else {
                        // Login riuscito: mostra la pagina del profilo dell'utente
                        new ProfiloGui(utente).mostra();
                    }
                }
            });
        });

        sfondo.add(card);

        RootPanel.get().clear();
        RootPanel.get().add(sfondo);

        // Stessa base grafica delle altre schermate
        Document.get().getBody().getStyle().setProperty("backgroundColor", "#E8E8E8");
        Document.get().getBody().getStyle().setProperty("margin", "0");
        Document.get().getBody().getStyle().setProperty("fontFamily", "sans-serif");
    }

    private void mostraErrore(Label messaggioErrore, String testo) {
        messaggioErrore.setText(testo);
        messaggioErrore.setVisible(true);
    }

    private void nascondiErrore(Label messaggioErrore) {
        messaggioErrore.setText("");
        messaggioErrore.setVisible(false);
    }
}
