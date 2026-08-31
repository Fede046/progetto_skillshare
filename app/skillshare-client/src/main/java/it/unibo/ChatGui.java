package it.unibo;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Schermata della Chat interna con pulsante per tornare indietro alla sezione
 * richieste.
 */
public class ChatGui {

    private final UtenteDTO utente;
    private final String idRichiestaScambio;
    private final ChatServiceAsync chatService = GWT.create(ChatService.class);

    private final FlowPanel listaMessaggi = new FlowPanel();
    private final TextBox inputMessaggio = new TextBox();
    private final Label lblErrore = new Label();

    public ChatGui(UtenteDTO utente, String idRichiestaScambio) {
        this.utente = utente;
        this.idRichiestaScambio = idRichiestaScambio;
    }

    public void mostra() {
        RootPanel.get().clear();

        FlowPanel pagina = new FlowPanel();
        pagina.add(new NavBar(utente, NavBar.SEZIONE_RICHIESTE).getWidget());

        FlowPanel contenuto = new FlowPanel();
        contenuto.addStyleName("app-page");

        FlowPanel card = new FlowPanel();
        card.addStyleName("profile-card");
        card.getElement().getStyle().setProperty("maxWidth", "700px");
        card.getElement().getStyle().setProperty("margin", "0 auto");

        // Intestazione con pulsante Indietro e Titolo allineati
        FlowPanel intestazione = new FlowPanel();
        intestazione.getElement().getStyle().setProperty("display", "flex");
        intestazione.getElement().getStyle().setProperty("alignItems", "center");
        intestazione.getElement().getStyle().setProperty("gap", "15px");
        intestazione.getElement().getStyle().setProperty("marginBottom", "15px");

        Button btnIndietro = new Button("← Indietro");
        btnIndietro.addStyleName("btn-secondary");
        btnIndietro.addStyleName("btn-sm");
        btnIndietro.addClickHandler(event -> new RichiesteGui(utente).mostra());

        HTML titolo = new HTML("<h2>Chat dello Scambio</h2>");
        titolo.getElement().getStyle().setProperty("margin", "0");

        intestazione.add(btnIndietro);
        intestazione.add(titolo);
        card.add(intestazione);

        // Contenitore messaggi con altezza fissa e scroll verticale pulito
        listaMessaggi.getElement().getStyle().setHeight(380, Unit.PX);
        listaMessaggi.getElement().getStyle().setProperty("overflowY", "auto");
        listaMessaggi.getElement().getStyle().setProperty("backgroundColor", "#F9F9F9");
        listaMessaggi.getElement().getStyle().setProperty("border", "1px solid #DDD");
        listaMessaggi.getElement().getStyle().setProperty("borderRadius", "8px");
        listaMessaggi.getElement().getStyle().setProperty("padding", "15px");
        listaMessaggi.getElement().getStyle().setProperty("marginBottom", "15px");
        listaMessaggi.add(creaMessaggioStato("Caricamento messaggi..."));
        card.add(listaMessaggi);

        // Banner errori interno
        lblErrore.addStyleName("form-errore");
        lblErrore.setVisible(false);
        card.add(lblErrore);

        // Form di invio in basso
        FlowPanel formInvio = new FlowPanel();
        formInvio.getElement().getStyle().setProperty("display", "flex");
        formInvio.getElement().getStyle().setProperty("gap", "10px");

        // Aspetto dei campi condiviso con il resto della piattaforma;
        // qui resta inline solo il flex, che dipende da questo layout
        inputMessaggio.addStyleName("campo-app");
        inputMessaggio.getElement().getStyle().setProperty("flex", "1");
        inputMessaggio.getElement().setAttribute("placeholder", "Scrivi un messaggio...");

        Button btnInvia = new Button("Invia");
        btnInvia.addStyleName("btn-primary");
        btnInvia.getElement().getStyle().setProperty("padding", "10px 20px");

        btnInvia.addClickHandler(event -> inviaMessaggio());
        inputMessaggio.addKeyPressHandler(event -> {
            if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                inviaMessaggio();
            }
        });

        formInvio.add(inputMessaggio);
        formInvio.add(btnInvia);
        card.add(formInvio);

        contenuto.add(card);
        pagina.add(contenuto);
        RootPanel.get().add(pagina);

        Document.get().getBody().getStyle().setProperty("backgroundColor", "#E8E8E8");
        Document.get().getBody().getStyle().setProperty("margin", "0");
        Document.get().getBody().getStyle().setProperty("fontFamily", "sans-serif");

        caricaMessaggi();
    }

    private void mostraErrore(String testo) {
        lblErrore.setText(testo);
        lblErrore.setVisible(true);
    }

    private void caricaMessaggi() {
        chatService.getMessaggi(idRichiestaScambio, utente.getEmail(),
                new AsyncCallback<List<MessaggioDTO>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        mostraErrore("Errore accesso chat: "
                                + (caught.getMessage() != null ? caught.getMessage() : "Non autorizzato"));
                    }

                    @Override
                    public void onSuccess(List<MessaggioDTO> result) {
                        mostraMessaggi(result);
                    }
                });
    }

    private void mostraMessaggi(List<MessaggioDTO> messaggi) {
        listaMessaggi.clear();

        if (messaggi == null || messaggi.isEmpty()) {
            listaMessaggi.add(creaMessaggioStato("Nessun messaggio nella chat. Inizia la conversazione!"));
            return;
        }

        for (MessaggioDTO messaggio : messaggi) {
            listaMessaggi.add(creaRigaMessaggio(messaggio));
        }

        // Scroll automatico in fondo alla chat
        listaMessaggi.getElement().setScrollTop(listaMessaggi.getElement().getScrollHeight());
    }

    private Widget creaRigaMessaggio(MessaggioDTO m) {
        boolean mioMessaggio = m.getIdMittente() != null && m.getIdMittente().equals(utente.getEmail());

        FlowPanel bolla = new FlowPanel();
        bolla.getElement().getStyle().setProperty("maxWidth", "75%");
        bolla.getElement().getStyle().setProperty("padding", "10px 14px");
        bolla.getElement().getStyle().setProperty("borderRadius", "12px");
        bolla.getElement().getStyle().setProperty("marginBottom", "10px");
        bolla.getElement().getStyle().setProperty("wordBreak", "break-word");

        if (mioMessaggio) {
            bolla.getElement().getStyle().setProperty("marginLeft", "auto");
            bolla.getElement().getStyle().setProperty("backgroundColor", "#800000"); // Bordeaux principale
            bolla.getElement().getStyle().setProperty("color", "white");
        } else {
            bolla.getElement().getStyle().setProperty("marginRight", "auto");
            bolla.getElement().getStyle().setProperty("backgroundColor", "#E2E2E2");
            bolla.getElement().getStyle().setProperty("color", "#333");
        }

        Label mittente = new Label(mioMessaggio ? "Tu" : m.getIdMittente());
        mittente.getElement().getStyle().setProperty("fontSize", "11px");
        mittente.getElement().getStyle().setProperty("opacity", "0.8");
        mittente.getElement().getStyle().setProperty("marginBottom", "4px");

        Label testo = new Label(m.getTesto());
        testo.getElement().getStyle().setProperty("fontSize", "14px");

        bolla.add(mittente);
        bolla.add(testo);
        return bolla;
    }

    private void inviaMessaggio() {
        String testo = inputMessaggio.getText().trim();
        if (testo.isEmpty()) {
            return;
        }

        lblErrore.setVisible(false);
        MessaggioDTO nuovoMessaggio = new MessaggioDTO();
        nuovoMessaggio.setIdRichiestaScambio(idRichiestaScambio);
        nuovoMessaggio.setIdMittente(utente.getEmail());
        nuovoMessaggio.setTesto(testo);

        chatService.inviaMessaggio(nuovoMessaggio, new AsyncCallback<MessaggioDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                mostraErrore("Impossibile inviare: " + caught.getMessage());
            }

            @Override
            public void onSuccess(MessaggioDTO result) {
                inputMessaggio.setText("");
                if (listaMessaggi.getWidgetCount() == 1 && listaMessaggi.getWidget(0) instanceof Label) {
                    listaMessaggi.clear();
                }
                listaMessaggi.add(creaRigaMessaggio(result));
                listaMessaggi.getElement().setScrollTop(listaMessaggi.getElement().getScrollHeight());
            }
        });
    }

    private Widget creaMessaggioStato(String testo) {
        Label msg = new Label(testo);
        msg.getElement().getStyle().setProperty("textAlign", "center");
        msg.getElement().getStyle().setProperty("color", "#777");
        msg.getElement().getStyle().setProperty("marginTop", "130px");
        return msg;
    }
}