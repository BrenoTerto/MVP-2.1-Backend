package com.redepatas.api.parceiro.dtos.ServicoDtos;

import lombok.Data;
import java.util.UUID;

@Data
public class AgendaHorarioResponseDTO {
    
    private UUID id;
    private String horarioInicio;
    private String horarioFim;
}
