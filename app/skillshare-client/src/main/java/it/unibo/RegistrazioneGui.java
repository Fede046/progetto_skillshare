package it.unibo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class RegistrazioneGui {

    private final RegistrazioneServiceAsync registrazioneService = GWT.create(RegistrazioneService.class);

    public void mostra() {
        // Card centrale
        VerticalPanel card = new VerticalPanel();
        card.setSpacing(15);

        // CSS
        card.getElement().getStyle().setProperty("backgroundColor", "#ffffff");
        card.getElement().getStyle().setProperty("padding", "30px");
        card.getElement().getStyle().setProperty("borderRadius", "10px");
        card.getElement().getStyle().setProperty("boxShadow", "0 4px 15px rgba(0, 0, 0, 0.1)");
        card.getElement().getStyle().setProperty("width", "350px");
        card.getElement().getStyle().setProperty("marginLeft", "auto");
        card.getElement().getStyle().setProperty("marginRight", "auto");
        card.getElement().getStyle().setProperty("marginTop", "60px");

        // Titolo della card
        HTML titolo = new HTML(
                "<h2 style='color: #333333; margin-top: 0; text-align: center; font-family: sans-serif;'>Crea Account</h2>");

        // Elemento per i messaggi di errore in rosso 
        HTML messaggioErrore = new HTML("");
        messaggioErrore.getElement().getStyle().setProperty("color", "#d9534f");
        messaggioErrore.getElement().getStyle().setProperty("fontSize", "13px");
        messaggioErrore.getElement().getStyle().setProperty("textAlign", "center");

        // Campi di testo
        TextBox nomeBox = creaCampoStilizzato("Nome");
        TextBox cognomeBox = creaCampoStilizzato("Cognome");
        TextBox emailBox = creaCampoStilizzato("Email");

        PasswordTextBox passwordBox = new PasswordTextBox();
        passwordBox.getElement().setPropertyString("placeholder", "Password (min. 8 caratteri)");
        applicaStileInput(passwordBox);

        // Bottone Registrati
        Button btnRegistrati = new Button("Registrati");
        btnRegistrati.getElement().getStyle().setProperty("backgroundColor", "#4CAF50");
        btnRegistrati.getElement().getStyle().setProperty("color", "white");
        btnRegistrati.getElement().getStyle().setProperty("border", "none");
        btnRegistrati.getElement().getStyle().setProperty("padding", "10px");
        btnRegistrati.getElement().getStyle().setProperty("borderRadius", "5px");
        btnRegistrati.getElement().getStyle().setProperty("width", "100%");
        btnRegistrati.getElement().getStyle().setProperty("cursor", "pointer");
        btnRegistrati.getElement().getStyle().setProperty("fontSize", "16px");
        btnRegistrati.getElement().getStyle().setProperty("fontWeight", "bold");
        btnRegistrati.getElement().getStyle().setProperty("backgroundImage", "none");
        
        // Tasto Indietro per tornare alla welcome screen
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

        // Gestione del Click sul bottone Registrati
        btnRegistrati.addClickHandler(event -> {
            messaggioErrore.setText(""); // Pulisce eventuali errori precedenti

            String email = emailBox.getText().trim();
            String password = passwordBox.getText();

            // Controllo formato email lato client
            if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
                messaggioErrore.setText("Inserisci un formato email valido.");
                return;
            }

            // Criterio: Password complessa (min 8 caratteri, 1 maiuscola, 1 minuscola, 1 numero, 1 simbolo)
            String regexPassword = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$";

            if (!password.matches(regexPassword)) {
                messaggioErrore.setText("La password deve avere almeno 8 caratteri, una maiuscola, un numero e un simbolo speciale.");
                return;
            }

            // Creazione del DTO usando i setter
            UtenteDTO nuovoUtente = new UtenteDTO();
            nuovoUtente.setNome(nomeBox.getText());
            nuovoUtente.setCognome(cognomeBox.getText());
            nuovoUtente.setEmail(email);
            nuovoUtente.setPassword(password);

            registrazioneService.registraUtente(nuovoUtente, new AsyncCallback<Boolean>() {
                @Override
                public void onFailure(Throwable caught) {
                    messaggioErrore.setText("Errore di connessione al server.");
                }

                @Override
                public void onSuccess(Boolean success) {
                    if (success) {
                        // Reindirizzamento al profilo in caso di successo
                        RootPanel.get().clear();
                        HTML profiloHTML = new HTML(
                                "<h1 style='text-align:center; margin-top:50px; font-family:sans-serif;'>Benvenuto nel tuo Profilo, "
                                        + nuovoUtente.getEmail() + "!</h1>");
                        RootPanel.get().add(profiloHTML);

                    } else {
                        // Errore in rosso se l'email risulta già registrata
                        messaggioErrore.setText("Questa email risulta già registrata.");
                    }
                }
            });
        });

        card.add(titolo);
        card.add(messaggioErrore);
        card.add(nomeBox);
        card.add(cognomeBox);
        card.add(emailBox);
        card.add(passwordBox);
        card.add(btnRegistrati);
        card.add(btnBack);

        RootPanel.get().clear();
        RootPanel.get().getElement().getStyle().setProperty("backgroundColor", "#f4f7f6");
        RootPanel.get().getElement().getStyle().setProperty("height", "100vh");
        RootPanel.get().add(card);
    }

    private TextBox creaCampoStilizzato(String placeholder) {
        TextBox box = new TextBox();
        box.getElement().setPropertyString("placeholder", placeholder);
        applicaStileInput(box);
        return box;
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