package com.redepatas.api.infra.security;

import java.util.UUID;

public interface TokenUser {
    String getLogin();
    Object getRole(); // Usando Object para acomodar ClientRole e outros tipos
    UUID getId();
}
