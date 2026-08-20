package it.unibo;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

public class ProfileServiceImpl extends RemoteServiceServlet implements ProfileService {

    private static final long serialVersionUID = 1L;

    @Override
    public UtenteDTO getProfilo(String email) throws IllegalArgumentException {
        return UtenteDatabase.getProfilo(email);
    }

    @Override
    public UtenteDTO updateProfile(UtenteDTO utente) throws IllegalArgumentException {
        if (utente == null) {
            throw new IllegalArgumentException("Dati non validi");
        }
        // Delega la persistenza e l'aggiornamento a UtenteDatabase
        return UtenteDatabase.aggiornaProfilo(utente);
    }
}