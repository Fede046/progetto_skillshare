package it.unibo;

import java.util.Arrays;
import java.util.List;

// Popola il database con utenti, annunci, richieste, messaggi e recensioni dimostrativi, per
// avere un'applicazione già navigabile appena avviata.
public final class DatabaseSeeder {

    /** Password condivisa da tutti gli account demo. */
    public static final String PASSWORD_DEMO = "Demo1234!";

    public static final String DEMO_1 = "demo1@skillshare.it";
    public static final String DEMO_2 = "demo2@skillshare.it";
    public static final String DEMO_3 = "demo3@skillshare.it";
    public static final String DEMO_4 = "demo4@skillshare.it";

    private DatabaseSeeder() {
        // Classe di sole utilità
    }

    public static void main(String[] args) {
        System.out.println("Seeding dei dati demo in corso...");
        boolean eseguito = popola();
        if (eseguito) {
            System.out.println("Fatto: 4 utenti demo, 4 annunci, 4 richieste, messaggi e recensioni.");
            System.out.println("Credenziali in artefatti/demo-credentials.md");
        } else {
            System.out.println("Dati demo già presenti: nessuna modifica.");
        }
        DatabaseCore.close();
    }

    // Crea i dati demo se non ci sono già.
    public static boolean popola() {
        if (giaPopolato()) {
            return false;
        }

        creaUtenti();

        AnnuncioDatabase annunci = new AnnuncioDatabase();
        AnnuncioDTO annuncioJava = pubblica(annunci, DEMO_1,
                "Ripetizioni di Java e Spring Boot",
                "Preparazione esami e progetti: basi del linguaggio, OOP, testing con JUnit.",
                "Programmazione Java",
                "Martedì e giovedì pomeriggio",
                "Conversazione in inglese");

        AnnuncioDTO annuncioInglese = pubblica(annunci, DEMO_2,
                "Conversazione in inglese B2/C1",
                "Un'ora di sola conversazione su temi a scelta, con correzione della pronuncia.",
                "Lingua inglese",
                "Lunedì sera e sabato mattina",
                "Aiuto con la programmazione");

        AnnuncioDTO annuncioFoto = pubblica(annunci, DEMO_3,
                "Ritratti fotografici e post-produzione",
                "Sessione di ritratti in esterna più editing base in Lightroom.",
                "Fotografia",
                "Weekend, luce permettendo",
                "Lezioni di chitarra");

        pubblica(annunci, DEMO_4,
                "Lezioni di chitarra acustica per principianti",
                "Accordi base, ritmica e prime canzoni complete. Chitarra non inclusa.",
                "Chitarra acustica",
                "Mercoledì pomeriggio",
                "Servizio fotografico per il mio portfolio");

        creaRichieste(annuncioJava, annuncioInglese, annuncioFoto);
        return true;
    }

    /** Il primo utente demo fa da sentinella per l'intero popolamento. */
    private static boolean giaPopolato() {
        try {
            UtenteDatabase.getProfilo(DEMO_1);
            return true;
        } catch (IllegalArgumentException e) {
            // "User not found": il database non è ancora stato popolato
            return false;
        }
    }

    private static void creaUtenti() {
        registra(DEMO_1, "Giulia", "Ferrari",
                "Studentessa di Ingegneria Informatica al terzo anno. Mi diverto a spiegare "
                        + "Java a chi lo sta imparando adesso, e in cambio cerco di sbloccare il mio inglese parlato.",
                "https://placehold.co/240x240/6E1E2B/FFFFFF?text=GF",
                Arrays.asList("Programmazione Java", "Spring Boot", "Git"));

        registra(DEMO_2, "Marco", "Bianchi",
                "Ho vissuto tre anni a Dublino e ora studio Lingue. Aiuto volentieri chi deve "
                        + "preparare una certificazione o semplicemente smettere di aver paura di parlare.",
                "https://placehold.co/240x240/6E1E2B/FFFFFF?text=MB",
                Arrays.asList("Lingua inglese", "Traduzione", "Public speaking"));

        registra(DEMO_3, "Sofia", "Greco",
                "Fotografa per passione da sei anni, soprattutto ritratti. Sto imparando la "
                        + "chitarra e cerco qualcuno con più pazienza di un tutorial su YouTube.",
                "https://placehold.co/240x240/6E1E2B/FFFFFF?text=SG",
                Arrays.asList("Fotografia", "Adobe Lightroom", "Video editing"));

        registra(DEMO_4, "Luca", "Moretti",
                "Suono la chitarra da dieci anni e insegno ai principianti. Mi serve qualcuno "
                        + "che sappia fotografare per il portfolio della mia band.",
                "https://placehold.co/240x240/6E1E2B/FFFFFF?text=LM",
                Arrays.asList("Chitarra acustica", "Teoria musicale", "Produzione audio"));
    }

    // Registra l'utente e ne completa il profilo. La registrazione accetta solo credenziali, bio e
    // tag arrivano dopo come farebbe un utente dalla schermata di modifica profilo.
    private static void registra(String email, String nome, String cognome,
            String bio, String photoUrl, List<String> tag) {
        UtenteDTO utente = new UtenteDTO(email, PASSWORD_DEMO, nome, cognome);
        UtenteDatabase.registra(utente);

        UtenteDTO profilo = UtenteDatabase.getProfilo(email);
        profilo.setBio(bio);
        profilo.setPhotoUrl(photoUrl);
        profilo.setTagCompetenza(tag);
        UtenteDatabase.aggiornaProfilo(profilo);
    }

    private static AnnuncioDTO pubblica(AnnuncioDatabase annunci, String autore, String titolo,
            String descrizione, String competenza, String disponibilita, String controprestazione) {
        AnnuncioDTO annuncio = new AnnuncioDTO();
        annuncio.setIdUtente(autore);
        annuncio.setTitolo(titolo);
        annuncio.setDescrizione(descrizione);
        annuncio.setCompetenzaOfferta(competenza);
        annuncio.setDisponibilita(disponibilita);
        annuncio.setControprestazione(controprestazione);
        return annunci.pubblica(annuncio);
    }

    // Crea quattro richieste, una per ogni stato previsto dal dominio. L'ordine conta: i messaggi
    // richiedono uno scambio ACCEPTED, la recensione uno COMPLETED.
    private static void creaRichieste(AnnuncioDTO annuncioJava, AnnuncioDTO annuncioInglese,
            AnnuncioDTO annuncioFoto) {
        RichiestaScambioDatabase richieste = new RichiestaScambioDatabase();
        MessaggioDatabase messaggi = new MessaggioDatabase();
        RecensioneDatabase recensioni = new RecensioneDatabase();

        // 1. PENDING: Marco ha scritto a Giulia e attende risposta
        salva(richieste, annuncioJava, DEMO_2, DEMO_1,
                "Ciao Giulia, mi servirebbe una mano con un progetto Spring. Ti va uno scambio?");

        // 2. ACCEPTED: scambio in corso fra Giulia e Sofia, con chat attiva
        RichiestaScambioDTO inCorso = salva(richieste, annuncioJava, DEMO_3, DEMO_1,
                "Ti insegno a fotografare in cambio di un ripasso di Java prima dell'esame.");
        richieste.accetta(inCorso.getId(), DEMO_1);
        invia(messaggi, inCorso, DEMO_3, "Perfetto! Quando ci vediamo per la prima lezione?");
        invia(messaggi, inCorso, DEMO_1, "Giovedì pomeriggio in biblioteca ti va?");
        invia(messaggi, inCorso, DEMO_3, "Ci sono, porto la reflex così iniziamo anche noi.");

        // 3. REJECTED: Luca ha chiesto a Marco, che ha declinato
        RichiestaScambioDTO rifiutata = salva(richieste, annuncioInglese, DEMO_4, DEMO_2,
                "Ti andrebbe di scambiare lezioni di chitarra con un po' di inglese?");
        richieste.rifiuta(rifiutata.getId(), DEMO_2);

        // 4. COMPLETED: scambio concluso fra Giulia e Sofia, con recensioni
        RichiestaScambioDTO conclusa = salva(richieste, annuncioFoto, DEMO_1, DEMO_3,
                "Mi piacerebbe imparare a usare Lightroom per le foto del mio blog.");
        richieste.accetta(conclusa.getId(), DEMO_3);
        invia(messaggi, conclusa, DEMO_1, "Grazie mille, la sessione di sabato è stata utilissima!");
        invia(messaggi, conclusa, DEMO_3, "Figurati! Segna pure lo scambio come completato.");
        richieste.completa(conclusa.getId(), DEMO_1);

        // Le recensioni rendono visibile il rating sui profili pubblici
        recensisci(recensioni, conclusa, DEMO_1, DEMO_3, 5,
                "Sofia è preparatissima e molto paziente. Ho imparato più in un pomeriggio "
                        + "che in settimane di tutorial.");
        recensisci(recensioni, conclusa, DEMO_3, DEMO_1, 4,
                "Scambio andato benissimo, Giulia è puntuale e sa spiegare. Consigliata.");
    }

    private static RichiestaScambioDTO salva(RichiestaScambioDatabase richieste, AnnuncioDTO annuncio,
            String richiedente, String creatore, String messaggio) {
        RichiestaScambioDTO richiesta = new RichiestaScambioDTO();
        richiesta.setIdAnnuncio(annuncio.getId());
        richiesta.setIdRichiedente(richiedente);
        richiesta.setIdCreatoreAnnuncio(creatore);
        richiesta.setMessaggio(messaggio);
        return richieste.salva(richiesta);
    }

    private static void invia(MessaggioDatabase messaggi, RichiestaScambioDTO richiesta,
            String mittente, String testo) {
        MessaggioDTO messaggio = new MessaggioDTO();
        messaggio.setIdRichiestaScambio(richiesta.getId());
        messaggio.setIdMittente(mittente);
        messaggio.setTesto(testo);
        messaggi.inviaMessaggio(messaggio);
    }

    private static void recensisci(RecensioneDatabase recensioni, RichiestaScambioDTO richiesta,
            String autore, String destinatario, int voto, String commento) {
        RecensioneDTO recensione = new RecensioneDTO();
        recensione.setIdRichiestaScambio(richiesta.getId());
        recensione.setIdAutore(autore);
        recensione.setIdDestinatario(destinatario);
        recensione.setVoto(voto);
        recensione.setCommento(commento);
        recensioni.lascia(recensione);
    }
}
