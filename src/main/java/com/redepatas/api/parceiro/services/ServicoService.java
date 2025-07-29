package com.redepatas.api.parceiro.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.redepatas.api.parceiro.dtos.ServicoDtos.CriarServicoDTO;
import com.redepatas.api.parceiro.dtos.ServicoDtos.ServicoResponseDTO;
import com.redepatas.api.parceiro.models.ServicoModel;
import com.redepatas.api.parceiro.models.TipoServico;
import com.redepatas.api.parceiro.repositories.ServicoRepository;

@Service
public class ServicoService {
    
    @Autowired
    private ServicoRepository servicoRepository;
    
    public ServicoResponseDTO criarServico(CriarServicoDTO dto) {
        // Validar se o tipo é permitido
        if (!TipoServico.isValid(dto.getTipo())) {
            throw new IllegalArgumentException(
                "Tipo de serviço inválido: " + dto.getTipo() + 
                ". Tipos permitidos: BANHO, TOSA, CONSULTA");
        }
        
        ServicoModel servico = new ServicoModel();
        servico.setNome(dto.getNome());
        servico.setTipo(TipoServico.fromString(dto.getTipo()));
        servico.setPrecoPequeno(dto.getPrecoPequeno());
        servico.setPrecoGrande(dto.getPrecoGrande() != null ? dto.getPrecoGrande() : 0.0);
        
        ServicoModel servicoSalvo = servicoRepository.save(servico);
        
        return converterParaDTO(servicoSalvo);
    }
    
    public List<ServicoResponseDTO> listarServicos() {
        return servicoRepository.findAll()
                .stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<ServicoResponseDTO> buscarServicoPorId(UUID id) {
        return servicoRepository.findById(id)
                .map(this::converterParaDTO);
    }
    
    public List<ServicoResponseDTO> listarServicosPorTipo(TipoServico tipo) {
        return servicoRepository.findByTipo(tipo)
                .stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }
    
    public void deletarServico(UUID id) {
        if (!servicoRepository.existsById(id)) {
            throw new IllegalArgumentException("Serviço não encontrado com ID: " + id);
        }
        servicoRepository.deleteById(id);
    }
    
    private ServicoResponseDTO converterParaDTO(ServicoModel servico) {
        ServicoResponseDTO dto = new ServicoResponseDTO();
        dto.setId(servico.getId());
        dto.setNome(servico.getNome());
        dto.setTipo(servico.getTipo());
        dto.setPrecoPequeno(servico.getPrecoPequeno());
        dto.setPrecoGrande(servico.getPrecoGrande());
        return dto;
    }
    
    public List<String> listarTiposPermitidos() {
        return List.of("BANHO", "TOSA", "CONSULTA");
    }
}
