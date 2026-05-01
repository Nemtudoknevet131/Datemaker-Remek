package com.example.demo.dto;

import com.example.demo.model.Idea;
import com.example.demo.model.IdeaReaction;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdeaDto {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private int likeCount;
    private String currentUserReaction;

    public static IdeaDto from(Idea idea, IdeaReaction.ReactionType reactionType) {
        return new IdeaDto(
            idea.getId(),
            idea.getTitle(),
            idea.getDescription(),
            idea.getImageUrl(),
            idea.getLikeCount(),
            reactionType != null ? reactionType.name() : "NONE"
        );
    }
}