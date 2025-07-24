package com.redepatas.api.parceiro.dtos.AgedamentoDtos;

import java.time.LocalDate;
import java.util.UUID;

// DTO que representa a notificação que o parceiro receberá na tela
public record AgendamentoNotificationDto(
    UUID idAgendamento,
    String nomeCliente,
    String nomePet,
    String servico,
    LocalDate data,
    String intervalo
) {}