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
 * Schermata "I miei annunci": elenco degli annunci pubblicati dall'utente.
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
            listaAnnunci.add(creaRigaAnnuncio(annuncio));
        }
    }

    /**
     * Riquadro di un singolo annuncio.
     * I pulsanti Modifica ed Elimina sono segnaposto per la Story "Gestione
     * Annuncio": volutamente senza listener, non fanno nulla al click.
     */
    private Widget creaRigaAnnuncio(AnnuncioDTO annuncio) {
        FlowPanel item = new FlowPanel();
        item.addStyleName("annuncio-item");

        Label titolo = new Label(testoOppure(annuncio.getTitolo(), ""));
        titolo.addStyleName("annuncio-titolo");
        item.add(titolo);

        FlowPanel campi = new FlowPanel();
        campi.addStyleName("annuncio-campi");
        campi.add(creaCampo("Competenza offerta", annuncio.getCompetenzaOfferta()));
        campi.add(creaCampo("Disponibilità", annuncio.getDisponibilita()));
        campi.add(creaCampo("Controprestazione", annuncio.getControprestazione()));
        item.add(campi);

        FlowPanel azioni = new FlowPanel();
        azioni.addStyleName("annuncio-azioni");

        Button btnModifica = new Button("Modifica");
        btnModifica.addStyleName("btn-secondary");
        btnModifica.addStyleName("btn-sm");
        azioni.add(btnModifica);

        Button btnElimina = new Button("Elimina");
        btnElimina.addStyleName("btn-secondary");
        btnElimina.addStyleName("btn-sm");
        azioni.add(btnElimina);

        item.add(azioni);
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
        return valore != null ? valore : fallback;
    }
}
