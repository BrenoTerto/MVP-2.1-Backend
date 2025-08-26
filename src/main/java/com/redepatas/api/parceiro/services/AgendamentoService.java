package com.redepatas.api.parceiro.services;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.redepatas.api.cliente.models.ClientModel;
import com.redepatas.api.cliente.models.PetModel;
import com.redepatas.api.cliente.repositories.ClientRepository;
import com.redepatas.api.cliente.repositories.PetRepository;
import com.redepatas.api.parceiro.dtos.AgedamentoDtos.AgendamentoNotificationDto;
import com.redepatas.api.parceiro.dtos.AgedamentoDtos.AvaliarServicoDto;
import com.redepatas.api.parceiro.dtos.AgedamentoDtos.ResponseAgendamentosdto;
import com.redepatas.api.parceiro.dtos.AgendamentoDtos.AgendamentoDetalheResponseDTO;
import com.redepatas.api.parceiro.dtos.AgendamentoDtos.AgendamentoListaResponseDTO;
import com.redepatas.api.parceiro.dtos.AgendamentoDtos.AgendamentoResponseDTO;
import com.redepatas.api.parceiro.dtos.AgendamentoDtos.CriarAgendamentoDTO;
import com.redepatas.api.parceiro.models.AdicionaisModel;
import com.redepatas.api.parceiro.models.AgendaDiaModel;
import com.redepatas.api.parceiro.models.AgendaHorarioModel;
import com.redepatas.api.parceiro.models.AgendamentoAdicionalModel;
import com.redepatas.api.parceiro.models.AgendamentoModel;
import com.redepatas.api.parceiro.models.PartnerModel;
import com.redepatas.api.parceiro.models.ServicoModel;
import com.redepatas.api.parceiro.models.Enum.DiaSemana;
import com.redepatas.api.parceiro.models.Enum.StatusAgendamento;
import com.redepatas.api.parceiro.repositories.AdicionaisRepository;
import com.redepatas.api.parceiro.repositories.AgendamentoRepository;
import com.redepatas.api.parceiro.repositories.PartnerRepository;
import com.redepatas.api.parceiro.repositories.ServicoRepository;


@Service
public class AgendamentoService {

    @Autowired
    private AgendamentoRepository agendamentoRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ServicoRepository servicoRepository;
    @Autowired
    private PetRepository petRepository;
    @Autowired
    private PartnerRepository partnerRepository;
    @Autowired
    private AdicionaisRepository adicionaisRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public DiaSemana mapearDiaSemana(LocalDate data) {
        switch (data.getDayOfWeek()) {
            case MONDAY:
                return DiaSemana.SEGUNDA;
            case TUESDAY:
                return DiaSemana.TERCA;
            case WEDNESDAY:
                return DiaSemana.QUARTA;
            case THURSDAY:
                return DiaSemana.QUINTA;
            case FRIDAY:
                return DiaSemana.SEXTA;
            case SATURDAY:
                return DiaSemana.SABADO;
            case SUNDAY:
                return DiaSemana.DOMINGO;
            default:
                throw new IllegalArgumentException("Dia da semana inválido");
        }
    }

    public String avaliarServico(String login, AvaliarServicoDto data) {
        ClientModel cliente = (ClientModel) clientRepository.findByLogin(login);
        if (cliente == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario nao encotrado/logado");
        }
        AgendamentoModel agendamento = agendamentoRepository.findById(data.idAgendamento())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Agendamento não encontrado"));
        LocalDateTime agora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime horaInicio = LocalTime.parse(agendamento.getHorario().getHorarioInicio(), formatter);
        LocalDateTime dataHoraAgendamento = LocalDateTime.of(agendamento.getDataAgendamento(), horaInicio);
        Duration duration = Duration.between(dataHoraAgendamento, agora);
        if (agendamento.getCliente().getLogin().equals(login)) {
            if (duration.toHours() < 2) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Esse serviço so pode ser avaliado após duas horas!");
            }
            if (agendamento.getAvaliado() || agendamento.getAvaliado() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serviço já avaliado!");
            }
            PartnerModel parceiro = agendamento.getParceiro();
            parceiro.setAvaliacao(parceiro.getAvaliacao() + data.nota());
            parceiro.setQtdAvaliacoes(parceiro.getQtdAvaliacoes() + 1);
            partnerRepository.save(parceiro);
            agendamento.setAvaliado(true);
            agendamentoRepository.save(agendamento);
            return "Serviço avaliado com sucesso!";
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Esse agendamento não pertence ao cliente!");
        }

    }

    @Transactional
    public AgendamentoResponseDTO criarAgendamento(CriarAgendamentoDTO dto, UUID clientId) {
        ServicoModel servico = servicoRepository.findById(dto.getServicoId())
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));
        ClientModel cliente = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        AgendaHorarioModel horario = servico.getAgenda() == null ? null
                : servico.getAgenda().getDias().stream()
                        .flatMap(d -> d.getHorarios().stream())
                        .filter(h -> h.getId().equals(dto.getHorarioId()))
                        .findFirst().orElse(null);
        if (horario == null) {
            throw new IllegalArgumentException("Horário não encontrado para este serviço");
        }

        PetModel pet = petRepository.findById(dto.getPetId())
                .orElseThrow(() -> new IllegalArgumentException("Pet não encontrado"));
        if (pet.getClient() == null || !pet.getClient().getIdUser().equals(clientId)) {
            throw new IllegalArgumentException("Este pet não pertence ao cliente autenticado");
        }

        boolean petGrande = pet.getPorte() != null && pet.getPorte().equalsIgnoreCase("GRANDE");
        Double precoBase;
        if (petGrande) {
            if (Boolean.FALSE.equals(servico.getAceitaPetGrande()) || servico.getPrecoGrande() == null) {
                throw new IllegalArgumentException("Este serviço não aceita pets grandes");
            }
            precoBase = servico.getPrecoGrande();
        } else {
            precoBase = servico.getPrecoPequeno();
        }

        double adicionaisTotal = 0.0;
        AgendamentoResponseDTO response = new AgendamentoResponseDTO();
        response.setItens(new ArrayList<>());
        List<AgendamentoAdicionalModel> itensPersist = new ArrayList<>();
        if (dto.getAdicionaisIds() != null && !dto.getAdicionaisIds().isEmpty()) {
            List<AdicionaisModel> adicionais = adicionaisRepository.findByServicoId(servico.getId());
            for (UUID adicionalId : dto.getAdicionaisIds()) {
                AdicionaisModel ad = adicionais.stream()
                        .filter(a -> a.getId().equals(adicionalId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Adicional inválido para este serviço"));
                Double precoAplicado = petGrande ? ad.getPrecoGrande() : ad.getPrecoPequeno();
                if (petGrande && ad.getPrecoGrande() == null) {
                    throw new IllegalArgumentException("Adicional não possui preço para pet grande");
                }
                adicionaisTotal += precoAplicado;

                AgendamentoResponseDTO.ItemAdicional item = new AgendamentoResponseDTO.ItemAdicional();
                item.setAdicionalId(ad.getId());
                item.setNome(ad.getNome());
                item.setPrecoAplicado(precoAplicado);
                response.getItens().add(item);

                AgendamentoAdicionalModel itemPersist = new AgendamentoAdicionalModel();
                itemPersist.setNomeSnapshot(ad.getNome());
                itemPersist.setPrecoPequenoSnapshot(ad.getPrecoPequeno());
                itemPersist.setPrecoGrandeSnapshot(ad.getPrecoGrande());
                itemPersist.setPrecoAplicado(precoAplicado);
                itensPersist.add(itemPersist);
            }
        }

        double precoFinal = precoBase + adicionaisTotal;

        agendamentoRepository.findByHorario_IdAndDataAgendamento(dto.getHorarioId(), dto.getDataAgendamento())
                .ifPresent(a -> {
                    throw new IllegalArgumentException("Horário já reservado para esta data");
                });

        AgendamentoModel ag = new AgendamentoModel();
        ag.setAvaliado(false);
        ag.setCliente(cliente);
        ag.setServico_tipo(servico.getTipo());
        ag.setParceiro(servico.getParceiro());
        ag.setHorario(horario);
        ag.setPet_avatarUrl(pet.getAvatarUrl());
        ag.setPet_castrado(pet.getCastrado());
        ag.setPet_especie(pet.getEspecie());
        ag.setPet_nome(pet.getNome());
        ag.setPet_observacoes(pet.getObservacoes());
        ag.setPet_peso(pet.getPeso());
        ag.setPet_porte(pet.getPorte());
        ag.setPet_raca(pet.getRaca());
        ag.setPet_sexo(pet.getSexo());
        ag.setPet_sociavel(pet.getSociavel());
        ag.setPet_tipoSanguineo(pet.getTipoSanguineo());
        ag.setDataAgendamento(dto.getDataAgendamento());
        ag.setDataCriacaoAgendamento(LocalDateTime.now());
        ag.setPrecoFinal(precoFinal);
        ag.setStatus(StatusAgendamento.PENDENTE);

        AgendaDiaModel dia = horario.getDia();
        if (dia != null && dia.getDiaSemana() != null) {
            DiaSemana diaSemanaData = mapearDiaSemana(dto.getDataAgendamento());
            if (!dia.getDiaSemana().equals(diaSemanaData)) {
                throw new IllegalArgumentException("Data não corresponde ao dia da agenda do horário");
            }
        }

        String destination = "/topic/agendamentos/" + servico.getParceiro().getIdPartner().toString();
        ag.setItens(itensPersist);
        AgendamentoModel salvo = agendamentoRepository.save(ag);
        AgendamentoNotificationDto notificationPayload = new AgendamentoNotificationDto(
                salvo.getId(),
                salvo.getCliente().getName(),
                pet.getNome(),
                pet.getAvatarUrl(),
                servico.getTipo().toString(),
                precoFinal,
                LocalDateTime.now(),
                dto.getDataAgendamento(),
                horario.getHorarioInicio() + " - " + horario.getHorarioFim());

        System.out.println("Enviando notificação para o destino: " + destination);
        messagingTemplate.convertAndSend(destination, notificationPayload);

        response.setId(salvo.getId());
        response.setServicoId(servico.getId());
        response.setParceiroId(servico.getParceiro().getIdPartner());
        response.setPetId(pet.getIdPet());
        response.setHorarioId(horario.getId());
        response.setDataAgendamento(salvo.getDataAgendamento());
        response.setPrecoBase(precoBase);
        response.setAdicionaisTotal(adicionaisTotal);
        response.setPrecoFinal(precoFinal);
        return response;
    }

    public List<AgendamentoListaResponseDTO> listarAgendamentosDoDia(UUID parceiroId, LocalDate data) {
        List<AgendamentoModel> lista = agendamentoRepository
                .findAllByParceiro_IdPartnerAndDataAgendamento(parceiroId, data);
        return lista.stream().map(ag -> new AgendamentoListaResponseDTO(
                ag.getId(),
                ag.getDataAgendamento(),
                ag.getHorario().getHorarioInicio() + " - " + ag.getHorario().getHorarioFim(),
                ag.getStatus(),
                ag.getCliente().getName(),
                ag.getPet_nome(),
                ag.getPrecoFinal(),
                ag.getServico_tipo())).toList();
    }

    public List<AgendamentoListaResponseDTO> listarAgendamentosPendentes(UUID parceiroId) {
        List<AgendamentoModel> lista = agendamentoRepository
                .findAllByParceiro_IdPartnerAndStatus(parceiroId, StatusAgendamento.PENDENTE);
        return lista.stream().map(ag -> new AgendamentoListaResponseDTO(
                ag.getId(),
                ag.getDataAgendamento(),
                ag.getHorario().getHorarioInicio() + " - " + ag.getHorario().getHorarioFim(),
                ag.getStatus(),
                ag.getCliente().getName(),
                ag.getPet_nome(),
                ag.getPrecoFinal(),
                ag.getServico_tipo())).toList();
    }

    public AgendamentoDetalheResponseDTO buscarDetalhesAgendamento(UUID parceiroId, UUID agendamentoId) {
        AgendamentoModel ag = agendamentoRepository.findByIdAndParceiro_IdPartner(agendamentoId, parceiroId)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado para este parceiro"));

        List<AgendamentoDetalheResponseDTO.ItemAdicional> itens = new ArrayList<>();
        if (ag.getItens() != null) {
            for (AgendamentoAdicionalModel it : ag.getItens()) {
                itens.add(new AgendamentoDetalheResponseDTO.ItemAdicional(
                        it.getNomeSnapshot(),
                        it.getPrecoAplicado()));
            }
        }

        return new AgendamentoDetalheResponseDTO(
                ag.getId(),
                ag.getDataAgendamento(),
                ag.getHorario().getHorarioInicio() + " - " + ag.getHorario().getHorarioFim(),
                ag.getStatus(),
                ag.getPrecoFinal(),
                ag.getServico_tipo(),
                ag.getCliente().getName(),
                ag.getPet_nome(),
                ag.getPet_especie(),
                ag.getPet_raca(),
                ag.getPet_observacoes(),
                ag.getPet_castrado(),
                ag.getPet_sociavel(),
                ag.getPet_sexo(),
                ag.getPet_peso(),
                ag.getPet_tipoSanguineo(),
                ag.getPet_porte(),
                ag.getPet_avatarUrl(),
                itens);
    }

    @Transactional
    public void decidirAgendamento(UUID parceiroId, UUID agendamentoId, boolean aceitar) {
        AgendamentoModel ag = agendamentoRepository.findByIdAndParceiro_IdPartner(agendamentoId, parceiroId)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado para este parceiro"));

        if (ag.getStatus() != StatusAgendamento.PENDENTE) {
            throw new IllegalArgumentException("Agendamento não está pendente");
        }

        if (aceitar) {
            ag.setStatus(StatusAgendamento.CONFIRMADO);
            List<AgendamentoModel> conflitos = agendamentoRepository
                    .findAllByHorario_IdAndDataAgendamento(ag.getHorario().getId(), ag.getDataAgendamento());
            for (AgendamentoModel outro : conflitos) {
                if (!outro.getId().equals(ag.getId()) && outro.getStatus() == StatusAgendamento.PENDENTE) {
                    outro.setStatus(StatusAgendamento.CANCELADO);
                }
            }
            agendamentoRepository.saveAll(conflitos);
        } else {
            ag.setStatus(StatusAgendamento.CANCELADO);
        }

        agendamentoRepository.save(ag);
    }

    public List<ResponseAgendamentosdto> meusAgendamentos(String login) {
        ClientModel cliente = (ClientModel) this.clientRepository.findByLogin(login);
        if (cliente == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usunencontrado/logado");
        List<AgendamentoModel> agendamentos = this.agendamentoRepository.findByCliente(cliente);
        return agendamentos.stream()
                .map(agendamento -> new ResponseAgendamentosdto(agendamento.getId(), agendamento.getStatus(),
                        Boolean.valueOf(false), agendamento.getServico_tipo().toString(), agendamento.getPet_nome(),
                        agendamento.getPet_nome(), agendamento.getDataAgendamento().format(this.formatter),
                        agendamento.getHorario().getHorarioInicio() + "-" + agendamento.getHorario().getHorarioInicio(),
                        agendamento.getParceiro().getName(), agendamento.getParceiro().getLogin()))

                .toList();
    }
}
