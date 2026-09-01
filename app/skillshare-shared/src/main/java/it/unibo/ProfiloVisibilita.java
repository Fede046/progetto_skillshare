package it.unibo;

// Decide se la schermata di profilo va mostrata in sola lettura. Sta in shared perche' serve sia
// alla GUI sia ai test lato server.
public final class ProfiloVisibilita {

    // Classe di sole utilita': non va istanziata
    private ProfiloVisibilita() {
    }

    // Vero quando il profilo in visualizzazione non e' quello dell'utente autenticato: in sola
    // lettura spariscono le azioni personali come "Modifica Profilo".
    public static boolean soloLettura(String idUtenteAutenticato, String idProfiloVisualizzato) {
        if (vuoto(idUtenteAutenticato) || vuoto(idProfiloVisualizzato)) {
            return true;
        }
        return !idUtenteAutenticato.trim().equals(idProfiloVisualizzato.trim());
    }

    private static boolean vuoto(String valore) {
        return valore == null || valore.trim().isEmpty();
    }
}
