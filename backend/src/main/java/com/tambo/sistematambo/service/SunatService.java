package com.tambo.sistematambo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tambo.sistematambo.response.SunatRucResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SunatService {

    private final String token;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public SunatService(@Value("${sunat.api.token:}") String token, ObjectMapper objectMapper) {
        this.token = token;
        this.objectMapper = objectMapper;
    }

    public SunatRucResponse buscarPorRuc(String ruc) {
        String normalized = ruc == null ? "" : ruc.trim();
        if (!normalized.matches("\\d{11}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El RUC debe tener 11 digitos");
        }
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Token SUNAT no configurado en el servidor");
        }

        try {
            String encodedRuc = URLEncoder.encode(normalized, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.decolecta.com/v1/sunat/ruc?numero=" + encodedRuc))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo consultar SUNAT");
            }

            JsonNode data = objectMapper.readTree(response.body());
            return new SunatRucResponse(
                    text(data, "razon_social"),
                    text(data, "numero_documento"),
                    text(data, "estado"),
                    text(data, "condicion"),
                    text(data, "direccion"),
                    text(data, "distrito"),
                    text(data, "provincia"),
                    text(data, "departamento"),
                    bool(data, "es_agente_retencion"),
                    bool(data, "es_buen_contribuyente"));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Respuesta SUNAT invalida");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Consulta SUNAT interrumpida");
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asText();
    }

    private Boolean bool(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && !value.isNull() && value.asBoolean();
    }
}
