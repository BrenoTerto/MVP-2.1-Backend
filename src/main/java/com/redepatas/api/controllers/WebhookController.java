package com.redepatas.api.controllers;

import com.redepatas.api.dtos.PayloadsDto.WebhookPayload;
import com.redepatas.api.services.WebhookService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final WebhookService webhookService;

    @Value("${asaas.webhook.token}")
    private String webhookToken;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/send")
    public ResponseEntity<Void> receberWebhook(
            @RequestBody WebhookPayload payload,
            @RequestHeader(value = "asaas-access-token", required = false) String tokenHeader) {
        if (tokenHeader == null || !tokenHeader.equals(webhookToken)) {
            return ResponseEntity.status(401).build();
        }
        webhookService.processarWebhook(payload);
        return ResponseEntity.ok().build();
    }
}
