package com.example.demo.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.model.Category;
import com.example.demo.dto.CategoryDto;
import com.example.demo.dto.IdeaDto;
import com.example.demo.dto.ReactionRequest;
import com.example.demo.model.Idea;
import com.example.demo.model.IdeaReaction;
import com.example.demo.model.User;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.IdeaReactionRepository;
import com.example.demo.repository.IdeaRepository;
import com.example.demo.repository.UserRepository;

@Service
public class InspirationService {

    private final CategoryRepository categoryRepository;
    private final IdeaRepository ideaRepository;
    private final IdeaReactionRepository ideaReactionRepository;
    private final UserRepository userRepository;

    public InspirationService(CategoryRepository categoryRepository,
            IdeaRepository ideaRepository,
            IdeaReactionRepository ideaReactionRepository,
            UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.ideaRepository = ideaRepository;
        this.ideaReactionRepository = ideaReactionRepository;
        this.userRepository = userRepository;
    }

    public List<CategoryDto> getAllCategories(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        User currentUser = userOpt.orElse(null);

        return categoryRepository.findAll().stream()
                .map(cat -> {
                    List<IdeaDto> ideas = ideaRepository.findByCategoryOrderByLikeCountDesc(cat).stream()
                            .map(idea -> {
                                IdeaReaction.ReactionType userReaction = null;

                                if (currentUser != null) {
                                    userReaction = ideaReactionRepository
                                            .findByIdeaAndUser(idea, currentUser)
                                            .map(IdeaReaction::getType)
                                            .orElse(null);
                                }

                                return IdeaDto.from(idea, userReaction);
                            })
                            .collect(Collectors.toList());

                    return CategoryDto.from(cat, ideas);
                })
                .collect(Collectors.toList());
    }

    public IdeaDto createIdea(String email, Long categoryId, String title,
            String description, String imageUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Idea idea = new Idea();
        idea.setTitle(title);
        idea.setDescription(description);
        idea.setImageUrl(imageUrl);
        idea.setCategory(category);
        idea.setCreatedBy(user);
        idea.setLikeCount(0);

        Idea saved = ideaRepository.save(idea);
        return IdeaDto.from(saved, null);
    }

    public IdeaDto reactToIdea(String email, ReactionRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Idea idea = ideaRepository.findById(req.getIdeaId())
                .orElseThrow(() -> new IllegalArgumentException("Idea not found"));

        Optional<IdeaReaction> existingOpt = ideaReactionRepository.findByIdeaAndUser(idea, user);
        IdeaReaction.ReactionType newType = req.getType();

        if (existingOpt.isPresent()) {
            IdeaReaction existing = existingOpt.get();

            if (existing.getType() == newType) {
                ideaReactionRepository.delete(existing);
            } else {
                existing.setType(newType);
                ideaReactionRepository.save(existing);
            }
        } else {
            IdeaReaction reaction = new IdeaReaction();
            reaction.setIdea(idea);
            reaction.setUser(user);
            reaction.setType(newType);
            ideaReactionRepository.save(reaction);
        }

        int likeCount = ideaReactionRepository.countByIdeaAndType(idea, IdeaReaction.ReactionType.LIKE);
        idea.setLikeCount(likeCount);
        ideaRepository.save(idea);

        return IdeaDto.from(idea, newType);
    }
}