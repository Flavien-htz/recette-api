package com.flavienhtz.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flavienhtz.api.model.Course;
import com.flavienhtz.api.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*") // Allow all origins
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getAll() {
        List<Course> courses = courseRepository.findAll();

        if (courses.isEmpty()) {
            return new ResponseEntity<>(courses, HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(courses, HttpStatus.OK);
        }

    }

    @PostMapping("/course")
    public ResponseEntity<Map<String, String>> create(@RequestParam("course") String courseBody) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        Course course = objectMapper.readValue(courseBody, Course.class);

        Course lastCourse = courseRepository.findTopByCheckedFalseOrderByNumeroDesc().orElse(null);
        if (lastCourse != null) {
            course.setNumero(lastCourse.getNumero() + 1);
        } else {
            course.setNumero(1L);
        }
        courseRepository.save(course);

        return new ResponseEntity<>(Map.of("message", "Course created"), HttpStatus.CREATED);
    }


    @PutMapping("/course/{id}")
    public ResponseEntity<Map<String, String>> update(@PathVariable int id, @RequestParam("course") String courseBody) throws JsonProcessingException {
        Course course = courseRepository.findById(id).orElse(null);

        if (course == null) {
            return new ResponseEntity<>(Map.of("message", "Course not found"), HttpStatus.NOT_FOUND);
        }

        ObjectMapper objectMapper = new ObjectMapper();

        Course newCourse = objectMapper.readValue(courseBody, Course.class);

        Course lastCourse;
        if (newCourse.getChecked()) {
            lastCourse = courseRepository.findTopByCheckedTrueOrderByNumeroDesc().orElse(null);
        } else {
            lastCourse = courseRepository.findTopByCheckedFalseOrderByNumeroDesc().orElse(null);
        }

        if (lastCourse == null) {
            newCourse.setNumero(1L);
        } else {
            newCourse.setNumero(lastCourse.getNumero() + 1);
        }


        courseRepository.save(newCourse);

        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PutMapping("/course/alterNumeroMenu")
    public ResponseEntity<String> alterNumeroMenu(@RequestParam("course1") String courseBody1, @RequestParam("course2") String courseBody2) throws JsonProcessingException {


        Course course1 = courseRepository.findById(Math.toIntExact(Long.parseLong(courseBody1))).orElse(null);
        Course course2 = courseRepository.findById(Math.toIntExact(Long.parseLong(courseBody2))).orElse(null);
        if (course1 == null || course2 == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if(course1.getChecked() != course2.getChecked()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Long temp = course1.getNumero();
        course1.setNumero(course2.getNumero());
        course2.setNumero(temp);

        courseRepository.save(course1);
        courseRepository.save(course2);

        return new ResponseEntity<>(HttpStatus.OK);

    }
}
