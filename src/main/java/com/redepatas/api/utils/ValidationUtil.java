package com.redepatas.api.utils;

/**
 * Utilitário para validação de documentos brasileiros (CPF e CNPJ)
 */
public class ValidationUtil {

    /**
     * Valida um CPF brasileiro
     * @param cpf O CPF a ser validado (pode conter ou não formatação)
     * @return true se o CPF for válido, false caso contrário
     */
    public static boolean isCPFValido(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return false;
        }

        // Remove caracteres não numéricos
        cpf = cpf.replaceAll("[^\\d]", "");

        // Verifica se tem 11 dígitos e não é uma sequência repetida
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {
            // Calcula o primeiro dígito verificador
            int soma = 0, peso = 10;
            for (int i = 0; i < 9; i++) {
                soma += (cpf.charAt(i) - '0') * peso--;
            }
            int digito1 = 11 - (soma % 11);
            digito1 = (digito1 >= 10) ? 0 : digito1;

            // Calcula o segundo dígito verificador
            soma = 0;
            peso = 11;
            for (int i = 0; i < 10; i++) {
                soma += (cpf.charAt(i) - '0') * peso--;
            }
            int digito2 = 11 - (soma % 11);
            digito2 = (digito2 >= 10) ? 0 : digito2;

            // Verifica se os dígitos verificadores estão corretos
            return cpf.charAt(9) - '0' == digito1 && cpf.charAt(10) - '0' == digito2;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Valida um CNPJ brasileiro
     * @param cnpj O CNPJ a ser validado (pode conter ou não formatação)
     * @return true se o CNPJ for válido, false caso contrário
     */
    public static boolean isCNPJValido(String cnpj) {
        if (cnpj == null || cnpj.trim().isEmpty()) {
            return false;
        }

        // Remove caracteres não numéricos
        cnpj = cnpj.replaceAll("[^\\d]", "");

        // Verifica se tem 14 dígitos e não é uma sequência repetida
        if (cnpj.length() != 14 || cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }

        try {
            // Calcula o primeiro dígito verificador
            int soma = 0;
            int[] peso1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            for (int i = 0; i < 12; i++) {
                soma += (cnpj.charAt(i) - '0') * peso1[i];
            }
            int digito1 = 11 - (soma % 11);
            digito1 = (digito1 >= 10) ? 0 : digito1;

            // Calcula o segundo dígito verificador
            soma = 0;
            int[] peso2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            for (int i = 0; i < 13; i++) {
                soma += (cnpj.charAt(i) - '0') * peso2[i];
            }
            int digito2 = 11 - (soma % 11);
            digito2 = (digito2 >= 10) ? 0 : digito2;

            // Verifica se os dígitos verificadores estão corretos
            return cnpj.charAt(12) - '0' == digito1 && cnpj.charAt(13) - '0' == digito2;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Valida se um documento é um CPF ou CNPJ válido
     * @param documento O documento a ser validado (pode conter ou não formatação)
     * @return true se for um CPF ou CNPJ válido, false caso contrário
     */
    public static boolean isDocumentoValido(String documento) {
        if (documento == null || documento.trim().isEmpty()) {
            return false;
        }

        // Remove caracteres não numéricos para verificar o tamanho
        String documentoLimpo = documento.replaceAll("[^\\d]", "");

        // Verifica se é CPF (11 dígitos) ou CNPJ (14 dígitos)
        if (documentoLimpo.length() == 11) {
            return isCPFValido(documento);
        } else if (documentoLimpo.length() == 14) {
            return isCNPJValido(documento);
        } else {
            return false;
        }
    }

    /**
     * Identifica o tipo de documento
     * @param documento O documento a ser identificado
     * @return "CPF", "CNPJ" ou "INVALIDO"
     */
    public static String getTipoDocumento(String documento) {
        if (documento == null || documento.trim().isEmpty()) {
            return "INVALIDO";
        }

        String documentoLimpo = documento.replaceAll("[^\\d]", "");

        if (documentoLimpo.length() == 11 && isCPFValido(documento)) {
            return "CPF";
        } else if (documentoLimpo.length() == 14 && isCNPJValido(documento)) {
            return "CNPJ";
        } else {
            return "INVALIDO";
        }
    }
}
