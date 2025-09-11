package com.redepatas.api.cliente.config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.redepatas.api.cliente.models.PlanoAssinatura;
import com.redepatas.api.cliente.repositories.PlanoAssinaturaRepository;
import com.redepatas.api.parceiro.models.TipoServico;

@Component
public class PlanoSeeder implements ApplicationRunner {

    private final PlanoAssinaturaRepository planoRepository;

    public PlanoSeeder(PlanoAssinaturaRepository planoRepository) {
        this.planoRepository = planoRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (planoRepository.count() > 0) {
            return;
        }

        List<PlanoAssinatura> planos = new ArrayList<>();
        List<TipoServico> servicosAcumulados = new ArrayList<>();

        // Plano Patinhas
        PlanoAssinatura patinhas = new PlanoAssinatura();
        patinhas.setNome("Patinhas");
        patinhas.setPreco(BigDecimal.valueOf(29.90));
        patinhas.setNivel(1);
        patinhas.setCustomizado(false);
        patinhas.setServicos(List.of(TipoServico.BANHO, TipoServico.TOSA_HIGIENICA, TipoServico.CONSULTA));
        servicosAcumulados.addAll(patinhas.getServicos());
        patinhas.setServicos(new ArrayList<>(servicosAcumulados));
        planos.add(patinhas);

        // Plano Patinhas 30
        PlanoAssinatura patinhas30 = new PlanoAssinatura();
        patinhas30.setNome("Patinhas 30");
        patinhas30.setPreco(BigDecimal.valueOf(29.90));
        patinhas30.setNivel(1);
        patinhas30.setCustomizado(true);
        List<TipoServico> servicosPatinhas30 = new ArrayList<>(patinhas.getServicos());
        servicosPatinhas30.addAll(List.of(
                TipoServico.HOTELZINHO,
                TipoServico.ADESTRADOR,
                TipoServico.PETSITTER,
                TipoServico.CRECHE
        ));

        patinhas30.setServicos(servicosPatinhas30);
        planos.add(patinhas30);

        // Plano Auconchego
        PlanoAssinatura auconchego = new PlanoAssinatura();
        auconchego.setNome("Auconchego");
        auconchego.setPreco(BigDecimal.valueOf(49.90));
        auconchego.setNivel(2);
        auconchego.setCustomizado(false);
        auconchego.setServicos(List.of(TipoServico.TAXI_DOG, TipoServico.HOTELZINHO));
        servicosAcumulados.addAll(auconchego.getServicos());
        auconchego.setServicos(new ArrayList<>(servicosAcumulados));
        planos.add(auconchego);

        // Plano Lambida da Saudade
        PlanoAssinatura lambida = new PlanoAssinatura();
        lambida.setNome("Lambida da Saudade");
        lambida.setPreco(BigDecimal.valueOf(69.90));
        lambida.setNivel(3);
        lambida.setCustomizado(false);
        lambida.setServicos(List.of(TipoServico.PRODUTOS, TipoServico.RACOES, TipoServico.ADESTRADOR));
        servicosAcumulados.addAll(lambida.getServicos());
        lambida.setServicos(new ArrayList<>(servicosAcumulados));
        planos.add(lambida);

        // Plano Amor Maior
        PlanoAssinatura amorMaior = new PlanoAssinatura();
        amorMaior.setNome("Amor Maior");
        amorMaior.setPreco(BigDecimal.valueOf(119.90));
        amorMaior.setNivel(4);
        amorMaior.setCustomizado(false);
        amorMaior.setServicos(List.of(TipoServico.VACINAS, TipoServico.EXAMES, TipoServico.MEDICAMENTOS,
                TipoServico.SERVICO_EM_CASA, TipoServico.CLINICO_24H, TipoServico.BRINDES_EXCLUSIVOS));
        servicosAcumulados.addAll(amorMaior.getServicos());
        amorMaior.setServicos(new ArrayList<>(servicosAcumulados));
        planos.add(amorMaior);

        // Salvar todos
        planoRepository.saveAll(planos);
        System.out.println("Planos base criados com hierarquia de serviços!");
    }
}
