package it.unibo;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Form di creazione di un nuovo annuncio.
 * Solo modalita' creazione: la modifica di un annuncio esistente arrivera'
 * con la Story "Gestione Annuncio".
 */
public class NuovoAnnuncioGui {

    private final UtenteDTO utente;
    private final AnnuncioServiceAsync annuncioService = GWT.create(AnnuncioService.class);

    private final TextBox titoloBox = new TextBox();
    private final TextArea descrizioneArea = new TextArea();
    private final TextBox competenzaBox = new TextBox();
    private final TextBox disponibilitaBox = new TextBox();
    private final TextBox controprestazioneBox = new TextBox();

    // Messaggio di errore in rosso sopra i pulsanti, nascosto finche' non serve
    private final Label messaggioErrore = new Label();

    public NuovoAnnuncioGui(UtenteDTO utente) {
        this.utente = utente;
    }

    public void mostra() {
        RootPanel.get().clear();

        FlowPanel pagina = new FlowPanel();
        pagina.add(new NavBar(utente, NavBar.SEZIONE_ANNUNCI).getWidget());

        FlowPanel contenuto = new FlowPanel();
        contenuto.addStyleName("app-page");

        VerticalPanel formContainer = new VerticalPanel();
        formContainer.addStyleName("profile-container");

        formContainer.add(new HTML("<h2>Nuovo Annuncio</h2>"));

        formContainer.add(creaEtichetta("Titolo:"));
        titoloBox.setWidth("100%");
        formContainer.add(titoloBox);

        formContainer.add(creaEtichetta("Descrizione:"));
        descrizioneArea.setVisibleLines(4);
        descrizioneArea.setWidth("100%");
        formContainer.add(descrizioneArea);

        formContainer.add(creaEtichetta("Competenza offerta:"));
        competenzaBox.getElement().setAttribute("placeholder", "Es. Programmazione Java");
        competenzaBox.setWidth("100%");
        formContainer.add(competenzaBox);

        formContainer.add(creaEtichetta("Disponibilità:"));
        disponibilitaBox.getElement().setAttribute("placeholder", "Es. Lunedì e mercoledì pomeriggio");
        disponibilitaBox.setWidth("100%");
        formContainer.add(disponibilitaBox);

        formContainer.add(creaEtichetta("Controprestazione:"));
        controprestazioneBox.getElement().setAttribute("placeholder", "Es. Lezioni di inglese");
        controprestazioneBox.setWidth("100%");
        formContainer.add(controprestazioneBox);

        messaggioErrore.addStyleName("form-errore");
        messaggioErrore.setVisible(false);
        formContainer.add(messaggioErrore);

        // Pulsanti azione (Annulla / Pubblica)
        FlowPanel buttonPanel = new FlowPanel();
        buttonPanel.addStyleName("profile-form-azioni");

        Button btnAnnulla = new Button("Annulla");
        btnAnnulla.addStyleName("btn-secondary");
        btnAnnulla.addClickHandler(event -> tornaAllaLista());

        Button btnPubblica = new Button("Pubblica Annuncio");
        btnPubblica.addStyleName("btn-primary");
        btnPubblica.addClickHandler(event -> pubblica());

        buttonPanel.add(btnAnnulla);
        buttonPanel.add(btnPubblica);
        formContainer.add(buttonPanel);

        contenuto.add(formContainer);
        pagina.add(contenuto);
        RootPanel.get().add(pagina);

        // Stessa base grafica delle altre schermate
        Document.get().getBody().getStyle().setProperty("backgroundColor", "#E8E8E8");
        Document.get().getBody().getStyle().setProperty("margin", "0");
        Document.get().getBody().getStyle().setProperty("fontFamily", "sans-serif");
    }

    private Label creaEtichetta(String testo) {
        Label etichetta = new Label(testo);
        etichetta.addStyleName("form-label");
        return etichetta;
    }

    /**
     * Invia il nuovo annuncio al server.
     * Prima elenca in un colpo solo i campi obbligatori lasciati vuoti, cosi'
     * l'utente non deve scoprirli uno alla volta; la validazione che fa fede
     * resta comunque quella di AnnuncioDatabase.
     */
    private void pubblica() {
        String mancanti = campiObbligatoriMancanti();
        if (!mancanti.isEmpty()) {
            mostraErrore("Non puoi pubblicare l'annuncio: compila " + mancanti + ".");
            return;
        }

        nascondiErrore();

        AnnuncioDTO annuncio = new AnnuncioDTO();
        annuncio.setIdUtente(utente.getEmail());
        annuncio.setTitolo(titoloBox.getText().trim());
        annuncio.setDescrizione(descrizioneArea.getText().trim());
        annuncio.setCompetenzaOfferta(competenzaBox.getText().trim());
        annuncio.setDisponibilita(disponibilitaBox.getText().trim());
        annuncio.setControprestazione(controprestazioneBox.getText().trim());

        annuncioService.pubblica(annuncio, new AsyncCallback<AnnuncioDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                // Restiamo sul form per non perdere quanto gia' inserito
                mostraErrore(caught.getMessage());
            }

            @Override
            public void onSuccess(AnnuncioDTO result) {
                // La lista ricarica e mostra il nuovo annuncio
                tornaAllaLista();
            }
        });
    }

    /**
     * Elenco dei campi obbligatori rimasti vuoti, gia' pronto da leggere.
     * Stringa vuota se sono tutti compilati.
     */
    private String campiObbligatoriMancanti() {
        List<String> mancanti = new ArrayList<>();

        if (titoloBox.getText().trim().isEmpty()) {
            mancanti.add("titolo");
        }
        if (competenzaBox.getText().trim().isEmpty()) {
            mancanti.add("competenza offerta");
        }
        if (disponibilitaBox.getText().trim().isEmpty()) {
            mancanti.add("disponibilità");
        }
        if (controprestazioneBox.getText().trim().isEmpty()) {
            mancanti.add("controprestazione");
        }

        if (mancanti.isEmpty()) {
            return "";
        }

        // "titolo", "titolo e disponibilità", "titolo, disponibilità e controprestazione"
        StringBuilder elenco = new StringBuilder();
        for (int i = 0; i < mancanti.size(); i++) {
            if (i > 0) {
                elenco.append(i == mancanti.size() - 1 ? " e " : ", ");
            }
            elenco.append(mancanti.get(i));
        }
        return elenco.toString();
    }

    private void mostraErrore(String testo) {
        messaggioErrore.setText(testo);
        messaggioErrore.setVisible(true);
    }

    private void nascondiErrore() {
        messaggioErrore.setVisible(false);
    }

    private void tornaAllaLista() {
        new MieiAnnunciGui(utente).mostra();
    }
}
