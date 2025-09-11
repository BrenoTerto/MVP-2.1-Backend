package com.redepatas.api.cliente.services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.redepatas.api.cliente.dtos.PayloadsDto.WebhookPayload;
import com.redepatas.api.cliente.models.PlanoAssinatura;
import com.redepatas.api.cliente.repositories.AssinaturaClienteRepository;
import com.redepatas.api.cliente.repositories.PlanoAssinaturaRepository;
import com.redepatas.api.parceiro.models.Enum.StatusAssinatura;

@Service
public class WebhookService {

    private final AssinaturaClienteRepository assinaturaClienteRepository;
    @Autowired
    PlanoAssinaturaRepository planoAssinaturaRepository;
    @Autowired
    AsaasClientService asaasClientService;

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
        PlanoAssinatura patinhas = planoAssinaturaRepository.findByNome("Patinhas")
                .orElseThrow(() -> new RuntimeException("Plano Patinhas não encontrado"));
        PlanoAssinatura patinhas30 = planoAssinaturaRepository.findByNome("Patinhas 30")
                .orElseThrow(() -> new RuntimeException("Plano Patinhas 30 não encontrado"));

        Integer qtdAssinaturas = assinaturaClienteRepository.countAssinaturasAtivas();
        System.out.println(qtdAssinaturas);
        if (qtdAssinaturas < 30) {
            assinatura.setPlano(patinhas30);
            assinatura.setStatusAssinatura(StatusAssinatura.ATIVA);
            asaasClientService.alterarDescricaoAssinatura(assinatura.getIdAsaas(), "Patinhas 30");
        }
        assinaturaClienteRepository.save(assinatura);
    }
}
