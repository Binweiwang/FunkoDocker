package services.funko;

import model.Funko;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Interfaz que contiene los métodos de caché para Funkos
 */
public interface FunkoService {
    Flux<Funko> findAll();

    Mono<Funko> findById(Long id);

    Flux<Funko> findByNombre(String nombre);

    Flux<Funko> findByModel(String model);

    Mono<Funko> findByUuid(UUID uuid);

    Mono<Funko> save(Funko funko);

    Mono<Funko> update(Funko funko);

    Mono<Funko> deleteByUuid(UUID uuid);

    Mono<Funko> deleteById(Long id);

    Mono<Void> deleteAll();

    Flux<Funko> importar();

    Flux<Funko> findByYear(int myYear);
}
