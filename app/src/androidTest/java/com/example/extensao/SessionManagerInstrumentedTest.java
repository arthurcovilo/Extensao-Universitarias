package com.example.extensao;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Testa o tratamento de sessão expirada/inválida (HTTP 401/403).
 * Atenção: usa as SharedPreferences reais do app no dispositivo.
 */
@RunWith(AndroidJUnit4.class)
public class SessionManagerInstrumentedTest {

    private SessionManager sessionManager;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        sessionManager = new SessionManager(context);
        sessionManager.saveSession("token-teste", "teste@email.com", "Teste", "USER");
    }

    @After
    public void tearDown() {
        sessionManager.clearSession();
    }

    @Test
    public void handleUnauthorized_401_limpaSessao() {
        assertTrue(sessionManager.handleUnauthorized(401));
        assertFalse(sessionManager.isLoggedIn());
    }

    @Test
    public void handleUnauthorized_403_limpaSessao() {
        assertTrue(sessionManager.handleUnauthorized(403));
        assertFalse(sessionManager.isLoggedIn());
    }

    @Test
    public void handleUnauthorized_outrosCodigos_mantemSessao() {
        assertFalse(sessionManager.handleUnauthorized(200));
        assertFalse(sessionManager.handleUnauthorized(500));
        assertTrue(sessionManager.isLoggedIn());
    }

    @Test
    public void handleUnauthorized_sessaoJaEncerrada_retornaTrue() {
        sessionManager.clearSession();
        assertTrue(sessionManager.handleUnauthorized(403));
        assertFalse(sessionManager.isLoggedIn());
    }
}
