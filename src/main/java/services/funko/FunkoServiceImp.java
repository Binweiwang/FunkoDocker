package services.funko;

import model.Funko;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import repository.funko.FunkoRepository;
import server.exceptions.funkos.FunkoNotFoundException;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FunkoServiceImp implements FunkoService {
    private static final int CACHE_SIZE = 10;
    private static FunkoServiceImp instance;
    private final Logger logger = LoggerFactory.getLogger(FunkoServiceImp.class);
    private final FunkosCache cache;
    private final FunkoRepository funkoRepository;

    private FunkoServiceImp(FunkoRepository funkoRepository) {
        this.funkoRepository = funkoRepository;
        this.cache = new FunkosCacheImp(CACHE_SIZE);
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
        return cache.get(id)
                .switchIfEmpty(funkoRepository.findById(id))
                .flatMap(funko -> cache.put(funko.getId(), funko)
                        .then(Mono.just(funko)))
                .switchIfEmpty(Mono.error(new FunkoNotFoundException("Funko con id " + id + " no encontrado")));
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
        return funkoRepository.findyByUuid(uuid)
                .flatMap(funko -> cache.put(funko.getId(),funko)
                        .then(Mono.just(funko)))
                .switchIfEmpty(Mono.error(new FunkoNotFoundException("Funko con uuid " + uuid + " no encontrado")));
    }
    @Override
    public Mono<Funko> save(Funko funko) {
        return funkoRepository.save(funko)
                .flatMap(saved -> funkoRepository.findyByUuid(saved.getCod()));
    }
    @Override
    public Mono<Funko> update(Funko funko) {
        return funkoRepository.findById(funko.getId())
                .switchIfEmpty(Mono.error(new FunkoNotFoundException("Funko con id " + funko.getId() + " no encontrado")))
                .flatMap(existing -> funkoRepository.update(funko)
                        .flatMap(updated -> cache.put(updated.getId(),updated)
                                .thenReturn(updated)));
    }
    @Override
    public Mono<Funko> deleteByUuid(UUID uuid) {
        return funkoRepository.deleteByUuid(uuid);
    }

    @Override
    public Mono<Funko> deleteById(Long id) {
        return funkoRepository.findById(id)
                .switchIfEmpty(Mono.error(new FunkoNotFoundException("Funko con id " + id + " no encontrado")))
                .flatMap(funko -> cache.remove(funko.getId())
                        .then(funkoRepository.deleteById(funko.getId()))
                        .thenReturn(funko));
    }

    @Override
    public Mono<Void> deleteAll() {
        cache.clear();
        return funkoRepository.deleteAll()
                .then(Mono.empty());
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