package com.redepatas.api.parceiro.dtos.AgendamentoDtos;

import jakarta.validation.constraints.NotNull;

public class AtualizarStatusAgendamentoDTO {
    @NotNull
    private Boolean aceitar;

    public Boolean getAceitar() {
        return aceitar;
    }

    public void setAceitar(Boolean aceitar) {
        this.aceitar = aceitar;
    }
}

