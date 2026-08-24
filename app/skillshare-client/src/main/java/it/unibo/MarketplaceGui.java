package it.unibo;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Schermata "Marketplace": tutti gli annunci pubblicati dagli utenti,
 * dal piu' recente al piu' vecchio.
 */
public class MarketplaceGui {

    private final UtenteDTO utente;
    private final MarketplaceServiceAsync marketplaceService = GWT.create(MarketplaceService.class);

    // Riempita dalla risposta RPC: al primo disegno mostra il caricamento
    private final FlowPanel listaAnnunci = new FlowPanel();

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

        listaAnnunci.clear();
        listaAnnunci.add(creaMessaggioVuoto("Caricamento annunci..."));
        card.add(listaAnnunci);

        return card;
    }

    /**
     * Chiede al server tutti gli annunci e ridisegna la lista.
     */
    private void caricaAnnunci() {
        marketplaceService.listaAnnunci(new AsyncCallback<List<AnnuncioDTO>>() {
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
            listaAnnunci.add(creaMessaggioVuoto("Nessun annuncio disponibile al momento"));
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
        campi.addStyleName("annuncio-campi");
        campi.add(creaCampo("Competenza offerta", annuncio.getCompetenzaOfferta()));
        campi.add(creaCampo("Controprestazione", annuncio.getControprestazione()));
        item.add(campi);

        return item;
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

    private Widget creaMessaggioVuoto(String testo) {
        Label messaggio = new Label(testo);
        messaggio.addStyleName("annunci-vuoto");
        return messaggio;
    }

    private String testoOppure(String valore, String fallback) {
        return valore != null && !valore.trim().isEmpty() ? valore : fallback;
    }
}
