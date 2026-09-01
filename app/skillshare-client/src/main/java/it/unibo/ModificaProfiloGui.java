package it.unibo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;

// Form di modifica del proprio profilo: foto (upload o URL), bio e tag di competenza
public class ModificaProfiloGui {

    private final UtenteDTO utente;
    private final List<String> tagList;
    private final ProfileServiceAsync profileService = GWT.create(ProfileService.class);

    private final FlowPanel tagContainer = new FlowPanel();
    private final TextBox photoUrlBox = new TextBox();

    // Upload della foto: il file non puo' viaggiare sull'RPC di GWT, quindi
    // si usa un FormPanel multipart verso una servlet dedicata
    private final FormPanel formUpload = new FormPanel();
    private final FileUpload selettoreFile = new FileUpload();
    private final Label esitoUpload = new Label();
    // Errore di salvataggio mostrato nel form, senza popup
    private final Label erroreSalvataggio = new Label();
    private final Button btnCarica = new Button("Carica foto");
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

        FlowPanel pagina = new FlowPanel();

        // Stessa barra di ProfiloGui: Modifica Profilo e' una sotto-schermata di Profilo
        pagina.add(new NavBar(utente, NavBar.SEZIONE_PROFILO).getWidget());

        FlowPanel contenuto = new FlowPanel();
        contenuto.addStyleName("app-page");

        VerticalPanel formContainer = new VerticalPanel();
        formContainer.addStyleName("profile-container");

        // Titolo Form
        HTML title = new HTML("<h2>Modifica Profilo</h2>");
        formContainer.add(title);

        // Foto profilo: caricamento da file, via principale
        Label lblUpload = new Label("Foto Profilo:");
        lblUpload.addStyleName("form-label");
        formContainer.add(lblUpload);
        formContainer.add(creaFormUpload());

        // Campo URL, mantenuto come alternativa: i profili gia' salvati
        // contengono URL remoti che devono restare modificabili
        Label lblPhoto = new Label("oppure indirizzo di un'immagine online:");
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
        FlowPanel addTagPanel = new FlowPanel();
        addTagPanel.addStyleName("profile-form-riga");

        newTagBox.getElement().setAttribute("placeholder", "Es. Java, GWT, SQL");
        Button btnAddTag = new Button("Aggiungi Tag");
        btnAddTag.addStyleName("btn-secondary");
        btnAddTag.addStyleName("btn-sm");
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

        // Esito del salvataggio: compare qui sopra invece che in un popup
        erroreSalvataggio.addStyleName("form-errore");
        erroreSalvataggio.setVisible(false);
        formContainer.add(erroreSalvataggio);

        // Pulsanti Azione (Salva / Annulla)
        FlowPanel buttonPanel = new FlowPanel();
        buttonPanel.addStyleName("profile-form-azioni");

        Button btnSalva = new Button("Salva Modifiche");
        btnSalva.addStyleName("btn-primary");
        btnSalva.addClickHandler(event -> salvaModifiche());

        Button btnAnnulla = new Button("Annulla");
        btnAnnulla.addStyleName("btn-secondary");
        btnAnnulla.addClickHandler(event -> {
            // Ritorna alla schermata profilo scartando le modifiche
            tornaAlProfilo(utente);
        });

        buttonPanel.add(btnAnnulla);
        buttonPanel.add(btnSalva);
        formContainer.add(buttonPanel);

        contenuto.add(formContainer);
        pagina.add(contenuto);
        RootPanel.get().add(pagina);

        // Stessa base grafica di ProfiloGui
        Document.get().getBody().getStyle().setProperty("backgroundColor", "#E8E8E8");
        Document.get().getBody().getStyle().setProperty("margin", "0");
        Document.get().getBody().getStyle().setProperty("fontFamily", "sans-serif");
    }

    // Ridisegna graficamente i badge dei tag con il tasto di rimozione
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

    // Form multipart per il caricamento della foto dal dispositivo. Punta alla servlet dedicata:
    // l'upload di file non passa dall'RPC di GWT, che sa serializzare solo oggetti Java.
    private Widget creaFormUpload() {
        // getHostPageBaseURL punta alla radice dell'app, non al modulo GWT
        formUpload.setAction(GWT.getHostPageBaseURL() + ProtocolloUploadFoto.PERCORSO_UPLOAD);
        formUpload.setEncoding(FormPanel.ENCODING_MULTIPART);
        formUpload.setMethod(FormPanel.METHOD_POST);

        FlowPanel contenuto = new FlowPanel();
        contenuto.addStyleName("foto-upload-riga");

        selettoreFile.setName(ProtocolloUploadFoto.CAMPO_FILE);
        selettoreFile.getElement().setAttribute("accept", "image/jpeg,image/png");
        contenuto.add(selettoreFile);

        // L'email identifica l'utente lato server: viaggia con il form
        Hidden campoEmail = new Hidden(ProtocolloUploadFoto.CAMPO_EMAIL, utente.getEmail());
        contenuto.add(campoEmail);

        btnCarica.addStyleName("btn-secondary");
        btnCarica.addStyleName("btn-sm");
        btnCarica.addClickHandler(event -> {
            String nome = selettoreFile.getFilename();
            if (nome == null || nome.trim().isEmpty()) {
                mostraEsitoUpload("Scegli prima un'immagine da caricare.", false);
                return;
            }
            btnCarica.setEnabled(false);
            btnCarica.setText("Caricamento...");
            formUpload.submit();
        });
        contenuto.add(btnCarica);

        formUpload.setWidget(contenuto);

        // La servlet risponde "OK|percorso" oppure "ERRORE|messaggio"
        formUpload.addSubmitCompleteHandler(event -> {
            btnCarica.setEnabled(true);
            btnCarica.setText("Carica foto");

            String risposta = event.getResults() == null ? "" : event.getResults().trim();
            // Alcuni browser incapsulano la risposta in tag HTML: li si rimuove
            risposta = risposta.replaceAll("<[^>]*>", "").trim();

            if (risposta.startsWith(ProtocolloUploadFoto.ESITO_OK)) {
                String percorso = risposta.substring(ProtocolloUploadFoto.ESITO_OK.length());
                // Allinea il campo URL: il salvataggio successivo non deve
                // sovrascrivere la foto appena caricata con il valore vecchio
                photoUrlBox.setText(percorso);
                mostraEsitoUpload("Foto caricata. Salva il profilo per confermare.", true);
            } else if (risposta.startsWith(ProtocolloUploadFoto.ESITO_ERRORE)) {
                mostraEsitoUpload(risposta.substring(ProtocolloUploadFoto.ESITO_ERRORE.length()), false);
            } else {
                mostraEsitoUpload("Caricamento non riuscito. Riprova.", false);
            }
        });

        FlowPanel blocco = new FlowPanel();
        blocco.add(formUpload);

        esitoUpload.setVisible(false);
        blocco.add(esitoUpload);

        return blocco;
    }

    /** Mostra l'esito dell'upload riusando gli stili di alert del progetto. */
    private void mostraEsitoUpload(String testo, boolean successo) {
        esitoUpload.setText(testo);
        esitoUpload.setStyleName(successo ? "foto-upload-esito-ok" : "form-errore");
        esitoUpload.setVisible(true);
    }

    // Invia i dati aggiornati al server tramite la chiamata asincrona RPC
    private void salvaModifiche() {
        UtenteDTO utenteModificato = new UtenteDTO();
        utenteModificato.setEmail(utente.getEmail());
        utenteModificato.setNome(utente.getNome());
        utenteModificato.setCognome(utente.getCognome());
        utenteModificato.setPhotoUrl(photoUrlBox.getText().trim());
        utenteModificato.setBio(bioArea.getText().trim());
        utenteModificato.setTagCompetenza(new ArrayList<>(tagList));

        erroreSalvataggio.setVisible(false);

        profileService.updateProfile(utenteModificato, new AsyncCallback<UtenteDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                // Il salvataggio e' fallito: restiamo sul form per non perdere
                // le modifiche gia' inserite dall'utente, e lo diciamo nel form
                erroreSalvataggio.setText("Salvataggio non riuscito: " + caught.getMessage());
                erroreSalvataggio.setVisible(true);
            }

            @Override
            public void onSuccess(UtenteDTO result) {
                // Nessuna conferma da chiudere: il profilo aggiornato che
                // compare subito e' gia' la conferma che il salvataggio e' andato.
                // Se il server non restituisce l'utente, usiamo i dati locali
                // appena inviati: cosi' la pagina non resta mai vuota.
                tornaAlProfilo(result != null ? result : utenteModificato);
            }
        });
    }

    // Unico punto di uscita verso la schermata del profilo, usato
    // sia da "Annulla" sia dal salvataggio.
    private void tornaAlProfilo(UtenteDTO daMostrare) {
        UtenteDTO profilo = (daMostrare != null) ? daMostrare : utente;
        new ProfiloGui(profilo).mostra();
    }
}