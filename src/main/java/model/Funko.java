package model;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Clase Funko que representa un Funko
 */
@Data
@Builder
public class Funko {
    private Long id;
    private UUID cod;
    private String nombre;
    private String modelo;
    private double precio;
    private LocalDate fecha_lanzamiento;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
