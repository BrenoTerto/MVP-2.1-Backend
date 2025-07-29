package com.redepatas.api.parceiro.controllers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.redepatas.api.parceiro.dtos.ServicoDtos.CriarServicoDTO;
import com.redepatas.api.parceiro.dtos.ServicoDtos.ServicoResponseDTO;
import com.redepatas.api.parceiro.models.TipoServico;
import com.redepatas.api.parceiro.services.ServicoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/servicos")
@Validated
public class ServicosController {
    
    @Autowired
    private ServicoService servicoService;
    
    @PostMapping
    public ResponseEntity<?> criarServico(@Valid @RequestBody CriarServicoDTO dto) {
        try {
            ServicoResponseDTO servico = servicoService.criarServico(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(servico);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<ServicoResponseDTO>> listarServicos() {
        List<ServicoResponseDTO> servicos = servicoService.listarServicos();
        return ResponseEntity.ok(servicos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarServicoPorId(@PathVariable UUID id) {
        Optional<ServicoResponseDTO> servico = servicoService.buscarServicoPorId(id);
        if (servico.isPresent()) {
            return ResponseEntity.ok(servico.get());
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<?> listarServicosPorTipo(@PathVariable String tipo) {
        try {
            TipoServico tipoServico = TipoServico.fromString(tipo);
            List<ServicoResponseDTO> servicos = servicoService.listarServicosPorTipo(tipoServico);
            return ResponseEntity.ok(servicos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarServico(@PathVariable UUID id) {
        try {
            servicoService.deletarServico(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }
    
    @GetMapping("/tipos-permitidos")
    public ResponseEntity<List<String>> listarTiposPermitidos() {
        List<String> tipos = servicoService.listarTiposPermitidos();
        return ResponseEntity.ok(tipos);
    }
}
