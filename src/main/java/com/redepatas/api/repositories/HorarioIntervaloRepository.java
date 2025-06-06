package com.redepatas.api.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.redepatas.api.models.Partner.HorarioIntervaloModel;

public interface HorarioIntervaloRepository extends JpaRepository<HorarioIntervaloModel, UUID> {
}
