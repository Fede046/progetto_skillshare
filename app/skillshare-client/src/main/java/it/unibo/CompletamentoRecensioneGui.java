package it.unibo;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 * Blocco "completamento e recensione" di una singola richiesta di scambio,
 * usato dentro le righe della schermata Richieste.
 *
 * <p>Mostra, a seconda dello stato: il pulsante per completare lo scambio,
 * il form di recensione, oppure la recensione gia' lasciata dall'utente.</p>
 */
public class CompletamentoRecensioneGui {

    private final UtenteDTO utente;
    private final RichiestaScambioServiceAsync richiestaService = GWT.create(RichiestaScambioService.class);
    private final RecensioneServiceAsync recensioneService = GWT.create(RecensioneService.class);

    private RichiestaScambioDTO richiesta;

    private final FlowPanel pannello = new FlowPanel();

    // Voto selezionato nel form: 0 finche' l'utente non sceglie
    private int votoScelto;

    public CompletamentoRecensioneGui(UtenteDTO utente, RichiestaScambioDTO richiesta) {
        this.utente = utente;
        this.richiesta = richiesta;
        pannello.addStyleName("completamento-blocco");
        ridisegna();
    }

    public Widget getWidget() {
        return pannello;
    }

    /**
     * Applica al blocco la richiesta aggiornata arrivata dal server e lo ridisegna.
     * Serve quando lo stato cambia fuori di qui, per esempio dopo che il creatore
     * ha accettato o rifiutato: senza questo il blocco resterebbe fermo alla copia
     * ricevuta alla costruzione e il pulsante di completamento comparirebbe solo
     * dopo un reload della pagina.
     */
    public void aggiornaRichiesta(RichiestaScambioDTO aggiornata) {
        if (aggiornata == null) {
            return;
        }
        this.richiesta = aggiornata;
        ridisegna();
    }

    /**
     * Ridisegna il blocco in base allo stato corrente della richiesta.
     */
    private void ridisegna() {
        pannello.clear();

        StatoRichiesta stato = richiesta.getStato();

        // Regola condivisa con il server: accettata e utente fra i partecipanti
        if (richiesta.completabileDa(utente.getEmail())) {
            pannello.setVisible(true);
            pannello.add(creaPulsanteCompleta());
            return;
        }

        if (stato == StatoRichiesta.COMPLETED) {
            pannello.setVisible(true);
            // Il form compare solo se l'utente non ha gia' recensito: lo si sa
            // solo dopo aver chiesto al server le recensioni dell'annuncio
            pannello.add(creaAttesa("Caricamento recensione..."));
            caricaMiaRecensione();
            return;
        }

        // PENDING o REJECTED: qui non c'e' nulla da mostrare
        pannello.setVisible(false);
    }

    // --- Completamento dello scambio ---

    private Widget creaPulsanteCompleta() {
        FlowPanel azioni = new FlowPanel();
        azioni.addStyleName("completamento-azioni");

        Button btnCompleta = new Button("Segna come completato");
        btnCompleta.addStyleName("btn-primary");
        btnCompleta.addStyleName("btn-sm");
        btnCompleta.addClickHandler(event -> completa(btnCompleta));

        azioni.add(btnCompleta);
        return azioni;
    }

    private void completa(Button pulsante) {
        pulsante.setEnabled(false);
        pulsante.setText("Invio...");

        richiestaService.completa(richiesta.getId(), utente.getEmail(),
                new AsyncCallback<RichiestaScambioDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        pulsante.setEnabled(true);
                        pulsante.setText("Segna come completato");
                        Window.alert(caught.getMessage());
                    }

                    @Override
                    public void onSuccess(RichiestaScambioDTO result) {
                        // Update inline: la riga passa a COMPLETATA e compare il form
                        richiesta = result;
                        notificaStatoAggiornato(result);
                        ridisegna();
                    }
                });
    }

    /**
     * Punto di aggancio per chi ospita il blocco: la riga della schermata
     * Richieste aggiorna il proprio badge di stato senza ricaricare la lista.
     */
    private AscoltatoreStato ascoltatore;

    public void setAscoltatoreStato(AscoltatoreStato ascoltatore) {
        this.ascoltatore = ascoltatore;
    }

    private void notificaStatoAggiornato(RichiestaScambioDTO aggiornata) {
        if (ascoltatore != null) {
            ascoltatore.statoAggiornato(aggiornata);
        }
    }

    /**
     * Avvisa che lo stato della richiesta e' cambiato.
     */
    public interface AscoltatoreStato {
        void statoAggiornato(RichiestaScambioDTO richiesta);
    }

    // --- Recensione ---

    /**
     * Cerca fra le recensioni dell'annuncio quella scritta da questo utente
     * per questa richiesta: se c'e' la mostra, altrimenti propone il form.
     */
    private void caricaMiaRecensione() {
        recensioneService.recensioniPerAnnuncio(richiesta.getIdAnnuncio(),
                new AsyncCallback<List<RecensioneDTO>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        pannello.clear();
                        pannello.add(creaAttesa("Impossibile caricare la recensione. Riprova."));
                    }

                    @Override
                    public void onSuccess(List<RecensioneDTO> result) {
                        RecensioneDTO mia = trovaMiaRecensione(result);
                        pannello.clear();
                        pannello.add(mia != null ? creaRecensioneInviata(mia) : creaFormRecensione());
                    }
                });
    }

    private RecensioneDTO trovaMiaRecensione(List<RecensioneDTO> recensioni) {
        if (recensioni == null) {
            return null;
        }
        for (RecensioneDTO recensione : recensioni) {
            if (richiesta.getId().equals(recensione.getIdRichiestaScambio())
                    && utente.getEmail().equals(recensione.getIdAutore())) {
                return recensione;
            }
        }
        return null;
    }

    /**
     * Recensione gia' inviata: sola lettura, nessun form riproponibile.
     */
    private Widget creaRecensioneInviata(RecensioneDTO recensione) {
        FlowPanel blocco = new FlowPanel();
        blocco.addStyleName("recensione-inviata");

        Label titolo = new Label("La tua recensione");
        titolo.addStyleName("annuncio-campo-etichetta");
        blocco.add(titolo);

        Label stelle = new Label(Stelle.perVoto(recensione.getVoto()));
        stelle.addStyleName("recensione-stelle");
        blocco.add(stelle);

        // Il commento e' facoltativo: se manca non lasciamo una riga vuota
        if (recensione.getCommento() != null && !recensione.getCommento().trim().isEmpty()) {
            Label commento = new Label(recensione.getCommento());
            commento.addStyleName("recensione-commento");
            blocco.add(commento);
        }

        return blocco;
    }

    /**
     * Form di recensione: stelle cliccabili, commento facoltativo e invio.
     */
    private Widget creaFormRecensione() {
        FlowPanel form = new FlowPanel();
        form.addStyleName("recensione-form");

        Label titolo = new Label("Lascia una recensione");
        titolo.addStyleName("annuncio-campo-etichetta");
        form.add(titolo);

        votoScelto = 0;
        SelettoreStelle selettore = new SelettoreStelle();
        form.add(selettore.getWidget());

        TextArea commentoArea = new TextArea();
        commentoArea.setVisibleLines(2);
        commentoArea.setWidth("100%");
        commentoArea.getElement().setAttribute("placeholder", "Commento facoltativo...");
        form.add(commentoArea);

        Label messaggioErrore = new Label();
        messaggioErrore.addStyleName("form-errore");
        messaggioErrore.setVisible(false);
        form.add(messaggioErrore);

        FlowPanel azioni = new FlowPanel();
        azioni.addStyleName("completamento-azioni");

        Button btnInvia = new Button("Invia recensione");
        btnInvia.addStyleName("btn-primary");
        btnInvia.addStyleName("btn-sm");
        btnInvia.addClickHandler(event -> inviaRecensione(commentoArea, messaggioErrore, btnInvia));
        azioni.add(btnInvia);

        form.add(azioni);
        return form;
    }

    private void inviaRecensione(TextArea commentoArea, Label messaggioErrore, Button btnInvia) {
        if (votoScelto < Stelle.VOTO_MINIMO) {
            messaggioErrore.setText("Scegli un voto da 1 a 5 prima di inviare la recensione.");
            messaggioErrore.setVisible(true);
            return;
        }
        messaggioErrore.setVisible(false);

        RecensioneDTO recensione = new RecensioneDTO();
        recensione.setIdRichiestaScambio(richiesta.getId());
        recensione.setIdAutore(utente.getEmail());
        recensione.setIdDestinatario(destinatario());
        recensione.setVoto(votoScelto);
        recensione.setCommento(commentoArea.getText().trim());

        btnInvia.setEnabled(false);
        btnInvia.setText("Invio...");

        recensioneService.lascia(recensione, new AsyncCallback<RecensioneDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                btnInvia.setEnabled(true);
                btnInvia.setText("Invia recensione");
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(RecensioneDTO result) {
                // Il form lascia il posto alla recensione appena inviata
                pannello.clear();
                pannello.add(creaRecensioneInviata(result));
            }
        });
    }

    // L'altro partecipante allo scambio rispetto a chi sta scrivendo
    private String destinatario() {
        return utente.getEmail().equals(richiesta.getIdRichiedente())
                ? richiesta.getIdCreatoreAnnuncio()
                : richiesta.getIdRichiedente();
    }

    private Widget creaAttesa(String testo) {
        Label messaggio = new Label(testo);
        messaggio.addStyleName("annunci-vuoto");
        return messaggio;
    }

    /**
     * Cinque stelle cliccabili: al passaggio del mouse e al click si riempiono
     * fino a quella scelta.
     */
    private class SelettoreStelle {

        private final FlowPanel riga = new FlowPanel();
        private final Label[] stelle = new Label[Stelle.VOTO_MASSIMO];

        SelettoreStelle() {
            riga.addStyleName("stelle-selettore");

            for (int i = 0; i < Stelle.VOTO_MASSIMO; i++) {
                final int voto = i + 1;
                Label stella = new Label("★");
                stella.addStyleName("stella");
                stella.setTitle(voto + " su " + Stelle.VOTO_MASSIMO);
                stella.addClickHandler(event -> {
                    votoScelto = voto;
                    aggiorna();
                });
                stelle[i] = stella;
                riga.add(stella);
            }

            aggiorna();
        }

        // Colora in bordeaux le stelle fino al voto scelto
        private void aggiorna() {
            for (int i = 0; i < stelle.length; i++) {
                if (i < votoScelto) {
                    stelle[i].addStyleName("stella-attiva");
                } else {
                    stelle[i].removeStyleName("stella-attiva");
                }
            }
        }

        Widget getWidget() {
            return riga;
        }
    }
}
