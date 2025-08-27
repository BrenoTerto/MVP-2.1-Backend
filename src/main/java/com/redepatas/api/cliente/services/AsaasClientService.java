package com.redepatas.api.cliente.services;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.client.RestClientException;

@Service
public class AsaasClientService {

    private static final String BASE_URL = "https://sandbox.asaas.com/api/v3/customers";

    private final RestTemplate restTemplate;

    @Value("${asaas.payments.sandbox}")
    private String accessToken;

    public AsaasClientService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public String verificarClienteExistente(String cpfCnpj) {
        String url = BASE_URL + "?cpfCnpj=" + cpfCnpj;

        HttpHeaders headers = new HttpHeaders();
        headers.set("access_token", accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                if (responseBody != null && responseBody.contains("\"data\":[{")) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(responseBody);
                    JsonNode dataArray = root.path("data");

                    if (dataArray.isArray() && dataArray.size() > 0) {
                        JsonNode customer = dataArray.get(0);
                        return customer.path("id").asText(); // retorna o ID
                    }
                }
            }
        } catch (Exception ex) {

        }

        return null;
    }

    public String criarCliente(String nome, String cpfCnpj, String email, String numero) {
        Map<String, String> body = new HashMap<>();
        String cpfFormatado = cpfCnpj.replaceAll("[.-]", "");
        body.put("name", nome);
        body.put("cpfCnpj", cpfFormatado);
        body.put("email", email);
        body.put("mobilePhone", numero);

        String idClienteExistente = verificarClienteExistente(cpfCnpj);
        if (idClienteExistente != null) {
            return "{\"message\": \"Cliente já existe\", \"id\": \"" + idClienteExistente + "\"}";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("access_token", accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(BASE_URL, request, String.class);
            return response.getBody();
        } catch (RestClientException ex) {
            return "{\"error\": \"Erro ao criar cliente: " + ex.getMessage() + "\"}";
        }
    }

    public String getLink(String idSub) {
        String url = "https://sandbox.asaas.com/api/v3/subscriptions/" + idSub + "/payments";

        HttpHeaders headers = new HttpHeaders();
        headers.set("access_token", accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    String.class);

            // Parse JSON e extrair campo "data"
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode dataNode = rootNode.get("data");
            JsonNode firstElement = dataNode.get(0);
            String invoiceUrl = firstElement.get("invoiceUrl").asText();
            // Retorna o conteúdo do campo "data" como String JSON
            return invoiceUrl.toString();

        } catch (RestClientException | IOException ex) {
            return "{\"error\": \"Erro ao buscar link: " + ex.getMessage() + "\"}";
        }
    }

    public String criarAssinatura(String customerId, BigDecimal valor, String descricao, String dataVencimento,
            String idAssinatura) {
        String url = "https://sandbox.asaas.com/api/v3/subscriptions";

        Map<String, Object> body = new HashMap<>();
        body.put("billingType", "CREDIT_CARD");
        body.put("cycle", "MONTHLY");
        body.put("value", valor);
        body.put("nextDueDate", dataVencimento);
        body.put("description", descricao);
        body.put("customer", customerId);
        body.put("externalReference", idAssinatura);

        HttpHeaders headers = new HttpHeaders();
        headers.set("access_token", accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return response.getBody();
        } catch (RestClientException ex) {
            return "{\"error\": \"Erro ao criar assinatura: " + ex.getMessage() + "\"}";
        }
    }

}
