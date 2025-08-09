package com.redepatas.api.parceiro.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.redepatas.api.parceiro.dtos.PartnerDtos.HorarioProjecao;
import com.redepatas.api.parceiro.models.AgendaHorarioModel;

@Repository
public interface HorariosRepository extends JpaRepository<AgendaHorarioModel, UUID> {

  @Query(value = """
      SELECT
        ah.id AS horario_id,
        ah.horario_inicio,
        ah.horario_Fim
      FROM
        Agenda a
      INNER JOIN
        Agenda_Dia x ON a.id = x.agenda_id
      INNER JOIN
        Agenda_Horario ah ON x.id = ah.dia_id
      WHERE
        a.servico_id = :idDoServico AND dia_semana = :diaSemana
      ORDER BY
        ah.horario_inicio;
            """, nativeQuery = true)
  List<HorarioProjecao> findHorariosByServicoId(@Param("idDoServico") UUID idDoServico,
      @Param("diaSemana") String diaSemana);
}
