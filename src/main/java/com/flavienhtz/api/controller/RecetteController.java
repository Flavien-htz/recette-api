package com.flavienhtz.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flavienhtz.api.repository.RecetteRepository;
import com.flavienhtz.api.model.Recette;
import com.flavienhtz.api.service.ImageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@RestController
@CrossOrigin(origins = "*") // Allow all origins
public class RecetteController {

    @Autowired
    private RecetteRepository recetteRepository;
    @Autowired
    private ImageService imageService;


    @GetMapping("/recette/{id}")
    public ResponseEntity<Recette> get(@PathVariable int id) {
        Recette recette = recetteRepository.findById(id).orElse(null);

        if (recette == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(recette, HttpStatus.OK);
        }

    }

    @GetMapping("/{id}/image")
    public ResponseEntity<org.springframework.core.io.Resource> getImage(@PathVariable int id) {
        Recette recette = recetteRepository.findById(id).orElse(null);

        if (recette == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return ResponseEntity.ok()
                    .body(imageService.loadFileAsResource(recette.getImageFilename()));
        }
    }

    @GetMapping("/recettes")
    public ResponseEntity<List<Recette>> getAll() {
        List<Recette> recettes = recetteRepository.findAll();
        if (recettes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(recettes, HttpStatus.OK);
        }
    }

    @PostMapping("/recette")
    public ResponseEntity<Map<String, String>> create(
            @RequestPart("file") MultipartFile file,
            @RequestParam("recette") String recetteBody) {
        try {

            if (file.getSize() > 2000 * 1024) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of("message", "Le fichier est trop volumineux"));
            }


            ObjectMapper objectMapper = new ObjectMapper();

            // Convertir en objet Recette
            Recette recette = objectMapper.readValue(recetteBody, Recette.class);

            // Vérifiez si une recette avec ce nom existe déjà
            List<Recette> recetteFind = recetteRepository.findByNom(recette.getNom());
            if (!recetteFind.isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "La recette existe déjà"));
            }

            // Gérer le téléchargement de l'image
            ResponseEntity<Map<String, String>> response = imageService.uploadImage(file);
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, String> responseMap = response.getBody();
                assert responseMap != null;
                String filename = responseMap.get("filename");
                recette.setImageFilename(filename);
                recetteRepository.save(recette);
                return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Recette créée"));
            } else {
                return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to create recette"));
        }
    }

    @DeleteMapping("/recette/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        return recetteRepository.findById(id)
                .map(recette -> {
                    if (recette.getImageFilename() != null) {
                        imageService.deleteImage(recette.getImageFilename());
                    }
                    recetteRepository.delete(recette);
                    return new ResponseEntity<Void>(HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    @PutMapping("/recette")
    public ResponseEntity<Map<String, Object>> update(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "recette", required = false) String recetteBody) {
        try {
            if (file != null) {
                if (file.getSize() > 2000 * 1024) {
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(Map.of("message", "Le fichier est trop volumineux"));
                }
            }

            ObjectMapper objectMapper = new ObjectMapper();

            // Convertir en objet Recette
            Recette recetteObj = objectMapper.readValue(recetteBody, Recette.class);

            Recette recette = recetteRepository.findById(recetteObj.getId()).orElse(null);
            if (recette == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

                if (recette.equals(recetteObj)) {
                    return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
                }

            // Mise à jour des champs textuels
            if (recetteObj.getNom() != null) recette.setNom(recetteObj.getNom());
            if (recetteObj.getDescription() != null) recette.setDescription(recetteObj.getDescription());
            if (recetteObj.getIngredients() != null) recette.setIngredients(recetteObj.getIngredients());

            // Gestion de l'image
            if (file != null) {
                ResponseEntity<Map<String, String>> response = imageService.uploadImage(file);
                if (response.getStatusCode() == HttpStatus.OK) {
                    // Supprimer l'ancienne image si elle existe
                    String oldImageFilename = recette.getImageFilename();
                    if (oldImageFilename != null) {
                        imageService.deleteImage(oldImageFilename);
                    }

                    // Mettre à jour avec la nouvelle image
                    Map<String, String> responseMap = response.getBody();
                    assert responseMap != null;
                    String filename = responseMap.get("filename");
                    recette.setImageFilename(filename);
                } else {
                    return ResponseEntity.status(response.getStatusCode())
                        .body(Map.of("message", "Une erreur est survenue à cause de l'image", "recette", recette));
                }
            }

            recetteRepository.save(recette);
            return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Recette modifiée", "recette", recette));

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}