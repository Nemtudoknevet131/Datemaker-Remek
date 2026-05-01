package com.example.demo.controller;

import com.example.demo.dto.CategoryDto;
import com.example.demo.dto.IdeaDto;
import com.example.demo.dto.ReactionRequest;
import com.example.demo.service.InspirationService;
import com.example.demo.service.InspirationStorageService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/inspiration")
@RequiredArgsConstructor
public class InspirationController {

    private final InspirationService inspirationService;
    private final InspirationStorageService inspirationStorageService;

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getCategories(Authentication auth) {
        return ResponseEntity.ok(inspirationService.getAllCategories(auth.getName()));
    }

    @PostMapping(value = "/upload", consumes = { "multipart/form-data" })
    public ResponseEntity<String> uploadImage(@RequestPart("file") MultipartFile file) {
        try {
            String url = inspirationStorageService.save(file);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed");
        }
    }

    @PostMapping("/ideas")
    public ResponseEntity<IdeaDto> createIdea(@RequestParam Long categoryId,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String imageUrl,
            Authentication auth) {
        return ResponseEntity.ok(
                inspirationService.createIdea(auth.getName(), categoryId, title, description, imageUrl));
    }

    @PostMapping("/react")
    public ResponseEntity<IdeaDto> react(@RequestBody ReactionRequest req, Authentication auth) {
        return ResponseEntity.ok(inspirationService.reactToIdea(auth.getName(), req));
    }
}
