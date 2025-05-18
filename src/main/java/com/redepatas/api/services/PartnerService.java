package com.redepatas.api.services;

import com.redepatas.api.dtos.PartnerDtos.DistanceDurationDto;
import com.redepatas.api.dtos.PartnerDtos.HorarioFuncionamentoDto;
import com.redepatas.api.dtos.PartnerDtos.PartenerServicesDto;
import com.redepatas.api.dtos.PartnerDtos.PartnerDto;
import com.redepatas.api.dtos.PartnerDtos.PartnerRecordDto;
import com.redepatas.api.dtos.PartnerDtos.ServicoDto;
import com.redepatas.api.dtos.PartnerDtos.SubServicoDto;
import com.redepatas.api.dtos.UserDtos.EnderecoDto;
import com.redepatas.api.models.ClientModel;
import com.redepatas.api.models.Endereco;
import com.redepatas.api.models.EnderecoPartner;
import com.redepatas.api.models.Partner.HorarioFuncionamentoModel;
import com.redepatas.api.models.Partner.PartnerModel;
import com.redepatas.api.models.Partner.Servico;
import com.redepatas.api.models.Partner.SubServico;
import com.redepatas.api.repositories.ClientRepository;
import com.redepatas.api.repositories.EnderecoPartnerRepository;
import com.redepatas.api.repositories.PartnerRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
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

        @Value("${api.key.google.maps}")
        private static String API_KEY;

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

                // Criação do endereço
                EnderecoPartner endereco = new EnderecoPartner(
                                dto.endereco().rua(),
                                dto.endereco().bairro(),
                                dto.endereco().cidade(),
                                dto.endereco().estado(),
                                dto.endereco().cep(),
                                dto.endereco().numero(),
                                dto.endereco().complemento(),
                                dto.endereco().lugar());

                // Criação dos serviços
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

                // Criação inicial do parceiro (sem os horários ainda)
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
                        horario.setHorarioInicio(horarioDto.horarioInicio());
                        horario.setHorarioFim(horarioDto.horarioFim());
                        horario.setPartner(partner);
                        return horario;
                }).toList();

                // Setando os horários no parceiro
                partner.setHorariosFuncionamento(horarios);

                // Persistência
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

                if ((rua != null && !rua.isBlank()) ||
                                (bairro != null && !bairro.isBlank()) ||
                                (cidade != null && !cidade.isBlank())) {
                        partners = partnerRepository.findByEndereco_Cidade(cidade);
                } else {
                        partners = partnerRepository.findByEndereco_Cidade(enderecoSelecionado.getCidade());
                }
                System.out.println(enderecoSelecionado.getCidade());
                if (porte != null && !porte.isBlank()) {
                        partners = partners.stream()
                                        .filter(partner -> partner.getTipoPet().equalsIgnoreCase("TODOS") ||
                                                        partner.getTipoPet().equalsIgnoreCase(porte))
                                        .collect(Collectors.toList());
                }

                String enderecoOrigem = enderecoSelecionado.getRua() + ", " +
                                enderecoSelecionado.getBairro() + ", " +
                                enderecoSelecionado.getCidade();

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
                                        .filter(servico -> servico.getNome().equalsIgnoreCase(nomeServico))
                                        .findFirst()
                                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                        "Serviço não encontrado para esse parceiro"));

                        result.add(new PartnerDto(
                                        partner.getIdPartner(),
                                        partner.getImageUrl(),
                                        partner.getName(),
                                        partner.getEmailContato(),
                                        mediaFormatada,
                                        enderecoDto,
                                        distancia,
                                        tempo,
                                        servicoEncontrado.getPrice()));
                }

                return result;
        }

        private String normalize(String str) {
                return Normalizer.normalize(str == null ? "" : str, Normalizer.Form.NFD)
                                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                                .toLowerCase()
                                .trim();
        }

        public PartenerServicesDto getOnlyPartner(UUID idPartner, String nomeServico) {
                PartnerModel partner = partnerRepository.findById(idPartner)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Parceiro não encontrado"));

                // Buscar o serviço específico pelo nome
                Servico servicoEncontrado = partner.getServicos().stream()
                                .filter(servico -> servico.getNome().equalsIgnoreCase(nomeServico))
                                .findFirst()
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Serviço não encontrado para esse parceiro"));

                // Mapear subserviços para DTOs
                List<SubServicoDto> subServicosDto = servicoEncontrado.getSubServicos().stream()
                                .map(sub -> new SubServicoDto(sub.getNome(), sub.getDescricao(), sub.getPreco()))
                                .collect(Collectors.toList());

                // Criar o DTO do serviço
                ServicoDto servicoDto = new ServicoDto(servicoEncontrado.getNome(), servicoEncontrado.getPrice(),
                                subServicosDto);

                // Mapear horários de funcionamento
                List<HorarioFuncionamentoDto> horariosDto = partner.getHorariosFuncionamento().stream()
                                .map(horario -> new HorarioFuncionamentoDto(
                                                horario.getDia(),
                                                horario.getHorarioInicio().toString(),
                                                horario.getHorarioFim().toString()))
                                .collect(Collectors.toList());

                return new PartenerServicesDto(servicoDto, horariosDto);
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
                        JSONArray elements = responseJson.getJSONArray("rows").getJSONObject(0)
                                        .getJSONArray("elements");

                        List<DistanceDurationDto> result = new ArrayList<>();
                        for (int i = 0; i < elements.length(); i++) {
                                JSONObject element = elements.getJSONObject(i);
                                String distance = element.getJSONObject("distance").getString("text");
                                String duration = element.getJSONObject("duration").getString("text");
                                result.add(new DistanceDurationDto(distance, duration));
                        }
                        return result;

                } catch (Exception e) {
                        e.printStackTrace();
                        return Collections.emptyList();
                }
        }

}
