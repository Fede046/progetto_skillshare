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

    private final ReputazioneServiceAsync reputazioneService = GWT.create(ReputazioneService.class);

    // Riempiti dalle risposte RPC: al primo disegno mostrano il caricamento
    private final FlowPanel rating = new FlowPanel();
    private final FlowPanel listaRecensioni = new FlowPanel();

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
        contenuto.add(creaSezioneRecensioni());
        pagina.add(contenuto);

        // Pulizia e rendering
        RootPanel.get().clear();
        RootPanel.get().add(pagina);

        // Stile base della pagina
        Document.get().getBody().getStyle().setProperty("backgroundColor", "#E8E8E8");
        Document.get().getBody().getStyle().setProperty("margin", "0");
        Document.get().getBody().getStyle().setProperty("fontFamily", "sans-serif");

        caricaReputazione();
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

        // Rating pubblico, subito sotto nome ed email (US-14)
        rating.clear();
        rating.addStyleName("profile-rating");
        rating.add(creaTestoVuotoRating("Caricamento rating..."));
        dati.add(rating);

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
     * Storico delle recensioni ricevute, a tutta larghezza sotto le due colonne.
     */
    private Widget creaSezioneRecensioni() {
        FlowPanel card = new FlowPanel();
        card.addStyleName("profile-card");
        card.addStyleName("profile-recensioni");
        card.add(creaTitoloSezione("Recensioni ricevute"));

        listaRecensioni.clear();
        listaRecensioni.add(creaTestoVuoto("Caricamento recensioni..."));
        card.add(listaRecensioni);

        return card;
    }

    /**
     * Rating medio e storico dell'utente in visualizzazione: due chiamate
     * indipendenti, cosi' il rating compare senza attendere l'elenco.
     */
    private void caricaReputazione() {
        String idUtente = utente.getEmail();

        reputazioneService.ratingMedio(idUtente, new AsyncCallback<Double>() {
            @Override
            public void onFailure(Throwable caught) {
                mostraTestoRating("Rating non disponibile");
            }

            @Override
            public void onSuccess(Double media) {
                mostraRating(media);
            }
        });

        reputazioneService.recensioniRicevute(idUtente, new AsyncCallback<List<RecensioneDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                listaRecensioni.clear();
                listaRecensioni.add(creaTestoVuoto("Impossibile caricare le recensioni. Riprova."));
            }

            @Override
            public void onSuccess(List<RecensioneDTO> result) {
                mostraRecensioni(result);
            }
        });
    }

    /**
     * Stelle e valore numerico del rating. La media arriva a null quando
     * l'utente non ha ancora recensioni: in quel caso si dichiara l'assenza
     * invece di mostrare cinque stelle vuote, che si leggerebbero come un
     * giudizio pessimo.
     */
    private void mostraRating(Double media) {
        rating.clear();

        if (media == null) {
            rating.add(creaTestoVuotoRating("Nessuna recensione ricevuta"));
            return;
        }

        Label stelle = new Label(Stelle.perMedia(media));
        stelle.addStyleName("recensione-stelle");
        rating.add(stelle);

        // Il valore esatto accanto alle stelle: l'arrotondamento e' alla
        // stella intera, la media va mostrata comunque per intero
        Label valore = new Label(Stelle.mediaFormattata(media));
        valore.addStyleName("profile-rating-valore");
        rating.add(valore);
    }

    private void mostraTestoRating(String testo) {
        rating.clear();
        rating.add(creaTestoVuotoRating(testo));
    }

    private void mostraRecensioni(List<RecensioneDTO> recensioni) {
        listaRecensioni.clear();

        if (recensioni == null || recensioni.isEmpty()) {
            listaRecensioni.add(creaTestoVuoto("Nessuna recensione ricevuta"));
            return;
        }

        // Stesso riquadro della pagina recensioni dell'annuncio (US-13)
        for (RecensioneDTO recensione : recensioni) {
            listaRecensioni.add(RecensioneItem.crea(recensione));
        }
    }

    private Widget creaTestoVuotoRating(String testo) {
        Label messaggio = new Label(testo);
        messaggio.addStyleName("profile-rating-vuoto");
        return messaggio;
    }

    private Widget creaTestoVuoto(String testo) {
        Label messaggio = new Label(testo);
        messaggio.addStyleName("annunci-vuoto");
        return messaggio;
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
