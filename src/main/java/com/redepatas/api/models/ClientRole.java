package com.redepatas.api.models;

public enum ClientRole {
    ADMIN("admin"),
    USER("user");

    private String role;

    ClientRole(String role){
        this.role = role;
    }

    public String getRole(){
        return role;
    }
}
