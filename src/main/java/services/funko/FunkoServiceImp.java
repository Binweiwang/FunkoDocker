package services.funko;

import model.Funko;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import repository.funko.FunkoRepository;
import server.exceptions.funkos.FunkoNotFoundException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Clase que implementa la interfaz de funkos service
 */
public class FunkoServiceImp implements FunkoService {
    // Atributos
    private static final int CACHE_SIZE = 10;
    private static FunkoServiceImp instance;
    private final Logger logger = LoggerFactory.getLogger(FunkoServiceImp.class);
    private final FunkosCache cache;
    private final FunkoRepository funkoRepository;

    /**
     * Constructor de la clase
     *
     * @param funkoRepository Repositorio de funkos
     */
    private FunkoServiceImp(FunkoRepository funkoRepository) {
        this.funkoRepository = funkoRepository;
        this.cache = new FunkosCacheImp(CACHE_SIZE);
    }

    /**
     * Singleton de la clase
     *
     * @param funkoRepository Repositorio de funkos
     * @return una instancia de la clase
     */
    public static FunkoServiceImp getInstance(FunkoRepository funkoRepository) {
        if (instance == null) {
            instance = new FunkoServiceImp(funkoRepository);
        }
        return instance;
    }

    /**
     * Método que devuelve todos los funkos
     *
     * @return Flux de funkos
     */
    @Override
    public Flux<Funko> findAll() {
        return funkoRepository.findAll();
    }

    /**
     * Método que devuelve un Mono funko por id
     *
     * @param id Id del funko
     * @return Mono de funko
     */
    @Override
    public Mono<Funko> findById(Long id) {
        return cache.get(id).switchIfEmpty(funkoRepository.findById(id)).flatMap(funko -> cache.put(funko.getId(), funko).then(Mono.just(funko))).switchIfEmpty(Mono.error(new FunkoNotFoundException("Funko con id " + id + " no encontrado")));
    }

    /**
     * Método que devuelve un Flux funko por nombre
     *
     * @param nombre Nombre del funko
     * @return Flux de funkos
     */
    @Override
    public Flux<Funko> findByNombre(String nombre) {
        return funkoRepository.findByName(nombre);
    }

    /**
     * Método que devuelve un Flux funko por modelo
     *
     * @param model Modelo del funko
     * @return Flux de funkos
     */
    @Override
    public Flux<Funko> findByModel(String model) {
        return funkoRepository.findByModel(model);
    }

    /**
     * Método que devuelve un Mono funko por uuid
     *
     * @param uuid Uuid del funko
     * @return Mono de funko
     */
    @Override
    public Mono<Funko> findByUuid(UUID uuid) {
        return funkoRepository.findByUuid(uuid).flatMap(funko -> cache.put(funko.getId(), funko).then(Mono.just(funko))).switchIfEmpty(Mono.error(new FunkoNotFoundException("Funko con uuid " + uuid + " no encontrado")));
    }

    /**
     * Método que guarda un funko
     *
     * @param funko Funko a guardar
     * @return Mono de funko
     */
    @Override
    public Mono<Funko> save(Funko funko) {
        return funkoRepository.save(funko).flatMap(saved -> funkoRepository.findByUuid(saved.getCod()));

    }

    /**
     * Método que actualiza un funko
     *
     * @param funko Funko a actualizar
     * @return Mono de funko
     */
    @Override
    public Mono<Funko> update(Funko funko) {
        return funkoRepository.findById(funko.getId()).switchIfEmpty(Mono.error(new FunkoNotFoundException("Funko con id " + funko.getId() + " no encontrado"))).flatMap(existing -> funkoRepository.update(funko).flatMap(updated -> cache.put(updated.getId(), updated).thenReturn(updated)));
    }

    /**
     * Método que elimina un funko por uuid
     *
     * @param uuid Uuid del funko
     * @return Mono de funko
     */
    @Override
    public Mono<Funko> deleteByUuid(UUID uuid) {
        return funkoRepository.findByUuid(uuid).switchIfEmpty(Mono.error(new FunkoNotFoundException("Funko con uuid " + uuid + " no encontrado"))).flatMap(funko -> cache.remove(funko.getId()).then(funkoRepository.deleteByUuid(uuid)).thenReturn(funko));
    }

    /**
     * Método que elimina un funko por id
     *
     * @param id Id del funko
     * @return Mono de funko
     */
    @Override
    public Mono<Funko> deleteById(Long id) {
        return funkoRepository.findById(id).switchIfEmpty(Mono.error(new FunkoNotFoundException("Funko con id " + id + " no encontrado"))).flatMap(funko -> cache.remove(funko.getId()).then(funkoRepository.deleteById(funko.getId())).thenReturn(funko));
    }

    /**
     * Método que elimina todos los funkos
     *
     * @return Mono Void
     */
    @Override
    public Mono<Void> deleteAll() {
        cache.clear();
        return funkoRepository.deleteAll().then(Mono.empty());
    }

    /**
     * Método que devuelve un Flux de funkos por año
     *
     * @param myYear Año del funko
     * @return Flux de funkos
     */
    @Override
    public Flux<Funko> findByYear(int myYear) {
        return funkoRepository.findByYear(myYear);
    }

    /**
     * Método que importa funkos desde un archivo
     *
     * @return Flux de funkos
     */
    @Override
    public Flux<Funko> importar() {
        logger.debug("Importado funkos desde un archivo");
        var fileCSV = Paths.get("").toAbsolutePath() + File.separator + "data" + File.separator + "funkos.csv";
        List<Funko> funkosToSave = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileCSV))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lines = line.split(",");
                Funko funko = Funko.builder().cod(UUID.fromString(lines[0].substring(1, 35))).nombre(lines[1]).modelo(lines[2]).precio(Double.parseDouble(lines[3])).fecha_lanzamiento(LocalDate.parse(lines[4])).build();
                funkosToSave.add(funko);
            }
        } catch (IOException e) {
            return Flux.error(e);
        }
        return Flux.fromIterable(funkosToSave).flatMap(funkoRepository::save);
    }
}