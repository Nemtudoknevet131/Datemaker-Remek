package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Idea;
import com.example.demo.model.IdeaReaction;
import com.example.demo.model.User;

public interface IdeaReactionRepository extends JpaRepository<IdeaReaction, Long> {
    Optional<IdeaReaction> findByIdeaAndUser(Idea idea, User user);

    int countByIdeaAndType(Idea idea, IdeaReaction.ReactionType type);
}