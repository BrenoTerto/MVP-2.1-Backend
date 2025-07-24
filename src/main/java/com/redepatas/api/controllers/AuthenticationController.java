package com.redepatas.api.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redepatas.api.dtos.ChangePasswordDto;
import com.redepatas.api.dtos.RecoveryPasswordDto;
import com.redepatas.api.dtos.RegisterDTO;
import com.redepatas.api.dtos.ReponseLoginDto;
import com.redepatas.api.infra.security.TokenService;
import com.redepatas.api.models.AuthenticationDTO;
import com.redepatas.api.models.ClientModel;
import com.redepatas.api.models.ClientRole;
import com.redepatas.api.models.ConfirmationToken;
import com.redepatas.api.models.PasswordResetToken;
import com.redepatas.api.repositories.ClientRepository;
import com.redepatas.api.repositories.ConfirmationTokenRepository;
import com.redepatas.api.repositories.PasswordResetTokenRepository;
import com.redepatas.api.services.AsaasClientService;
import com.redepatas.api.services.AssinaturaServices;
import com.redepatas.api.services.EmailService;
import com.redepatas.api.services.RateLimiterService;
import com.redepatas.api.services.UserServices;

import io.github.bucket4j.Bucket;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
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
import org.springframework.web.bind.annotation.PathVariable;
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
    private RateLimiterService rateLimiterService;

    @Autowired
    private AssinaturaServices assinaturaService;

    @Autowired
    private ClientRepository repository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AsaasClientService asaasClientService;

    @Autowired
    private ConfirmationTokenRepository tokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetToken;

    @Autowired
    private UserServices userServices;

    @Autowired
    EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<ReponseLoginDto> login(@RequestBody @Valid AuthenticationDTO data) {
        try {
            var clientPassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
            var auth = this.authenticationManager.authenticate(clientPassword);

            ClientModel client = (ClientModel) auth.getPrincipal();

            if (!client.isEmailConfirmado()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cadastro não confirmado!");
            }

            var token = tokenService.generateToken(client);
            return ResponseEntity.ok(new ReponseLoginDto(token));

        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas!");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor.");
        }
    }

    public static boolean isCPFValido(String cpf) {

        cpf = cpf.replaceAll("[^\\d]", "");

        if (cpf.length() != 11 || cpf.matches("(\\d)\1{10}"))
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
        try {
            if (!isCPFValido(data.CPF())) {
                return ResponseEntity.badRequest().body("CPF inválido.");
            }

            if (this.repository.existsByLogin(data.login())) {
                return ResponseEntity.badRequest().body("Login já cadastrados!");
            } else if (this.repository.existsByCPF(data.CPF())) {
                return ResponseEntity.badRequest().body("CPF já cadastrado");
            }
            boolean exists = this.repository.existsByEmailOuNumero(
                    data.numero(),
                    data.email());

            if (exists) {
                return ResponseEntity.badRequest().body("Email ou telefone já cadastrados!");
            }

            String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());

            ClientModel newUser = new ClientModel(data.login(), encryptedPassword, data.email(), data.numero(),
                    data.name(), ClientRole.USER, data.CPF());
            newUser.setEmailConfirmado(false);
            this.repository.save(newUser);

            String token = UUID.randomUUID().toString();
            ConfirmationToken confirmationToken = new ConfirmationToken(
                    null, token,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusMinutes(30),
                    null,
                    newUser);

            String link = "https://assinatura.redepatas.com.br/confirmEmail/" + token;
            emailService.enviarConfirmacao(data.email(), data.name(), link);
            tokenRepository.save(confirmationToken);

            return ResponseEntity.ok().body("Usuário cadastrado com sucesso!");

        } catch (IllegalArgumentException e) {
            System.out.println(e);
            return ResponseEntity.badRequest().body("Dados inválidos fornecidos!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno no servidor.");
        }
    }

    @GetMapping("/criarAssinatura/{token}")
    public String confirmEmail(@PathVariable String token) {
        Optional<ConfirmationToken> optionalToken = tokenRepository.findByToken(token);
        Long plano = (long) 1;
        if (optionalToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido ou não encontrado!");
        }

        ConfirmationToken confirmationToken = optionalToken.get();
        ClientModel user = confirmationToken.getUser();

        if (confirmationToken.getConfirmedAt() != null) {
            return assinaturaService.criarAssinatura(user.getLogin(), plano);
        }

        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link de confirmação expirado!");
        }

        String idCustomer;
        String clienteResponse = asaasClientService.criarCliente(user.getName(), user.getCPF(), user.getEmail(),
                user.getPhoneNumber());

        if (clienteResponse == null || clienteResponse.contains("erro")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Erro ao criar cliente asaas, Erro: " + clienteResponse);
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(clienteResponse);
            idCustomer = rootNode.get("id").asText();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Erro ao processar resposta do Asaas: " + e.getMessage());
        }
        user.setIdCustomer(idCustomer);
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        tokenRepository.save(confirmationToken);
        user.setEmailConfirmado(true);
        repository.save(user);
        return assinaturaService.criarAssinatura(user.getLogin(), plano);
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

        String idCustomer;
        String clienteResponse = asaasClientService.criarCliente(user.getName(), user.getLogin(), user.getEmail(),
                user.getPhoneNumber());

        if (clienteResponse == null || clienteResponse.contains("erro")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao criar cliente asaas, Erro: " + clienteResponse);
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(clienteResponse);
            idCustomer = rootNode.get("id").asText();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao processar resposta do Asaas: " + e.getMessage());
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

    @PostMapping("/sendEmailRecovery/{email}")
    public ResponseEntity<Void> sendEmailRecovery(@PathVariable("email") String email) throws MessagingException {
        var client = repository.findByLogin(email);
        ClientModel clientx = (ClientModel) client;
        if (client != null && clientx.isEmailConfirmado()) {
            String token = tokenService.generateTokenPassword(email);
            emailService.enviarRecovery(email, token);
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validateHash/{hashPuro}")
    public ResponseEntity<String> validarHash(@PathVariable("hashPuro") String hashPuro) {
        PasswordResetToken token = tokenService.validarTokenDeReset(hashPuro);
        if (token == null || token.getExpiration().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido ou expirado");
        }
        return ResponseEntity.ok("Token válido");
    }

    @Transactional
    @PutMapping("/recoveryPassword")
    public ResponseEntity<String> recoveryPassword(@RequestBody @Valid RecoveryPasswordDto data) {
        PasswordResetToken token = tokenService.validarTokenDeReset(data.hash());
        if (token == null || token.getExpiration().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido ou expirado");
        }
        String response = userServices.recoveryPassword(token.getEmail(), data.newPassword());
        passwordResetToken.delete(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reenviarEmail/{login}")
    public ResponseEntity<String> reenviarEmailConfirmacao(@PathVariable String login, HttpServletRequest request) {

        Bucket bucket = rateLimiterService.resolveBucket(login);

        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Aguarde 30 segundos antes de reenviar o email de confirmação.");
        }

        try {
            ClientModel cliente = (ClientModel) repository.findByLogin(login);

            if (cliente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado");
            }

            if (cliente.isEmailConfirmado()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Este email já foi confirmado.");
            }

            Optional<ConfirmationToken> optionalToken = tokenRepository.findByUser(cliente);

            if (optionalToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Nenhum token de confirmação encontrado para este usuário.");
            }

            ConfirmationToken confirmationToken = optionalToken.get();

            if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                String novoToken = UUID.randomUUID().toString();
                confirmationToken.setToken(novoToken);
                confirmationToken.setCreatedAt(LocalDateTime.now());
                confirmationToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));
                tokenRepository.save(confirmationToken);
            }

            String link = "https://assinatura.redepatas.com.br/confirmEmail/" + confirmationToken.getToken();
            emailService.enviarConfirmacao(cliente.getEmail(), cliente.getName(), link);

            return ResponseEntity.ok("Email de confirmação reenviado com sucesso");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }

}
