package it.unibo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LoginGui {

    private final AccessoServiceAsync accessoService = GWT.create(AccessoService.class);

    public void mostra() {
        VerticalPanel card = new VerticalPanel();
        card.setSpacing(15);

        card.getElement().getStyle().setProperty("backgroundColor", "#ffffff");
        card.getElement().getStyle().setProperty("padding", "30px");
        card.getElement().getStyle().setProperty("borderRadius", "10px");
        card.getElement().getStyle().setProperty("boxShadow", "0 4px 15px rgba(0, 0, 0, 0.1)");
        card.getElement().getStyle().setProperty("width", "350px");
        card.getElement().getStyle().setProperty("marginLeft", "auto");
        card.getElement().getStyle().setProperty("marginRight", "auto");
        card.getElement().getStyle().setProperty("marginTop", "80px");

        HTML titolo = new HTML(
                "<h2 style='color: #333333; margin-top: 0; text-align: center; font-family: sans-serif;'>Accedi</h2>");

        // Etichetta per gli errori in rosso (inizialmente vuota)
        HTML messaggioErrore = new HTML();
        messaggioErrore.getElement().getStyle().setProperty("color", "red");
        messaggioErrore.getElement().getStyle().setProperty("fontSize", "13px");
        messaggioErrore.getElement().getStyle().setProperty("textAlign", "center");

        TextBox emailBox = new TextBox();
        emailBox.getElement().setPropertyString("placeholder", "Email");
        applicaStileInput(emailBox);

        PasswordTextBox passwordBox = new PasswordTextBox();
        passwordBox.getElement().setPropertyString("placeholder", "Password");
        applicaStileInput(passwordBox);

        Button btnLogin = new Button("Accedi");
        btnLogin.getElement().getStyle().setProperty("backgroundColor", "#008CBA");
        btnLogin.getElement().getStyle().setProperty("color", "white");
        btnLogin.getElement().getStyle().setProperty("border", "none");
        btnLogin.getElement().getStyle().setProperty("padding", "10px");
        btnLogin.getElement().getStyle().setProperty("borderRadius", "5px");
        btnLogin.getElement().getStyle().setProperty("width", "100%");
        btnLogin.getElement().getStyle().setProperty("cursor", "pointer");
        btnLogin.getElement().getStyle().setProperty("fontSize", "16px");
        btnLogin.getElement().getStyle().setProperty("fontWeight", "bold");
        btnLogin.getElement().getStyle().setProperty("backgroundImage", "none");

        Button btnBack = new Button("Indietro");
        btnBack.getElement().getStyle().setProperty("backgroundColor", "#f0f0f0");
        btnBack.getElement().getStyle().setProperty("color", "#333333");
        btnBack.getElement().getStyle().setProperty("border", "1px solid #ccc");
        btnBack.getElement().getStyle().setProperty("padding", "8px");
        btnBack.getElement().getStyle().setProperty("borderRadius", "5px");
        btnBack.getElement().getStyle().setProperty("width", "100%");
        btnBack.getElement().getStyle().setProperty("cursor", "pointer");
        btnBack.getElement().getStyle().setProperty("fontSize", "14px");
        btnBack.getElement().getStyle().setProperty("backgroundImage", "none");

        btnBack.addClickHandler(event -> {
            new WelcomeGui().mostra();
        });

        btnLogin.addClickHandler(event -> {
            String email = emailBox.getText().trim();
            String password = passwordBox.getText();

            // Pulisce eventuali errori precedenti
            messaggioErrore.setText("");

            if (email.isEmpty() || password.isEmpty()) {
                messaggioErrore.setText("Compila tutti i campi.");
                return;
            }

            accessoService.login(email, password, new AsyncCallback<UtenteDTO>() {
                @Override
                public void onFailure(Throwable caught) {
                    // Questo ora scatta solo se cade la rete
                    messaggioErrore.setText("Errore di connessione al server. Riprova più tardi.");
                }

                @Override
                public void onSuccess(UtenteDTO utente) {
                    if ("User not found".equals(utente.getNome())) {
                        messaggioErrore.setText("Utente non trovato");
                    } else if ("Wrong password".equals(utente.getNome())) {
                        messaggioErrore.setText("Password sbagliata");
                    } else {
                        RootPanel.get().clear();
                        HTML profiloHTML = new HTML(
                                "<h1 style='text-align:center; margin-top:50px; font-family:sans-serif;'>Home / Profilo di "
                                        + utente.getNome() + " " + utente.getCognome() + "</h1>" +
                                        "<p style='text-align:center; color:#666;'>Email: " + utente.getEmail()
                                        + "</p>");
                        RootPanel.get().add(profiloHTML);
                    }
                }
            });
        });

        card.add(titolo);
        card.add(messaggioErrore);
        card.add(emailBox);
        card.add(passwordBox);
        card.add(btnLogin);
        card.add(btnBack);

        RootPanel.get().clear();
        RootPanel.get().getElement().getStyle().setProperty("backgroundColor", "#f4f7f6");
        RootPanel.get().getElement().getStyle().setProperty("height", "100vh");
        RootPanel.get().add(card);
    }

    private void applicaStileInput(com.google.gwt.user.client.ui.UIObject widget) {
        widget.getElement().getStyle().setProperty("width", "100%");
        widget.getElement().getStyle().setProperty("padding", "10px");
        widget.getElement().getStyle().setProperty("borderRadius", "5px");
        widget.getElement().getStyle().setProperty("border", "1px solid #ccc");
        widget.getElement().getStyle().setProperty("boxSizing", "border-box");
        widget.getElement().getStyle().setProperty("fontSize", "14px");
    }
}