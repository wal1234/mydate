package com.example.scheduleservice.controller;

import com.example.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class ImageUploadController {

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    @Value("${app.upload.max-size:10485760}")
    private long maxSize;

    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("Upload image, filename={}, size={}", file.getOriginalFilename(), file.getSize());
        
        if (file.isEmpty()) {
            return Result.fail(400, "File is empty");
        }
        
        if (file.getSize() > maxSize) {
            return Result.fail(400, "File size exceeds limit");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.fail(400, "Not an image file");
        }
        
        try {
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            String url = "/uploads/" + filename;
            log.info("Image uploaded successfully, url={}", url);
            return Result.ok(url);
        } catch (IOException e) {
            log.error("Failed to upload image", e);
            return Result.fail(500, "Failed to upload image");
        }
    }

    @PostMapping("/images")
    public Result<List<String>> uploadImages(@RequestParam("files") MultipartFile[] files) {
        log.info("Upload multiple images, count={}", files.length);
        
        List<String> urls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String contentType = file.getContentType();
                if (contentType != null && contentType.startsWith("image/")) {
                    try {
                        Path uploadDir = Paths.get(uploadPath);
                        if (!Files.exists(uploadDir)) {
                            Files.createDirectories(uploadDir);
                        }
                        
                        String originalFilename = file.getOriginalFilename();
                        String extension = "";
                        if (originalFilename != null && originalFilename.contains(".")) {
                            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                        }
                        
                        String filename = UUID.randomUUID().toString() + extension;
                        Path filePath = uploadDir.resolve(filename);
                        Files.copy(file.getInputStream(), filePath);
                        
                        urls.add("/uploads/" + filename);
                    } catch (IOException e) {
                        log.error("Failed to upload image: {}", file.getOriginalFilename(), e);
                    }
                }
            }
        }
        
        log.info("Multiple images uploaded, count={}", urls.size());
        return Result.ok(urls);
    }
}
