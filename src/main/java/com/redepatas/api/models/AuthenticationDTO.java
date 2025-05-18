package com.redepatas.api.models;

import jakarta.validation.constraints.NotNull;

public record AuthenticationDTO (
    @NotNull(message = "O campo login é obrigatório.") String login, 
    @NotNull(message = "O campo senha é obrigatório.") String password
){
}
