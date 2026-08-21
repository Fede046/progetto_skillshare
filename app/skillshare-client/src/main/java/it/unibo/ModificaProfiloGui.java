package it.unibo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.ArrayList;
import java.util.List;

public class ModificaProfiloGui {

    private final UtenteDTO utente;
    private final List<String> tagList;
    private final ProfileServiceAsync profileService = GWT.create(ProfileService.class);

    private final FlowPanel tagContainer = new FlowPanel();
    private final TextBox photoUrlBox = new TextBox();
    private final TextArea bioArea = new TextArea();
    private final TextBox newTagBox = new TextBox();

    public ModificaProfiloGui(UtenteDTO utente) {
        this.utente = utente;
        this.tagList = (utente.getTagCompetenza() != null)
                ? new ArrayList<>(utente.getTagCompetenza())
                : new ArrayList<>();
    }

    public void mostra() {
        RootPanel.get().clear();

        VerticalPanel formContainer = new VerticalPanel();
        formContainer.addStyleName("profile-container");

        // Titolo Form
        HTML title = new HTML("<h2>Modifica Profilo</h2>");
        formContainer.add(title);

        // Campo URL Foto Profilo
        Label lblPhoto = new Label("URL Foto Profilo:");
        lblPhoto.addStyleName("form-label");
        formContainer.add(lblPhoto);

        photoUrlBox.setText(utente.getPhotoUrl() != null ? utente.getPhotoUrl() : "");
        photoUrlBox.setWidth("100%");
        formContainer.add(photoUrlBox);

        // Campo Bio
        Label lblBio = new Label("Biografia / Informazioni su di te:");
        lblBio.addStyleName("form-label");
        formContainer.add(lblBio);

        bioArea.setText(utente.getBio() != null ? utente.getBio() : "");
        bioArea.setVisibleLines(4);
        bioArea.setWidth("100%");
        formContainer.add(bioArea);

        // Sezione Gestione Tag Competenze
        Label lblTags = new Label("Competenze / Tag:");
        lblTags.addStyleName("form-label");
        formContainer.add(lblTags);

        // Form per aggiungere un nuovo Tag
        HorizontalPanel addTagPanel = new HorizontalPanel();
        addTagPanel.setSpacing(5);

        newTagBox.getElement().setAttribute("placeholder", "Es. Java, GWT, SQL");
        Button btnAddTag = new Button("Aggiungi Tag");
        btnAddTag.addClickHandler(event -> {
            String tag = newTagBox.getText().trim();
            if (!tag.isEmpty() && !tagList.contains(tag)) {
                tagList.add(tag);
                newTagBox.setText("");
                renderTags();
            }
        });

        addTagPanel.add(newTagBox);
        addTagPanel.add(btnAddTag);
        formContainer.add(addTagPanel);

        // Contenitore per i tag con pulsante di rimozione 'x'
        tagContainer.addStyleName("tag-badges-container");
        formContainer.add(tagContainer);
        renderTags();

        // Pulsanti Azione (Salva / Annulla)
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(10);

        Button btnSalva = new Button("Salva Modifiche");
        btnSalva.addStyleName("btn-primary");
        btnSalva.addClickHandler(event -> salvaModifiche());

        Button btnAnnulla = new Button("Annulla");
        btnAnnulla.addStyleName("btn-secondary");
        btnAnnulla.addClickHandler(event -> {
            // Ritorna alla schermata profilo scartando le modifiche
            tornaAlProfilo(utente);
        });

        buttonPanel.add(btnSalva);
        buttonPanel.add(btnAnnulla);
        formContainer.add(buttonPanel);

        RootPanel.get().add(formContainer);
    }

    /**
     * Ridisegna graficamente i badge dei tag con il tasto di rimozione
     */
    private void renderTags() {
        tagContainer.clear();
        if (tagList.isEmpty()) {
            Label emptyLbl = new Label("Nessuna competenza aggiunta.");
            emptyLbl.addStyleName("profile-bio");
            tagContainer.add(emptyLbl);
            return;
        }

        for (String tag : tagList) {
            FlowPanel badge = new FlowPanel();
            badge.addStyleName("skill-badge");

            Label tagText = new Label(tag);
            tagText.getElement().getStyle().setProperty("display", "inline-block");

            Button removeBtn = new Button("×");
            removeBtn.addStyleName("badge-remove-btn");
            removeBtn.addClickHandler(event -> {
                tagList.remove(tag);
                renderTags();
            });

            badge.add(tagText);
            badge.add(removeBtn);
            tagContainer.add(badge);
        }
    }

    /**
     * Invia i dati aggiornati al server tramite la chiamata asincrona RPC
     */
    private void salvaModifiche() {
        UtenteDTO utenteModificato = new UtenteDTO();
        utenteModificato.setEmail(utente.getEmail());
        utenteModificato.setNome(utente.getNome());
        utenteModificato.setCognome(utente.getCognome());
        utenteModificato.setPhotoUrl(photoUrlBox.getText().trim());
        utenteModificato.setBio(bioArea.getText().trim());
        utenteModificato.setTagCompetenza(new ArrayList<>(tagList));

        profileService.updateProfile(utenteModificato, new AsyncCallback<UtenteDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                // Il salvataggio e' fallito: restiamo sul form per non perdere
                // le modifiche gia' inserite dall'utente.
                Window.alert("Errore durante il salvataggio: " + caught.getMessage());
            }

            @Override
            public void onSuccess(UtenteDTO result) {
                Window.alert("Profilo aggiornato con successo!");
                // Ritorna alla schermata del profilo visualizzando i nuovi dati.
                // Se il server non restituisce l'utente, usiamo i dati locali
                // appena inviati: cosi' la pagina non resta mai vuota.
                tornaAlProfilo(result != null ? result : utenteModificato);
            }
        });
    }

    /**
     * Unico punto di uscita verso la schermata del profilo, usato sia da
     * "Annulla" sia dal salvataggio.
     *
     * <p>Se il profilo da mostrare fosse nullo si ricade sull'utente ricevuto
     * nel costruttore: {@link ProfiloGui} viene sempre costruita con dati
     * validi e la pagina non puo' quindi rimanere bianca.</p>
     *
     * @param daMostrare il profilo da visualizzare, eventualmente nullo
     */
    private void tornaAlProfilo(UtenteDTO daMostrare) {
        UtenteDTO profilo = (daMostrare != null) ? daMostrare : utente;
        new ProfiloGui(profilo).mostra();
    }
}