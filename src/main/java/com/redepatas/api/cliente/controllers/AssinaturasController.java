package com.redepatas.api.cliente.controllers;

import com.redepatas.api.cliente.dtos.AssinaturaDtos.AssinaturaAtivaDto;
import com.redepatas.api.cliente.dtos.AssinaturaDtos.PlanoDto;
import com.redepatas.api.cliente.models.PlanoAssinatura;
import com.redepatas.api.cliente.repositories.PlanoAssinaturaRepository;
import com.redepatas.api.cliente.services.AsaasClientService;
import com.redepatas.api.cliente.services.AssinaturaServices;
import com.redepatas.api.cliente.repositories.AssinaturaClienteRepository;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/assinaturas")
public class AssinaturasController {

    @Autowired
    private PlanoAssinaturaRepository planoAssinaturaRepository;

    @Autowired
    private AssinaturaServices assinaturaService;

    @Autowired
    private AssinaturaClienteRepository assinaturaClienteRepository;

    @Autowired
    AsaasClientService asaasClientService;

    @GetMapping("/listar")
    public List<PlanoAssinatura> listarPlanos() {
        return planoAssinaturaRepository.findAll();
    }

    @GetMapping("/myPlan")
    public AssinaturaAtivaDto listarPlanoAtivoPorCliente(@AuthenticationPrincipal UserDetails userDetails) {
        String login = userDetails.getUsername();

        AssinaturaClienteModel assinaturaAtiva = assinaturaService.buscarAssinaturaAtivaPorLogin(login);
        PlanoAssinatura plano = assinaturaAtiva.getPlano();

        PlanoDto planoDto = new PlanoDto(
                plano.getId(),
                plano.getNome(),
                plano.getPreco(),
                new ArrayList<>(plano.getBeneficios()), // Convertendo Set para List
                plano.getDuracaoDias());

        return new AssinaturaAtivaDto(
                assinaturaAtiva.getId(),
                assinaturaAtiva.getDataInicio().toLocalDate(),
                assinaturaAtiva.getDataFim().toLocalDate(),
                assinaturaAtiva.getStatusAssinatura().name(),
                planoDto);
    }

    @PostMapping("/newSignature/{login}")
    public ResponseEntity<String> criarAssinatura(@PathVariable("login") String login) {
        Long plano = (long) 1; // MUDAR FUTURAMENTE PARA MULTIPLOS PLANOS
        String resultado = assinaturaService.criarAssinatura(login, plano);

        if (resultado.contains("Falha")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultado);
        }

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/getLink/{idSub}")
    public ResponseEntity<String> getLink(@PathVariable("idSub") String idSub) {
        AssinaturaClienteModel assinatura = assinaturaClienteRepository.findByIdAsaas(idSub);
        if (assinatura == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhuma assinatura encontrada com esse ID");
        }
        return ResponseEntity.ok(asaasClientService.getLink(idSub));
    }
}
