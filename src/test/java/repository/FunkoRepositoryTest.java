package repository;

import database.DatabaseManager;
import model.Funko;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.funko.FunkoRepository;
import repository.funko.FunkoRepositoryImp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class FunkoRepositoryTest {
    private FunkoRepository funkoRepository;

    @BeforeEach
    void setUp() {
        funkoRepository = FunkoRepositoryImp.getInstance(DatabaseManager.getInstance());
        DatabaseManager.getInstance().initTables();
    }

    @Test
    void findAll() {
        var funko1 = getFunko(1L, 12.5);
        var funko2 = getFunko(2L, 15.5);

        funkoRepository.save(funko1).block();
        funkoRepository.save(funko2).block();

        List<Funko> funkos = funkoRepository.findAll().collectList().block();
        assertEquals(2, funkos.size());
    }

    @Test
    void findById() {
        var funko1 = getFunko(1L, 12.5);
        var funko2 = getFunko(2L, 15.5);

        funkoRepository.save(funko1).block();
        funkoRepository.save(funko2).block();

        var funkoFind1 = funkoRepository.findById(1L).block();
        var funkoFind2 = funkoRepository.findById(2L).block();

        assertAll("Obtener funko por id",
                () -> assertEquals(funko1.getNombre(), funkoFind1.getNombre()),
                () -> assertEquals(funko1.getModelo(), funkoFind1.getModelo()),
                () -> assertEquals(funko2.getPrecio(), funkoFind2.getPrecio()),
                () -> assertEquals(funko2.getFecha_lanzamiento(), funkoFind2.getFecha_lanzamiento())
        );
    }

    @Test
    void findByName() {
        var funko1 = getFunko(1L, 12.5);
        var funko2 = getFunko(2L, 15.5);

        funkoRepository.save(funko1).block();
        funkoRepository.save(funko2).block();

        List<Funko> funkosFound = funkoRepository.findByName("Funko").collectList().block();


        assertAll("Obtener funko por nombre",
                () -> assertEquals(2, funkosFound.size()),
                () -> assertEquals(funko1.getNombre(), funkosFound.get(0).getNombre()),
                () -> assertEquals(funko2.getNombre(), funkosFound.get(1).getNombre())
        );
    }

    @Test
    void findByUuid() {
        var funko1 = getFunko(1L, 12.5);
        var funko2 = getFunko(2L, 15.5);

        funkoRepository.save(funko1).block();
        funkoRepository.save(funko2).block();

        var funkoFound1 = funkoRepository.findByUuid(funko1.getCod()).block();
        var funkoFound2 = funkoRepository.findByUuid(funko2.getCod()).block();

        assertAll("Obtener funko por uuid",
                () -> assertEquals(funko1.getNombre(), funkoFound1.getNombre()),
                () -> assertEquals(funko1.getModelo(), funkoFound1.getModelo()),
                () -> assertEquals(funko2.getPrecio(), funkoFound2.getPrecio()),
                () -> assertEquals(funko2.getFecha_lanzamiento(), funkoFound2.getFecha_lanzamiento())
        );
    }

    @Test
    void deleteByUuid(){
        var funko1 = getFunko(1L, 12.5);
        var funko2 = getFunko(2L, 15.5);

        Funko funkoSaved = funkoRepository.save(funko1).block();
        funkoRepository.save(funko2).block();

        funkoRepository.deleteByUuid(funkoSaved.getCod()).block();

        assertAll("Borrar funko por uuid",
                () -> assertEquals(1, funkoRepository.findAll().collectList().block().size()),
                () -> assertFalse(funkoRepository.findByUuid(funkoSaved.getCod()).blockOptional().isPresent())
        );
    }

    @Test
    void findByModel(){
        var funko1 = getFunko(1L, 12.5);
        var funko2 = getFunko(2L, 15.5);

        funkoRepository.save(funko1).block();
        funkoRepository.save(funko2).block();

        List<Funko> funkosFound = funkoRepository.findByModel("MARVEL").collectList().block();

        assertAll("Obtener funko por modelo",
                () -> assertEquals(2, funkosFound.size()),
                () -> assertEquals(funko1.getModelo(), funkosFound.get(0).getModelo()),
                () -> assertEquals(funko2.getModelo(), funkosFound.get(1).getModelo())
        );
    }

    @Test
    void findByYear(){
        var funko1 = getFunko(1L, 12.5);
        var funko2 = getFunko(2L, 15.5);

        funkoRepository.save(funko1).block();
        funkoRepository.save(funko2).block();

        List<Funko> funkosFound = funkoRepository.findByYear(2023).collectList().block();

        assertAll("Obtener funko por año",
                () -> assertEquals(2, funkosFound.size()),
                () -> assertEquals(funko1.getFecha_lanzamiento(), funkosFound.get(0).getFecha_lanzamiento()),
                () -> assertEquals(funko2.getFecha_lanzamiento(), funkosFound.get(1).getFecha_lanzamiento())
        );
    }

    @Test
    void saveFunko(){
        var funko1 = getFunko(1L, 12.5);

        funkoRepository.save(funko1).block();

        Funko funkoFound= funkoRepository.findById(funko1.getId()).block();

        assertAll("Guardar un funko",
                () -> assertEquals(funko1.getNombre(), funkoFound.getNombre()),
                () -> assertEquals(funko1.getModelo(), funkoFound.getModelo()),
                () -> assertEquals(funko1.getPrecio(), funkoFound.getPrecio()),
                () -> assertEquals(funko1.getFecha_lanzamiento(), funkoFound.getFecha_lanzamiento())
        );
    }

    @Test
    void updateFunko(){
        var funko1 = getFunko(1L, 12.5);

        funkoRepository.save(funko1).block();

        Funko funkoFound= funkoRepository.findById(funko1.getId()).block();

        funkoFound.setNombre("Funko2");
        funkoFound.setModelo("MARVEL");
        funkoFound.setPrecio(15.5);
        funkoFound.setFecha_lanzamiento(LocalDate.now());

        funkoRepository.update(funkoFound).block();

        Funko funkoUpdated = funkoRepository.findById(funkoFound.getId()).block();

        assertAll("Actualizar un funko",
                () -> assertEquals(funkoFound.getNombre(), funkoUpdated.getNombre()),
                () -> assertEquals(funkoFound.getModelo(), funkoUpdated.getModelo()),
                () -> assertEquals(funkoFound.getPrecio(), funkoUpdated.getPrecio()),
                () -> assertEquals(funkoFound.getFecha_lanzamiento(), funkoUpdated.getFecha_lanzamiento())
        );
    }

    @Test
    void deleteById() {
        var funko1 = getFunko(1L, 12.5);
        var funko2 = getFunko(2L, 15.5);

        Funko funkoSaved = funkoRepository.save(funko1).block();
        funkoRepository.save(funko2).block();

        funkoRepository.deleteById(funkoSaved.getId()).block();

        assertAll("Borrar funko por id",
                () -> assertEquals(1, funkoRepository.findAll().collectList().block().size()),
                () -> assertFalse(funkoRepository.findById(funkoSaved.getId()).blockOptional().isPresent())
        );
    }


    @Test
    void deleteAll(){
        var funko1 = getFunko(1L, 12.5);
        var funko2 = getFunko(2L, 15.5);

        funkoRepository.save(funko1).block();
        funkoRepository.save(funko2).block();

        funkoRepository.deleteAll().block();

        assertEquals(0, funkoRepository.findAll().collectList().block().size());
    }

    @Test



    private Funko getFunko(Long id, double precio) {
        return Funko.builder()
                .id(id)
                .cod(UUID.randomUUID())
                .nombre("Funko")
                .modelo("MARVEL")
                .precio(precio)
                .fecha_lanzamiento(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

    }
}
