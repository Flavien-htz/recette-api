package com.flavienhtz.api.service;

import net.coobird.thumbnailator.Thumbnails;


import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

@Service
public class ImageService {

    @Value("${upload.dir}")
    private String uploadDir;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("jpg", "jpeg", "png", "webp");

    public ResponseEntity<Map<String, String>> uploadImage(MultipartFile file) throws IOException {
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = StringUtils.getFilenameExtension(originalFilename);

        // Validate file type
        assert extension != null;
        if (!ALLOWED_IMAGE_TYPES.contains(extension.toLowerCase())) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(Map.of("message", "Invalid file type"));
        }

        // Generate a unique filename
        String fileName = UUID.randomUUID().toString() + "." + extension;
        Path path = Paths.get(uploadDir).resolve(fileName);

        // Check if the directory exists, create it if not
        Files.createDirectories(path.getParent());

        // Resize the image using Thumbnailator
        Thumbnails.of(file.getInputStream())
                .size(400, 400)
                .keepAspectRatio(false)
                .crop(Positions.CENTER)
                .outputQuality(0.8f) // Set output quality to 80%
                .toFile(path.toFile());

        return ResponseEntity.ok(Map.of("filename", fileName));
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path path = Paths.get(uploadDir).resolve(fileName);
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("Fail to load content!");
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Error: " + ex.getMessage());
        }
    }

    public void deleteImage(String filename) {
        try {
            Path fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = fileStorageLocation.resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'image: " + filename, e);
        }
    }
}
