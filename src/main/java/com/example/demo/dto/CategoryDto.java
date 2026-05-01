package com.example.demo.dto;

import com.example.demo.model.Category;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private Long id;
    private String name;
    private List<IdeaDto> ideas;

    public static CategoryDto from(Category category, List<IdeaDto> ideas) {
        return new CategoryDto(category.getId(), category.getName(), ideas);
    }
}