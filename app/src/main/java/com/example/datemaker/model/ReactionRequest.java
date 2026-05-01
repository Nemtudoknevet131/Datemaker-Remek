package com.example.datemaker.model;

public class ReactionRequest {
    private Long ideaId;
    private String type;

    public ReactionRequest(Long ideaId, String type) {
        this.ideaId = ideaId;
        this.type = type;
    }

    public Long getIdeaId() {
        return ideaId;
    }

    public void setIdeaId(Long ideaId) {
        this.ideaId = ideaId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}