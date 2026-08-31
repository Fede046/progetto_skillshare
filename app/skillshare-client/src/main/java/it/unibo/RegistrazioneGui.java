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
import com.google.gwt.user.client.ui.Widget;

/**
 * Schermata di registrazione. Lo stile arriva dalle classi condivise in
 * skillshare.css (auth-*, btn-*, form-errore), le stesse usate da
 * WelcomeGui e LoginGui.
 */
public class RegistrazioneGui {

    private final RegistrazioneServiceAsync registrazioneService = GWT.create(RegistrazioneService.class);

    public void mostra() {
        FlowPanel sfondo = new FlowPanel();
        sfondo.addStyleName("auth-sfondo");

        FlowPanel card = new FlowPanel();
        card.addStyleName("auth-card");

        FlowPanel intestazione = new FlowPanel();
        intestazione.addStyleName("auth-intestazione");

        Label titolo = new Label("Crea account");
        titolo.addStyleName("auth-titolo");
        intestazione.add(titolo);

        Label sottotitolo = new Label("Bastano pochi dati per iniziare a scambiare competenze.");
        sottotitolo.addStyleName("auth-sottotitolo");
        intestazione.add(sottotitolo);

        card.add(intestazione);

        // Stesso alert usato negli altri form del progetto
        Label messaggioErrore = new Label();
        messaggioErrore.addStyleName("form-errore");
        messaggioErrore.setVisible(false);
        card.add(messaggioErrore);

        FlowPanel form = new FlowPanel();
        form.addStyleName("auth-form");

        TextBox nomeBox = creaCampo("Mario");
        TextBox cognomeBox = creaCampo("Rossi");
        TextBox emailBox = creaCampo("nome@unibo.it");
        form.add(creaGruppoCampo("Nome", nomeBox));
        form.add(creaGruppoCampo("Cognome", cognomeBox));
        form.add(creaGruppoCampo("Email", emailBox));

        PasswordTextBox passwordBox = new PasswordTextBox();
        passwordBox.getElement().setPropertyString("placeholder", "Almeno 8 caratteri");
        passwordBox.addStyleName("auth-campo");
        form.add(creaGruppoCampo("Password", passwordBox));

        FlowPanel azioni = new FlowPanel();
        azioni.addStyleName("auth-azioni");

        Button btnRegistrati = new Button("Registrati");
        btnRegistrati.addStyleName("btn-primary");

        Button btnBack = new Button("Indietro");
        btnBack.addStyleName("btn-secondary");
        btnBack.addClickHandler(event -> new WelcomeGui().mostra());

        azioni.add(btnRegistrati);
        azioni.add(btnBack);
        form.add(azioni);
        card.add(form);

        // Gestione del Click sul bottone Registrati
        btnRegistrati.addClickHandler(event -> {
            nascondiErrore(messaggioErrore); // Pulisce eventuali errori precedenti

            String email = emailBox.getText().trim();
            String password = passwordBox.getText();

            // Controllo formato email lato client
            if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
                mostraErrore(messaggioErrore, "Inserisci un indirizzo email valido, ad esempio nome@unibo.it");
                return;
            }

            // Criterio: Password complessa (min 8 caratteri, 1 maiuscola, 1 minuscola, 1 numero, 1 simbolo)
            String regexPassword = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$";

            if (!password.matches(regexPassword)) {
                mostraErrore(messaggioErrore, "La password deve avere almeno 8 caratteri, "
                        + "una maiuscola, una minuscola, un numero e un simbolo speciale.");
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
                    mostraErrore(messaggioErrore, "Errore di connessione al server. Riprova più tardi.");
                }

                @Override
                public void onSuccess(Boolean success) {
                    if (success) {
                        // Reindirizzamento al login in caso di successo
                        new LoginGui().mostra();

                    } else {
                        // Errore se l'email risulta gia' registrata
                        mostraErrore(messaggioErrore,
                                "Questa email risulta già registrata. Prova ad accedere.");
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

    /** Campo con la sua etichetta sopra, cosi' resta chiaro cosa si sta scrivendo. */
    private FlowPanel creaGruppoCampo(String etichetta, Widget campo) {
        FlowPanel gruppo = new FlowPanel();
        gruppo.addStyleName("auth-campo-gruppo");

        Label label = new Label(etichetta);
        label.addStyleName("auth-campo-etichetta");

        gruppo.add(label);
        gruppo.add(campo);
        return gruppo;
    }

    private TextBox creaCampo(String placeholder) {
        TextBox box = new TextBox();
        box.getElement().setPropertyString("placeholder", placeholder);
        box.addStyleName("auth-campo");
        return box;
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
