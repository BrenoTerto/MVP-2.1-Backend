package com.redepatas.api.parceiro.services;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Rate limiting minimalista para poucos usuários:
 *  - Cooldown mínimo entre envios por e-mail.
 *  - Limite diário máximo por e-mail.
 *  - Limite amplo por IP apenas para conter abuso grosseiro.
 * Mantém resposta neutra mesmo se bloqueado.
 */
@Service
public class PasswordResetRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetRateLimiter.class);

    private final Map<String, EmailEntry> emailMap = new ConcurrentHashMap<>();
    private final Map<String, IpEntry> ipMap = new ConcurrentHashMap<>();

    private final int dailyEmailMax;
    private final long cooldownSeconds;
    private final int ipHourlySoftMax;
    private final int ipHourlyHardMax;
    private final int ipDistinctEmailsHardMax = 40;

    public PasswordResetRateLimiter(
            @Value("${security.reset.rateLimit.email.dailyMax:5}") int dailyEmailMax,
            @Value("${security.reset.rateLimit.email.cooldownSeconds:300}") long cooldownSeconds,
            @Value("${security.reset.rateLimit.ip.hourlySoftMax:50}") int ipHourlySoftMax) {
        this.dailyEmailMax = dailyEmailMax;
        this.cooldownSeconds = cooldownSeconds;
        this.ipHourlySoftMax = ipHourlySoftMax;
        this.ipHourlyHardMax = ipHourlySoftMax * 4;
    }

    /**
     * @return true se pode enviar agora, false se deve suprimir envio (mas resposta neutra permanece).
     */
    public boolean allow(String email, String ip) {
        String normEmail = email == null ? "" : email.trim().toLowerCase();
        boolean emailAllowed = checkEmail(normEmail);
        boolean ipAllowed = checkIp(ip, normEmail);
        boolean allowed = emailAllowed && ipAllowed;
        if (!allowed) {
            log.warn("Rate limit / cooldown bloqueou envio reset - email='{}' ip='{}' (emailAllowed={}, ipAllowed={})", normEmail, ip, emailAllowed, ipAllowed);
        }
        return allowed;
    }

    private boolean checkEmail(String email) {
        EmailEntry entry = emailMap.compute(email, (k, existing) -> {
            Instant now = Instant.now();
            LocalDate today = LocalDate.now();
            if (existing == null || !existing.day.equals(today)) {
                return new EmailEntry(today, 1, now);
            }
            // mesma data
            if (now.getEpochSecond() - existing.lastSent.getEpochSecond() < cooldownSeconds) {
                existing.cooldownHit = true;
                return existing;
            }
            if (existing.count < dailyEmailMax) {
                existing.count += 1;
                existing.lastSent = now;
            }
            return existing;
        });
        if (entry.day.equals(LocalDate.now())) {
            if (entry.cooldownHit) return false;
            return entry.count <= dailyEmailMax;
        }
        return true;
    }

    private boolean checkIp(String ip, String email) {
        if (ip == null) ip = "";
        IpEntry entry = ipMap.compute(ip, (k, existing) -> {
            Instant now = Instant.now();
            if (existing == null || now.getEpochSecond() - existing.windowStart.getEpochSecond() >= 3600) {
                IpEntry ne = new IpEntry(now, 1);
                ne.addEmail(email);
                return ne;
            }
            existing.count += 1;
            existing.addEmail(email);
            return existing;
        });

        if (entry.count <= ipHourlySoftMax) return true;

        if (entry.count <= ipHourlyHardMax && entry.getDistinctEmailCount() <= ipDistinctEmailsHardMax) {
            log.info("Rate limit IP soft excedido ip='{}' count={} distinctEmails={}", ip, entry.count, entry.getDistinctEmailCount());
            return true;
        }

        log.warn("Rate limit IP HARD bloqueando ip='{}' count={} distinctEmails={}", ip, entry.count, entry.getDistinctEmailCount());
        return false;
    }

    private static class EmailEntry {
        final LocalDate day;
        int count;
        Instant lastSent;
        boolean cooldownHit = false;
        EmailEntry(LocalDate day, int count, Instant lastSent) {
            this.day = day;
            this.count = count;
            this.lastSent = lastSent;
        }
    }

    private static class IpEntry {
        final Instant windowStart;
        int count;
        private final Set<String> distinctEmails = new java.util.HashSet<>();
        IpEntry(Instant windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
        void addEmail(String email) {
            if (email == null || email.isBlank()) return;
            if (distinctEmails.size() < 100) { 
                distinctEmails.add(email);
            }
        }
        int getDistinctEmailCount() { return distinctEmails.size(); }
    }
}
