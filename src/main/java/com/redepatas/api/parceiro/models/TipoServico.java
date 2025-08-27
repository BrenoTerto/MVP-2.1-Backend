package com.redepatas.api.parceiro.models;

public enum TipoServico {
    BANHO("Banho"),
    TOSA("Tosa"),
    TOSA_HIGIENICA("Tosa_Higienica"),
    CONSULTA("Consulta");

    private final String descricao;

    TipoServico(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static boolean isValid(String tipo) {
        if (tipo == null)
            return false;

        for (TipoServico tipoServico : TipoServico.values()) {
            if (tipoServico.name().equalsIgnoreCase(tipo) ||
                    tipoServico.getDescricao().equalsIgnoreCase(tipo)) {
                return true;
            }
        }
        return false;
    }

    public static TipoServico fromString(String tipo) {
        if (tipo == null)
            return null;

        for (TipoServico tipoServico : TipoServico.values()) {
            if (tipoServico.name().equalsIgnoreCase(tipo) ||
                    tipoServico.getDescricao().equalsIgnoreCase(tipo)) {
                return tipoServico;
            }
        }
        throw new IllegalArgumentException("Tipo de serviço inválido: " + tipo +
                ". Tipos permitidos: BANHO, TOSA, CONSULTA");
    }
}
