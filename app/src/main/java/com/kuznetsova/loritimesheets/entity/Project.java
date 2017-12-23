package com.kuznetsova.loritimesheets.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class Project implements Serializable{
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;

    public Project(String name) {
        this.name = name;
    }

    public Project(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Project() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
