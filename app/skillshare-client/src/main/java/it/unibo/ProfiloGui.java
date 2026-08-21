package it.unibo;

import java.util.List;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ProfiloGui {

    private final UtenteDTO utente;

    public ProfiloGui(UtenteDTO utente) {
        this.utente = utente;
    }

    public void mostra() {
        RootPanel.get().clear();

        VerticalPanel container = new VerticalPanel();
        container.addStyleName("profile-container");

        // Titolo
        HTML header = new HTML("<h2>Profilo Utente</h2>");
        container.add(header);

        // Immagine Profilo
        String fotoUrl = (utente.getPhotoUrl() != null && !utente.getPhotoUrl().trim().isEmpty())
                ? utente.getPhotoUrl()
                : "https://via.placeholder.com/150";
        Image fotoProfilo = new Image(fotoUrl);
        fotoProfilo.setPixelSize(120, 120);
        fotoProfilo.addStyleName("profile-avatar");
        container.add(fotoProfilo);

        // Nome e Cognome
        Label nomeCognome = new Label(utente.getNome() + " " + utente.getCognome());
        nomeCognome.addStyleName("profile-name");
        container.add(nomeCognome);

        // Email
        Label emailLabel = new Label("Email: " + utente.getEmail());
        emailLabel.addStyleName("profile-email");
        container.add(emailLabel);

        // Bio
        String testoBio = (utente.getBio() != null && !utente.getBio().trim().isEmpty())
                ? utente.getBio()
                : "Nessuna biografia inserita.";
        Label bioLabel = new Label("Bio: " + testoBio);
        bioLabel.addStyleName("profile-bio");
        container.add(bioLabel);

        // Sezione Skill Tags
        Label tagHeader = new Label("Competenze:");
        tagHeader.addStyleName("profile-tag-header");
        container.add(tagHeader);

        FlowPanel tagContainer = new FlowPanel();
        tagContainer.addStyleName("tag-badges-container");

        List<String> tags = utente.getTagCompetenza();
        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                Label badge = new Label(tag);
                badge.addStyleName("skill-badge");
                tagContainer.add(badge);
            }
        } else {
            Label noTags = new Label("Nessuna competenza inserita.");
            tagContainer.add(noTags);
        }
        container.add(tagContainer);

        // Pulsanti di azione
        FlowPanel buttonPanel = new FlowPanel();
        buttonPanel.addStyleName("profile-actions");

        Button btnModifica = new Button("Modifica Profilo");
        btnModifica.addClickHandler(event -> {
            new ModificaProfiloGui(utente).mostra();
        });
        btnModifica.addStyleName("btn-primary");
        buttonPanel.add(btnModifica);

        Button btnLogout = new Button("Logout");
        btnLogout.addClickHandler(event -> {
            new WelcomeGui().mostra();
        });
        btnLogout.addStyleName("btn-secondary");
        buttonPanel.add(btnLogout);
    }
}