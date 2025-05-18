package com.redepatas.api.services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.redepatas.api.dtos.UserDtos.EnderecoDto;

public class GoogleGeoUtils {

    @Value("${api.key.google.maps}")
    private static String API_KEY;

    public static EnderecoDto reverseGeocode(Double latitude, Double longitude) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude + "&key=" + API_KEY;
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao consultar endereço atual.");
        }

        List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("results");
        if (results.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereço não encontrado para as coordenadas.");
        }

        Map<String, Object> primeiroResultado = results.get(0);
        List<Map<String, Object>> components = (List<Map<String, Object>>) primeiroResultado.get("address_components");

        String rua = null, bairro = null, cidade = null, estado = null;

        for (Map<String, Object> comp : components) {
            List<String> types = (List<String>) comp.get("types");
            String nome = (String) comp.get("long_name");

            if (types.contains("route")) rua = nome;
            else if (types.contains("sublocality") || types.contains("political")) bairro = nome;
            else if (types.contains("administrative_area_level_2")) cidade = nome;
            else if (types.contains("administrative_area_level_1")) estado = nome;
        }

        String lugar = (String) primeiroResultado.get("formatted_address");

        return new EnderecoDto(
                null, rua, cidade, estado, bairro,
                null, null, null, lugar, false
        );
    }
}
