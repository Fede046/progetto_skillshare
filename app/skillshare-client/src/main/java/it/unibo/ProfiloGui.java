package it.unibo;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ProfiloGui {

    private UtenteDTO utente;

    // Passiamo l'utente loggato al costruttore
    public ProfiloGui(UtenteDTO utente) {
        this.utente = utente;
    }

    public void mostra() {
        // Pannello principale (Card centrale)
        VerticalPanel card = new VerticalPanel();
        card.setSpacing(20);

        // CSS per centrare la card (identico a WelcomeGui)
        card.getElement().getStyle().setProperty("backgroundColor", "#ffffff");
        card.getElement().getStyle().setProperty("padding", "40px");
        card.getElement().getStyle().setProperty("borderRadius", "10px");
        card.getElement().getStyle().setProperty("boxShadow", "0 4px 15px rgba(0, 0, 0, 0.1)");
        card.getElement().getStyle().setProperty("width", "350px");
        card.getElement().getStyle().setProperty("marginLeft", "auto");
        card.getElement().getStyle().setProperty("marginRight", "auto");
        card.getElement().getStyle().setProperty("marginTop", "100px");
        card.getElement().getStyle().setProperty("textAlign", "center");

        // Titolo
        HTML titolo = new HTML(
                "<h1 style='color: #333333; font-size: 28px; margin: 0 0 10px 0; font-family: sans-serif;'>Il tuo Profilo</h1>"
        );

        // SPAZIO APPOSITO PER I MESSAGGI (sostituisce gli alert)
        HTML messaggioSpazio = new HTML();
        messaggioSpazio.getElement().getStyle().setProperty("fontSize", "14px");
        messaggioSpazio.getElement().getStyle().setProperty("textAlign", "center");
        messaggioSpazio.getElement().getStyle().setProperty("minHeight", "20px"); // Mantiene lo spazio anche se vuoto
        messaggioSpazio.getElement().getStyle().setProperty("marginBottom", "15px");

        // Foto profilo (o placeholder con le iniziali se non caricata)
        Widget avatar = creaAvatar();

        // Riepilogo dati utente
        HTML datiUtente = new HTML(
                "<div style='color: #666666; font-size: 15px; text-align: left; background: #f9f9f9; padding: 15px; border-radius: 5px; margin-bottom: 20px;'>" +
                "<b>Nome:</b> " + utente.getNome() + "<br><br>" +
                "<b>Cognome:</b> " + utente.getCognome() + "<br><br>" +
                "<b>Email:</b> " + utente.getEmail() +
                "</div>"
        );

        // Biografia: se assente mostra un testo segnaposto
        String bio = utente.getBio();
        boolean bioPresente = bio != null && !bio.trim().isEmpty();
        HTML sezioneBio = new HTML(
                "<div style='text-align: left; margin-bottom: 20px;'>" +
                "<b style='color: #333333; font-size: 14px;'>Biografia</b>" +
                "<p style='color: " + (bioPresente ? "#666666" : "#aaaaaa") + "; font-size: 14px; " +
                "font-style: " + (bioPresente ? "normal" : "italic") + "; margin: 8px 0 0 0; white-space: pre-wrap;'>" +
                (bioPresente ? bio : "Nessuna biografia inserita.") + "</p>" +
                "</div>"
        );

        // Elenco delle competenze dell'utente
        Widget sezioneTag = creaSezioneTag();

        // Pulsante per aprire il form di modifica del profilo
        Button btnModifica = new Button("Modifica Profilo");
        stileBottone(btnModifica, "#4CAF50"); // Verde
        btnModifica.addClickHandler(event -> {
            new ModificaProfiloGui(utente).mostra();
        });

        // Pulsante per il Logout
        Button btnLogout = new Button("Logout");
        stileBottone(btnLogout, "#f44336"); // Rosso per indicare l'uscita
        btnLogout.addClickHandler(event -> {
            // Torna alla WelcomeGui
            new WelcomeGui().mostra();
        });

        // Aggiunta degli elementi alla card
        card.add(titolo);
        card.add(messaggioSpazio);
        card.add(avatar);
        card.add(datiUtente);
        card.add(sezioneBio);
        card.add(sezioneTag);
        card.add(btnModifica);
        card.add(btnLogout);

        // Pulizia e rendering
        RootPanel.get().clear();
        RootPanel.get().add(card);

        // Stile base della pagina
        com.google.gwt.dom.client.Document.get().getBody().getStyle().setBackgroundColor("#f4f7f6");
        com.google.gwt.dom.client.Document.get().getBody().getStyle().setProperty("margin", "0");
        com.google.gwt.dom.client.Document.get().getBody().getStyle().setProperty("fontFamily", "sans-serif");
    }

    /**
     * Costruisce la foto profilo. Se l'URL non è stato impostato - oppure se
     * l'immagine non riesce a caricarsi - viene mostrato un placeholder circolare
     * con le iniziali dell'utente.
     */
    private Widget creaAvatar() {
        FlowPanel contenitore = new FlowPanel();
        contenitore.addStyleName("profile-avatar-wrapper");

        String photoUrl = utente.getPhotoUrl();
        if (photoUrl == null || photoUrl.trim().isEmpty()) {
            contenitore.add(creaPlaceholderAvatar());
            return contenitore;
        }

        Image foto = new Image(photoUrl.trim());
        foto.addStyleName("profile-avatar");
        // Fallback nel caso l'URL sia rotto o l'immagine non sia raggiungibile
        foto.addErrorHandler(event -> {
            contenitore.clear();
            contenitore.add(creaPlaceholderAvatar());
        });
        contenitore.add(foto);
        return contenitore;
    }

    /**
     * Placeholder circolare con le iniziali di nome e cognome.
     */
    private Widget creaPlaceholderAvatar() {
        String iniziali = "";
        if (utente.getNome() != null && !utente.getNome().trim().isEmpty()) {
            iniziali += utente.getNome().trim().charAt(0);
        }
        if (utente.getCognome() != null && !utente.getCognome().trim().isEmpty()) {
            iniziali += utente.getCognome().trim().charAt(0);
        }
        if (iniziali.isEmpty()) {
            iniziali = "?";
        }

        Label placeholder = new Label(iniziali.toUpperCase());
        placeholder.addStyleName("profile-avatar");
        placeholder.addStyleName("profile-avatar-placeholder");
        placeholder.setTitle("Nessuna foto profilo caricata");
        return placeholder;
    }

    /**
     * Costruisce l'elenco dei tag competenza sotto forma di badge.
     */
    private Widget creaSezioneTag() {
        FlowPanel sezione = new FlowPanel();
        sezione.getElement().getStyle().setProperty("textAlign", "left");
        sezione.getElement().getStyle().setProperty("marginBottom", "20px");

        HTML etichetta = new HTML(
                "<b style='color: #333333; font-size: 14px;'>Competenze</b>"
        );
        sezione.add(etichetta);

        FlowPanel badges = new FlowPanel();
        badges.addStyleName("tag-badges-container");

        if (utente.getTagCompetenza() == null || utente.getTagCompetenza().isEmpty()) {
            Label vuoto = new Label("Nessuna competenza aggiunta.");
            vuoto.getElement().getStyle().setProperty("color", "#aaaaaa");
            vuoto.getElement().getStyle().setProperty("fontSize", "14px");
            vuoto.getElement().getStyle().setProperty("fontStyle", "italic");
            badges.add(vuoto);
        } else {
            for (String tag : utente.getTagCompetenza()) {
                Label badge = new Label(tag);
                badge.addStyleName("skill-badge");
                badges.add(badge);
            }
        }

        sezione.add(badges);
        return sezione;
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
        button.getElement().getStyle().setProperty("marginBottom", "10px");
    }
}
