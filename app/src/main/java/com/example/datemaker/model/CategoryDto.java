package com.example.datemaker.model;

import java.util.List;

public class CategoryDto {
    private Long id;
    private String name;
    private List<IdeaDto> ideas;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<IdeaDto> getIdeas() {
        return ideas;
    }

    public void setIdeas(List<IdeaDto> ideas) {
        this.ideas = ideas;
    }
}