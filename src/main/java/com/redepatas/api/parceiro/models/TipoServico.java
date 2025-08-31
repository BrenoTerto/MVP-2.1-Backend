package com.redepatas.api.parceiro.models;

public enum TipoServico {
    BANHO("Banho"),
    TOSA("Tosa"),
    TOSA_HIGIENICA("Tosa Higienica"),
    CONSULTA("Consulta"),
    TAXI_DOG("Taxi DOG"),
    HOTELZINHO("Hotelzinho"),
    PRODUTOS("Produtos"),
    RACOES("Rações"),
    ADESTRADOR("Adestrador"),
    VACINAS("Vacinas"),
    EXAMES("Exames"),
    MEDICAMENTOS("Medicamentos"),
    SERVICO_EM_CASA("Serviço em Casa"),
    CLINICO_24H("Clínico 24h"),
    BRINDES_EXCLUSIVOS("Brindes Exclusivos");

    private final String descricao;

    TipoServico(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static boolean isValid(String tipo) {
        if (tipo == null) return false;
        for (TipoServico ts : TipoServico.values()) {
            if (ts.name().equalsIgnoreCase(tipo) || ts.getDescricao().equalsIgnoreCase(tipo)) {
                return true;
            }
        }
        return false;
    }

    public static TipoServico fromString(String tipo) {
        if (tipo == null) return null;
        for (TipoServico ts : TipoServico.values()) {
            if (ts.name().equalsIgnoreCase(tipo) || ts.getDescricao().equalsIgnoreCase(tipo)) {
                return ts;
            }
        }
        throw new IllegalArgumentException("Tipo de serviço inválido: " + tipo);
    }
}
