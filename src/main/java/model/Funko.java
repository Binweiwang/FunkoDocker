package model;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class Funko {
    private UUID cod;
    private String nombre;
    private String modelo;
    private double precio;
    private LocalDate fecha_lanzamiento;
}
