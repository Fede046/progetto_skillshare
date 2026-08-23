package it.unibo;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Schermata "I miei annunci": elenco degli annunci pubblicati dall'utente
 * con le azioni di modifica e rimozione.
 */
public class MieiAnnunciGui {

    private final UtenteDTO utente;
    private final AnnuncioServiceAsync annuncioService = GWT.create(AnnuncioService.class);

    // Riempito dalla risposta RPC: al primo disegno mostra il caricamento
    private final FlowPanel listaAnnunci = new FlowPanel();

    public MieiAnnunciGui(UtenteDTO utente) {
        this.utente = utente;
    }

    public void mostra() {
        FlowPanel pagina = new FlowPanel();
        pagina.add(new NavBar(utente, NavBar.SEZIONE_ANNUNCI).getWidget());

        FlowPanel contenuto = new FlowPanel();
        contenuto.addStyleName("app-page");
        contenuto.add(creaSezioneAnnunci());
        pagina.add(contenuto);

        RootPanel.get().clear();
        RootPanel.get().add(pagina);

        // Stessa base grafica delle altre schermate
        Document.get().getBody().getStyle().setProperty("backgroundColor", "#E8E8E8");
        Document.get().getBody().getStyle().setProperty("margin", "0");
        Document.get().getBody().getStyle().setProperty("fontFamily", "sans-serif");

        caricaAnnunci();
    }

    /**
     * Intestazione con l'azione di creazione e la lista,
     * che viene riempita dalla chiamata RPC.
     */
    private Widget creaSezioneAnnunci() {
        FlowPanel card = new FlowPanel();
        card.addStyleName("profile-card");

        FlowPanel intestazione = new FlowPanel();
        intestazione.addStyleName("annunci-intestazione");

        Label titolo = new Label("I miei annunci");
        titolo.addStyleName("profile-sezione-titolo");
        intestazione.add(titolo);

        Button btnNuovo = new Button("+ Nuovo annuncio");
        btnNuovo.addStyleName("btn-primary");
        btnNuovo.addStyleName("btn-sm");
        btnNuovo.addClickHandler(event -> new NuovoAnnuncioGui(utente).mostra());
        intestazione.add(btnNuovo);

        card.add(intestazione);

        listaAnnunci.clear();
        listaAnnunci.add(creaMessaggioVuoto("Caricamento annunci..."));
        card.add(listaAnnunci);

        return card;
    }

    /**
     * Chiede al server gli annunci dell'utente e ridisegna la lista.
     */
    private void caricaAnnunci() {
        annuncioService.annunciDiUtente(utente.getEmail(), new AsyncCallback<List<AnnuncioDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                listaAnnunci.clear();
                listaAnnunci.add(creaMessaggioVuoto("Impossibile caricare gli annunci. Riprova."));
            }

            @Override
            public void onSuccess(List<AnnuncioDTO> result) {
                mostraAnnunci(result);
            }
        });
    }

    private void mostraAnnunci(List<AnnuncioDTO> annunci) {
        listaAnnunci.clear();

        if (annunci == null || annunci.isEmpty()) {
            listaAnnunci.add(creaMessaggioVuoto("Non hai ancora pubblicato annunci"));
            return;
        }

        for (AnnuncioDTO annuncio : annunci) {
            listaAnnunci.add(new RigaAnnuncio(annuncio).widget());
        }
    }

    /**
     * Apre il form di modifica inline nella riga dell'annuncio,
     * pre-compilato con i dati correnti.
     */
    private void apriFormModifica(AnnuncioDTO annuncio, RigaAnnuncio riga) {
        if (riga.formAperto) {
            return;
        }
        riga.formAperto = true;

        FlowPanel form = new FlowPanel();
        form.addStyleName("annuncio-modifica-form");

        TextBox titoloBox = new TextBox();
        titoloBox.setWidth("100%");
        titoloBox.setText(testoOppure(annuncio.getTitolo(), ""));

        TextArea descrizioneArea = new TextArea();
        descrizioneArea.setVisibleLines(3);
        descrizioneArea.setWidth("100%");
        descrizioneArea.setText(testoOppure(annuncio.getDescrizione(), ""));

        TextBox competenzaBox = new TextBox();
        competenzaBox.setWidth("100%");
        competenzaBox.setText(testoOppure(annuncio.getCompetenzaOfferta(), ""));

        TextBox disponibilitaBox = new TextBox();
        disponibilitaBox.setWidth("100%");
        disponibilitaBox.setText(testoOppure(annuncio.getDisponibilita(), ""));

        TextBox controprestazioneBox = new TextBox();
        controprestazioneBox.setWidth("100%");
        controprestazioneBox.setText(testoOppure(annuncio.getControprestazione(), ""));

        Button btnSalva = new Button("Salva");
        btnSalva.addStyleName("btn-primary");
        btnSalva.addStyleName("btn-sm");

        Button btnAnnulla = new Button("Annulla");
        btnAnnulla.addStyleName("btn-secondary");
        btnAnnulla.addStyleName("btn-sm");

        btnAnnulla.addClickHandler(event -> {
            riga.formAperto = false;
            form.removeFromParent();
        });

        btnSalva.addClickHandler(event -> {
            AnnuncioDTO aggiornato = new AnnuncioDTO();
            aggiornato.setTitolo(titoloBox.getText().trim());
            aggiornato.setDescrizione(descrizioneArea.getText().trim());
            aggiornato.setCompetenzaOfferta(competenzaBox.getText().trim());
            aggiornato.setDisponibilita(disponibilitaBox.getText().trim());
            aggiornato.setControprestazione(controprestazioneBox.getText().trim());

            annuncioService.modifica(annuncio.getId(), utente.getEmail(), aggiornato,
                    new AsyncCallback<AnnuncioDTO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            // Se il server rifiuta, mostriamo il messaggio di errore
                            Window.alert(caught.getMessage());
                        }

                        @Override
                        public void onSuccess(AnnuncioDTO result) {
                            // Aggiorna la riga visibile con i nuovi dati e chiude il form
                            riga.aggiorna(result);
                            riga.formAperto = false;
                            form.removeFromParent();
                        }
                    });
        });

        form.add(creaEtichetta("Titolo:"));
        form.add(titoloBox);
        form.add(creaEtichetta("Descrizione:"));
        form.add(descrizioneArea);
        form.add(creaEtichetta("Competenza offerta:"));
        form.add(competenzaBox);
        form.add(creaEtichetta("Disponibilità:"));
        form.add(disponibilitaBox);
        form.add(creaEtichetta("Controprestazione:"));
        form.add(controprestazioneBox);

        FlowPanel azioniForm = new FlowPanel();
        azioniForm.addStyleName("profile-form-azioni");
        azioniForm.add(btnSalva);
        azioniForm.add(btnAnnulla);
        form.add(azioniForm);

        // Il form appare in cima alla riga dell'annuncio
        riga.widget().insert(form, 0);
    }

    /**
     * Chiede al server la rimozione dell'annuncio e, in caso di successo,
     * lo elimina dalla lista visibile senza ricaricare la pagina.
     */
    private void rimuoviAnnuncio(AnnuncioDTO annuncio, RigaAnnuncio riga) {
        // Richiesta di conferma prima di eliminare l'annuncio
        if (!Window.confirm("Vuoi davvero rimuovere l'annuncio \"" + testoOppure(annuncio.getTitolo(), "") + "\"?")) {
            return;
        }

        annuncioService.rimuovi(annuncio.getId(), utente.getEmail(), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                // Se il server rifiuta, mostriamo il messaggio di errore
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                // Rimozione immediata dalla lista visibile, senza reload
                riga.widget().removeFromParent();

                if (listaAnnunci.getWidgetCount() == 0) {
                    listaAnnunci.add(creaMessaggioVuoto("Non hai ancora pubblicato annunci"));
                }
            }
        });
    }

    private Label creaEtichetta(String testo) {
        Label etichetta = new Label(testo);
        etichetta.addStyleName("form-label");
        return etichetta;
    }

    private Widget creaCampo(String etichetta, Label valore) {
        FlowPanel campo = new FlowPanel();
        campo.addStyleName("annuncio-campo");

        Label lbl = new Label(etichetta);
        lbl.addStyleName("annuncio-campo-etichetta");
        campo.add(lbl);

        valore.addStyleName("annuncio-campo-valore");
        campo.add(valore);

        return campo;
    }

    private Widget creaMessaggioVuoto(String testo) {
        Label messaggio = new Label(testo);
        messaggio.addStyleName("annunci-vuoto");
        return messaggio;
    }

    private String testoOppure(String valore, String fallback) {
        return valore != null ? valore : fallback;
    }

    /**
     * Riquadro di un singolo annuncio con i pulsanti Modifica ed Elimina.
     * Conserva i riferimenti ai valori mostrati per aggiornarli inline
     * dopo una modifica e per rimuovere la riga dopo l'eliminazione.
     */
    private class RigaAnnuncio {

        private final FlowPanel item = new FlowPanel();
        private final FlowPanel campi = new FlowPanel();
        private final Label titolo = new Label();
        private final Label competenza = new Label();
        private final Label disponibilita = new Label();
        private final Label controprestazione = new Label();
        private AnnuncioDTO annuncio;
        private boolean formAperto;

        RigaAnnuncio(AnnuncioDTO annuncio) {
            this.annuncio = annuncio;

            item.addStyleName("annuncio-item");

            titolo.addStyleName("annuncio-titolo");
            item.add(titolo);

            campi.addStyleName("annuncio-campi");
            campi.add(creaCampo("Competenza offerta", competenza));
            campi.add(creaCampo("Disponibilità", disponibilita));
            campi.add(creaCampo("Controprestazione", controprestazione));
            item.add(campi);

            FlowPanel azioni = new FlowPanel();
            azioni.addStyleName("annuncio-azioni");

            Button btnModifica = new Button("Modifica");
            btnModifica.addStyleName("btn-secondary");
            btnModifica.addStyleName("btn-sm");
            btnModifica.addClickHandler(event -> apriFormModifica(this.annuncio, this));
            azioni.add(btnModifica);

            Button btnElimina = new Button("Elimina");
            btnElimina.addStyleName("btn-secondary");
            btnElimina.addStyleName("btn-sm");
            btnElimina.addClickHandler(event -> rimuoviAnnuncio(this.annuncio, this));
            azioni.add(btnElimina);

            item.add(azioni);

            aggiorna(annuncio);
        }

        FlowPanel widget() {
            return item;
        }

        // Aggiorna i valori mostrati nella riga con i dati dell'annuncio (post-modifica)
        void aggiorna(AnnuncioDTO annuncio) {
            this.annuncio = annuncio;

            titolo.setText(testoOppure(annuncio.getTitolo(), ""));
            competenza.setText(testoOppure(annuncio.getCompetenzaOfferta(), "-"));
            disponibilita.setText(testoOppure(annuncio.getDisponibilita(), "-"));
            controprestazione.setText(testoOppure(annuncio.getControprestazione(), "-"));
        }
    }
}
