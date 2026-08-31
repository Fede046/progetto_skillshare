package it.unibo;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Serve le foto profilo caricate dagli utenti.
 *
 * Serve una servlet perché le immagini non stanno dentro la webapp ma nella
 * cartella dati indicata da DATA_DIR, che Jetty non espone da sé.
 */
public class FotoProfiloServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest richiesta, HttpServletResponse risposta) throws IOException {
        // Il nome del file è la parte di URL dopo /app/fotoProfilo/
        String percorso = richiesta.getPathInfo();
        String nomeFile = percorso == null ? null : percorso.replaceFirst("^/", "");

        File file = ArchivioFotoProfilo.trova(nomeFile);
        if (file == null) {
            // L'avatar del client ha già un fallback sul placeholder con le
            // iniziali: un 404 pulito basta a farlo scattare
            risposta.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        risposta.setContentType(ArchivioFotoProfilo.contentTypeDi(nomeFile));
        risposta.setContentLengthLong(file.length());

        try (OutputStream uscita = risposta.getOutputStream()) {
            Files.copy(file.toPath(), uscita);
        }
    }
}
