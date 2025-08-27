package com.redepatas.api.parceiro.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Testes unitários para PasswordResetRateLimiter.
 * Usa configurações reduzidas para facilitar validação rápida.
 */
public class PasswordResetRateLimiterTest {

    @Test
    void testDailyLimitAndCooldown() {
        // dailyMax=3, cooldownSeconds=60, ipHourlySoftMax=100 (não deve limitar neste cenário)
        PasswordResetRateLimiter rl = new PasswordResetRateLimiter(3, 60, 100);
        String email = "user@example.com";
        String ip = "10.0.0.1";

        // Primeira solicitação deve passar
        assertTrue(rl.allow(email, ip));
        // Segunda imediatamente: deve falhar por cooldown
        assertFalse(rl.allow(email, ip));
        // Simular outra conta (não afeta)
        assertTrue(rl.allow("outro@example.com", ip));
        // Terceira tentativa mesmo email ainda dentro do cooldown -> continua bloqueada
        assertFalse(rl.allow(email, ip));
    }

    @Test
    void testIpLimitIndependenteDoEmail() {
        // dailyMax=10 (não atinge), cooldown=1s para não atrapalhar, ipHourlySoftMax=3
        PasswordResetRateLimiter rl = new PasswordResetRateLimiter(10, 1, 3);
        String ip = "177.0.0.5";

        assertTrue(rl.allow("a@a.com", ip));
        assertTrue(rl.allow("b@a.com", ip));
        assertTrue(rl.allow("c@a.com", ip));
        // 4ª dentro da mesma janela para mesmo IP deve bloquear
        assertFalse(rl.allow("d@a.com", ip));
    }
}
