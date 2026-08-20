package it.unibo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class ProfileServiceImplTest {

    @Test
    void testGetProfiloSuccess() {
        ProfileServiceImpl service = new ProfileServiceImpl();
        String email = "rpc.test_" + System.currentTimeMillis() + "@unibo.it";
        
        UtenteDTO utente = new UtenteDTO(
                email,
                "@Password123",
                "Luca",
                "Verdi",
                "Bio RPC",
                "https://example.com/luca.png",
                List.of("Java", "RPC")
        );
        UtenteDatabase.registra(utente);

        UtenteDTO result = service.getProfilo(email);

        assertNotNull(result);
        assertEquals("Luca", result.getNome());
        assertEquals("Verdi", result.getCognome());
        assertEquals("Bio RPC", result.getBio());
        assertTrue(result.getTagCompetenza().contains("RPC"));
    }

    @Test
    void testGetProfiloUtenteInesistenteLanciaEccezione() {
        ProfileServiceImpl service = new ProfileServiceImpl();
        assertThrows(IllegalArgumentException.class, () -> {
            service.getProfilo("inesistente_" + System.currentTimeMillis() + "@unibo.it");
        });
    }
}