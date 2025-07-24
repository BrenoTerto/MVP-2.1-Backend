package com.redepatas.api.config;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.redepatas.api.cliente.models.PlanoAssinatura;
import com.redepatas.api.cliente.repositories.PlanoAssinaturaRepository;

import jakarta.annotation.PostConstruct;

@Component
public class PlanoAssinaturaInitializer {

    @Autowired
    private PlanoAssinaturaRepository planoRepository;

    @PostConstruct
    public void init() {
        criarPlanoSeNaoExistir("Patinhas", new BigDecimal("29.90"),
                "Banhos", "Tosas", "Consultas");
        criarPlanoSeNaoExistir("PetFeliz", new BigDecimal("49.90"),
                "Banhos", "Tosas", "Consultas", "Produtos", "Rações");
        criarPlanoSeNaoExistir("PediGree", new BigDecimal("69.90"),
                "Banhos", "Tosas", "Consultas", "Vacinas", "Exames", "Medicamentos");
        criarPlanoSeNaoExistir("PetVip", new BigDecimal("119.90"),
                "Banhos", "Tosas", "Consultas", "Serviços Pet em Casa", "Clínico 24h", "Brindes Exclusivos");
    }

    private void criarPlanoSeNaoExistir(String nome, BigDecimal preco, String... beneficios) {
        if (planoRepository.findByNome(nome).isEmpty()) {
            PlanoAssinatura plano = new PlanoAssinatura();
            plano.setNome(nome);
            plano.setPreco(preco);
            Set<String> beneficiosSet = new HashSet<>();
            for (String beneficio : beneficios) {
                beneficiosSet.add(beneficio);
            }
            plano.setBeneficios(beneficiosSet);
            planoRepository.save(plano);
        }
    }
}
