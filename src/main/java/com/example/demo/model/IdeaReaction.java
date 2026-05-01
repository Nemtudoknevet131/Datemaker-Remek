package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inspiration_reactions", uniqueConstraints = @UniqueConstraint(columnNames = { "idea_id", "user_id" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdeaReaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idea_id")
    private Idea idea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private ReactionType type;

    public enum ReactionType {
        LIKE, DISLIKE
    }
}