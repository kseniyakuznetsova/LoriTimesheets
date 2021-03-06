package com.kuznetsova.loritimesheets.entity;

import java.io.Serializable;

public class Task implements Serializable{
    private String id;
    private String name;
    private Project project;

    public Task() {
    }

    public Task(String id) {
        this.id = id;
    }

    public Task(String id, String name, Project project) {
        this.id = id;
        this.name = name;
        this.project = project;
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
