package com.redepatas.api.parceiro.controllers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import com.redepatas.api.parceiro.services.PasswordResetRateLimiter;
import com.redepatas.api.parceiro.services.PartnerService;

public class PartnerControllerResendDirectTest {

    static class FlagPartnerService extends PartnerService {
        volatile boolean called = false;
        @Override
        public void resendConfirmationEmail(String emailContato) {
            called = true;
        }
    }

    @Test
    void enviaQuandoRateLimitPermite() {
        PartnerController controller = new PartnerController();

        FlagPartnerService svc = new FlagPartnerService();
        setField(controller, "partnerService", svc);

        PasswordResetRateLimiter limiter = new PasswordResetRateLimiter(10, 1, 100) {
            @Override
            public boolean allow(String email, String ip) { return true; }
        };
        setField(controller, "rateLimiter", limiter);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Forwarded-For", "203.0.113.7");

        ResponseEntity<Void> resp = controller.resendConfirmation("teste@example.com", req);
        assertEquals(204, resp.getStatusCode().value());
        assertTrue(svc.called, "Deveria ter chamado resendConfirmationEmail");
    }

    @Test
    void naoEnviaQuandoRateLimitBloqueia() {
        PartnerController controller = new PartnerController();

        FlagPartnerService svc = new FlagPartnerService();
        setField(controller, "partnerService", svc);

        PasswordResetRateLimiter limiter = new PasswordResetRateLimiter(10, 1, 100) {
            @Override
            public boolean allow(String email, String ip) { return false; }
        };
        setField(controller, "rateLimiter", limiter);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Forwarded-For", "203.0.113.8");

        ResponseEntity<Void> resp = controller.resendConfirmation("teste2@example.com", req);
        assertEquals(204, resp.getStatusCode().value());
        assertFalse(svc.called, "Não deveria chamar resendConfirmationEmail quando bloqueado");
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

