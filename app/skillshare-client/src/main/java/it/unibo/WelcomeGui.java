package it.unibo;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Prima schermata dell'applicazione: presenta la piattaforma e porta
 * a registrazione o accesso.
 * Lo stile arriva dalle classi condivise in skillshare.css (auth-*, btn-*),
 * le stesse usate da LoginGui e RegistrazioneGui.
 */
public class WelcomeGui {

    public void mostra() {
        FlowPanel sfondo = new FlowPanel();
        sfondo.addStyleName("auth-sfondo");

        FlowPanel card = new FlowPanel();
        card.addStyleName("auth-card");
        card.addStyleName("auth-card-welcome");

        // Intestazione: il nome della piattaforma in bordeaux e' il primo
        // segno di identita' che l'utente incontra
        FlowPanel intestazione = new FlowPanel();
        intestazione.addStyleName("auth-intestazione");

        Label marchio = new Label("Skillshare");
        marchio.addStyleName("auth-marchio");
        intestazione.add(marchio);

        Label sottotitolo = new Label("Scambia competenze con altri studenti: "
                + "accedi alla piattaforma o crea un nuovo account.");
        sottotitolo.addStyleName("auth-sottotitolo");
        intestazione.add(sottotitolo);

        card.add(intestazione);

        FlowPanel azioni = new FlowPanel();
        azioni.addStyleName("auth-azioni");

        // Registrarsi e' l'azione di maggiore enfasi per un nuovo utente:
        // prende il bordeaux, accedere resta secondario
        Button btnRegistrati = new Button("Registrati");
        btnRegistrati.addStyleName("btn-primary");
        btnRegistrati.addClickHandler(event -> new RegistrazioneGui().mostra());
        azioni.add(btnRegistrati);

        Button btnLogin = new Button("Login");
        btnLogin.addStyleName("btn-secondary");
        btnLogin.addClickHandler(event -> new LoginGui().mostra());
        azioni.add(btnLogin);

        card.add(azioni);
        sfondo.add(card);

        RootPanel.get().clear();
        RootPanel.get().add(sfondo);

        // Stessa base grafica delle altre schermate
        Document.get().getBody().getStyle().setProperty("backgroundColor", "#E8E8E8");
        Document.get().getBody().getStyle().setProperty("margin", "0");
        Document.get().getBody().getStyle().setProperty("fontFamily", "sans-serif");
    }
}
