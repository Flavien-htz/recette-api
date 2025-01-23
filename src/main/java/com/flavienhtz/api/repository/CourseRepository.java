package com.flavienhtz.api.repository;

import com.flavienhtz.api.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Integer> {

    // Recupérer la dernière course ajoutée
    Optional<Course> findTopByOrderByIdDesc();

    // Dernière entrée où checked est false
    Optional<Course>  findTopByCheckedFalseOrderByNumeroDesc();

    // Dernière entrée où checked est true
    Optional<Course> findTopByCheckedTrueOrderByNumeroDesc();

}
