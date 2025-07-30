package com.redepatas.api.parceiro.controllers;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * Handler global de exceções para o módulo Partner
 * Centraliza o tratamento de erros relacionados ao cadastro de parceiros
 */
@ControllerAdvice(basePackages = "com.redepatas.api.parceiro.controllers")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PartnerExceptionHandler {

    /**
     * Trata erros de campos obrigatórios ausentes no multipart form
     * Prioridade alta para interceptar antes do DefaultHandlerExceptionResolver
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<String> handleMissingRequestPart(MissingServletRequestPartException ex) {
        String partName = ex.getRequestPartName();
        String message;
        
        if ("partnerData".equals(partName)) {
            message = "Os dados do parceiro são obrigatórios. Certifique-se de incluir o campo 'partnerData' com o JSON dos dados do parceiro no Multipart Form.";
        } else if ("image".equals(partName)) {
            message = "A imagem é obrigatória. Certifique-se de incluir o campo 'image' com o arquivo de imagem no Multipart Form.";
        } else {
            message = "Campo obrigatório ausente: " + partName + ". Verifique se está usando Multipart Form com os campos corretos.";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    /**
     * Trata erros de arquivos que excedem o tamanho máximo permitido
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("O arquivo enviado é muito grande. Tamanho máximo permitido excedido.");
    }

    /**
     * Trata erros de validação de campos do DTO
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        StringBuilder message = new StringBuilder("Dados inválidos: ");
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            message.append(error.getField()).append(" - ").append(error.getDefaultMessage()).append("; ")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message.toString());
    }

    /**
     * Trata erros gerais de parsing de JSON
     */
    @ExceptionHandler(com.fasterxml.jackson.core.JsonProcessingException.class)
    public ResponseEntity<String> handleJsonProcessingException(com.fasterxml.jackson.core.JsonProcessingException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("JSON inválido nos dados do parceiro. Verifique a formatação: " + ex.getMessage());
    }

    /**
     * Trata erros de I/O durante o processamento de arquivos
     */
    @ExceptionHandler(java.io.IOException.class)
    public ResponseEntity<String> handleIOException(java.io.IOException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao processar arquivo: " + ex.getMessage());
    }

    /**
     * Trata ResponseStatusException lançadas pelos services
     */
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(org.springframework.web.server.ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(ex.getReason());
    }

    /**
     * Trata DataIntegrityViolationException (violações de constraints do banco)
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(org.springframework.dao.DataIntegrityViolationException ex) {
        String message = ex.getMessage();
        
        if (message.contains("login")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Já existe um parceiro com este login.");
        } else if (message.contains("email")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Já existe um parceiro com este email.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Violação de restrição de dados: Alguns dados já existem no sistema.");
        }
    }

    /**
     * Handler genérico para capturar exceções não tratadas especificamente
     * Útil para debug quando exceções não estão sendo capturadas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        ex.printStackTrace();
        
        String message = ex.getMessage();
        
        // Tratar exceções específicas de banco de dados
        if (ex.getClass().getSimpleName().contains("TransactionSystemException") ||
            message.contains("could not execute statement")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro de validação: Verifique se todos os campos obrigatórios estão preenchidos corretamente.");
        }
        
        // Tratar violações de constraint unique
        if (message.contains("login") || message.contains("duplicate key")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Já existe um parceiro com este login.");
        }
        
        // Tratar erros de argumentos inválidos
        if (ex instanceof IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Dados inválidos: " + message);
        }
        
        // Tratar erros de runtime do service
        if (ex instanceof RuntimeException && message != null) {
            if (message.contains("login")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Já existe um parceiro com este login.");
            } else if (message.contains("email")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Já existe um parceiro com este email.");
            }
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("ERRO INTERNO: " + ex.getClass().getSimpleName() + " - " + message);
    }
}
