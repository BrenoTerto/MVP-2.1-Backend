package com.redepatas.api.parceiro.services;

import com.redepatas.api.cliente.repositories.ClientRepository;
import com.redepatas.api.cliente.models.ClientRole;
import com.redepatas.api.parceiro.dtos.PartnerDtos.DistanceDurationDto;
import com.redepatas.api.parceiro.dtos.PartnerDtos.PartnerProfileDto;
import com.redepatas.api.parceiro.dtos.PartnerDtos.PartnerRecordDto;

import com.redepatas.api.parceiro.models.EnderecoPartner;
import com.redepatas.api.parceiro.models.PartnerModel;
import com.redepatas.api.parceiro.models.Enum.DiaSemana;
import com.redepatas.api.parceiro.models.Enum.TipoPet;
import com.redepatas.api.parceiro.repositories.EnderecoPartnerRepository;
import com.redepatas.api.parceiro.repositories.PartnerRepository;
import com.redepatas.api.cliente.services.FileService;
import com.redepatas.api.cliente.services.IS3Service;
import com.redepatas.api.utils.ValidationUtil;
import com.redepatas.api.parceiro.models.PartnerConfirmationToken;
import com.redepatas.api.parceiro.repositories.PartnerConfirmationTokenRepository;
import com.redepatas.api.cliente.services.EmailService;
import jakarta.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.UUID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;

@Service
public class PartnerService {

        @Autowired
        EnderecoPartnerRepository enderecoPartnerRepository;
        @Autowired
        PartnerRepository partnerRepository;
        @Autowired
        ClientRepository clientRepository;
        @Autowired
        private FileService fileService;
        @Autowired
        IS3Service is3Service;
        @Autowired
        PartnerConfirmationTokenRepository partnerConfirmationTokenRepository;
        @Autowired
        EmailService emailService;
        @Value("${api.key.google.maps}")
        private String API_KEY;

        public String createPartner(PartnerRecordDto dto, MultipartFile image) {

                // Validar se o login é um CPF ou CNPJ válido
                if (!ValidationUtil.isDocumentoValido(dto.login())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "O login deve ser um CPF ou CNPJ válido.");
                }

                // Validar se a imagem foi fornecida
                if (image == null || image.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "A imagem é obrigatória para o cadastro do parceiro.");
                }

                // Verificar se o login já existe
                if (partnerRepository.findByLogin(dto.login()) != null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Já existe um parceiro com este login.");
                }

                if (dto.emailContato() != null && !dto.emailContato().isBlank()
                                && partnerRepository.existsByEmailContato(dto.emailContato())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Já existe um parceiro com este e-mail de contato.");
                }

                // Criptografar a senha
                String encryptedPassword = new BCryptPasswordEncoder().encode(dto.password());

                // Processar imagem
                String imageUrl;
                try {
                        MultipartFile webpFile = fileService.convertToWebP(image);
                        Map<String, String> response = is3Service.uploadFile(webpFile, "partners").getBody();
                        if (response == null || !response.containsKey("fileUrl")) {
                                throw new RuntimeException("Erro ao fazer upload da imagem para a AWS.");
                        }
                        imageUrl = response.get("fileUrl");
                } catch (IOException e) {
                        throw new RuntimeException("Erro ao processar imagem para o avatar.", e);
                }

                if (dto.endereco() == null) {
                        throw new RuntimeException("Endereço é obrigatório para criar um parceiro.");
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

                PartnerModel partner = new PartnerModel(
                                dto.login(),
                                encryptedPassword,
                                dto.name(),
                                imageUrl,
                                dto.emailContato(),
                                dto.numeroContato(),
                                endereco,
                                dto.categoria(),
                                dto.descricao(),
                                TipoPet.fromString(dto.tipoPet()),
                                ClientRole.PARTNER);

                endereco.setPartnerModel(partner);
                partner.setEndereco(endereco);

                partnerRepository.save(partner);

                if (partner.getEmailContato() != null && !partner.getEmailContato().isBlank()) {
                        String token = UUID.randomUUID().toString();
                        PartnerConfirmationToken confirmationToken = new PartnerConfirmationToken(
                                        null,
                                        token,
                                        LocalDateTime.now(),
                                        LocalDateTime.now().plusMinutes(30),
                                        null,
                                        partner);
                        partnerConfirmationTokenRepository.save(confirmationToken);
                        String link = "https://parceiro.redepatas.com.br/confirmPartnerEmail/" + token;
                        try {
                                emailService.enviarConfirmacao(partner.getEmailContato(), partner.getName(), link);
                        } catch (MessagingException e) {
                                e.printStackTrace();
                        }
                }

                return "Parceiro cadastrado com sucesso! Confirme seu e-mail para ativar o login.";
        }

        public String updateBasic(String login, String name, String descricao) {
                var partner = partnerRepository.findByLogin(login);
                if (partner == null) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Parceiro não encontrado");
                }
                if (name != null && !name.isBlank()) {
                        partner.setName(name.trim());
                }
                if (descricao != null) {
                        partner.setDescricao(descricao.trim());
                }
                partnerRepository.save(partner);
                return "Perfil do parceiro atualizado com sucesso";
        }

        public String updateAddress(String login, String rua, String bairro, String cidade, String estado,
                        String cep, Integer numero, String complemento, String lugar) {
                var partner = partnerRepository.findByLogin(login);
                if (partner == null) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Parceiro não encontrado");
                }

                EnderecoPartner end = partner.getEndereco();
                if (end == null) {
                        end = new EnderecoPartner();
                        end.setPartnerModel(partner);
                        partner.setEndereco(end);
                }
                if (rua != null)
                        end.setRua(rua);
                if (bairro != null)
                        end.setBairro(bairro);
                if (cidade != null)
                        end.setCidade(cidade);
                if (estado != null)
                        end.setEstado(estado);
                if (cep != null)
                        end.setCep(cep);
                if (numero != null)
                        end.setNumero(numero);
                if (complemento != null)
                        end.setComplemento(complemento);
                if (lugar != null)
                        end.setLugar(lugar);

                // save via partner cascade
                partnerRepository.save(partner);
                return "Endereço do parceiro atualizado com sucesso";
        }

        public String changePassword(String login, String oldPassword, String newPassword,
                        AuthenticationManager authenticationManager) {
                var partner = partnerRepository.findByLogin(login);
                if (partner == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parceiro não encontrado!");
                }
                try {
                        var auth = new UsernamePasswordAuthenticationToken(login,
                                        oldPassword);
                        authenticationManager.authenticate(auth);
                } catch (Exception e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha antiga incorreta!");
                }
                String encryptedPassword = new BCryptPasswordEncoder().encode(newPassword);
                partner.setPassword(encryptedPassword);
                partnerRepository.save(partner);
                return "Senha alterada com sucesso";
        }

        public PartnerProfileDto getProfile(String login) {
                var partner = partnerRepository.findByLogin(login);
                if (partner == null) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Parceiro nǜo encontrado");
                }
                return new PartnerProfileDto(
                                partner.getIdPartner(),
                                partner.getName(),
                                partner.getImageUrl(),
                                partner.getEmailContato(),
                                partner.getNumeroContato(),
                                partner.getDescricao());
        }
        
        public void resendConfirmationEmail(String emailContato) {
                if (emailContato == null || emailContato.isBlank()) {
                        return;
                }

                PartnerModel partner = partnerRepository.findByEmailContato(emailContato);
                if (partner == null || partner.isEmailConfirmado()) {
                        return;
                }

                String token = UUID.randomUUID().toString();
                PartnerConfirmationToken confirmationToken = new PartnerConfirmationToken(
                                null,
                                token,
                                LocalDateTime.now(),
                                LocalDateTime.now().plusMinutes(30),
                                null,
                                partner);
                partnerConfirmationTokenRepository.save(confirmationToken);

                String link = "https://parceiro.redepatas.com.br/confirmPartnerEmail/" + token;
                try {
                        emailService.enviarConfirmacao(partner.getEmailContato(), partner.getName(), link);
                } catch (MessagingException e) {
                        e.printStackTrace();
                }
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

}
