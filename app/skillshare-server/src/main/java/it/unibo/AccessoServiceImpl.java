package it.unibo;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

public class AccessoServiceImpl extends RemoteServiceServlet implements AccessoService {

    @Override
    public UtenteDTO login(String email, String password) {
        try {
            return UtenteDatabase.verificaCredenziali(email, password);
        } catch (IllegalArgumentException e) {
            // Invece di lanciare eccezioni (errore 500), restituiamo un DTO che porta il
            // messaggio di errore nel campo "nome".
            UtenteDTO erroreDto = new UtenteDTO();
            erroreDto.setNome(e.getMessage());
            return erroreDto;
        }
    }
}