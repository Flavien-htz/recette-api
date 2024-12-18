package com.flavienhtz.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flavienhtz.api.model.Menu;
import com.flavienhtz.api.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*") // Allow all origins
public class MenuController {

    @Autowired
    private MenuRepository menuRepository;


    @GetMapping("/menu/{id}")
    public ResponseEntity<Menu> get(@PathVariable int id) {
        Menu menu = menuRepository.findById(id).orElse(null);

        if (menu == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(menu, HttpStatus.OK);
        }
    }

    @GetMapping("/menus")
    public ResponseEntity<List<Menu>> getAll() {
        List<Menu> menus = menuRepository.findAll();

        if (menus.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(menus, HttpStatus.OK);
        }
    }

    @PostMapping("/menu")
    public ResponseEntity<Map<String, String>> create(@RequestParam("menu") String menuBody) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        // Convertir en objet Recette
        Menu menu = objectMapper.readValue(menuBody, Menu.class);

// Créer un objet Calendar et y associer la date
        Calendar calendarStart = Calendar.getInstance();
        calendarStart.setTime(menu.getStartDate());
        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(menu.getEndDate());

        // Extraire l'heure
        int heureStart = calendarStart.get(Calendar.HOUR_OF_DAY);
        int heureEnd = calendarEnd.get(Calendar.HOUR_OF_DAY);

        if (heureStart -1 == 12 && heureEnd -1 == 20) {
            menu.setEndDate(calendarStart.getTime());

            Menu menu2 = objectMapper.readValue(menuBody, Menu.class);
            calendarEnd.add(Calendar.HOUR_OF_DAY, -1);
            menu2.setStartDate(calendarEnd.getTime());
            menuRepository.save(menu2);
        }


        menuRepository.save(menu);

        return new ResponseEntity<>(Map.of("message", "Menu créé"), HttpStatus.CREATED);

    }

    @PutMapping("/menu/{id}")
    public ResponseEntity<Map<String, String>> update(@PathVariable int id, @RequestParam("menu") String menuBody) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        // Convertir en objet Recette
        Menu menu = objectMapper.readValue(menuBody, Menu.class);

        menu.setId(id);

        menuRepository.save(menu);

        return new ResponseEntity<>(Map.of("message", "Menu modifié"), HttpStatus.OK);

    }

}
