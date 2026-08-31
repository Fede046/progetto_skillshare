package it.unibo;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

/**
 * Riceve la foto profilo inviata dal FormPanel di GWT in multipart.
 *
 * Non è una RemoteServiceServlet: l'upload di file non passa dal meccanismo
 * RPC di GWT, che serializza solo oggetti Java. La risposta è quindi HTML,
 * il formato che il FormPanel sa leggere nel proprio evento di submit:
 * "OK|percorso" in caso di successo, "ERRORE|messaggio" altrimenti.
 */
@MultipartConfig(
        fileSizeThreshold = 256 * 1024,
        maxFileSize = ValidatoreFotoProfilo.DIMENSIONE_MASSIMA_BYTE,
        maxRequestSize = ValidatoreFotoProfilo.DIMENSIONE_MASSIMA_BYTE * 2)
public class FotoProfiloUploadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Nomi dei campi e prefissi di risposta vivono in ProtocolloUploadFoto,
    // condiviso con il client
    private static final String CAMPO_FILE = ProtocolloUploadFoto.CAMPO_FILE;
    private static final String CAMPO_EMAIL = ProtocolloUploadFoto.CAMPO_EMAIL;
    private static final String ESITO_OK = ProtocolloUploadFoto.ESITO_OK;
    private static final String ESITO_ERRORE = ProtocolloUploadFoto.ESITO_ERRORE;

    @Override
    protected void doPost(HttpServletRequest richiesta, HttpServletResponse risposta) throws IOException {
        // Il FormPanel legge il corpo della risposta come HTML
        risposta.setContentType("text/html; charset=UTF-8");
        PrintWriter out = risposta.getWriter();

        try {
            String email = richiesta.getParameter(CAMPO_EMAIL);
            if (email == null || email.trim().isEmpty()) {
                out.print(ESITO_ERRORE + "Utente non identificato: rifai l'accesso.");
                return;
            }

            Part parte = richiesta.getPart(CAMPO_FILE);
            if (parte == null) {
                out.print(ESITO_ERRORE + "Nessun file selezionato");
                return;
            }

            String nomeFile = parte.getSubmittedFileName();

            // Le regole di accettazione stanno in ValidatoreFotoProfilo, così
            // restano verificabili con i test unitari
            ValidatoreFotoProfilo.valida(nomeFile, parte.getContentType(), parte.getSize());

            String estensione = ValidatoreFotoProfilo.estensioneDi(nomeFile);
            String percorsoPubblico;
            try (InputStream contenuto = parte.getInputStream()) {
                percorsoPubblico = ArchivioFotoProfilo.salva(email, estensione, contenuto);
            }

            // Il profilo punta alla nuova immagine: si riusa photoUrl, quindi
            // la visualizzazione esistente non cambia. getProfilo lancia
            // IllegalArgumentException se l'utente non esiste.
            UtenteDTO utente = UtenteDatabase.getProfilo(email);
            utente.setPhotoUrl(percorsoPubblico);
            UtenteDatabase.aggiornaProfilo(utente);

            out.print(ESITO_OK + percorsoPubblico);

        } catch (IllegalArgumentException e) {
            // Messaggio gia' pronto per l'utente
            out.print(ESITO_ERRORE + e.getMessage());
        } catch (Exception e) {
            // Il contenitore applica maxFileSize prima ancora di arrivare qui:
            // in quel caso l'eccezione non e' una IllegalArgumentException
            out.print(ESITO_ERRORE + "Caricamento non riuscito: verifica che il file sia "
                    + "un'immagine JPG o PNG entro 2 MB.");
        }
    }
}
