package it.unibo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Schermata "Marketplace": tutti gli annunci pubblicati dagli utenti,
 * dal piu' recente al piu' vecchio.
 */
public class MarketplaceGui {

    private final UtenteDTO utente;
    private final MarketplaceServiceAsync marketplaceService = GWT.create(MarketplaceService.class);
    private final RichiestaScambioServiceAsync richiestaScambioService = GWT.create(RichiestaScambioService.class);

    // Riempita dalla risposta RPC: al primo disegno mostra il caricamento
    private final FlowPanel listaAnnunci = new FlowPanel();
    // Id degli annunci su cui l'utente ha gia' una richiesta non completata:
    // su questi il pulsante "Proponi scambio" resta disabilitato (Issue #136)
    private final Set<String> annunciConRichiestaAperta = new HashSet<>();
    // Controlli per ricerca e ordinamento
    private final TextBox searchBox = new TextBox();
    private final ListBox sortBox = new ListBox();
    // Ricerca multi-campo: nessun campo spuntato di default
    private final CheckBox chkTitolo = new CheckBox("Titolo");
    private final CheckBox chkCompetenza = new CheckBox("Competenza offerta");
    private final CheckBox chkControprestazione = new CheckBox("Controprestazione");
    private final CheckBox chkAutore = new CheckBox("Autore");

    public MarketplaceGui(UtenteDTO utente) {
        this.utente = utente;
    }

    public void mostra() {
        FlowPanel pagina = new FlowPanel();
        pagina.add(new NavBar(utente, NavBar.SEZIONE_MARKETPLACE).getWidget());

        FlowPanel contenuto = new FlowPanel();
        contenuto.addStyleName("app-page");
        contenuto.add(creaSezione());
        pagina.add(contenuto);

        RootPanel.get().clear();
        RootPanel.get().add(pagina);

        // Stessa base grafica delle altre schermate
        Document.get().getBody().getStyle().setProperty("backgroundColor", "#E8E8E8");
        Document.get().getBody().getStyle().setProperty("margin", "0");
        Document.get().getBody().getStyle().setProperty("fontFamily", "sans-serif");

        caricaAnnunci();
    }

    private Widget creaSezione() {
        FlowPanel card = new FlowPanel();
        card.addStyleName("profile-card");

        FlowPanel intestazione = new FlowPanel();
        intestazione.addStyleName("annunci-intestazione");

        Label titolo = new Label("Marketplace");
        titolo.addStyleName("profile-sezione-titolo");
        intestazione.add(titolo);

        card.add(intestazione);
        // Barra con Ricerca per Competenza e Ordinamento
        card.add(creaBarraControlli());

        listaAnnunci.clear();
        listaAnnunci.add(creaMessaggioVuoto("Caricamento annunci..."));
        card.add(listaAnnunci);

        return card;
    }

    private Widget creaBarraControlli() {
        FlowPanel controlli = new FlowPanel();
        controlli.addStyleName("marketplace-controlli");

        // Campo Ricerca per Competenza (ricerca in tempo reale durante la digitazione)
        searchBox.getElement().setAttribute("placeholder", "Cerca negli annunci...");
        searchBox.addStyleName("marketplace-search-box");
        searchBox.addKeyUpHandler(event -> caricaAnnunci());

        // Dropdown Ordinamento
        sortBox.addItem("Data (più recenti)", "data");
        sortBox.addItem("Per titolo", "titolo");
        sortBox.addItem("Rating (highest first)", "rating");
        sortBox.addStyleName("marketplace-sort-box");
        sortBox.addChangeHandler(event -> caricaAnnunci());

        controlli.add(searchBox);
        controlli.add(sortBox);

        // Campi in cui cercare: nessuno spuntato => ricerca su tutti i campi (null)
        FlowPanel campiRicerca = new FlowPanel();
        campiRicerca.addStyleName("marketplace-campi-ricerca");
        campiRicerca.add(chkTitolo);
        campiRicerca.add(chkCompetenza);
        campiRicerca.add(chkControprestazione);
        campiRicerca.add(chkAutore);
        chkTitolo.addValueChangeHandler(event -> caricaAnnunci());
        chkCompetenza.addValueChangeHandler(event -> caricaAnnunci());
        chkControprestazione.addValueChangeHandler(event -> caricaAnnunci());
        chkAutore.addValueChangeHandler(event -> caricaAnnunci());
        controlli.add(campiRicerca);

        return controlli;
    }

    /**
     * Chiede al server gli annunci che corrispondono alla query nei campi
     * selezionati, ordinati.
     */
    /**
     * Ricarica la lista: prima le richieste gia' inviate dall'utente, poi gli annunci.
     * Serve a sapere su quali annunci il pulsante "Proponi scambio" va disabilitato.
     * Se la chiamata sulle richieste fallisce si prosegue comunque: il blocco
     * autoritativo resta sul server, qui e' solo un aiuto visivo.
     */
    private void caricaAnnunci() {
        richiestaScambioService.richiesteInviateDaRichiedente(utente.getEmail(),
                new AsyncCallback<List<RichiestaScambioDTO>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        cercaEMostraAnnunci();
                    }

                    @Override
                    public void onSuccess(List<RichiestaScambioDTO> result) {
                        aggiornaAnnunciConRichiestaAperta(result);
                        cercaEMostraAnnunci();
                    }
                });
    }

    /**
     * Ricostruisce l'elenco degli annunci bloccati: una richiesta blocca finche'
     * non raggiunge lo stato COMPLETED (stessa regola applicata dal server).
     */
    private void aggiornaAnnunciConRichiestaAperta(List<RichiestaScambioDTO> richieste) {
        annunciConRichiestaAperta.clear();
        if (richieste == null) {
            return;
        }
        for (RichiestaScambioDTO richiesta : richieste) {
            if (richiesta.getStato() != StatoRichiesta.COMPLETED) {
                annunciConRichiestaAperta.add(richiesta.getIdAnnuncio());
            }
        }
    }

    private void cercaEMostraAnnunci() {
        String query = searchBox.getText().trim();
        boolean ordinaPerTitolo = "titolo".equals(sortBox.getSelectedValue());
        boolean ordinaPerRating = "rating".equals(sortBox.getSelectedValue());

        // Campi selezionati con le checkbox; se nessuno e' spuntato si cerca su tutti
        // (null)
        Set<CampoRicerca> campi = new HashSet<>();
        if (chkTitolo.getValue())
            campi.add(CampoRicerca.TITOLO);
        if (chkCompetenza.getValue())
            campi.add(CampoRicerca.COMPETENZA);
        if (chkControprestazione.getValue())
            campi.add(CampoRicerca.CONTROPRESTAZIONE);
        if (chkAutore.getValue())
            campi.add(CampoRicerca.AUTORE);
        if (campi.isEmpty()) {
            campi = null;
        }

        marketplaceService.cercaAnnunci(query, campi, ordinaPerTitolo, ordinaPerRating,
                new AsyncCallback<List<AnnuncioDTO>>() {
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
            listaAnnunci.add(creaMessaggioVuoto("Nessun annuncio trovato"));
            return;
        }

        for (AnnuncioDTO annuncio : annunci) {
            listaAnnunci.add(creaRigaAnnuncio(annuncio));
        }
    }

    /**
     * Riquadro di un annuncio: titolo, autore e le due voci dello scambio.
     */
    private Widget creaRigaAnnuncio(AnnuncioDTO annuncio) {
        FlowPanel item = new FlowPanel();
        item.addStyleName("annuncio-item");

        Label titolo = new Label(testoOppure(annuncio.getTitolo(), ""));
        titolo.addStyleName("annuncio-titolo");
        item.add(titolo);

        // Nel marketplace conta sapere subito chi propone lo scambio
        Label autore = new Label("di " + testoOppure(annuncio.getNomeAutore(), "Autore sconosciuto"));
        autore.addStyleName("annuncio-autore");
        item.add(autore);
        FlowPanel campi = new FlowPanel();
        Double rating = annuncio.getValutazioneAutore();
        String testoRating = (rating != null)
                ? Stelle.mediaFormattata(rating) + " / 5.0"
                : "Nessuna recensione";
        campi.add(creaCampo("Rating autore", testoRating));

        campi.addStyleName("annuncio-campi");
        campi.add(creaCampo("Competenza offerta", annuncio.getCompetenzaOfferta()));
        campi.add(creaCampo("Controprestazione", annuncio.getControprestazione()));
        item.add(campi);
        // Descrizione estesa: tendina accordion a tutta larghezza, solo visibilita in
        // memoria
        String descrizione = annuncio.getDescrizione();
        if (descrizione != null && !descrizione.trim().isEmpty()) {
            FocusPanel toggleDescrizione = new FocusPanel();
            toggleDescrizione.addStyleName("annuncio-descrizione-toggle");
            toggleDescrizione.getElement().setAttribute("role", "button");
            toggleDescrizione.getElement().setAttribute("tabindex", "0");
            toggleDescrizione.getElement().setAttribute("aria-expanded", "false");

            Label testoToggle = new Label("Descrizione");
            testoToggle.addStyleName("annuncio-descrizione-toggle-testo");

            Label chevron = new Label("\u25B8");
            chevron.addStyleName("annuncio-descrizione-toggle-chevron");

            FlowPanel intestazione = new FlowPanel();
            intestazione.addStyleName("annuncio-descrizione-toggle-intestazione");
            intestazione.add(testoToggle);
            intestazione.add(chevron);
            toggleDescrizione.setWidget(intestazione);

            FlowPanel contenutoDescrizione = new FlowPanel();
            contenutoDescrizione.addStyleName("annuncio-descrizione-contenuto");
            contenutoDescrizione.setVisible(false);

            Label testoDescrizione = new Label(descrizione);
            testoDescrizione.addStyleName("annuncio-descrizione-testo");
            contenutoDescrizione.add(testoDescrizione);

            toggleDescrizione.addClickHandler(event -> toggleDescrizione(toggleDescrizione, contenutoDescrizione));
            toggleDescrizione.addKeyDownHandler(event -> {
                int codice = event.getNativeKeyCode();
                if (codice == KeyCodes.KEY_ENTER || codice == KeyCodes.KEY_SPACE) {
                    event.preventDefault();
                    toggleDescrizione(toggleDescrizione, contenutoDescrizione);
                }
            });

            item.add(toggleDescrizione);
            item.add(contenutoDescrizione);
        }

        // Azione "Proponi scambio": sul proprio annuncio il pulsante e' nascosto
        // e un messaggio spiega il perche'. Il controllo autoritativo resta sul server.
        FlowPanel azioni = new FlowPanel();
        azioni.addStyleName("annuncio-azioni");

        Button btnRecensioni = new Button("Vedi recensioni");
        btnRecensioni.addStyleName("btn-secondary");
        btnRecensioni.addStyleName("btn-sm");
        btnRecensioni.addClickHandler(event -> new RecensioniAnnuncioGui(utente, annuncio).mostra());
        azioni.add(btnRecensioni);

        // Profilo pubblico di chi propone lo scambio: rating e recensioni
        // ricevute aiutano a valutarlo prima di contattarlo (US-14)
        Button btnProfilo = new Button("Vedi profilo");
        btnProfilo.addStyleName("btn-secondary");
        btnProfilo.addStyleName("btn-sm");
        btnProfilo.addClickHandler(event -> new ProfiloGui(utente, annuncio.getIdUtente()).mostra());
        azioni.add(btnProfilo);

        if (utente.getEmail().equals(annuncio.getIdUtente())) {
            Label tuoAnnuncio = new Label("Questo è il tuo annuncio: non puoi richiedere uno scambio con te stesso");
            tuoAnnuncio.addStyleName("annuncio-proprio-nota");
            azioni.add(tuoAnnuncio);
        } else {
            Button btnProponi = new Button("Proponi scambio");
            btnProponi.addStyleName("btn-primary");
            btnProponi.addStyleName("btn-sm");

            // Con una richiesta gia' aperta su questo annuncio il pulsante resta
            // grigio e inattivo finche' lo scambio non viene completato (Issue #136)
            if (annunciConRichiestaAperta.contains(annuncio.getId())) {
                btnProponi.setText("Richiesta già inviata");
                btnProponi.setEnabled(false);
                btnProponi.setTitle("Hai già una richiesta su questo annuncio non ancora completata");
            } else {
                btnProponi.addClickHandler(event -> apriDettaglioAnnuncio(annuncio));
            }

            azioni.add(btnProponi);
        }

        item.add(azioni);

        return item;
    }

    // Apre/chiude la tendina descrizione e aggiorna lo stato accessibile
    private void toggleDescrizione(FocusPanel toggle, FlowPanel contenuto) {
        boolean apri = !contenuto.isVisible();
        contenuto.setVisible(apri);
        toggle.setStyleName("annuncio-descrizione-toggle-aperto", apri);
        toggle.getElement().setAttribute("aria-expanded", String.valueOf(apri));
    }

    /**
     * Apre il dettaglio di un annuncio altrui con il campo messaggio facoltativo
     * e l'azione "Proponi scambio". Il feedback di successo sostituisce il
     * contenuto
     * del dialog; gli errori RPC compaiono nella label rossa interna.
     */
    private void apriDettaglioAnnuncio(AnnuncioDTO annuncio) {
        DialogBox dialog = new DialogBox();
        dialog.setText(testoOppure(annuncio.getTitolo(), "Dettaglio annuncio"));
        dialog.setGlassEnabled(true);
        dialog.setAnimationEnabled(true);
        dialog.addStyleName("dettaglio-annuncio-dialog");

        TextArea messaggioArea = new TextArea();
        messaggioArea.addStyleName("campo-app");
        messaggioArea.setVisibleLines(3);
        messaggioArea.setWidth("100%");
        messaggioArea.getElement().setAttribute("placeholder", "Messaggio facoltativo per l'autore...");

        Label messaggioErrore = new Label();
        messaggioErrore.addStyleName("form-errore");
        messaggioErrore.setVisible(false);

        Button btnProponi = new Button("Proponi scambio");
        btnProponi.addStyleName("btn-primary");

        Button btnAnnulla = new Button("Annulla");
        btnAnnulla.addStyleName("btn-secondary");
        btnAnnulla.addClickHandler(event -> dialog.hide());

        FlowPanel contenuto = new FlowPanel();
        contenuto.addStyleName("dettaglio-annuncio-contenuto");

        contenuto.add(creaCampo("Autore", annuncio.getNomeAutore()));
        contenuto.add(creaCampo("Competenza offerta", annuncio.getCompetenzaOfferta()));
        contenuto.add(creaCampo("Disponibilità", annuncio.getDisponibilita()));
        contenuto.add(creaCampo("Controprestazione", annuncio.getControprestazione()));
        if (annuncio.getDescrizione() != null && !annuncio.getDescrizione().trim().isEmpty()) {
            contenuto.add(creaCampo("Descrizione", annuncio.getDescrizione()));
        }

        contenuto.add(creaEtichetta("Messaggio (facoltativo):"));
        contenuto.add(messaggioArea);
        contenuto.add(messaggioErrore);

        FlowPanel azioni = new FlowPanel();
        azioni.addStyleName("profile-form-azioni");
        btnProponi.addClickHandler(
                event -> inviaRichiestaScambio(annuncio, messaggioArea, messaggioErrore, btnProponi, dialog));
        azioni.add(btnAnnulla);
        azioni.add(btnProponi);
        contenuto.add(azioni);

        dialog.setWidget(contenuto);
        dialog.center();
        dialog.show();
    }

    /**
     * Invia la richiesta di scambio via RPC. Durante l'invio il pulsante viene
     * disabilitato con feedback "Invio in corso..."; in caso di errore il messaggio
     * del server compare nella label rossa e si puo' riprovare; in caso di successo
     * il dialog mostra la conferma.
     */
    private void inviaRichiestaScambio(AnnuncioDTO annuncio, TextArea messaggioArea,
            Label messaggioErrore, Button btnProponi, DialogBox dialog) {
        messaggioErrore.setVisible(false);

        // Il messaggio e' facoltativo: se vuoto si invia null (percorso valido nel
        // Database)
        String messaggio = messaggioArea.getText().trim();
        if (messaggio.isEmpty()) {
            messaggio = null;
        }

        btnProponi.setEnabled(false);
        btnProponi.setText("Invio in corso...");

        richiestaScambioService.inviaRichiestaScambio(annuncio.getId(), utente.getEmail(), messaggio,
                new AsyncCallback<RichiestaScambioDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        btnProponi.setEnabled(true);
                        btnProponi.setText("Proponi scambio");
                        messaggioErrore.setText(caught.getMessage());
                        messaggioErrore.setVisible(true);
                    }

                    @Override
                    public void onSuccess(RichiestaScambioDTO result) {
                        // Blocca subito il pulsante nella lista, senza attendere un refresh
                        annunciConRichiestaAperta.add(annuncio.getId());
                        mostraConfermaInvio(dialog);
                    }
                });
    }

    /**
     * Sostituisce il contenuto del dialog con il messaggio di conferma dell'invio.
     */
    private void mostraConfermaInvio(DialogBox dialog) {
        dialog.setText("Richiesta inviata");

        FlowPanel conferma = new FlowPanel();
        conferma.addStyleName("dettaglio-annuncio-contenuto");

        Label messaggio = new Label("Richiesta di scambio inviata!");
        messaggio.addStyleName("dettaglio-annuncio-successo");
        conferma.add(messaggio);

        FlowPanel azioni = new FlowPanel();
        azioni.addStyleName("profile-form-azioni");

        Button btnChiudi = new Button("Chiudi");
        btnChiudi.addStyleName("btn-primary");
        btnChiudi.addClickHandler(event -> {
            dialog.hide();
            // Ridisegna la lista: il pulsante dell'annuncio appena richiesto e' ora grigio
            caricaAnnunci();
        });

        azioni.add(btnChiudi);
        conferma.add(azioni);

        dialog.setWidget(conferma);
        dialog.center();
    }

    private Widget creaCampo(String etichetta, String valore) {
        FlowPanel campo = new FlowPanel();
        campo.addStyleName("annuncio-campo");

        Label lbl = new Label(etichetta);
        lbl.addStyleName("annuncio-campo-etichetta");
        campo.add(lbl);

        Label val = new Label(testoOppure(valore, "-"));
        val.addStyleName("annuncio-campo-valore");
        campo.add(val);

        return campo;
    }

    private Widget creaEtichetta(String testo) {
        Label etichetta = new Label(testo);
        etichetta.addStyleName("form-label");
        return etichetta;
    }

    private Widget creaMessaggioVuoto(String testo) {
        Label messaggio = new Label(testo);
        messaggio.addStyleName("annunci-vuoto");
        return messaggio;
    }

    private String testoOppure(String valore, String fallback) {
        return valore != null && !valore.trim().isEmpty() ? valore : fallback;
    }
}
