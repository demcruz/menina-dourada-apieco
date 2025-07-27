package br.com.ecommerce.meninadourada.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class AiDescriptionService {


    private static final Logger logger = LoggerFactory.getLogger(AiDescriptionService.class);
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // A chave da API do Gemini (deve ser configurada no application.properties)
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    // A URL do endpoint da API do Gemini para o modelo gemini-2.0-flash
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    @Autowired
    public AiDescriptionService(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }

    /**
     * Gera uma descrição de texto para um produto com base em uma imagem.
     *
     * @param imageFile O arquivo de imagem do produto.
     * @return A descrição gerada pela IA.
     * @throws IOException Se houver um erro de IO ao ler o arquivo.
     * @throws InterruptedException Se a requisição HTTP for interrompida.
     * @throws RuntimeException Se a resposta da API do Gemini for inválida ou ocorrer outro erro.
     */
    public String generateDescriptionFromImage(MultipartFile imageFile) throws IOException, InterruptedException {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("O arquivo de imagem não pode ser nulo ou vazio.");
        }

        logger.info("Iniciando geração de descrição para imagem: {}", imageFile.getOriginalFilename());

        // Converte a imagem para Base64
        String base64ImageData = Base64.getEncoder().encodeToString(imageFile.getBytes());

        // Monta o payload para a API do Gemini
        ObjectNode payload = objectMapper.createObjectNode();
        ArrayNode contents = payload.putArray("contents");
        ObjectNode userContent = contents.addObject();
        ArrayNode parts = userContent.putArray("parts");

        // Adiciona o prompt de texto
        parts.addObject().put("text", "Descreva esta imagem de produto de forma concisa e atraente para um e-commerce. Mencione características visuais, cores, material aparente e uso.");

        // Adiciona a imagem em Base64
        ObjectNode inlineData = parts.addObject().putObject("inlineData");
        inlineData.put("mimeType", imageFile.getContentType());
        inlineData.put("data", base64ImageData);

        // Constrói a requisição HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_API_URL + "?key=" + geminiApiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                .build();

        // Envia a requisição e recebe a resposta
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode rootNode = objectMapper.readTree(response.body());
            // Extrai o texto da resposta do Gemini
            JsonNode textNode = rootNode.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            if (textNode.isTextual()) {
                String generatedText = textNode.asText();
                logger.info("Descrição gerada pela IA para {}: {}", imageFile.getOriginalFilename(), generatedText);
                return generatedText;
            } else {
                logger.error("Resposta da API do Gemini não contém texto esperado: {}", response.body());
                throw new RuntimeException("Resposta da API do Gemini inválida ou sem texto.");
            }
        } else {
            logger.error("Erro na API do Gemini. Status: {}, Resposta: {}", response.statusCode(), response.body());
            throw new RuntimeException("Falha ao gerar descrição pela IA. Status: " + response.statusCode() + ", Erro: " + response.body());
        }
    }
}

