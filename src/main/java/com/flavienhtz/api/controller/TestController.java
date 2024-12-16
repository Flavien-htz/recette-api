package com.flavienhtz.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flavienhtz.api.model.Recette;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class TestController {

    private static final String UPLOAD_DIR = "uploads/recettes/"; // Define upload directory


    @PostMapping("/upload")
    public ResponseEntity<String> testFileUpload(@RequestPart("file") MultipartFile file) {


        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty!");
        }

        try {
            // Définir un chemin pour enregistrer le fichier
            String fileName = file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);

            // Créer le répertoire si nécessaire
            Files.createDirectories(filePath.getParent());

            // Sauvegarder le fichier
            Files.write(filePath, file.getBytes());

            return ResponseEntity.ok("File uploaded successfully: " + filePath.toString());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to upload file!");
        }
    }

    @PostMapping(value = "/recette/test", consumes = "multipart/form-data")
    public ResponseEntity<String> debugCreate(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart("recette") String recetteJson) throws JsonProcessingException {

        System.out.println("File: " + (file != null ? file.getOriginalFilename() : "No file"));
        System.out.println("Recette JSON: " + recetteJson);
        // Instanciez ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        // Convertir en objet Recette
        Recette recette = objectMapper.readValue(recetteJson, Recette.class);

        // Afficher le résultat
        System.out.println(recette);

        return ResponseEntity.ok("Debugging complete");
    }
}
