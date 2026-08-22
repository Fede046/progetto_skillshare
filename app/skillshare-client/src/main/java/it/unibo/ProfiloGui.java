package it.unibo;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class ProfiloGui {

    private UtenteDTO utente;
    private final AnnuncioServiceAsync annuncioService = GWT.create(AnnuncioService.class);

    // Riempito dalla risposta RPC: al primo disegno mostra il caricamento
    private final FlowPanel listaAnnunci = new FlowPanel();

    // Passiamo l'utente loggato al costruttore
    public ProfiloGui(UtenteDTO utente) {
        this.utente = utente;
    }

    public void mostra() {
        FlowPanel pagina = new FlowPanel();

        // Barra di navigazione orizzontale: questa e' la home dopo il login
        pagina.add(new NavBar(utente, NavBar.SEZIONE_PROFILO).getWidget());

        FlowPanel contenuto = new FlowPanel();
        contenuto.addStyleName("app-page");
        contenuto.add(creaIntestazione());
        contenuto.add(creaColonne());
        contenuto.add(creaSezioneAnnunci());
        pagina.add(contenuto);

        // Pulizia e rendering
        RootPanel.get().clear();
        RootPanel.get().add(pagina);

        // Stile base della pagina
        Document.get().getBody().getStyle().setProperty("backgroundColor", "#E8E8E8");
        Document.get().getBody().getStyle().setProperty("margin", "0");
        Document.get().getBody().getStyle().setProperty("fontFamily", "sans-serif");

        caricaAnnunci();
    }

    /**
     * Sezione "I miei annunci": intestazione con l'azione di creazione
     * e la lista, che viene riempita dalla chiamata RPC.
     */
    private Widget creaSezioneAnnunci() {
        FlowPanel card = new FlowPanel();
        card.addStyleName("profile-card");

        FlowPanel intestazione = new FlowPanel();
        intestazione.addStyleName("annunci-intestazione");
        intestazione.add(creaTitoloSezione("I miei annunci"));

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
                mostraAnnunci(null);
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

    /**
     * Intestazione: fascia bordeaux, avatar che la scavalca, dati e azione a fianco.
     */
    private Widget creaIntestazione() {
        FlowPanel hero = new FlowPanel();
        hero.addStyleName("profile-hero");

        FlowPanel banda = new FlowPanel();
        banda.addStyleName("profile-hero-banda");
        hero.add(banda);

        FlowPanel corpo = new FlowPanel();
        corpo.addStyleName("profile-hero-corpo");

        FlowPanel riga = new FlowPanel();
        riga.addStyleName("profile-hero-riga");

        FlowPanel avatar = new FlowPanel();
        avatar.addStyleName("profile-hero-avatar");
        avatar.add(creaAvatar());
        riga.add(avatar);

        FlowPanel dati = new FlowPanel();
        dati.addStyleName("profile-header-dati");

        Label nome = new Label(testoOppure(utente.getNome(), "") + " " + testoOppure(utente.getCognome(), ""));
        nome.addStyleName("profile-nome");
        dati.add(nome);

        Label email = new Label(testoOppure(utente.getEmail(), ""));
        email.addStyleName("profile-email");
        dati.add(email);

        riga.add(dati);

        FlowPanel azioni = new FlowPanel();
        azioni.addStyleName("profile-hero-azioni");

        Button btnModifica = new Button("Modifica Profilo");
        btnModifica.addStyleName("btn-primary");
        btnModifica.addClickHandler(event -> new ModificaProfiloGui(utente).mostra());
        azioni.add(btnModifica);

        riga.add(azioni);
        corpo.add(riga);
        hero.add(corpo);
        return hero;
    }

    /**
     * Biografia e competenze affiancate su due colonne.
     */
    private Widget creaColonne() {
        FlowPanel colonne = new FlowPanel();
        colonne.addStyleName("profile-colonne");
        colonne.add(creaColonnaBio());
        colonne.add(creaColonnaCompetenze());
        return colonne;
    }

    private Widget creaColonnaBio() {
        FlowPanel colonna = new FlowPanel();
        colonna.addStyleName("profile-colonna");

        FlowPanel card = new FlowPanel();
        card.addStyleName("profile-card");
        card.add(creaTitoloSezione("Biografia"));

        String bio = utente.getBio();
        boolean bioPresente = bio != null && !bio.trim().isEmpty();

        Label testo = new Label(bioPresente ? bio : "Nessuna biografia inserita.");
        testo.addStyleName(bioPresente ? "profile-testo" : "profile-bio");
        card.add(testo);

        colonna.add(card);
        return colonna;
    }

    private Widget creaColonnaCompetenze() {
        FlowPanel colonna = new FlowPanel();
        colonna.addStyleName("profile-colonna");

        FlowPanel card = new FlowPanel();
        card.addStyleName("profile-card");
        card.add(creaTitoloSezione("Competenze"));

        FlowPanel badges = new FlowPanel();
        badges.addStyleName("tag-badges-container");

        if (utente.getTagCompetenza() == null || utente.getTagCompetenza().isEmpty()) {
            Label vuoto = new Label("Nessuna competenza aggiunta.");
            vuoto.addStyleName("profile-bio");
            badges.add(vuoto);
        } else {
            for (String tag : utente.getTagCompetenza()) {
                Label badge = new Label(tag);
                badge.addStyleName("skill-badge");
                badges.add(badge);
            }
        }

        card.add(badges);
        colonna.add(card);
        return colonna;
    }

    private Widget creaTitoloSezione(String testo) {
        Label titolo = new Label(testo);
        titolo.addStyleName("profile-sezione-titolo");
        return titolo;
    }

    /**
     * Costruisce la foto profilo. Se l'URL non e' stato impostato - oppure se
     * l'immagine non riesce a caricarsi - viene mostrato un placeholder circolare
     * con le iniziali dell'utente.
     */
    private Widget creaAvatar() {
        FlowPanel contenitore = new FlowPanel();
        contenitore.addStyleName("profile-avatar-wrapper");

        String photoUrl = utente.getPhotoUrl();
        if (photoUrl == null || photoUrl.trim().isEmpty()) {
            contenitore.add(creaPlaceholderAvatar());
            return contenitore;
        }

        Image foto = new Image(photoUrl.trim());
        foto.addStyleName("profile-avatar");
        // Fallback nel caso l'URL sia rotto o l'immagine non sia raggiungibile
        foto.addErrorHandler(event -> {
            contenitore.clear();
            contenitore.add(creaPlaceholderAvatar());
        });
        contenitore.add(foto);
        return contenitore;
    }

    /**
     * Placeholder circolare con le iniziali di nome e cognome.
     */
    private Widget creaPlaceholderAvatar() {
        String iniziali = "";
        if (utente.getNome() != null && !utente.getNome().trim().isEmpty()) {
            iniziali += utente.getNome().trim().charAt(0);
        }
        if (utente.getCognome() != null && !utente.getCognome().trim().isEmpty()) {
            iniziali += utente.getCognome().trim().charAt(0);
        }
        if (iniziali.isEmpty()) {
            iniziali = "?";
        }

        Label placeholder = new Label(iniziali.toUpperCase());
        placeholder.addStyleName("profile-avatar");
        placeholder.addStyleName("profile-avatar-placeholder");
        placeholder.setTitle("Nessuna foto profilo caricata");
        return placeholder;
    }

    private String testoOppure(String valore, String fallback) {
        return valore != null ? valore : fallback;
    }
}
