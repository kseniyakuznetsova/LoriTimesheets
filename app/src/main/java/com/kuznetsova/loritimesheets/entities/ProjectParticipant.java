package com.kuznetsova.loritimesheets.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProjectParticipant {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("project")
    @Expose
    private Project project;

    public ProjectParticipant(String id, Project project) {
        this.id = id;
        this.project = project;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
