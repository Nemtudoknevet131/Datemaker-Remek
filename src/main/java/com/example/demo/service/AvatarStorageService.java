package com.example.demo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
public class AvatarStorageService {
    @Value("${app.avatar.dir:avatars}")
    private String avatarDir;

    @Value("${app.avatar.url-prefix:/avatars}")
    private String urlPrefix;

    public String save(MultipartFile file) throws IOException {
        Files.createDirectories(Path.of(avatarDir));
        String ext = getExt(file.getOriginalFilename());
        String name = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);
        Path out = Path.of(avatarDir, name);
        Files.copy(file.getInputStream(), out, StandardCopyOption.REPLACE_EXISTING);

        String base = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return base + urlPrefix + "/" + name;
    }

    public String getExt(String fn) {
        if (fn == null)
            return "";
        int i = fn.lastIndexOf('.');
        return i >= 0 ? fn.substring(i + 1) : "";
    }
}