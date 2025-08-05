package com.redepatas.api.parceiro.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.redepatas.api.parceiro.dtos.ServicoDtos.AdicionalResponseDTO;
import com.redepatas.api.parceiro.dtos.ServicoDtos.AgendaResponseDTO;
import com.redepatas.api.parceiro.dtos.ServicoDtos.AgendaDiaResponseDTO;
import com.redepatas.api.parceiro.dtos.ServicoDtos.AgendaHorarioResponseDTO;
import com.redepatas.api.parceiro.dtos.ServicoDtos.AtualizarServicoDTO;
import com.redepatas.api.parceiro.dtos.ServicoDtos.CriarAdicionalDTO;
import com.redepatas.api.parceiro.dtos.ServicoDtos.CriarAgendaDTO;
import com.redepatas.api.parceiro.dtos.ServicoDtos.CriarAgendaDiaDTO;
import com.redepatas.api.parceiro.dtos.ServicoDtos.CriarAgendaHorarioDTO;
import com.redepatas.api.parceiro.dtos.ServicoDtos.CriarServicoDTO;
import com.redepatas.api.parceiro.dtos.ServicoDtos.ServicoResponseDTO;
import com.redepatas.api.parceiro.models.AdicionaisModel;
import com.redepatas.api.parceiro.models.AgendaDiaModel;
import com.redepatas.api.parceiro.models.AgendaHorarioModel;
import com.redepatas.api.parceiro.models.AgendaModel;
import com.redepatas.api.parceiro.models.PartnerModel;
import com.redepatas.api.parceiro.models.ServicoModel;
import com.redepatas.api.parceiro.models.TipoServico;
import com.redepatas.api.parceiro.models.Enum.DiaSemana;
import com.redepatas.api.parceiro.repositories.PartnerRepository;
import com.redepatas.api.parceiro.repositories.ServicoRepository;

@Service
public class ServicoService {

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    public ServicoResponseDTO criarServico(CriarServicoDTO dto) {
        Optional<PartnerModel> parceiroOpt = partnerRepository.findById(dto.getParceiroId());
        if (parceiroOpt.isEmpty()) {
            throw new IllegalArgumentException("Parceiro não encontrado com ID: " + dto.getParceiroId());
        }
        PartnerModel parceiro = parceiroOpt.get();

        if (!TipoServico.isValid(dto.getTipo())) {
            throw new IllegalArgumentException(
                    "Tipo de serviço inválido: " + dto.getTipo() +
                            ". Tipos permitidos: BANHO, TOSA, CONSULTA");
        }
        if (servicoRepository.existsByTipoAndParceiro(TipoServico.fromString(dto.getTipo()),
                parceiro)) {
            throw new IllegalArgumentException("Já existe um serviço com este nome e tipo para este parceiro");
        }

        ServicoModel servico = new ServicoModel();
        servico.setParceiro(parceiro);
        servico.setDescricao(dto.getDescricao());
        servico.setTipo(TipoServico.fromString(dto.getTipo()));
        servico.setPrecoPequeno(dto.getPrecoPequeno());

        Boolean aceitaPetGrande = dto.getAceitaPetGrande() != null ? dto.getAceitaPetGrande() : true;
        servico.setAceitaPetGrande(aceitaPetGrande);

        if (aceitaPetGrande) {
            servico.setPrecoGrande(dto.getPrecoGrande() != null ? dto.getPrecoGrande() : 0.0);
        } else {
            servico.setPrecoGrande(null);
        }

        if (dto.getAdicionais() != null && !dto.getAdicionais().isEmpty()) {
            List<AdicionaisModel> adicionais = dto.getAdicionais().stream()
                    .map(adicionalDTO -> {
                        AdicionaisModel adicional = converterAdicionalDTO(adicionalDTO);
                        if (adicional.getPrecoGrande() == null && aceitaPetGrande) {
                            throw new IllegalArgumentException("Preço grande não pode ser nulo se aceita pet grande");
                        }
                        return adicional;
                    })
                    .collect(Collectors.toList());
            servico.setAdicionais(adicionais);
        } else {
            servico.setAdicionais(new ArrayList<>());
        }

        if (dto.getAgenda() != null) {
            AgendaModel agenda = converterAgendaDTO(dto.getAgenda());
            servico.setAgenda(agenda);
            agenda.setServico(servico);
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

    public ServicoResponseDTO buscarServicoPorId(UUID servicoId, UUID parceiroId) {
        Optional<ServicoModel> servicoOpt = servicoRepository.findById(servicoId);
        if (servicoOpt.isEmpty()) {
            throw new IllegalArgumentException("Serviço não encontrado com ID: " + servicoId);
        }

        ServicoModel servico = servicoOpt.get();
        if (!servico.getParceiro().getIdPartner().equals(parceiroId)) {
            throw new IllegalArgumentException("Você não tem permissão para visualizar este serviço");
        }

        return converterParaDTO(servico);
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

        // Processar agenda se fornecida
        if (dto.getAgenda() != null) {
            // Se já existe uma agenda, remover a referência e salvar
            if (servico.getAgenda() != null) {
                servico.setAgenda(null);
                servicoRepository.saveAndFlush(servico);
            }
            
            // Criar nova agenda
            AgendaModel novaAgenda = converterAgendaDTO(dto.getAgenda());
            servico.setAgenda(novaAgenda);
            novaAgenda.setServico(servico);
        }

        ServicoModel servicoAtualizado = servicoRepository.save(servico);

        return converterParaDTO(servicoAtualizado);
    }

    private ServicoResponseDTO converterParaDTO(ServicoModel servico) {
        ServicoResponseDTO dto = new ServicoResponseDTO();
        dto.setId(servico.getId());
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

        // Converter agenda
        if (servico.getAgenda() != null) {
            AgendaResponseDTO agendaDTO = converterAgendaParaDTO(servico.getAgenda());
            dto.setAgenda(agendaDTO);
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

    private AgendaModel converterAgendaDTO(CriarAgendaDTO dto) {
        AgendaModel agenda = new AgendaModel();
        List<AgendaDiaModel> dias = new ArrayList<>();
        if (dto.getDias() != null) {
            for (CriarAgendaDiaDTO diaDTO : dto.getDias()) {
                AgendaDiaModel dia = new AgendaDiaModel();
                dia.setDiaSemana(DiaSemana.valueOf(diaDTO.getDiaSemana()));
                dia.setAgenda(agenda);

                List<AgendaHorarioModel> horarios = new ArrayList<>();
                if (diaDTO.getHorarios() != null) {
                    for (CriarAgendaHorarioDTO horarioDTO : diaDTO.getHorarios()) {
                        AgendaHorarioModel horario = new AgendaHorarioModel();
                        horario.setHorarioInicio(horarioDTO.getHorarioInicio());
                        horario.setHorarioFim(horarioDTO.getHorarioFim());
                        horario.setDia(dia);
                        horarios.add(horario);
                    }
                }
                dia.setHorarios(horarios);
                dias.add(dia);
            }
        }
        agenda.setDias(dias);
        return agenda;
    }

    public List<String> listarTiposPermitidos() {
        return List.of("BANHO", "TOSA", "CONSULTA");
    }

    private AgendaResponseDTO converterAgendaParaDTO(AgendaModel agenda) {
        AgendaResponseDTO dto = new AgendaResponseDTO();
        dto.setId(agenda.getId());
        
        if (agenda.getDias() != null) {
            List<AgendaDiaResponseDTO> diasDTO = agenda.getDias().stream()
                    .map(this::converterAgendaDiaParaDTO)
                    .collect(Collectors.toList());
            dto.setDias(diasDTO);
        } else {
            dto.setDias(new ArrayList<>());
        }
        
        return dto;
    }

    private AgendaDiaResponseDTO converterAgendaDiaParaDTO(AgendaDiaModel dia) {
        AgendaDiaResponseDTO dto = new AgendaDiaResponseDTO();
        dto.setId(dia.getId());
        dto.setDiaSemana(dia.getDiaSemana().name());
        
        if (dia.getHorarios() != null) {
            List<AgendaHorarioResponseDTO> horariosDTO = dia.getHorarios().stream()
                    .map(this::converterAgendaHorarioParaDTO)
                    .collect(Collectors.toList());
            dto.setHorarios(horariosDTO);
        } else {
            dto.setHorarios(new ArrayList<>());
        }
        
        return dto;
    }

    private AgendaHorarioResponseDTO converterAgendaHorarioParaDTO(AgendaHorarioModel horario) {
        AgendaHorarioResponseDTO dto = new AgendaHorarioResponseDTO();
        dto.setId(horario.getId());
        dto.setHorarioInicio(horario.getHorarioInicio());
        dto.setHorarioFim(horario.getHorarioFim());
        return dto;
    }

}
