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
import com.google.gwt.user.client.ui.Widget;

/**
 * Schermata "Richieste": richieste di scambio ricevute dall'utente sui propri
 * annunci (con azioni Accetta/Rifiuta sulle PENDING) e richieste inviate con il
 * loro stato corrente. Due viste commutabili: "Ricevute" e "Inviate".
 */
public class RichiesteGui {

    private final UtenteDTO utente;
    private final RichiestaScambioServiceAsync richiestaScambioService = GWT.create(RichiestaScambioService.class);

    // Riempita dalla risposta RPC: al primo disegno mostra il caricamento
    private final FlowPanel listaRichieste = new FlowPanel();

    private final Button btnRicevute = new Button("Ricevute");
    private final Button btnInviate = new Button("Inviate");

    // True = vista "Ricevute" (default), false = vista "Inviate"
    private boolean vistaRicevute = true;

    public RichiesteGui(UtenteDTO utente) {
        this.utente = utente;
    }

    public void mostra() {
        FlowPanel pagina = new FlowPanel();
        pagina.add(new NavBar(utente, NavBar.SEZIONE_RICHIESTE).getWidget());

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

        caricaVistaCorrente();
    }

    /**
     * Card con titolo, toggle Ricevute/Inviate e la lista della vista corrente.
     */
    private Widget creaSezione() {
        FlowPanel card = new FlowPanel();
        card.addStyleName("profile-card");

        FlowPanel intestazione = new FlowPanel();
        intestazione.addStyleName("annunci-intestazione");

        Label titolo = new Label("Richieste");
        titolo.addStyleName("profile-sezione-titolo");
        intestazione.add(titolo);
        card.add(intestazione);

        FlowPanel toggle = new FlowPanel();
        toggle.addStyleName("richieste-tabs");

        btnRicevute.addClickHandler(event -> {
            vistaRicevute = true;
            caricaVistaCorrente();
        });
        btnInviate.addClickHandler(event -> {
            vistaRicevute = false;
            caricaVistaCorrente();
        });

        toggle.add(btnRicevute);
        toggle.add(btnInviate);
        card.add(toggle);

        listaRichieste.clear();
        listaRichieste.add(creaMessaggioVuoto("Caricamento richieste..."));
        card.add(listaRichieste);

        return card;
    }

    /**
     * Carica dal server la lista della vista corrente e ridisegna.
     * Lo switch di vista ricarica sempre: cosi' la vista "Inviate" riflette
     * lo stato aggiornato dalle decisioni prese nel frattempo dai creatori.
     */
    private void caricaVistaCorrente() {
        aggiornaStileToggle();

        listaRichieste.clear();
        listaRichieste.add(creaMessaggioVuoto("Caricamento richieste..."));

        if (vistaRicevute) {
            richiestaScambioService.richiesteRicevuteDaCreatore(utente.getEmail(),
                    new AsyncCallback<List<RichiestaScambioDTO>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            listaRichieste.clear();
                            listaRichieste.add(creaMessaggioVuoto("Impossibile caricare le richieste. Riprova."));
                        }

                        @Override
                        public void onSuccess(List<RichiestaScambioDTO> result) {
                            mostraRichiesteRicevute(result);
                        }
                    });
        } else {
            richiestaScambioService.richiesteInviateDaRichiedente(utente.getEmail(),
                    new AsyncCallback<List<RichiestaScambioDTO>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            listaRichieste.clear();
                            listaRichieste.add(creaMessaggioVuoto("Impossibile caricare le richieste. Riprova."));
                        }

                        @Override
                        public void onSuccess(List<RichiestaScambioDTO> result) {
                            mostraRichiesteInviate(result);
                        }
                    });
        }
    }

    // Evidenzia il tab della vista corrente
    private void aggiornaStileToggle() {
        btnRicevute.setStyleName("richieste-tab");
        btnInviate.setStyleName("richieste-tab");
        if (vistaRicevute) {
            btnRicevute.addStyleName("richieste-tab-attivo");
        } else {
            btnInviate.addStyleName("richieste-tab-attivo");
        }
    }

    private void mostraRichiesteRicevute(List<RichiestaScambioDTO> richieste) {
        listaRichieste.clear();

        if (richieste == null || richieste.isEmpty()) {
            listaRichieste.add(creaMessaggioVuoto("Nessuna richiesta ricevuta"));
            return;
        }

        for (RichiestaScambioDTO richiesta : richieste) {
            listaRichieste.add(new RigaRichiestaRicevuta(richiesta).widget());
        }
    }

    private void mostraRichiesteInviate(List<RichiestaScambioDTO> richieste) {
        listaRichieste.clear();

        if (richieste == null || richieste.isEmpty()) {
            listaRichieste.add(creaMessaggioVuoto("Non hai inviato richieste"));
            return;
        }

        for (RichiestaScambioDTO richiesta : richieste) {
            listaRichieste.add(creaRigaInviata(richiesta));
        }
    }

    /**
     * Riga di una richiesta inviata. Anche il richiedente e' partecipante allo
     * scambio, quindi vede completamento e recensione come il creatore.
     */
    private Widget creaRigaInviata(RichiestaScambioDTO richiesta) {
        FlowPanel item = new FlowPanel();
        item.addStyleName("annuncio-item");

        FlowPanel campi = new FlowPanel();
        campi.addStyleName("annuncio-campi");
        campi.add(creaCampo("Inviata a", richiesta.getIdCreatoreAnnuncio()));
        campi.add(creaCampo("Messaggio", richiesta.getMessaggio()));
        campi.add(creaCampo("Data", formattaData(richiesta.getDataCreazione())));
        item.add(campi);

        Label statoBadge = new Label(testoStato(richiesta.getStato()));
        statoBadge.addStyleName("richiesta-stato");
        statoBadge.addStyleName("richiesta-stato-" + nomeStato(richiesta.getStato()));
        item.add(statoBadge);

        // Chat disponibile finche' lo scambio e' in corso
        if (richiesta.getStato() == StatoRichiesta.ACCEPTED) {
            FlowPanel azioni = new FlowPanel();
            azioni.addStyleName("annuncio-azioni");

            Button btnChat = new Button("Apri Chat");
            btnChat.addStyleName("btn-primary");
            btnChat.addStyleName("btn-sm");
            btnChat.addClickHandler(event -> new ChatGui(utente, richiesta.getId()).mostra());

            azioni.add(btnChat);
            item.add(azioni);
        }

        item.add(creaBloccoCompletamento(richiesta, statoBadge));

        return item;
    }

    /**
     * Blocco completamento/recensione agganciato a una riga: quando lo scambio
     * viene completato aggiorna il badge di stato senza ricaricare la lista.
     */
    private Widget creaBloccoCompletamento(RichiestaScambioDTO richiesta, Label statoBadge) {
        CompletamentoRecensioneGui blocco = new CompletamentoRecensioneGui(utente, richiesta);
        blocco.setAscoltatoreStato(aggiornata -> {
            statoBadge.setText(testoStato(aggiornata.getStato()));
            statoBadge.setStyleName("richiesta-stato");
            statoBadge.addStyleName("richiesta-stato-" + nomeStato(aggiornata.getStato()));
        });
        return blocco.getWidget();
    }

    /**
     * Riga di una richiesta ricevuta, con azioni Accetta/Rifiuta sulle PENDING.
     * Conserva i riferimenti ai widget per l'update inline dopo la decisione.
     */
    private class RigaRichiestaRicevuta {

        private final FlowPanel item = new FlowPanel();
        private final Label statoBadge = new Label();
        private final FlowPanel azioni = new FlowPanel();
        private final Button btnAccetta = new Button("Accetta");
        private final Button btnRifiuta = new Button("Rifiuta");
        private final Button btnChat = new Button("Apri Chat");
        private RichiestaScambioDTO richiesta;
        // Conservato per aggiornarlo dopo Accetta/Rifiuta: e' lui a mostrare
        // il pulsante "Segna come completato"
        private final CompletamentoRecensioneGui blocco;

        RigaRichiestaRicevuta(RichiestaScambioDTO richiesta) {
            this.richiesta = richiesta;

            item.addStyleName("annuncio-item");

            FlowPanel campi = new FlowPanel();
            campi.addStyleName("annuncio-campi");
            campi.add(creaCampo("Richiedente", richiesta.getIdRichiedente()));
            campi.add(creaCampo("Messaggio", richiesta.getMessaggio()));
            campi.add(creaCampo("Data", formattaData(richiesta.getDataCreazione())));
            item.add(campi);

            statoBadge.addStyleName("richiesta-stato");
            item.add(statoBadge);

            azioni.addStyleName("annuncio-azioni");

            btnAccetta.addStyleName("btn-primary");
            btnAccetta.addStyleName("btn-sm");
            btnAccetta.addClickHandler(event -> aggiornaStato(StatoRichiesta.ACCEPTED, btnAccetta));

            btnRifiuta.addStyleName("btn-danger");
            btnRifiuta.addStyleName("btn-sm");
            btnRifiuta.addClickHandler(event -> aggiornaStato(StatoRichiesta.REJECTED, btnRifiuta));

            btnChat.addStyleName("btn-primary");
            btnChat.addStyleName("btn-sm");
            btnChat.addClickHandler(event -> new ChatGui(utente, this.richiesta.getId()).mostra());

            azioni.add(btnAccetta);
            azioni.add(btnRifiuta);
            azioni.add(btnChat);
            item.add(azioni);

            aggiornaBadge();

            // Il completamento aggiorna badge e pulsanti insieme: chiusa la chat,
            // la riga passa da sola alla fase di recensione
            blocco = new CompletamentoRecensioneGui(utente, richiesta);
            blocco.setAscoltatoreStato(aggiornata -> {
                this.richiesta = aggiornata;
                aggiornaBadge();
            });
            item.add(blocco.getWidget());
        }

        FlowPanel widget() {
            return item;
        }

        /**
         * Ridisegna badge e azioni in base allo stato corrente.
         * Le richieste gia' decise non mostrano Accetta/Rifiuta: la UI non offre
         * mai una ri-decisione, quindi il Database non ha bisogno di una guardia.
         * La chat prende il loro posto finche' lo scambio e' in corso.
         */
        private void aggiornaBadge() {
            StatoRichiesta stato = richiesta.getStato();
            statoBadge.setText(testoStato(stato));
            statoBadge.setStyleName("richiesta-stato");
            statoBadge.addStyleName("richiesta-stato-" + nomeStato(stato));

            btnAccetta.setVisible(stato == StatoRichiesta.PENDING);
            btnRifiuta.setVisible(stato == StatoRichiesta.PENDING);
            btnChat.setVisible(stato == StatoRichiesta.ACCEPTED);

            azioni.setVisible(stato == StatoRichiesta.PENDING || stato == StatoRichiesta.ACCEPTED);
        }

        private void aggiornaStato(StatoRichiesta nuovoStato, Button pulsante) {
            pulsante.setEnabled(false);
            pulsante.setText("Invio...");

            AsyncCallback<RichiestaScambioDTO> callback = new AsyncCallback<RichiestaScambioDTO>() {
                @Override
                public void onFailure(Throwable caught) {
                    pulsante.setEnabled(true);
                    pulsante.setText(nuovoStato == StatoRichiesta.ACCEPTED ? "Accetta" : "Rifiuta");
                    Window.alert(caught.getMessage());
                }

                @Override
                public void onSuccess(RichiestaScambioDTO result) {
                    // Update inline della riga, senza ricaricare la lista
                    richiesta = result;
                    aggiornaBadge();
                    // Il blocco ha una copia propria della richiesta: senza questa
                    // riga resterebbe sullo stato precedente e il pulsante
                    // "Segna come completato" comparirebbe solo dopo un reload
                    blocco.aggiornaRichiesta(result);
                }
            };

            if (nuovoStato == StatoRichiesta.ACCEPTED) {
                richiestaScambioService.accetta(richiesta.getId(), utente.getEmail(), callback);
            } else {
                richiestaScambioService.rifiuta(richiesta.getId(), utente.getEmail(), callback);
            }
        }
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

    private String testoStato(StatoRichiesta stato) {
        if (stato == null) {
            return "-";
        }
        switch (stato) {
            case ACCEPTED:
                return "Accettata";
            case REJECTED:
                return "Rifiutata";
            case COMPLETED:
                return "Completata";
            default:
                return "In attesa";
        }
    }

    private String nomeStato(StatoRichiesta stato) {
        return stato == null ? "pending" : stato.name().toLowerCase();
    }

    // Data e ora leggibili: GWT emula java.util.Date ma non java.text.DateFormat
    private String formattaData(long millis) {
        if (millis <= 0) {
            return "-";
        }
        java.util.Date data = new java.util.Date(millis);
        return dueCifre(data.getDate()) + "/" + dueCifre(data.getMonth() + 1) + "/" + (1900 + data.getYear())
                + " " + dueCifre(data.getHours()) + ":" + dueCifre(data.getMinutes());
    }

    private String dueCifre(int valore) {
        return valore < 10 ? "0" + valore : String.valueOf(valore);
    }
}
