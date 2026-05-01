package com.example.demo.dto;

import com.example.demo.model.IdeaReaction;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReactionRequest {
    private Long ideaId;
    private IdeaReaction.ReactionType type;
}