package it.unibo;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

public class AccessoServiceImpl extends RemoteServiceServlet implements AccessoService {

    @Override
    public UtenteDTO login(String email, String password) {
        try {
            return UtenteDatabase.verificaCredenziali(email, password);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}