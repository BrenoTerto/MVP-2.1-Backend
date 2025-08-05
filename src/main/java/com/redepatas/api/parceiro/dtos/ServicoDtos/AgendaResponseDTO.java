package com.redepatas.api.parceiro.dtos.ServicoDtos;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class AgendaResponseDTO {
    
    private UUID id;
    private List<AgendaDiaResponseDTO> dias;
}
