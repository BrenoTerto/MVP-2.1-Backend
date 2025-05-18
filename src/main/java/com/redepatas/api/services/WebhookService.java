package com.redepatas.api.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.redepatas.api.dtos.PayloadsDto.WebhookPayload;
import com.redepatas.api.models.Enum.StatusAssinatura;
import com.redepatas.api.repositories.AssinaturaClienteRepository;

import java.util.UUID;

@Service
public class WebhookService {

    private final AssinaturaClienteRepository assinaturaClienteRepository;

    public WebhookService(AssinaturaClienteRepository assinaturaClienteRepository) {
        this.assinaturaClienteRepository = assinaturaClienteRepository;
    }

    public void processarWebhook(WebhookPayload payload) {
        String event = payload.event();

        if (!"PAYMENT_RECEIVED".equals(event)
                && !"subscription.activated".equals(event)
                && !"PAYMENT_CONFIRMED".equals(event)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Evento não suportado: " + event);
        }

        String externalReference = payload.payment().externalReference();

        if (externalReference == null || externalReference.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ExternalReference está vazio ou nulo");
        }

        UUID id;
        try {
            id = UUID.fromString(externalReference);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ExternalReference inválido para UUID: " + externalReference);
        }

        var assinatura = assinaturaClienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Assinatura não encontrada com ID: " + id));

        assinatura.setStatusAssinatura(StatusAssinatura.ATIVA);
        assinaturaClienteRepository.save(assinatura);
    }

}
