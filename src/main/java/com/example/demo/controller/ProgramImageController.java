package com.example.demo.controller;

import com.example.demo.model.ProgramImage;
import com.example.demo.model.User;
import com.example.demo.repository.ProgramImageRepository;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/images")
public class ProgramImageController {

    @Autowired
    private ProgramImageRepository programImageRepository;

    @Autowired
    private UserService userService;

    private static final String UPLOAD_DIR = "C:/demo/uploads/";

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam String userId,
            @RequestParam String partnerId,
            @RequestParam("caption") String caption,
            @RequestParam("image") MultipartFile imageFile) throws IOException {

        System.out.println("📩 /api/images/upload CALLED!");
        System.out.println("userId = " + userId);
        System.out.println("partnerId = " + partnerId);
        System.out.println("caption = " + caption);
        System.out.println("fileName = " + (imageFile != null ? imageFile.getOriginalFilename() : "null"));

        Long userIdLong = Long.parseLong(userId);
        Long partnerIdLong = Long.parseLong(partnerId);

        User user = userService.findById(userIdLong).orElse(null);
        User partner = userService.findById(partnerIdLong).orElse(null);

        if (user == null)
            return ResponseEntity.badRequest().body("User not found.");

        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
        String filePath = UPLOAD_DIR + fileName;
        imageFile.transferTo(new File(filePath));

        ProgramImage programImage = new ProgramImage();
        programImage.setCaption(caption);
        programImage.setImageUrl(fileName);
        programImage.setUser(user);
        programImage.setPartner(partner);

        programImageRepository.save(programImage);

        return ResponseEntity.ok("Image uploaded successfully");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getImagesForUser(@PathVariable Long userId, HttpServletRequest request) {
        var user = userService.findById(userId).orElse(null);
        if (user == null)
            return ResponseEntity.badRequest().body("User not found.");

        var images = programImageRepository.findByUserIdOrPartnerId(userId, userId);

        String baseUrl = String.format("%s://%s:%d/uploads/",
                request.getScheme(), request.getServerName(), request.getServerPort());

        images.forEach(img -> {
            img.setImageUrl(baseUrl + img.getImageUrl());
        });

        return ResponseEntity.ok(images);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable Long id) {
        return programImageRepository.findById(id)
                .map(image -> {
                    File file = new File(
                            image.getImageUrl().replace("http://192.168.1.71:8080/uploads/", "C:/demo/uploads/"));

                    if (file.exists()) {
                        boolean deleted = file.delete();
                        System.out.println("File deleted: " + deleted);
                    }

                    programImageRepository.delete(image);
                    return ResponseEntity.ok("Image deleted successfully.");
                }).orElse(ResponseEntity.notFound().build());
    }
}