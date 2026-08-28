package it.unibo;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Schermata "Recensioni dell'annuncio": tutte le recensioni collegate a un
 * annuncio, aperta dal dettaglio annuncio del Marketplace.
 */
public class RecensioniAnnuncioGui {

    private final UtenteDTO utente;
    private final AnnuncioDTO annuncio;
    private final RecensioneServiceAsync recensioneService = GWT.create(RecensioneService.class);

    // Riempita dalla risposta RPC: al primo disegno mostra il caricamento
    private final FlowPanel listaRecensioni = new FlowPanel();

    public RecensioniAnnuncioGui(UtenteDTO utente, AnnuncioDTO annuncio) {
        this.utente = utente;
        this.annuncio = annuncio;
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

        caricaRecensioni();
    }

    private Widget creaSezione() {
        FlowPanel card = new FlowPanel();
        card.addStyleName("profile-card");

        FlowPanel intestazione = new FlowPanel();
        intestazione.addStyleName("annunci-intestazione");

        Label titolo = new Label("Recensioni");
        titolo.addStyleName("profile-sezione-titolo");
        intestazione.add(titolo);

        Button btnIndietro = new Button("Torna al Marketplace");
        btnIndietro.addStyleName("btn-secondary");
        btnIndietro.addStyleName("btn-sm");
        btnIndietro.addClickHandler(event -> new MarketplaceGui(utente).mostra());
        intestazione.add(btnIndietro);

        card.add(intestazione);

        // Di quale annuncio stiamo leggendo le recensioni
        Label sottotitolo = new Label(testoOppure(annuncio.getTitolo(), "Annuncio"));
        sottotitolo.addStyleName("annuncio-titolo");
        card.add(sottotitolo);

        listaRecensioni.clear();
        listaRecensioni.add(creaMessaggioVuoto("Caricamento recensioni..."));
        card.add(listaRecensioni);

        return card;
    }

    private void caricaRecensioni() {
        recensioneService.recensioniPerAnnuncio(annuncio.getId(), new AsyncCallback<List<RecensioneDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                listaRecensioni.clear();
                listaRecensioni.add(creaMessaggioVuoto("Impossibile caricare le recensioni. Riprova."));
            }

            @Override
            public void onSuccess(List<RecensioneDTO> result) {
                mostraRecensioni(result);
            }
        });
    }

    private void mostraRecensioni(List<RecensioneDTO> recensioni) {
        listaRecensioni.clear();

        if (recensioni == null || recensioni.isEmpty()) {
            listaRecensioni.add(creaMessaggioVuoto("Nessuna recensione per questo annuncio"));
            return;
        }

        for (RecensioneDTO recensione : recensioni) {
            listaRecensioni.add(creaRigaRecensione(recensione));
        }
    }

    /**
     * Riquadro di una recensione: autore in cima, poi stelle e,
     * solo se presente, il commento.
     */
    private Widget creaRigaRecensione(RecensioneDTO recensione) {
        FlowPanel item = new FlowPanel();
        item.addStyleName("annuncio-item");

        item.add(creaIntestazioneAutore(recensione));

        Label stelle = new Label(Stelle.perVoto(recensione.getVoto()));
        stelle.addStyleName("recensione-stelle");
        item.add(stelle);

        // Il commento e' facoltativo: se manca si omette la riga, senza placeholder
        if (recensione.getCommento() != null && !recensione.getCommento().trim().isEmpty()) {
            Label commento = new Label(recensione.getCommento());
            commento.addStyleName("recensione-commento");
            item.add(commento);
        }

        return item;
    }

    /**
     * Chi ha scritto la recensione. Se e' l'autore dell'annuncio lo si segnala:
     * la sua voce pesa diversamente da quella di chi ha ricevuto il servizio.
     */
    private Widget creaIntestazioneAutore(RecensioneDTO recensione) {
        FlowPanel intestazione = new FlowPanel();
        intestazione.addStyleName("recensione-autore-riga");

        Label nome = new Label(testoOppure(recensione.getNomeAutore(),
                testoOppure(recensione.getIdAutore(), "Utente sconosciuto")));
        nome.addStyleName("recensione-autore");
        intestazione.add(nome);

        if (recensione.getIdAutore() != null
                && recensione.getIdAutore().equals(annuncio.getIdUtente())) {
            Label badge = new Label("Autore dell'annuncio");
            badge.addStyleName("recensione-autore-badge");
            intestazione.add(badge);
        }

        return intestazione;
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
