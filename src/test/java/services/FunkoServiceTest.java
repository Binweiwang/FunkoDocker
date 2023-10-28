package services;

import model.Funko;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import repository.funko.FunkoRepository;
import services.funko.FunkoServiceImp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class FunkoServiceTest {

    @Mock
    FunkoRepository funkoRepository;

    @InjectMocks
    FunkoServiceImp service;


    @Test
    void findAll() {
        var funkos = List.of(
                getFunko(1L, 10.0),
                getFunko(2L, 20.0)
        );

        when(funkoRepository.findAll()).thenReturn(Flux.fromIterable(funkos));

        var funkosFound = service.findAll().collectList().block();

        assertAll("Obtener todos los funkos",
                () -> assertEquals(funkosFound.size(),2),
                () -> assertEquals(funkosFound.get(0).getNombre(), funkos.get(0).getNombre() ),
                () -> assertEquals(funkosFound.get(1).getNombre(), funkos.get(1).getNombre()),
                () -> assertEquals(funkosFound.get(0).getPrecio(), funkos.get(0).getPrecio()),
                () -> assertEquals(funkosFound.get(1).getPrecio(), funkos.get(1).getPrecio())
        );
    }

    @Test
    void findById(){
        var funko = getFunko(1L, 10.0);

        when(funkoRepository.findById(1L)).thenReturn(Mono.just(funko));

        var funkoFound = service.findById(1L).block();

        assertAll("Obtener un funko por id",
                () -> assertEquals(funkoFound.getNombre(), "Funko" ),
                () -> assertEquals(funkoFound.getPrecio(), 10.0)
        );
    }

    @Test
    void findByNombre(){
        var funkos = List.of(
                getFunko(1L, 10.0),
                getFunko(2L, 20.0)
        );

        when(funkoRepository.findByName("Funko")).thenReturn(Flux.fromIterable(funkos));

        var funkosFound = service.findByNombre("Funko").collectList().block();

        assertAll("Obtener todos los funkos por nombre",
                () -> assertEquals(funkosFound.size(),2),
                () -> assertEquals(funkosFound.get(0).getNombre(), "Funko" ),
                () -> assertEquals(funkosFound.get(1).getNombre(), "Funko"),
                () -> assertEquals(funkosFound.get(0).getPrecio(), 10.0),
                () -> assertEquals(funkosFound.get(1).getPrecio(), 20.0)
        );
    }

    @Test
    void findByModel(){
        var funkos = List.of(
                getFunko(1L, 10.0),
                getFunko(2L, 20.0)
        );

        when(funkoRepository.findByModel("MARVEL")).thenReturn(Flux.fromIterable(funkos));

        var funkosFound = service.findByModel("MARVEL").collectList().block();

        assertAll("Obtener todos los funkos por modelo",
                () -> assertEquals(funkosFound.size(),2),
                () -> assertEquals(funkosFound.get(0).getNombre(), "Funko" ),
                () -> assertEquals(funkosFound.get(1).getNombre(), "Funko"),
                () -> assertEquals(funkosFound.get(0).getPrecio(), 10.0),
                () -> assertEquals(funkosFound.get(1).getPrecio(), 20.0)
        );
    }

    @Test
    void findByUuid(){
        var funko = getFunko(1L, 10.0);

        when(funkoRepository.findByUuid(funko.getCod())).thenReturn(Mono.just(funko));

        var funkoFound = service.findByUuid(funko.getCod()).block();

        assertAll("Obtener un funko por uuid",
                () -> assertEquals(funkoFound.getNombre(), funko.getNombre() ),
                () -> assertEquals(funkoFound.getPrecio(), funko.getPrecio())
        );
    }


    @Test
    void saveFunko(){
        var funko = getFunko(1L, 10.0);

        when(funkoRepository.save(funko)).thenReturn(Mono.just(funko));
        when(funkoRepository.findByUuid(funko.getCod())).thenReturn(Mono.just(funko));

        var funkoSaved = service.save(funko).block();

        assertAll("Guardar un funko",
                () -> assertEquals(funkoSaved.getNombre(), funko.getNombre() ),
                () -> assertEquals(funkoSaved.getPrecio(), funko.getPrecio())
        );
    }

    @Test
    void updateFunko(){
        var funko = getFunko(1L, 10.0);

        when(funkoRepository.findById(funko.getId())).thenReturn(Mono.just(funko));
        var funkoFound = funkoRepository.findById(funko.getId()).block();
        funkoFound.setNombre("FunkoModificado");
        funkoFound.setPrecio(12.0);
        when(funkoRepository.update(funko)).thenReturn(Mono.just(funko));

        var funkoUpdated = service.update(funkoFound).block();

        assertAll("Actualizar un funko",
                () -> assertEquals(funkoUpdated.getNombre(), funkoFound.getNombre() ),
                () -> assertEquals(funkoUpdated.getPrecio(), funkoFound.getPrecio())
        );
    }

    @Test
    void deleteByUuid(){
        var funko = getFunko(1L, 10.0);

        when(funkoRepository.findByUuid(funko.getCod())).thenReturn(Mono.just(funko));
        when(funkoRepository.deleteByUuid(funko.getCod())).thenReturn(Mono.just(funko));

        var funkoDeleted = service.deleteByUuid(funko.getCod()).block();

        assertAll("Borrar un funko",
                () -> assertEquals(funkoDeleted.getNombre(),funko.getNombre() ),
                () -> assertEquals(funkoDeleted.getPrecio(), funko.getPrecio())
        );
    }


    @Test
    void deleteById(){
        var funko = getFunko(1L, 10.0);

        when(funkoRepository.save(funko)).thenReturn(Mono.just(funko));
        when(funkoRepository.findByUuid(funko.getCod())).thenReturn(Mono.just(funko));
        var funkoSaved = service.save(funko).block();
        when(funkoRepository.findById(funkoSaved.getId())).thenReturn(Mono.just(funkoSaved));
        when(funkoRepository.deleteById(funkoSaved.getId())).thenReturn(Mono.just(funkoSaved).hasElement());

        var funkoDeleted = service.deleteById(funkoSaved.getId()).block();

        assertAll("Borrar un funko",
                () -> assertEquals(funkoDeleted.getNombre(), funko.getNombre() ),
                () -> assertEquals(funkoDeleted.getPrecio(), funko.getPrecio())
        );
    }

    @Test
    void deleteAll(){
        var funko1 = getFunko(1L, 10.0);
        var funko2 = getFunko(2L, 20.0);

        when(funkoRepository.save(funko1)).thenReturn(Mono.just(funko1));
        when(funkoRepository.save(funko2)).thenReturn(Mono.just(funko2));

        funkoRepository.save(funko1).block();
        funkoRepository.save(funko2).block();

        when(funkoRepository.deleteAll()).thenReturn(Mono.empty());

        var funkoDeleted = service.deleteAll().block();

        assertEquals(funkoDeleted, null);

    }

    private Funko getFunko(Long id, double precio) {
    return Funko.builder()
            .id(id)
            .cod(UUID.randomUUID())
            .nombre("Funko")
            .modelo("MARVEL")
            .precio(precio)
            .fecha_lanzamiento(LocalDate.now())
            .updatedAt(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .build();
    }
}
