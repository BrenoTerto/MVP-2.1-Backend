package com.redepatas.api.parceiro.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.redepatas.api.parceiro.dtos.ServicoDtos.AdicionalResponseDTO;
import com.redepatas.api.parceiro.dtos.ServicoDtos.CriarAdicionalDTO;
import com.redepatas.api.parceiro.models.AdicionaisModel;
import com.redepatas.api.parceiro.models.ServicoModel;
import com.redepatas.api.parceiro.repositories.AdicionaisRepository;
import com.redepatas.api.parceiro.repositories.ServicoRepository;

@Service
public class AdicionaisService {

    @Autowired
    private AdicionaisRepository adicionaisRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    public AdicionalResponseDTO adicionarAdicional(UUID servicoId, CriarAdicionalDTO dto, UUID parceiroId) {
        // Verificar se o serviço existe e pertence ao parceiro
        Optional<ServicoModel> servicoOpt = servicoRepository.findById(servicoId);
        if (servicoOpt.isEmpty()) {
            throw new IllegalArgumentException("Serviço não encontrado com ID: " + servicoId);
        }

        ServicoModel servico = servicoOpt.get();

        // Verificar se o serviço pertence ao parceiro autenticado
        if (!servico.getParceiro().getIdPartner().equals(parceiroId)) {
            throw new IllegalArgumentException("Você não tem permissão para adicionar adicionais neste serviço");
        }

        // Verificar se já existe um adicional com o mesmo nome para este serviço
        if (adicionaisRepository.existsByNomeAndServicoId(dto.getNome(), servicoId)) {
            throw new IllegalArgumentException("Já existe um adicional com este nome para este serviço");
        }

        AdicionaisModel adicional = new AdicionaisModel();
        adicional.setNome(dto.getNome());
        adicional.setDescricao(dto.getDescricao());
        adicional.setPrecoPequeno(dto.getPrecoPequeno());

        if (dto.getPrecoGrande() != null) {
            adicional.setPrecoGrande(dto.getPrecoGrande());
        } else {
            adicional.setPrecoGrande(null);
        }

        // Adicionar o adicional à lista do serviço
        if (servico.getAdicionais() == null) {
            servico.setAdicionais(new ArrayList<>());
        }
        servico.getAdicionais().add(adicional);

        // Salvar o serviço (cascade ALL vai salvar o adicional também)
        ServicoModel servicoSalvo = servicoRepository.save(servico);

        // Pegar o adicional salvo (último da lista)
        AdicionaisModel adicionalSalvo = servicoSalvo.getAdicionais().get(servicoSalvo.getAdicionais().size() - 1);

        return converterParaDTO(adicionalSalvo);
    }

    public List<AdicionalResponseDTO> listarAdicionaisPorServico(UUID servicoId, UUID parceiroId) {
        // Verificar se o serviço existe e pertence ao parceiro
        Optional<ServicoModel> servicoOpt = servicoRepository.findById(servicoId);
        if (servicoOpt.isEmpty()) {
            throw new IllegalArgumentException("Serviço não encontrado com ID: " + servicoId);
        }

        ServicoModel servico = servicoOpt.get();

        // Verificar se o serviço pertence ao parceiro autenticado
        if (!servico.getParceiro().getIdPartner().equals(parceiroId)) {
            throw new IllegalArgumentException("Você não tem permissão para ver adicionais deste serviço");
        }

        return adicionaisRepository.findByServicoId(servicoId)
                .stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public AdicionalResponseDTO atualizarAdicional(UUID adicionalId, CriarAdicionalDTO dto, UUID servicoId,
            UUID parceiroId) {
        // Verificar se o serviço existe e pertence ao parceiro
        Optional<ServicoModel> servicoOpt = servicoRepository.findById(servicoId);
        if (servicoOpt.isEmpty()) {
            throw new IllegalArgumentException("Serviço não encontrado com ID: " + servicoId);
        }

        ServicoModel servico = servicoOpt.get();

        // Verificar se o serviço pertence ao parceiro autenticado
        if (!servico.getParceiro().getIdPartner().equals(parceiroId)) {
            throw new IllegalArgumentException("Você não tem permissão para atualizar adicionais deste serviço");
        }

        // Verificar se o adicional existe e pertence ao serviço
        Optional<AdicionaisModel> adicionalOpt = adicionaisRepository.findById(adicionalId);
        if (adicionalOpt.isEmpty()) {
            throw new IllegalArgumentException("Adicional não encontrado com ID: " + adicionalId);
        }

        // Verificar se o adicional pertence ao serviço através da lista de adicionais
        // do serviço
        List<AdicionaisModel> adicionaisDoServico = adicionaisRepository.findByServicoId(servicoId);
        AdicionaisModel adicional = adicionalOpt.get();

        if (!adicionaisDoServico.contains(adicional)) {
            throw new IllegalArgumentException("Este adicional não pertence ao serviço especificado");
        }

        adicional.setNome(dto.getNome());
        adicional.setDescricao(dto.getDescricao());
        adicional.setPrecoPequeno(dto.getPrecoPequeno());

        if (dto.getPrecoGrande() != null) {
            adicional.setPrecoGrande(dto.getPrecoGrande());
        } else {
            adicional.setPrecoGrande(null);
        }

        AdicionaisModel adicionalAtualizado = adicionaisRepository.save(adicional);

        return converterParaDTO(adicionalAtualizado);
    }

    public void deletarAdicional(UUID adicionalId, UUID servicoId, UUID parceiroId) {
        // Verificar se o serviço existe e pertence ao parceiro
        Optional<ServicoModel> servicoOpt = servicoRepository.findById(servicoId);
        if (servicoOpt.isEmpty()) {
            throw new IllegalArgumentException("Serviço não encontrado com ID: " + servicoId);
        }

        ServicoModel servico = servicoOpt.get();

        // Verificar se o serviço pertence ao parceiro autenticado
        if (!servico.getParceiro().getIdPartner().equals(parceiroId)) {
            throw new IllegalArgumentException("Você não tem permissão para deletar adicionais deste serviço");
        }

        // Verificar se o adicional existe
        if (!adicionaisRepository.existsById(adicionalId)) {
            throw new IllegalArgumentException("Adicional não encontrado com ID: " + adicionalId);
        }

        // Verificar se o adicional pertence ao serviço
        List<AdicionaisModel> adicionaisDoServico = adicionaisRepository.findByServicoId(servicoId);
        boolean adicionalPertenceAoServico = adicionaisDoServico.stream()
                .anyMatch(adicional -> adicional.getId().equals(adicionalId));

        if (!adicionalPertenceAoServico) {
            throw new IllegalArgumentException("Este adicional não pertence ao serviço especificado");
        }

        adicionaisRepository.deleteById(adicionalId);
    }

    private AdicionalResponseDTO converterParaDTO(AdicionaisModel adicional) {
        AdicionalResponseDTO dto = new AdicionalResponseDTO();
        dto.setId(adicional.getId());
        dto.setNome(adicional.getNome());
        dto.setDescricao(adicional.getDescricao());
        dto.setPrecoPequeno(adicional.getPrecoPequeno());
        dto.setPrecoGrande(adicional.getPrecoGrande());
        return dto;
    }
}
