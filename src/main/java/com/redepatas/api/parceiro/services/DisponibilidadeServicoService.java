package com.redepatas.api.parceiro.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.redepatas.api.parceiro.dtos.DisponibilidadeDtos.AtualizarDisponibilidadeDTO;
import com.redepatas.api.parceiro.dtos.DisponibilidadeDtos.CriarDisponibilidadeDTO;
import com.redepatas.api.parceiro.dtos.DisponibilidadeDtos.DisponibilidadeResponseDTO;
import com.redepatas.api.parceiro.dtos.DisponibilidadeDtos.SlotsDisponiveisDTO;
import com.redepatas.api.parceiro.dtos.DisponibilidadeDtos.SlotHorarioDTO;
import com.redepatas.api.parceiro.models.DisponibilidadeServicoModel;
import com.redepatas.api.parceiro.models.PartnerModel;
import com.redepatas.api.parceiro.models.ServicoModel;
import com.redepatas.api.parceiro.models.Enum.DiaSemana;
import com.redepatas.api.parceiro.repositories.AgendamentoRepository;
import com.redepatas.api.parceiro.repositories.DisponibilidadeServicoRepository;
import com.redepatas.api.parceiro.repositories.PartnerRepository;
import com.redepatas.api.parceiro.repositories.ServicoRepository;

@Service
public class DisponibilidadeServicoService {

    @Autowired
    private DisponibilidadeServicoRepository disponibilidadeRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    public DisponibilidadeResponseDTO criarDisponibilidade(UUID servicoId, UUID parceiroId, CriarDisponibilidadeDTO dto) {
        // Buscar serviço
        ServicoModel servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));

        // Verificar se o serviço pertence ao parceiro autenticado
        if (!servico.getParceiro().getIdPartner().equals(parceiroId)) {
            throw new IllegalArgumentException("Você não tem permissão para criar disponibilidade para este serviço");
        }

        // Buscar parceiro
        PartnerModel parceiro = partnerRepository.findById(parceiroId)
                .orElseThrow(() -> new IllegalArgumentException("Parceiro não encontrado"));

        // Validar horários
        if (dto.horarioFim().isBefore(dto.horarioInicio()) || dto.horarioFim().equals(dto.horarioInicio())) {
            throw new IllegalArgumentException("Horário de fim deve ser posterior ao horário de início");
        }

        // Verificar se já existe disponibilidade para esse dia
        if (disponibilidadeRepository.existsByServicoIdAndDiaSemanaAndAtivoTrue(servicoId, dto.diaSemana())) {
            throw new IllegalArgumentException("Já existe disponibilidade ativa para este serviço neste dia da semana");
        }

        DisponibilidadeServicoModel disponibilidade = new DisponibilidadeServicoModel();
        disponibilidade.setServico(servico);
        disponibilidade.setParceiro(parceiro);
        disponibilidade.setDiaSemana(dto.diaSemana());
        disponibilidade.setHorarioInicio(dto.horarioInicio());
        disponibilidade.setHorarioFim(dto.horarioFim());
        disponibilidade.setDuracaoSlotMinutos(dto.duracaoSlotMinutos());
        disponibilidade.setAtivo(true);

        DisponibilidadeServicoModel savedDisponibilidade = disponibilidadeRepository.save(disponibilidade);

        return mapToResponseDTO(savedDisponibilidade);
    }

    public List<DisponibilidadeResponseDTO> listarDisponibilidadePorServico(UUID servicoId) {
        List<DisponibilidadeServicoModel> disponibilidades = 
                disponibilidadeRepository.findByServicoIdAndAtivoTrue(servicoId);
        
        return disponibilidades.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }
    
    public List<DisponibilidadeResponseDTO> listarDisponibilidadePorServico(UUID servicoId, UUID parceiroId) {
        // Verificar se o serviço pertence ao parceiro
        ServicoModel servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));
        
        if (!servico.getParceiro().getIdPartner().equals(parceiroId)) {
            throw new IllegalArgumentException("Você não tem permissão para ver disponibilidades deste serviço");
        }
        
        List<DisponibilidadeServicoModel> disponibilidades = 
                disponibilidadeRepository.findByServicoIdAndAtivoTrue(servicoId);
        
        return disponibilidades.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public List<DisponibilidadeResponseDTO> listarDisponibilidadePorParceiro(UUID parceiroId) {
        List<DisponibilidadeServicoModel> disponibilidades = 
                disponibilidadeRepository.findByParceiroIdAndAtivoTrue(parceiroId);
        
        return disponibilidades.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public DisponibilidadeResponseDTO atualizarDisponibilidade(UUID disponibilidadeId, AtualizarDisponibilidadeDTO dto) {
        DisponibilidadeServicoModel disponibilidade = disponibilidadeRepository.findById(disponibilidadeId)
                .orElseThrow(() -> new IllegalArgumentException("Disponibilidade não encontrada"));

        if (dto.horarioInicio() != null) {
            disponibilidade.setHorarioInicio(dto.horarioInicio());
        }
        
        if (dto.horarioFim() != null) {
            disponibilidade.setHorarioFim(dto.horarioFim());
        }

        // Validar horários se ambos foram fornecidos ou se um foi alterado
        if (disponibilidade.getHorarioFim().isBefore(disponibilidade.getHorarioInicio()) || 
            disponibilidade.getHorarioFim().equals(disponibilidade.getHorarioInicio())) {
            throw new IllegalArgumentException("Horário de fim deve ser posterior ao horário de início");
        }

        if (dto.duracaoSlotMinutos() != null) {
            disponibilidade.setDuracaoSlotMinutos(dto.duracaoSlotMinutos());
        }

        if (dto.ativo() != null) {
            disponibilidade.setAtivo(dto.ativo());
        }

        DisponibilidadeServicoModel savedDisponibilidade = disponibilidadeRepository.save(disponibilidade);
        return mapToResponseDTO(savedDisponibilidade);
    }

    public void deletarDisponibilidade(UUID disponibilidadeId) {
        if (!disponibilidadeRepository.existsById(disponibilidadeId)) {
            throw new IllegalArgumentException("Disponibilidade não encontrada");
        }
        disponibilidadeRepository.deleteById(disponibilidadeId);
    }

    public List<SlotsDisponiveisDTO> obterSlotsDisponiveis(UUID servicoId, LocalDate data) {
        // Converter LocalDate para DiaSemana
        DayOfWeek dayOfWeek = data.getDayOfWeek();
        DiaSemana diaSemana = DiaSemana.valueOf(dayOfWeek.name());

        List<DisponibilidadeServicoModel> disponibilidades = 
                disponibilidadeRepository.findByServicoIdAndDiaSemanaAndAtivoTrue(servicoId, diaSemana);

        List<SlotsDisponiveisDTO> slotsDisponiveis = new ArrayList<>();

        for (DisponibilidadeServicoModel disponibilidade : disponibilidades) {
            List<SlotHorarioDTO> slots = gerarSlots(disponibilidade, data);
            slotsDisponiveis.add(new SlotsDisponiveisDTO(diaSemana, slots));
        }

        return slotsDisponiveis;
    }

    private List<SlotHorarioDTO> gerarSlots(DisponibilidadeServicoModel disponibilidade, LocalDate data) {
        List<SlotHorarioDTO> slots = new ArrayList<>();
        
        LocalTime horarioAtual = disponibilidade.getHorarioInicio();
        LocalTime horarioFim = disponibilidade.getHorarioFim();
        int duracaoMinutos = disponibilidade.getDuracaoSlotMinutos();

        while (horarioAtual.plusMinutes(duracaoMinutos).isBefore(horarioFim) || 
               horarioAtual.plusMinutes(duracaoMinutos).equals(horarioFim)) {
            
            LocalTime inicioSlot = horarioAtual;
            LocalTime fimSlot = horarioAtual.plusMinutes(duracaoMinutos);

            // Verificar se há conflitos com agendamentos existentes
            boolean disponivel = agendamentoRepository.findConflitosHorario(
                    disponibilidade.getParceiro().getId(), 
                    data, 
                    inicioSlot, 
                    fimSlot
            ).isEmpty();

            slots.add(new SlotHorarioDTO(inicioSlot, fimSlot, disponivel));
            horarioAtual = horarioAtual.plusMinutes(duracaoMinutos);
        }

        return slots;
    }

    private DisponibilidadeResponseDTO mapToResponseDTO(DisponibilidadeServicoModel disponibilidade) {
        return new DisponibilidadeResponseDTO(
                disponibilidade.getId(),
                disponibilidade.getDiaSemana(),
                disponibilidade.getHorarioInicio(),
                disponibilidade.getHorarioFim(),
                disponibilidade.getDuracaoSlotMinutos(),
                disponibilidade.getAtivo(),
                disponibilidade.getServico().getNome()
        );
    }
}
