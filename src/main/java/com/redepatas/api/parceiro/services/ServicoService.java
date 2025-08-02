package com.redepatas.api.parceiro.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.redepatas.api.parceiro.dtos.ServicoDtos.AdicionalResponseDTO;
import com.redepatas.api.parceiro.dtos.ServicoDtos.AtualizarServicoDTO;
import com.redepatas.api.parceiro.dtos.ServicoDtos.CriarAdicionalDTO;
import com.redepatas.api.parceiro.dtos.ServicoDtos.CriarServicoDTO;
import com.redepatas.api.parceiro.dtos.ServicoDtos.ServicoResponseDTO;
import com.redepatas.api.parceiro.models.AdicionaisModel;
import com.redepatas.api.parceiro.models.PartnerModel;
import com.redepatas.api.parceiro.models.ServicoModel;
import com.redepatas.api.parceiro.models.TipoServico;
import com.redepatas.api.parceiro.repositories.PartnerRepository;
import com.redepatas.api.parceiro.repositories.ServicoRepository;

@Service
public class ServicoService {

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    public ServicoResponseDTO criarServico(CriarServicoDTO dto) {
        // Buscar o parceiro
        Optional<PartnerModel> parceiroOpt = partnerRepository.findById(dto.getParceiroId());
        if (parceiroOpt.isEmpty()) {
            throw new IllegalArgumentException("Parceiro não encontrado com ID: " + dto.getParceiroId());
        }
        PartnerModel parceiro = parceiroOpt.get();

        // Validar se o tipo é permitido
        if (!TipoServico.isValid(dto.getTipo())) {
            throw new IllegalArgumentException(
                    "Tipo de serviço inválido: " + dto.getTipo() +
                            ". Tipos permitidos: BANHO, TOSA, CONSULTA");
        }

        // Verificar se já existe um serviço com mesmo nome e tipo para este parceiro
        if (servicoRepository.existsByNomeAndTipoAndParceiro(dto.getNome(), TipoServico.fromString(dto.getTipo()),
                parceiro)) {
            throw new IllegalArgumentException("Já existe um serviço com este nome e tipo para este parceiro");
        }

        ServicoModel servico = new ServicoModel();
        servico.setParceiro(parceiro); // Associar ao parceiro
        servico.setNome(dto.getNome());
        servico.setDescricao(dto.getDescricao());
        servico.setTipo(TipoServico.fromString(dto.getTipo()));
        servico.setPrecoPequeno(dto.getPrecoPequeno());

        // Lógica para pet grande
        Boolean aceitaPetGrande = dto.getAceitaPetGrande() != null ? dto.getAceitaPetGrande() : true;
        servico.setAceitaPetGrande(aceitaPetGrande);

        if (aceitaPetGrande) {
            // Se aceita pet grande, define como 0.0 inicialmente (usuário pode atualizar
            // depois)
            servico.setPrecoGrande(dto.getPrecoGrande() != null ? dto.getPrecoGrande() : 0.0);
        } else {
            // Se não aceita pet grande, define como null
            servico.setPrecoGrande(null);
        }

        // Processar adicionais se fornecidos
        if (dto.getAdicionais() != null && !dto.getAdicionais().isEmpty()) {
            List<AdicionaisModel> adicionais = dto.getAdicionais().stream()
                    .map(adicionalDTO -> {
                        AdicionaisModel adicional = converterAdicionalDTO(adicionalDTO);
                        return adicional;
                    })
                    .collect(Collectors.toList());
            servico.setAdicionais(adicionais);
        } else {
            servico.setAdicionais(new ArrayList<>());
        }

        ServicoModel servicoSalvo = servicoRepository.save(servico);

        return converterParaDTO(servicoSalvo);
    }

    // Método específico para listar serviços por parceiro
    public List<ServicoResponseDTO> listarServicosPorParceiro(UUID parceiroId) {
        return servicoRepository.findByParceiroIdPartner(parceiroId)
                .stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public void deletarServicoPorParceiro(UUID servicoId, UUID parceiroId) {
        Optional<ServicoModel> servicoOpt = servicoRepository.findById(servicoId);
        if (servicoOpt.isEmpty()) {
            throw new IllegalArgumentException("Serviço não encontrado com ID: " + servicoId);
        }

        ServicoModel servico = servicoOpt.get();
        if (!servico.getParceiro().getIdPartner().equals(parceiroId)) {
            throw new IllegalArgumentException("Você não tem permissão para deletar este serviço");
        }

        servicoRepository.deleteById(servicoId);
    }

    public ServicoResponseDTO atualizarServicoParcialPorParceiro(UUID id, AtualizarServicoDTO dto, UUID parceiroId) {
        Optional<ServicoModel> servicoOpt = servicoRepository.findById(id);
        if (servicoOpt.isEmpty()) {
            throw new IllegalArgumentException("Serviço não encontrado com ID: " + id);
        }

        ServicoModel servico = servicoOpt.get();

        // Verificar se o serviço pertence ao parceiro autenticado
        if (!servico.getParceiro().getIdPartner().equals(parceiroId)) {
            throw new IllegalArgumentException("Você não tem permissão para atualizar este serviço");
        }

        // Atualizar apenas os campos fornecidos
        if (dto.getNome() != null && !dto.getNome().trim().isEmpty()) {
            servico.setNome(dto.getNome());
        }
        if (dto.getDescricao() != null) {
            servico.setDescricao(dto.getDescricao());
        }
        if (dto.getPrecoPequeno() != null) {
            servico.setPrecoPequeno(dto.getPrecoPequeno());
        }

        // Lógica para aceitaPetGrande
        if (dto.getAceitaPetGrande() != null) {
            if (dto.getAceitaPetGrande()) {
                // Se passou a aceitar pet grande, define como 0.0 (usuário pode atualizar
                // depois)
                servico.setAceitaPetGrande(true);
                servico.setPrecoGrande(dto.getPrecoGrande() != null ? dto.getPrecoGrande() : 0.0);
            } else {
                // Se não aceita mais pet grande, define como null
                servico.setAceitaPetGrande(false);
                servico.setPrecoGrande(null);
            }
        } else if (dto.getPrecoGrande() != null) {
            // Se apenas o preço grande foi atualizado, verificar se aceita pet grande
            if (servico.getAceitaPetGrande()) {
                servico.setPrecoGrande(dto.getPrecoGrande());
            } else {
                throw new IllegalArgumentException("Este serviço não aceita pets grandes");
            }
        }

        // Processar adicionais se fornecidos
        if (dto.getAdicionais() != null) {
            // Limpar adicionais existentes
            servico.getAdicionais().clear();

            // Adicionar novos adicionais
            List<AdicionaisModel> novosAdicionais = dto.getAdicionais().stream()
                    .map(adicionalDTO -> {
                        AdicionaisModel adicional = converterAdicionalDTO(adicionalDTO);
                        return adicional;
                    })
                    .collect(Collectors.toList());
            servico.setAdicionais(novosAdicionais);
        }

        ServicoModel servicoAtualizado = servicoRepository.save(servico);

        return converterParaDTO(servicoAtualizado);
    }

    private ServicoResponseDTO converterParaDTO(ServicoModel servico) {
        ServicoResponseDTO dto = new ServicoResponseDTO();
        dto.setId(servico.getId());
        dto.setNome(servico.getNome());
        dto.setDescricao(servico.getDescricao());
        dto.setTipo(servico.getTipo());
        dto.setPrecoPequeno(servico.getPrecoPequeno());
        dto.setPrecoGrande(servico.getPrecoGrande());
        dto.setAceitaPetGrande(servico.getAceitaPetGrande());

        // Incluir informações do parceiro
        if (servico.getParceiro() != null) {
            dto.setParceiroId(servico.getParceiro().getIdPartner());
            dto.setNomeParceiro(servico.getParceiro().getName());
        }

        // Converter adicionais
        if (servico.getAdicionais() != null) {
            List<AdicionalResponseDTO> adicionaisDTO = servico.getAdicionais().stream()
                    .map(this::converterAdicionalParaDTO)
                    .collect(Collectors.toList());
            dto.setAdicionais(adicionaisDTO);
        } else {
            dto.setAdicionais(new ArrayList<>());
        }

        return dto;
    }

    private AdicionaisModel converterAdicionalDTO(CriarAdicionalDTO dto) {
        AdicionaisModel adicional = new AdicionaisModel();
        adicional.setNome(dto.getNome());
        adicional.setDescricao(dto.getDescricao());
        adicional.setPrecoPequeno(dto.getPrecoPequeno());
        if (dto.getPrecoGrande() != null) {
            adicional.setPrecoGrande(dto.getPrecoGrande());
        } else {
            adicional.setPrecoGrande(null);
        }
        return adicional;
    }

    private AdicionalResponseDTO converterAdicionalParaDTO(AdicionaisModel adicional) {
        AdicionalResponseDTO dto = new AdicionalResponseDTO();
        dto.setId(adicional.getId());
        dto.setNome(adicional.getNome());
        dto.setDescricao(adicional.getDescricao());
        dto.setPrecoPequeno(adicional.getPrecoPequeno());
        dto.setPrecoGrande(adicional.getPrecoGrande());
        return dto;
    }

    public List<String> listarTiposPermitidos() {
        return List.of("BANHO", "TOSA", "CONSULTA");
    }
}
