package com.redepatas.api.cliente.controllers;

import com.redepatas.api.cliente.dtos.PayloadsDto.WebhookPayload;
import com.redepatas.api.cliente.services.WebhookService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/send")
    public ResponseEntity<Void> receberWebhook(@RequestBody WebhookPayload payload) {
        webhookService.processarWebhook(payload);
        return ResponseEntity.ok().build();
    }
}
