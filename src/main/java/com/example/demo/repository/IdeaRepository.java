package com.example.demo.repository;

import com.example.demo.model.Idea;
import com.example.demo.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IdeaRepository extends JpaRepository<Idea, Long> {
    List<Idea> findByCategoryOrderByLikeCountDesc(Category category);
}