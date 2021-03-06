package com.kuznetsova.loritimesheets.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Token implements Serializable{
    @SerializedName("access_token")
    @Expose
    private String token;

    public Token(){}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
