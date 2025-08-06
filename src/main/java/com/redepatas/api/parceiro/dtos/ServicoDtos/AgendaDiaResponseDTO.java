package com.redepatas.api.parceiro.dtos.ServicoDtos;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class AgendaDiaResponseDTO {
    
    private UUID id;
    private String diaSemana;
    private List<AgendaHorarioResponseDTO> horarios;
}
