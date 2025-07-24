package com.redepatas.api.cliente.controllers;

import com.redepatas.api.cliente.dtos.AssinaturaDtos.AssinaturaAtivaDto;
import com.redepatas.api.cliente.dtos.AssinaturaDtos.PlanoDto;
import com.redepatas.api.cliente.models.PlanoAssinatura;
import com.redepatas.api.cliente.repositories.PlanoAssinaturaRepository;
import com.redepatas.api.cliente.services.AsaasClientService;
import com.redepatas.api.cliente.services.AssinaturaServices;

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

    @PostMapping("/newSignature/{idAssinatura}")
    public ResponseEntity<String> criarAssinatura(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("idAssinatura") Long planoId) {

        String login = userDetails.getUsername();

        String resultado = assinaturaService.criarAssinatura(login, planoId);

        if (resultado.contains("Falha")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultado);
        }

        return ResponseEntity.ok(resultado);
    }

}
