package it.unibo;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class WelcomeGui {

    public void mostra() {
        // Pannello principale (Card centrale)
        VerticalPanel card = new VerticalPanel();
        card.setSpacing(20);

        // CSS per centrare la card
        card.getElement().getStyle().setProperty("backgroundColor", "#ffffff");
        card.getElement().getStyle().setProperty("padding", "40px");
        card.getElement().getStyle().setProperty("borderRadius", "10px");
        card.getElement().getStyle().setProperty("boxShadow", "0 4px 15px rgba(0, 0, 0, 0.1)");
        card.getElement().getStyle().setProperty("width", "350px");
        card.getElement().getStyle().setProperty("marginLeft", "auto");
        card.getElement().getStyle().setProperty("marginRight", "auto");
        card.getElement().getStyle().setProperty("marginTop", "100px");
        card.getElement().getStyle().setProperty("textAlign", "center");

        // Scritta Welcome
        HTML titolo = new HTML(
                "<h1 style='color: #b20000; font-size: 35px; margin: 0 0 25px 0; font-family: sans-serif;'>Welcome</h1>"
                        +
                        "<p style='color: #666666; font-size: 14px; margin-bottom: 0px; text-align:center;'>Accedi alla piattaforma o crea un nuovo account.</p>");

        // Pulsante per la Registrazione
        Button btnRegistrati = new Button("Registrati");
        stileBottone(btnRegistrati, "#4CAF50");
        btnRegistrati.addClickHandler(event -> {
            new RegistrazioneGui().mostra();
        });

        // Pulsante per il Login
        Button btnLogin = new Button("Login");
        stileBottone(btnLogin, "#008CBA");
        btnLogin.addClickHandler(event -> {
            // new LoginGui().mostra();
        });

        // Add elementi alla card
        card.add(titolo);
        card.add(btnRegistrati);
        card.add(btnLogin);

        RootPanel.get().clear();
        RootPanel.get().add(card);

        com.google.gwt.dom.client.Document.get().getBody().getStyle().setBackgroundColor("#f4f7f6");
        com.google.gwt.dom.client.Document.get().getBody().getStyle().setProperty("margin", "0");
        com.google.gwt.dom.client.Document.get().getBody().getStyle().setProperty("fontFamily", "sans-serif");
    }

    // Metodo di supporto per forzare i colori corretti sui bottoni
    private void stileBottone(Button button, String coloreSfondo) {
        button.getElement().getStyle().setProperty("backgroundImage", "none");
        button.getElement().getStyle().setProperty("backgroundColor", coloreSfondo);
        button.getElement().getStyle().setProperty("color", "white");
        button.getElement().getStyle().setProperty("border", "none");
        button.getElement().getStyle().setProperty("padding", "12px");
        button.getElement().getStyle().setProperty("borderRadius", "5px");
        button.getElement().getStyle().setProperty("width", "100%");
        button.getElement().getStyle().setProperty("cursor", "pointer");
        button.getElement().getStyle().setProperty("fontSize", "16px");
        button.getElement().getStyle().setProperty("fontWeight", "bold");
    }
}