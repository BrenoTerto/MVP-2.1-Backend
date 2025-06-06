package com.redepatas.api.services;

import com.redepatas.api.dtos.PartnerDtos.DistanceDurationDto;
import com.redepatas.api.dtos.PartnerDtos.HorarioFuncionamentoDto;
import com.redepatas.api.dtos.PartnerDtos.HorarioIntervaloDto;
import com.redepatas.api.dtos.PartnerDtos.PartenerServicesDto;
import com.redepatas.api.dtos.PartnerDtos.PartnerDto;
import com.redepatas.api.dtos.PartnerDtos.PartnerRecordDto;
import com.redepatas.api.dtos.PartnerDtos.ServicoDto;
import com.redepatas.api.dtos.PartnerDtos.SubServicoDto;
import com.redepatas.api.dtos.UserDtos.EnderecoDto;
import com.redepatas.api.models.ClientModel;
import com.redepatas.api.models.Endereco;
import com.redepatas.api.models.EnderecoPartner;
import com.redepatas.api.models.Enum.DiaSemana;
import com.redepatas.api.models.Partner.HorarioFuncionamentoModel;
import com.redepatas.api.models.Partner.HorarioIntervaloModel;
import com.redepatas.api.models.Partner.PartnerModel;
import com.redepatas.api.models.Partner.Servico;
import com.redepatas.api.models.Partner.SubServico;
import com.redepatas.api.repositories.AgendamentoRepository;
import com.redepatas.api.repositories.ClientRepository;
import com.redepatas.api.repositories.EnderecoPartnerRepository;
import com.redepatas.api.repositories.PartnerRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PartnerService {

        @Autowired
        EnderecoPartnerRepository enderecoPartnerRepository;
        @Autowired
        PartnerRepository partnerRepository;
        @Autowired
        ClientRepository clientRepository;
        @Autowired
        AgendamentoRepository agendamentoRepository;

        @Value("${api.key.google.maps}")
        private String API_KEY;

        public String createPartner(PartnerRecordDto dto) {

                if (partnerRepository.existsByCnpjCpf(dto.cnpjCpf())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Já existe um parceiro com este CNPJ/CPF.");
                }

                if (dto.emailContato() != null && !dto.emailContato().isBlank()
                                && partnerRepository.existsByEmailContato(dto.emailContato())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Já existe um parceiro com este e-mail de contato.");
                }

                EnderecoPartner endereco = new EnderecoPartner(
                                dto.endereco().rua(),
                                dto.endereco().bairro(),
                                dto.endereco().cidade(),
                                dto.endereco().estado(),
                                dto.endereco().cep(),
                                dto.endereco().numero(),
                                dto.endereco().complemento(),
                                dto.endereco().lugar());

                List<Servico> servicos = dto.servicos().stream().map(servicoDto -> {
                        Servico servico = new Servico();
                        servico.setNome(servicoDto.nome());
                        servico.setPrice(servicoDto.price());

                        List<SubServico> subServicos = servicoDto.subServicos().stream().map(subDto -> {
                                SubServico sub = new SubServico();
                                sub.setNome(subDto.nome());
                                sub.setDescricao(subDto.descricao());
                                sub.setPreco(subDto.preco());
                                sub.setServico(servico);
                                return sub;
                        }).toList();

                        servico.setSubServicos(subServicos);
                        return servico;
                }).toList();

                PartnerModel partner = new PartnerModel(
                                dto.name(),
                                dto.imageUrl(),
                                dto.cnpjCpf(),
                                dto.emailContato(),
                                dto.numeroContato(),
                                endereco,
                                dto.categoria(),
                                dto.descricao(),
                                dto.tipoPet(),
                                servicos,
                                null);

                endereco.setPartnerModel(partner);
                servicos.forEach(s -> s.setPartner(partner));

                List<HorarioFuncionamentoModel> horarios = dto.horariosFuncionamento().stream().map(horarioDto -> {
                        HorarioFuncionamentoModel horario = new HorarioFuncionamentoModel();
                        horario.setDia(horarioDto.dia());
                        horario.setPartner(partner);

                        List<HorarioIntervaloModel> intervalos = horarioDto.intervalos().stream().map(intervaloDto -> {
                                HorarioIntervaloModel intervalo = new HorarioIntervaloModel();
                                intervalo.setHorarioInicio(intervaloDto.horarioInicio());
                                intervalo.setHorarioFim(intervaloDto.horarioFim());
                                intervalo.setHorarioFuncionamento(horario);
                                return intervalo;
                        }).toList();

                        horario.setIntervalos(intervalos);
                        return horario;
                }).toList();

                partner.setHorariosFuncionamento(horarios);

                partnerRepository.save(partner);
                return "Parceiro cadastrado com sucesso!";
        }

        public List<PartnerDto> getAllPartners(String porte, String login, String nomeServico,
                        String rua, String bairro, String cidade) {
                var user = clientRepository.findByLogin(login);
                if (user == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não encontrado!");
                }
                var client = (ClientModel) user;

                Endereco enderecoSelecionado = client.getEnderecos().stream()
                                .filter(e -> Boolean.TRUE.equals(e.getSelecionado()))
                                .findFirst()
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Nenhum endereço selecionado."));

                List<PartnerModel> partners;
                String enderecoOrigem;
                if ((rua != null && !rua.isBlank()) ||
                                (bairro != null && !bairro.isBlank()) ||
                                (cidade != null && !cidade.isBlank())) {
                        partners = partnerRepository.findAll().stream()
                                        .filter(p -> normalizar(p.getEndereco().getCidade()).equals(normalizar(cidade)))
                                        .collect(Collectors.toList());
                        enderecoOrigem = rua + ", " +
                                        bairro + ", " +
                                        cidade;
                } else {
                        partners = partnerRepository.findAll().stream()
                                        .filter(p -> normalizar(p.getEndereco().getCidade())
                                                        .equals(normalizar(enderecoSelecionado.getCidade())))
                                        .collect(Collectors.toList());
                        enderecoOrigem = enderecoSelecionado.getRua() + ", " +
                                        enderecoSelecionado.getBairro() + ", " +
                                        enderecoSelecionado.getCidade();
                }
                if (porte != null && !porte.isBlank()) {
                        partners = partners.stream()
                                        .filter(partner -> partner.getTipoPet().equalsIgnoreCase("TODOS") ||
                                                        partner.getTipoPet().equalsIgnoreCase(porte))
                                        .collect(Collectors.toList());
                }

                if (nomeServico != null && !nomeServico.isBlank()) {
                        partners = partners.stream()
                                        .filter(partner -> partner.getServicos().stream()
                                                        .anyMatch(servico -> normalizar(servico.getNome())
                                                                        .equals(normalizar(nomeServico))))
                                        .collect(Collectors.toList());
                        if (partners.isEmpty()) {
                                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Nenhum parceiro encontrado com o serviço solicitado.");
                        }
                }

                List<String> enderecosDestino = partners.stream()
                                .map(p -> {
                                        EnderecoPartner e = p.getEndereco();
                                        return e.getRua() + ", " + e.getBairro() + ", " + e.getCidade() + ", "
                                                        + e.getEstado();
                                })
                                .collect(Collectors.toList());

                List<DistanceDurationDto> distancias = consultarDistancias(enderecoOrigem, enderecosDestino);

                List<PartnerDto> result = new ArrayList<>();
                for (int i = 0; i < partners.size(); i++) {
                        PartnerModel partner = partners.get(i);
                        EnderecoPartner endereco = partner.getEndereco();

                        EnderecoDto enderecoDto = endereco != null
                                        ? new EnderecoDto(
                                                        endereco.getIdEndereco().toString(),
                                                        endereco.getRua(),
                                                        endereco.getCidade(),
                                                        endereco.getEstado(),
                                                        endereco.getBairro(),
                                                        endereco.getCep(),
                                                        endereco.getComplemento(),
                                                        endereco.getNumero(),
                                                        endereco.getLugar(),
                                                        null)
                                        : null;

                        double media = (double) partner.getAvaliacao() / partner.getQtdAvaliacoes();
                        double mediaFormatada = Math.round(media * 100.0) / 100.0;

                        String distancia = distancias.size() > i ? distancias.get(i).distancia() : null;
                        String tempo = distancias.size() > i ? distancias.get(i).tempo() : null;

                        Servico servicoEncontrado = partner.getServicos().stream()
                                        .filter(servico -> normalizar(servico.getNome())
                                                        .equals(normalizar(nomeServico)))
                                        .findFirst()
                                        .get();

                        result.add(new PartnerDto(
                                        partner.getIdPartner(),
                                        partner.getImageUrl(),
                                        partner.getName(),
                                        partner.getEmailContato(),
                                        partner.getDescricao(),
                                        mediaFormatada,
                                        enderecoDto,
                                        distancia,
                                        tempo,
                                        servicoEncontrado.getPrice()));
                }

                return result;
        }

        public PartenerServicesDto getOnlyPartner(UUID idPartner, String nomeServico, LocalDate data) {
                PartnerModel partner = partnerRepository.findById(idPartner)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Parceiro não encontrado"));

                Servico servicoEncontrado = partner.getServicos().stream()
                                .filter(servico -> normalizar(servico.getNome()).equals(normalizar(nomeServico)))
                                .findFirst()
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Serviço não encontrado para esse parceiro"));

                List<SubServicoDto> subServicosDto = servicoEncontrado.getSubServicos().stream()
                                .map(sub -> new SubServicoDto(sub.getNome(), sub.getDescricao(), sub.getPreco()))
                                .collect(Collectors.toList());

                ServicoDto servicoDto = new ServicoDto(servicoEncontrado.getNome(), servicoEncontrado.getPrice(),
                                subServicosDto);

                List<HorarioFuncionamentoDto> horariosDto = new ArrayList<>();
                if (data != null) {
                        DiaSemana diaSemana = converterDayOfWeekParaDiaSemana(data.getDayOfWeek());

                        partner.getHorariosFuncionamento().stream()
                                        .filter(horario -> horario.getDia() == diaSemana)
                                        .findFirst()
                                        .ifPresent(horario -> {
                                                List<HorarioIntervaloDto> intervalosDto = horario.getIntervalos()
                                                                .stream()
                                                                .map(intervalo -> {
                                                                        boolean reservado = agendamentoRepository
                                                                                        .existsByPartnerModelAndDataAgendamentoAndIntervalo(
                                                                                                        partner, data,
                                                                                                        intervalo);
                                                                        return new HorarioIntervaloDto(
                                                                                        intervalo.getId(),
                                                                                        intervalo.getHorarioInicio(),
                                                                                        intervalo.getHorarioFim(),
                                                                                        reservado);
                                                                })
                                                                .collect(Collectors.toList());
                                                horariosDto.add(new HorarioFuncionamentoDto(horario.getDia(),
                                                                intervalosDto));
                                        });
                }

                return new PartenerServicesDto(servicoDto, horariosDto);
        }

        private static DiaSemana converterDayOfWeekParaDiaSemana(java.time.DayOfWeek dayOfWeek) {
                return switch (dayOfWeek) {
                        case MONDAY -> DiaSemana.SEGUNDA;
                        case TUESDAY -> DiaSemana.TERCA;
                        case WEDNESDAY -> DiaSemana.QUARTA;
                        case THURSDAY -> DiaSemana.QUINTA;
                        case FRIDAY -> DiaSemana.SEXTA;
                        case SATURDAY -> DiaSemana.SABADO;
                        case SUNDAY -> DiaSemana.DOMINGO;
                };
        }

        private List<DistanceDurationDto> consultarDistancias(String origem, List<String> destinos) {
                try {
                        String destinosParam = destinos.stream()
                                        .map(dest -> URLEncoder.encode(dest, StandardCharsets.UTF_8))
                                        .collect(Collectors.joining("|"));

                        String origemParam = URLEncoder.encode(origem, StandardCharsets.UTF_8);

                        String urlStr = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" +
                                        origemParam + "&destinations=" + destinosParam + "&departure_time=now" + "&key="
                                        + API_KEY;
                        URL url = new URL(urlStr);
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setRequestMethod("GET");

                        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        String inputLine;
                        StringBuilder content = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                                content.append(inputLine);
                        }

                        in.close();
                        con.disconnect();

                        JSONObject responseJson = new JSONObject(content.toString());

                        JSONArray rows = responseJson.optJSONArray("rows");

                        JSONArray elements = rows.getJSONObject(0).optJSONArray("elements");

                        List<DistanceDurationDto> result = new ArrayList<>();
                        for (int i = 0; i < elements.length(); i++) {
                                JSONObject element = elements.getJSONObject(i);

                                if (element.has("distance") && element.has("duration")) {
                                        String distance = element.getJSONObject("distance").getString("text");
                                        String duration = element.getJSONObject("duration").getString("text");
                                        result.add(new DistanceDurationDto(distance, duration));
                                } else {
                                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                        "Elemento sem distância/duração válido: "
                                                                        + element.toString(2));
                                }
                        }

                        return result;

                } catch (Exception e) {
                        e.printStackTrace();
                        return Collections.emptyList();
                }
        }

        private static String normalizar(String str) {
                if (str == null)
                        return null;
                return java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD)
                                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                                .toLowerCase()
                                .trim();
        }

}
