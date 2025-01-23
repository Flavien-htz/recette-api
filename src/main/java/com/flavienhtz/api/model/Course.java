package com.flavienhtz.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Entity
@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Use IDENTITY strategy
    private Long id;

    private Long numero;
    private String nom;
    private String quantite;

    private Boolean checked = false;


}
