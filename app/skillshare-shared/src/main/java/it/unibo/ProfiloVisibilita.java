package it.unibo;

/**
 * Decide se la schermata di profilo va mostrata in sola lettura.
 * Sta in shared perche' serve sia alla GUI sia ai test lato server.
 */
public final class ProfiloVisibilita {

    // Classe di sole utilita': non va istanziata
    private ProfiloVisibilita() {
    }

    /**
     * Vero quando il profilo in visualizzazione non e' quello dell'utente
     * autenticato: in sola lettura spariscono le azioni personali come
     * "Modifica Profilo".
     * Con un id mancante si resta in sola lettura: mostrare per errore il
     * pulsante di modifica sul profilo di un altro sarebbe piu' grave che
     * nasconderlo sul proprio.
     * Gli id vengono confrontati dopo il trim, come fa UtenteDatabase quando
     * li usa come chiave.
     *
     * @param idUtenteAutenticato L'id (email) di chi sta navigando.
     * @param idProfiloVisualizzato L'id (email) del profilo aperto.
     * @return true se il profilo va mostrato in sola lettura.
     */
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
