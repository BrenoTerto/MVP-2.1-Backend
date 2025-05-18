package com.redepatas.api.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redepatas.api.dtos.ChangePasswordDto;
import com.redepatas.api.dtos.RegisterDTO;
import com.redepatas.api.dtos.ReponseLoginDto;
import com.redepatas.api.infra.security.TokenService;
import com.redepatas.api.models.AuthenticationDTO;
import com.redepatas.api.models.ClientModel;
import com.redepatas.api.models.ClientRole;
import com.redepatas.api.models.ConfirmationToken;
import com.redepatas.api.repositories.ClientRepository;
import com.redepatas.api.repositories.ConfirmationTokenRepository;
import com.redepatas.api.services.AsaasClientService;
import com.redepatas.api.services.EmailService;
import com.redepatas.api.services.UserServices;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private ClientRepository repository;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private AsaasClientService asaasClientService;

    @Autowired
    private ConfirmationTokenRepository tokenRepository;

    @Autowired
    private UserServices userServices;

    @Autowired
    EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<ReponseLoginDto> login(@RequestBody @Valid AuthenticationDTO data) {
        ClientModel client = (ClientModel) repository.findByLogin(data.login());
        if (client == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas!");
        }
        if (!client.isEmailConfirmado()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cadastro não confirmado!");
        }
        try {
            var clientPassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
            var auth = this.authenticationManager.authenticate(clientPassword);
            var token = tokenService.generateToken((ClientModel) auth.getPrincipal());
            return ResponseEntity.ok(new ReponseLoginDto(token));
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas!");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor.");
        }
    }

    private boolean isEmail(String login) {
        return login.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private boolean isTelefone(String login) {
        return login.matches("^\\d{10,11}$");
    }

    public static boolean isCPFValido(String cpf) {
        // Remove caracteres não numéricos
        cpf = cpf.replaceAll("[^\\d]", "");

        // Verifica se tem 11 dígitos ou todos os dígitos iguais (ex: 00000000000)
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}"))
            return false;

        try {
            int soma = 0, peso = 10;
            for (int i = 0; i < 9; i++) {
                soma += (cpf.charAt(i) - '0') * peso--;
            }
            int digito1 = 11 - (soma % 11);
            digito1 = (digito1 >= 10) ? 0 : digito1;

            soma = 0;
            peso = 11;
            for (int i = 0; i < 10; i++) {
                soma += (cpf.charAt(i) - '0') * peso--;
            }
            int digito2 = 11 - (soma % 11);
            digito2 = (digito2 >= 10) ? 0 : digito2;

            return cpf.charAt(9) - '0' == digito1 && cpf.charAt(10) - '0' == digito2;
        } catch (Exception e) {
            return false;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterDTO data) {
        Boolean tipoLogin;
        try {
            if (this.repository.findByLogin(data.login()) != null
                    || this.repository.findByPhoneNumber(data.login()) != null
                    || this.repository.findByCPF(data.CPF()) != null) {
                return ResponseEntity.badRequest().body("Email, telefone ou CPF já cadastrados!");
            }
            if (!isCPFValido(data.CPF())) {
                return ResponseEntity.badRequest().body("CPF inválido.");
            }
            if (isEmail(data.login())) {
                tipoLogin = true;
            } else if (isTelefone(data.login())) {
                tipoLogin = false;
            } else {
                throw new IllegalArgumentException("Login deve ser um e-mail ou telefone válido.");
            }
            String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());

            ClientModel newUser = new ClientModel(data.login(), encryptedPassword, data.CPF(), data.name(),
                    ClientRole.USER);
            newUser.setEmailConfirmado(false);
            this.repository.save(newUser);

            String token = UUID.randomUUID().toString();
            ConfirmationToken confirmationToken = new ConfirmationToken(
                    null, token,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusMinutes(30),
                    null,
                    newUser);
            String link = "http://localhost:8080/auth/confirm?token=" + token;
            emailService.enviarConfirmacao(data.login(), data.name(), link);
            tokenRepository.save(confirmationToken);

            return ResponseEntity.ok().body("Usuário cadastrado com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Dados inválidos fornecidos!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno no servidor.");
        }
    }

    @GetMapping("/confirm")
    public ResponseEntity<String> confirm(@RequestParam("token") String token) {
        Optional<ConfirmationToken> optionalToken = tokenRepository.findByToken(token);

        if (optionalToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Token inválido ou não encontrado.");
        }
        ConfirmationToken confirmationToken = optionalToken.get();
        if (confirmationToken.getConfirmedAt() != null) {
            return ResponseEntity.badRequest().body("Conta já confirmada.");
        }

        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token expirado.");
        }

        ClientModel user = confirmationToken.getUser();

        

        Boolean tipoLogin = isEmail(user.getLogin());
        String idCustomer;
        String clienteResponse = asaasClientService.criarCliente(user.getName(), user.getCPF(), user.getLogin(), tipoLogin);
        System.out.println(clienteResponse);

        if (clienteResponse == null || clienteResponse.contains("erro")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao criar cliente asaas, Erro: " + clienteResponse);
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(clienteResponse);
            idCustomer = rootNode.get("id").asText();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao processar resposta do Asaas: " + e.getMessage());
        }
        user.setIdCustomer(idCustomer);
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        tokenRepository.save(confirmationToken);
        user.setEmailConfirmado(true);
        repository.save(user);
        return ResponseEntity.ok("Cadastro confirmado com sucesso!");
    }

    @PutMapping("/changePassword")
    public ResponseEntity<String> changePassword(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ChangePasswordDto data) {
        String login = userDetails.getUsername();
        String response = userServices.changePassword(login, data);
        if (response.contains("Falha")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.ok(response);
    }

}
