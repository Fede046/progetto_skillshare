package it.unibo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ProfileServiceImplTest {

    @BeforeAll
    static void setUpDatabase() {
        // Database in memoria per i test: non tocca il file progetto_sweng.db
        DatabaseCore.enableTestMode();
    }

    @AfterAll
    static void tearDownDatabase() {
        DatabaseCore.disableTestMode();
    }

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
    @Test
    void testUpdateProfileSuccess() {
        ProfileServiceImpl service = new ProfileServiceImpl();
        String email = "rpc.update_" + System.currentTimeMillis() + "@unibo.it";

        UtenteDTO utente = new UtenteDTO(
                email,
                "@Password123",
                "Sara",
                "Neri",
                "Bio iniziale",
                "https://example.com/old.png",
                List.of("HTML")
        );
        UtenteDatabase.registra(utente);

        UtenteDTO mod = new UtenteDTO();
        mod.setEmail(email);
        mod.setBio("Bio aggiornata via RPC");
        mod.setPhotoUrl("https://example.com/new.png");
        mod.setTagCompetenza(List.of("HTML", "CSS", "TypeScript"));

        UtenteDTO aggiornato = service.updateProfile(mod);

        assertNotNull(aggiornato);
        assertEquals("Bio aggiornata via RPC", aggiornato.getBio());
        assertEquals("https://example.com/new.png", aggiornato.getPhotoUrl());
        assertEquals(3, aggiornato.getTagCompetenza().size());
        assertTrue(aggiornato.getTagCompetenza().contains("TypeScript"));
    }

    @Test
    void testUpdateProfileDatiNullLanciaEccezione() {
        ProfileServiceImpl service = new ProfileServiceImpl();
        assertThrows(IllegalArgumentException.class, () -> {
            service.updateProfile(null);
        });
    }
}