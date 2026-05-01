package com.example.demo.config;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InspirationSeeder {
    private final CategoryRepository categoryRepository;
    private final IdeaRepository ideaRepository;

    @PostConstruct
    public void seed() {
        if (categoryRepository.count() > 0)
            return;

        String[][] data = {
                { "Romantic", "Candlelight dinner", "Stargazing night", "Love letter exchange" },
                { "Adventure", "Hiking trip", "Kayaking together", "Hot air balloon ride" },
                { "At Home", "Movie marathon", "Cooking together", "Game night" },
                { "Food & Drinks", "Wine tasting", "Picnic brunch", "Bake-off challenge" },
                { "Culture & Arts", "Museum visit", "Paint and sip", "Local theater date" },
                { "Outdoors & Nature", "Beach walk", "Picnic in park", "Camping weekend" }
        };

        for (String[] catData : data) {
            Category c = new Category();
            c.setName(catData[0]);
            categoryRepository.save(c);

            for (int i = 1; i <= 3; i++) {
                Idea idea = new Idea();
                idea.setTitle(catData[i]);
                idea.setDescription("A fun idea: " + catData[i]);
                idea.setImageUrl("/inspirations/sample" + i + ".jpg");
                idea.setCategory(c);
                ideaRepository.save(idea);
            }
        }
    }
}