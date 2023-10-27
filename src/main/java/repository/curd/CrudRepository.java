package repository.curd;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Interfaz que contiene los métodos CRUD
 *
 * @param <T>  Tipo de objeto
 * @param <ID> Tipo de id
 */
public interface CrudRepository<T, ID> {
    Flux<T> findAll();

    Mono<T> findById(ID id);

    Mono<T> save(T t);

    Mono<T> update(T t);

    Mono<Boolean> deleteById(ID id);

    Mono<Void> deleteAll();
}
