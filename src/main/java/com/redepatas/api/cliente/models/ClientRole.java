package com.redepatas.api.cliente.models;

public enum ClientRole {
    ADMIN("admin"),
    USER("user"),
    PARTNER("partner");

    private String role;

    ClientRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
