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

// Schermata del profilo: avatar, bio, competenze, rating e recensioni ricevute.
// La stessa classe mostra sia il proprio profilo sia quello pubblico di un altro utente
public class ProfiloGui {

    // Chi sta navigando: serve alla NavBar e a decidere la sola lettura
    private final UtenteDTO utenteLoggato;

    // Di chi e' il profilo mostrato. Sul profilo altrui arriva via RPC,
    // quindi al primo disegno puo' essere ancora null.
    private UtenteDTO visualizzato;

    // Id del profilo da caricare quando non e' il proprio
    private final String idVisualizzato;

    // Vero quando la schermata e' stata aperta sul profilo di un altro utente
    private final boolean profiloAltrui;

    private final ProfileServiceAsync profileService = GWT.create(ProfileService.class);
    private final ReputazioneServiceAsync reputazioneService = GWT.create(ReputazioneService.class);

    // Riempiti dalle risposte RPC: al primo disegno mostrano il caricamento
    private final FlowPanel rating = new FlowPanel();
    private final FlowPanel listaRecensioni = new FlowPanel();

    // Passiamo l'utente loggato al costruttore
    public ProfiloGui(UtenteDTO utente) {
        this.utenteLoggato = utente;
        this.visualizzato = utente;
        this.idVisualizzato = utente != null ? utente.getEmail() : null;
        this.profiloAltrui = false;
    }

    // Profilo pubblico di un altro utente, aperto dal Marketplace: i dati non sono in mano al
    // chiamante e vengono caricati qui via ProfileService, cosi' l'eventuale errore si vede sulla
    public ProfiloGui(UtenteDTO utenteLoggato, String idVisualizzato) {
        this.utenteLoggato = utenteLoggato;
        this.visualizzato = null;
        this.idVisualizzato = idVisualizzato;
        this.profiloAltrui = true;
    }

    public void mostra() {
        // Il proprio profilo e' gia' in memoria: si disegna subito
        if (visualizzato != null) {
            disegnaProfilo();
            return;
        }

        renderizza(creaPaginaMessaggio("Caricamento profilo..."));
        caricaProfilo();
    }

    // Recupera il profilo dell'utente selezionato. Un id inesistente o non caricabile porta a un
    // messaggio esplicito, non a una pagina vuota.
    private void caricaProfilo() {
        profileService.getProfilo(idVisualizzato, new AsyncCallback<UtenteDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                renderizza(creaPaginaMessaggio(
                        "Impossibile aprire questo profilo: l'utente non esiste piu' o non e' raggiungibile."));
            }

            @Override
            public void onSuccess(UtenteDTO result) {
                if (result == null) {
                    renderizza(creaPaginaMessaggio("Impossibile aprire questo profilo: utente non trovato."));
                    return;
                }
                visualizzato = result;
                disegnaProfilo();
            }
        });
    }

    private void disegnaProfilo() {
        FlowPanel contenuto = new FlowPanel();
        contenuto.addStyleName("app-page");
        contenuto.add(creaIntestazione());
        contenuto.add(creaColonne());
        contenuto.add(creaSezioneRecensioni());

        renderizza(contenuto);
        caricaReputazione();
    }

    // Disegna la pagina completa di barra di navigazione e sfondo.
    private void renderizza(Widget contenuto) {
        FlowPanel pagina = new FlowPanel();

        // Barra di navigazione orizzontale: questa e' la home dopo il login
        pagina.add(new NavBar(utenteLoggato, sezioneAttiva()).getWidget());
        pagina.add(contenuto);

        // Pulizia e rendering
        RootPanel.get().clear();
        RootPanel.get().add(pagina);

        // Stile base della pagina
        Document.get().getBody().getStyle().setProperty("backgroundColor", "#E8E8E8");
        Document.get().getBody().getStyle().setProperty("margin", "0");
        Document.get().getBody().getStyle().setProperty("fontFamily", "sans-serif");
    }

    // Sul profilo di un altro utente la voce "Profilo" resta cliccabile:
    // porta al proprio, non a quello in visualizzazione.
    private String sezioneAttiva() {
        return profiloAltrui ? NavBar.SEZIONE_MARKETPLACE : NavBar.SEZIONE_PROFILO;
    }

    // Vero quando le azioni personali non vanno mostrate. Durante il caricamento il profilo non e'
    // ancora noto e si resta in sola lettura.
    private boolean soloLettura() {
        return ProfiloVisibilita.soloLettura(
                utenteLoggato != null ? utenteLoggato.getEmail() : null,
                visualizzato != null ? visualizzato.getEmail() : null);
    }

    // Pagina di servizio per attesa ed errore: stessa cornice del
    // profilo, con la via d'uscita verso il Marketplace.
    private Widget creaPaginaMessaggio(String testo) {
        FlowPanel contenuto = new FlowPanel();
        contenuto.addStyleName("app-page");

        FlowPanel card = new FlowPanel();
        card.addStyleName("profile-card");
        card.add(creaTitoloSezione("Profilo"));
        card.add(creaTestoVuoto(testo));

        if (profiloAltrui) {
            FlowPanel azione = new FlowPanel();
            azione.addStyleName("profile-messaggio-azione");
            azione.add(creaBottoneIndietro());
            card.add(azione);
        }

        contenuto.add(card);
        return contenuto;
    }

    private Widget creaBottoneIndietro() {
        Button btnIndietro = new Button("Torna al Marketplace");
        btnIndietro.addStyleName("btn-secondary");
        btnIndietro.addStyleName("btn-sm");
        btnIndietro.addClickHandler(event -> new MarketplaceGui(utenteLoggato).mostra());
        return btnIndietro;
    }

    // Intestazione: fascia bordeaux, avatar che la scavalca, dati e azione a fianco.
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

        Label nome = new Label(testoOppure(visualizzato.getNome(), "") + " "
                + testoOppure(visualizzato.getCognome(), ""));
        nome.addStyleName("profile-nome");
        dati.add(nome);

        Label email = new Label(testoOppure(visualizzato.getEmail(), ""));
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

        // Sul profilo di un altro utente la modifica non ha senso e sparisce
        if (!soloLettura()) {
            Button btnModifica = new Button("Modifica Profilo");
            btnModifica.addStyleName("btn-primary");
            btnModifica.addClickHandler(event -> new ModificaProfiloGui(visualizzato).mostra());
            azioni.add(btnModifica);
        }

        // Arrivando dal Marketplace serve comunque la via di ritorno, anche
        // quando il profilo aperto e' il proprio
        if (profiloAltrui) {
            azioni.add(creaBottoneIndietro());
        }

        riga.add(azioni);
        corpo.add(riga);
        hero.add(corpo);
        return hero;
    }

    // Biografia e competenze affiancate su due colonne.
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

        String bio = visualizzato.getBio();
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

        if (visualizzato.getTagCompetenza() == null || visualizzato.getTagCompetenza().isEmpty()) {
            Label vuoto = new Label("Nessuna competenza aggiunta.");
            vuoto.addStyleName("profile-bio");
            badges.add(vuoto);
        } else {
            for (String tag : visualizzato.getTagCompetenza()) {
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

    // Storico delle recensioni ricevute, a tutta larghezza sotto le due colonne.
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

    // Rating medio e storico dell'utente in visualizzazione: due chiamate indipendenti, cosi' il
    // rating compare senza attendere l'elenco.
    private void caricaReputazione() {
        String idUtente = visualizzato.getEmail();

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

    // Stelle e valore numerico del rating.
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

    // Costruisce la foto profilo. Se l'URL non e' stato impostato - oppure se l'immagine non riesce a
    // caricarsi - viene mostrato un placeholder circolare con le iniziali dell'utente.
    private Widget creaAvatar() {
        FlowPanel contenitore = new FlowPanel();
        contenitore.addStyleName("profile-avatar-wrapper");

        String photoUrl = visualizzato.getPhotoUrl();
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

    // Placeholder circolare con le iniziali di nome e cognome.
    private Widget creaPlaceholderAvatar() {
        String iniziali = "";
        if (visualizzato.getNome() != null && !visualizzato.getNome().trim().isEmpty()) {
            iniziali += visualizzato.getNome().trim().charAt(0);
        }
        if (visualizzato.getCognome() != null && !visualizzato.getCognome().trim().isEmpty()) {
            iniziali += visualizzato.getCognome().trim().charAt(0);
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
