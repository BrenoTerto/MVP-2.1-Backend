package com.redepatas.api.services;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.redepatas.api.dtos.AgedamentoDtos.AgendamentoResponseDto;
import com.redepatas.api.dtos.AgedamentoDtos.AvaliarServicoDto;
import com.redepatas.api.dtos.AgedamentoDtos.ClientAgendamentoDto;
import com.redepatas.api.dtos.AgedamentoDtos.ResponseAgendamentosdto;
import com.redepatas.api.dtos.petDtos.GetPetsDto;
import com.redepatas.api.dtos.petDtos.VacinaDto;
import com.redepatas.api.models.AgendamentoModel;
import com.redepatas.api.models.ClientModel;
import com.redepatas.api.models.PetModel;
import com.redepatas.api.models.Enum.StatusAgendamento;
import com.redepatas.api.models.Partner.HorarioIntervaloModel;
import com.redepatas.api.models.Partner.PartnerModel;
import com.redepatas.api.repositories.AgendamentoRepository;
import com.redepatas.api.repositories.ClientRepository;
import com.redepatas.api.repositories.HorarioIntervaloRepository;
import com.redepatas.api.repositories.PartnerRepository;
import com.redepatas.api.repositories.PetRepository;

import jakarta.mail.MessagingException;

@Service
public class AgendamentoService {

        @Autowired
        private AgendamentoRepository agendamentoRepository;

        @Autowired
        private ClientRepository clientRepository;

        @Autowired
        private PartnerRepository partnerRepository;

        @Autowired
        private PetRepository petRepository;

        @Autowired
        private EmailService emailService;
        @Autowired
        private HorarioIntervaloRepository horarioIntervaloRepository;

        public String criarAgendamento(String login, UUID idParceiro, UUID idPet, String servico,
                        LocalDate dataAgendamento, UUID idIntervalo) throws MessagingException {

                ClientModel cliente = (ClientModel) clientRepository.findByLogin(login);
                if (cliente == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario nao encotrado/logado");
                }
                PetModel pet = petRepository.findById(idPet)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Pet não encontrado"));
                if (!pet.getClient().getLogin().equals(login)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este pet não pertence ao cliente.");
                }
                PartnerModel parceiro = partnerRepository.findById(idParceiro)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Parceiro nao encontrado"));

                // Busque o intervalo pelo ID usando o repository
                HorarioIntervaloModel horarioIntervalo = horarioIntervaloRepository.findById(idIntervalo)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Intervalo de horário não encontrado"));

                boolean reservado = agendamentoRepository.existsByPartnerModelAndDataAgendamentoAndIntervalo(
                                parceiro, dataAgendamento, horarioIntervalo);
                if (reservado) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Esse horário já está reservado para a data escolhida.");
                }

                AgendamentoModel agendamento = new AgendamentoModel(
                                cliente,
                                parceiro,
                                pet,
                                dataAgendamento,
                                horarioIntervalo,
                                StatusAgendamento.PENDENTE,
                                servico);

                agendamentoRepository.save(agendamento);
                String intervalo = horarioIntervalo.getHorarioInicio() + " - " + horarioIntervalo.getHorarioFim();
                emailService.enviarAgendamento(parceiro.getEmailContato(), parceiro.getName(), pet.getNome(),
                                dataAgendamento, intervalo, agendamento.getId().toString());
                return "Agendamento criado com sucesso! Aguarde a confirmação";
        }

        public AgendamentoResponseDto buscarPorId(UUID idAgendamento) {
                AgendamentoModel agendamento = agendamentoRepository.findById(idAgendamento)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Agendamento não encontrado"));

                ClientModel cliente = agendamento.getCliente();
                PetModel pet = agendamento.getPetModel();

                ClientAgendamentoDto clientDto = new ClientAgendamentoDto(
                                cliente.getName(),
                                cliente.getPhotoUrl());

                GetPetsDto petDto = new GetPetsDto(
                                pet.getIdPet(),
                                pet.getRgPet(),
                                pet.getAvatarUrl(),
                                pet.getNome(),
                                pet.getEspecie(),
                                pet.getRaca(),
                                pet.getObservacoes(),
                                pet.getCastrado(),
                                pet.getSociavel(),
                                pet.getVacinas().stream().map(v -> new VacinaDto(
                                                v.getNome(),
                                                v.getDataAplicacao())).toList(),
                                pet.getDataNascimento(),
                                pet.getSexo(),
                                pet.getPeso(),
                                pet.getTipoSanguineo(),
                                pet.getPorte());

                return new AgendamentoResponseDto(
                                agendamento.getId(),
                                agendamento.getDataAgendamento(),
                                agendamento.getIntervalo().getHorarioInicio() + "-"
                                                + agendamento.getIntervalo().getHorarioFim(),
                                agendamento.getStatusAgendamento(),
                                clientDto,
                                petDto);
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
                Duration duration = Duration.between(agendamento.getDataAgendamento(), agora);
                if (agendamento.getCliente().getLogin().equals(login)) {
                        if (duration.toHours() < 2) {
                                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                "Esse serviço so pode ser avaliado após duas horas!");
                        }
                        if (agendamento.getAvaliado() || agendamento.getAvaliado() == null) {
                                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serviço já avaliado!");
                        }
                        PartnerModel parceiro = agendamento.getPartnerModel();
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

        public List<ResponseAgendamentosdto> meusAgendamentos(String login) {
                ClientModel cliente = (ClientModel) clientRepository.findByLogin(login);
                if (cliente == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não encontrado/logado");
                }

                List<AgendamentoModel> agendamentos = agendamentoRepository.findByCliente(cliente);

                return agendamentos.stream()
                                .map(agendamento -> new ResponseAgendamentosdto(
                                                agendamento.getId(),
                                                agendamento.getStatusAgendamento(),
                                                agendamento.getAvaliado(),
                                                agendamento.getServico(),
                                                agendamento.getPetModel().getNome(),
                                                agendamento.getPetModel().getRaca(),
                                                agendamento.getDataAgendamento(),
                                                agendamento.getIntervalo().getHorarioInicio() + "-"
                                                                + agendamento.getIntervalo().getHorarioInicio(),
                                                agendamento.getPartnerModel().getName(),
                                                agendamento.getPartnerModel().getCnpjCpf()

                                ))
                                .toList();
        }

        public String alterarStatusAgendamento(UUID idAgendamento, String status) {
                AgendamentoModel agendamento = agendamentoRepository.findById(idAgendamento)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Agendamento não encontrado"));

                if (agendamento.getStatusAgendamento() != StatusAgendamento.PENDENTE) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Agendamento já foi aceito ou cancelado");
                }
                if (status == null || status.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status não pode ser vazio");
                }
                if (status.equalsIgnoreCase("cancelado")) {
                        agendamento.setStatusAgendamento(StatusAgendamento.CANCELADO);
                        agendamentoRepository.save(agendamento);
                        return "Agendamento cancelado com sucesso!";
                }
                agendamento.setStatusAgendamento(StatusAgendamento.CONFIRMADO);
                agendamentoRepository.save(agendamento);
                return "Agendamento aceito com sucesso!";
        }
}
