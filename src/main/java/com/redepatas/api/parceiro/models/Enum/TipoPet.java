package com.redepatas.api.parceiro.models.Enum;

public enum TipoPet {
    TODOS,
    GRANDE,
    PEQUENO;

    public static TipoPet fromString(String value) {
        if (value == null) return null;
        String norm = value.trim().toUpperCase();
        for (TipoPet tp : values()) {
            if (tp.name().equals(norm)) return tp;
        }
        throw new IllegalArgumentException("Tipo de pet inválido: " + value + 
                ". Tipos permitidos: TODOS, GRANDE, PEQUENO");
    }
}

