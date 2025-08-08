package com.redepatas.api.parceiro.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.redepatas.api.cliente.models.ClientModel;
import com.redepatas.api.cliente.repositories.ClientRepository;
import com.redepatas.api.parceiro.dtos.PartnerDtos.DistanceDurationDto;
import com.redepatas.api.parceiro.dtos.PartnerDtos.EnderecoParteDto;
import com.redepatas.api.parceiro.dtos.PartnerDtos.ParceiroBuscaProjecao;
import com.redepatas.api.parceiro.dtos.PartnerDtos.PartnerDto;
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
    @Autowired
    private ClientRepository clientRepository;

    @Value("${api.key.google.maps}")
    private String API_KEY;

    public List<PartnerDto> buscarParceirosDisponiveis(String cidade, String rua, String bairro, String loginCliente,
            String diaSemana, String tipoServico, String tamanhoPet) {

        var user = clientRepository.findByLogin(loginCliente);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não encontrado!");
        }
        var client = (ClientModel) user;
        String enderecoOrigem = null;
        boolean cidadeFornecida = cidade != null && !cidade.isBlank();
        boolean ruaFornecida = rua != null && !rua.isBlank();
        boolean bairroFornecido = bairro != null && !bairro.isBlank();

        if (cidadeFornecida && ruaFornecida && bairroFornecido) {
            enderecoOrigem = rua + ", " + bairro + ", " + cidade;
        } else if (!cidadeFornecida && !ruaFornecida && !bairroFornecido) {
            enderecoOrigem = client.getEnderecos().stream()
                    .filter(e -> Boolean.TRUE.equals(e.getSelecionado()))
                    .findFirst()
                    .map(e -> e.getRua() + ", " + e.getBairro() + ", " + e.getCidade())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Nenhum endereço foi fornecido na busca e não há um endereço selecionado no seu perfil."));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Endereço incompleto! Para buscar utilizar a localização, por favor, forneça todos os campos: cidade, rua e bairro.");
        }

        List<ParceiroBuscaProjecao> resultadosDoBanco = partnerRepository.findParceirosDisponiveis(cidade, diaSemana,
                tipoServico, tamanhoPet);

        if (resultadosDoBanco.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> enderecosDestino = resultadosDoBanco.stream()
                .map(p -> p.getRua() + ", " + p.getBairro() + ", " + p.getCidade())
                .collect(Collectors.toList());

        List<DistanceDurationDto> distancias = consultarDistancias(enderecoOrigem, enderecosDestino);

        if (distancias.size() != resultadosDoBanco.size()) {
            System.err.println(
                    "AVISO: A contagem de distâncias retornada pela API não corresponde à contagem de parceiros.");
        }

        List<PartnerDto> resultadoFinal = new ArrayList<>();
        for (int i = 0; i < resultadosDoBanco.size(); i++) {
            ParceiroBuscaProjecao projecao = resultadosDoBanco.get(i);

            DistanceDurationDto distanciaInfo = (i < distancias.size()) ? distancias.get(i) : null;
            String distancia = (distanciaInfo != null) ? distanciaInfo.distancia() : "N/D";
            String tempo = (distanciaInfo != null) ? distanciaInfo.tempo() : "N/D";

            EnderecoParteDto enderecoDto = new EnderecoParteDto(projecao.getRua(), projecao.getBairro());
            resultadoFinal.add(new PartnerDto(
                    projecao.getId_partner(),
                    projecao.getId_servico(),
                    projecao.getImagem(),
                    projecao.getNome_do_parceiro(),
                    projecao.getServico_oferecido(),
                    projecao.getPreco_pequeno().longValue(),
                    projecao.getPreco_grande() != null ? projecao.getPreco_grande().longValue() : 0L,
                    projecao.getAvaliacao() != null ? projecao.getAvaliacao().longValue() : 0L,
                    enderecoDto,
                    distancia,
                    tempo));
        }

        return resultadoFinal;
    }

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

    // Não atualiza agenda nem adicionais nesta rota. Essas entidades possuem suas próprias rotas de atualização.

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
