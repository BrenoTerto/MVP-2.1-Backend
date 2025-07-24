package com.redepatas.api.parceiro.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.redepatas.api.parceiro.models.HorarioIntervaloModel;

public interface HorarioIntervaloRepository extends JpaRepository<HorarioIntervaloModel, UUID> {
}
