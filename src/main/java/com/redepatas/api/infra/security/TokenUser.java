package com.redepatas.api.infra.security;

import java.util.UUID;

public interface TokenUser {
    String getLogin();
    String getNome();
    Object getRole();
    UUID getId();
}
