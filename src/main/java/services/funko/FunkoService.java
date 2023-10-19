package services;

import model.Funko;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FunkoService {
    Flux<Funko> findAll();
    Mono<Funko> findById(Long id);
    Flux<Funko> findByNombre(String nombre);
    Mono<Funko> findByUuid(UUID uuid);
    Mono<Funko> save(Funko funko);
    Mono<Funko> update(Funko funko);
    Mono<Funko> deleteByUuid(UUID uuid);
    Mono<Boolean> deleteById(Long id);
    Mono<Void> deleteAll();
    Flux<Funko> importar();
}
