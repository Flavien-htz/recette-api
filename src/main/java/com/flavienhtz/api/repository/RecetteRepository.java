package com.flavienhtz.api.repository;

import com.flavienhtz.api.model.Recette;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecetteRepository extends JpaRepository<Recette, Integer> {

    @Query("from Recette where nom = ?1")
    List<Recette> findByNom(String nom);


}
