package com.redepatas.api.cliente.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redepatas.api.cliente.dtos.ChangePasswordDto;
import com.redepatas.api.cliente.dtos.RecoveryPasswordDto;
import com.redepatas.api.cliente.dtos.RegisterDTO;
import com.redepatas.api.cliente.dtos.ReponseLoginDto;
import com.redepatas.api.cliente.models.AuthenticationDTO;
import com.redepatas.api.cliente.models.ClientModel;
import com.redepatas.api.cliente.models.ClientRole;
import com.redepatas.api.cliente.models.ConfirmationToken;
import com.redepatas.api.cliente.models.PasswordResetToken;
import com.redepatas.api.cliente.repositories.ClientRepository;
import com.redepatas.api.cliente.repositories.ConfirmationTokenRepository;
import com.redepatas.api.cliente.repositories.PasswordResetTokenRepository;
import com.redepatas.api.cliente.services.AsaasClientService;
import com.redepatas.api.cliente.services.EmailService;
import com.redepatas.api.cliente.services.UserServices;
import com.redepatas.api.infra.security.TokenService;
import com.redepatas.api.utils.ValidationUtil;

import jakarta.mail.MessagingException;
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

        } catch (ResponseStatusException e) {
            throw e;
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas!");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterDTO data) {
        try {
            if (!ValidationUtil.isCPFValido(data.CPF())) {
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

            String link = "https://beta.redepatas.com/confirmEmail/" + token;
            emailService.enviarConfirmacao(data.email(), data.name(), link);
            tokenRepository.save(confirmationToken);

            return ResponseEntity.ok().body("Usuário cadastrado com sucesso!");

        } catch (IllegalArgumentException e) {
            System.out.println(e);
            return ResponseEntity.badRequest().body("Dados inválidos fornecidos!" + e);
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

        String idCustomer;
        String clienteResponse = asaasClientService.criarCliente(user.getName(), user.getCPF(), user.getEmail(),
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
        var userDetails = repository.findByEmail(email);
        if (userDetails instanceof ClientModel clientModel) {
            if (clientModel.isEmailConfirmado()) {
                String token = tokenService.generateTokenPassword(clientModel.getEmail());
                emailService.enviarRecovery(clientModel.getEmail(), token);
            }
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
}
