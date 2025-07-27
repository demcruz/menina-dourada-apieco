package br.com.ecommerce.meninadourada.controller;


import br.com.ecommerce.meninadourada.service.AiDescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * Controller REST para gerar descrições de produtos usando IA (Gemini API).
 * Expõe um endpoint para o frontend enviar uma imagem e receber uma descrição.
 */
@RestController
@RequestMapping("/api/ai") // Endpoint base para funcionalidades de IA
public class AiDescriptionController {

        private static final Logger logger = LoggerFactory.getLogger(AiDescriptionController.class);

        private final AiDescriptionService aiDescriptionService;

        @Autowired
        public AiDescriptionController(AiDescriptionService aiDescriptionService) {
            this.aiDescriptionService = aiDescriptionService;
        }

        /**
         * Endpoint HTTP POST para gerar uma descrição de produto a partir de uma imagem.
         * Recebe um arquivo de imagem e retorna uma descrição gerada por IA.
         *
         * @param file O arquivo de imagem do produto.
         * @return ResponseEntity contendo a descrição gerada e status 200 OK.
         */
        @PostMapping(value = "/generate-description", consumes = {"multipart/form-data"})
        public ResponseEntity<Map<String, String>> generateDescription(
                @RequestParam("image") MultipartFile file) { // O nome do campo no frontend deve ser 'image'
            try {
                logger.info("Recebida requisição para gerar descrição para imagem: {}", file.getOriginalFilename());
                String generatedDescription = aiDescriptionService.generateDescriptionFromImage(file);

                Map<String, String> response = new HashMap<>();
                response.put("description", generatedDescription);
                logger.info("Descrição gerada com sucesso para {}.", file.getOriginalFilename());
                return ResponseEntity.ok(response);
            } catch (IllegalArgumentException e) {
                logger.error("Erro de validação na requisição de geração de descrição: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                logger.error("Erro ao gerar descrição por IA para imagem {}: {}", file.getOriginalFilename(), e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Falha ao gerar descrição: " + e.getMessage()));
            }
        }
    }

