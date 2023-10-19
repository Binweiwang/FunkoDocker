package services.funko;

import model.Funko;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import repository.funko.FunkoRepository;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FunkoServiceImp implements FunkoService {
    private static FunkoServiceImp instance;
    private final Logger logger = LoggerFactory.getLogger(FunkoServiceImp.class);
    private final FunkoRepository funkoRepository;
    private FunkoServiceImp(FunkoRepository funkoRepository) {
        this.funkoRepository = funkoRepository;
    }
    public static FunkoServiceImp getInstance(FunkoRepository funkoRepository) {
        if (instance == null) {
            instance = new FunkoServiceImp(funkoRepository);
        }
        return instance;
    }
    @Override
    public Flux<Funko> findAll() {
        return funkoRepository.findAll();
    }

    @Override
    public Mono<Funko> findById(Long id) {
        return funkoRepository.findById(id);
    }

    @Override
    public Flux<Funko> findByNombre(String nombre) {
        return funkoRepository.findByName(nombre);
    }

    @Override
    public Flux<Funko> findByModel(String model) {
        return funkoRepository.findByModel(model);
    }

    @Override
    public Mono<Funko> findByUuid(UUID uuid) {
        return funkoRepository.findyByUuid(uuid);
    }
    @Override
    public Mono<Funko> save(Funko funko) {
        return funkoRepository.save(funko);
    }
    @Override
    public Mono<Funko> update(Funko funko) {
        return funkoRepository.update(funko);
    }
    @Override
    public Mono<Funko> deleteByUuid(UUID uuid) {
        return funkoRepository.deleteByUuid(uuid);
    }

    @Override
    public Mono<Boolean> deleteById(Long id) {
        return funkoRepository.deleteById(id);
    }

    @Override
    public Mono<Void> deleteAll() {
        return funkoRepository.deleteAll();
    }
    @Override
    public Flux<Funko> importar() {
        logger.debug("Importado funkos desde un archivo");
        var fileCSV = Paths.get("").toAbsolutePath().toString() + File.separator + "data" + File.separator + "funkos.csv";
        List<Funko> funkosToSave = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileCSV))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lines = line.split(",");
                Funko funko = Funko.builder()
                        .cod(UUID.fromString(lines[0].substring(1, 35)))
                        .nombre(lines[1])
                        .modelo(lines[2])
                        .precio(Double.parseDouble(lines[3]))
                        .fecha_lanzamiento(LocalDate.parse(lines[4]))
                        .build();
                funkosToSave.add(funko);
            }
        } catch (IOException e) {
            return Flux.error(e);
        }
        return Flux.fromIterable(funkosToSave)
                .flatMap(funkoRepository::save);
    }

    @Override
    public Flux<Funko> findByYear(int myYear) {
        return funkoRepository.findByYear(myYear);
    }
}