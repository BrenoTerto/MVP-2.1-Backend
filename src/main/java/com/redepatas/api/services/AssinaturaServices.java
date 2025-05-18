package com.redepatas.api.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redepatas.api.models.AssinaturaClienteModel;
import com.redepatas.api.models.ClientModel;
import com.redepatas.api.models.PlanoAssinatura;
import com.redepatas.api.models.Enum.StatusAssinatura;
import com.redepatas.api.repositories.AssinaturaClienteRepository;
import com.redepatas.api.repositories.ClientRepository;
import com.redepatas.api.repositories.PlanoAssinaturaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AssinaturaServices {

    @Autowired
    ClientRepository repository;

    @Autowired
    PlanoAssinaturaRepository planoAssinaturaRepository;

    @Autowired
    private AsaasClientService asaasClientService;

    @Autowired
    AssinaturaClienteRepository assinaturaClienteRepository;

    public List<PlanoAssinatura> listarPlanos() {
        return planoAssinaturaRepository.findAll();
    }

    @Autowired
    private ClientRepository clientRepository;

    public AssinaturaClienteModel buscarAssinaturaAtivaPorLogin(String login) {
        ClientModel user = (ClientModel) clientRepository.findByLogin(login);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }

        AssinaturaClienteModel assinatura = user.getAssinatura();

        if (assinatura == null ||
                assinatura.getStatusAssinatura() != StatusAssinatura.ATIVA ||
                assinatura.getDataFim().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assinatura ativa não encontrada");
        }

        return assinatura;
    }

    public String criarAssinatura(String login, Long planoId) {
        ClientModel cliente = (ClientModel) clientRepository.findByLogin(login);

        if (cliente == null) {
            throw new RuntimeException("Cliente não encontrado");
        }

        return iniciarAssinatura(cliente, planoId);
    }

    public String iniciarAssinatura(ClientModel client, Long planoId) {
        PlanoAssinatura plano = planoAssinaturaRepository.findById(planoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plano não encontrado"));
    
        AssinaturaClienteModel assinatura = new AssinaturaClienteModel();
        assinatura.setCliente(client);
        assinatura.setPlano(plano);
        assinatura.setStatusAssinatura(StatusAssinatura.PENDENTE);
        assinatura.setDataInicio(LocalDateTime.now());
        assinatura.setDataFim(LocalDateTime.now().plusDays(plano.getDuracaoDias()));
    
        assinatura = assinaturaClienteRepository.save(assinatura); 
    
        LocalDate dataVencimento = LocalDate.now().plusDays(5);
        String dataVencimentoString = dataVencimento.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    
        String responseAssinatura = asaasClientService.criarAssinatura(
            client.getIdCustomer(),
            plano.getPreco(),
            plano.getNome(),
            dataVencimentoString,
            assinatura.getId().toString() 
        );
    
        String idAsaasAssinatura;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseAssinatura);
            idAsaasAssinatura = rootNode.get("id").asText();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar resposta do Asaas: " + e.getMessage(), e);
        }
    
        assinatura.setIdAsaas(idAsaasAssinatura);
        assinaturaClienteRepository.save(assinatura);
    
        return "Assinatura criada com sucesso!";
    }
    
    
}
