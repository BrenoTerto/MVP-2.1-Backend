package com.redepatas.api.parceiro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.redepatas.api.parceiro.dtos.PartnerDtos.ParceiroBuscaProjecao;
import com.redepatas.api.parceiro.models.PartnerModel;

import java.util.List;
import java.util.UUID;

@Repository
public interface PartnerRepository extends JpaRepository<PartnerModel, UUID> {

    boolean existsByEmailContato(String email);

    PartnerModel findByLogin(String login);

    PartnerModel findByEmailContato(String emailContato);

    List<PartnerModel> findByEndereco_Cidade(String cidade);

    @Query(value = """
                SELECT DISTINCT
                    p.id_partner,
                    s.id AS id_servico,
                    p.name AS nome_do_parceiro,
                    p.image_url AS imagem,
                    p.avaliacao,
                    ep.cidade,
                    ep.estado,
                    ep.bairro,
                    ep.rua,
                    s.descricao AS servico_oferecido,
                    s.preco_pequeno,
                    s.preco_grande
                FROM
                    partner p
                INNER JOIN
                    endereco_partner ep ON p.id_partner = ep.id_partner
                INNER JOIN
                    Servico s ON p.id_partner = s.parceiro_id
                INNER JOIN
                    Agenda a ON s.id = a.servico_id
                INNER JOIN
                    Agenda_Dia x ON a.id = x.agenda_id
                INNER JOIN
                    Agenda_Horario ah ON x.id = ah.dia_id
                WHERE
                    ep.cidade ILIKE :cidade
                    AND x.dia_semana = :diaSemana
                    AND s.tipo ILIKE :tipoServico
                    AND (:tamanhoPet <> 'GRANDE' OR s.aceita_pet_grande = TRUE)
            """, nativeQuery = true)
    List<ParceiroBuscaProjecao> findParceirosDisponiveis(
            @Param("cidade") String cidade,
            @Param("diaSemana") String diaSemana,
            @Param("tipoServico") String tipoServico,
            @Param("tamanhoPet") String tamanhoPet);
}
