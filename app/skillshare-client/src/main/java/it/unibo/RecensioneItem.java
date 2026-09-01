package it.unibo;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

// Riquadro di una singola recensione: autore, stelle e commento facoltativo.
public final class RecensioneItem {

    // Classe di sole utilita': non va istanziata
    private RecensioneItem() {
    }

    // Riquadro senza badge accanto al nome dell'autore.
    public static Widget crea(RecensioneDTO recensione) {
        return crea(recensione, null);
    }

    public static Widget crea(RecensioneDTO recensione, String testoBadge) {
        FlowPanel item = new FlowPanel();
        item.addStyleName("annuncio-item");

        item.add(creaIntestazioneAutore(recensione, testoBadge));

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

    // Chi ha scritto la recensione, con l'eventuale badge a fianco. Senza nome
    // risolto resta visibile l'id (l'email) dell'autore.
    private static Widget creaIntestazioneAutore(RecensioneDTO recensione, String testoBadge) {
        FlowPanel intestazione = new FlowPanel();
        intestazione.addStyleName("recensione-autore-riga");

        Label nome = new Label(testoOppure(recensione.getNomeAutore(),
                testoOppure(recensione.getIdAutore(), "Utente sconosciuto")));
        nome.addStyleName("recensione-autore");
        intestazione.add(nome);

        if (testoBadge != null && !testoBadge.trim().isEmpty()) {
            Label badge = new Label(testoBadge);
            badge.addStyleName("recensione-autore-badge");
            intestazione.add(badge);
        }

        return intestazione;
    }

    private static String testoOppure(String valore, String fallback) {
        return valore != null && !valore.trim().isEmpty() ? valore : fallback;
    }
}
