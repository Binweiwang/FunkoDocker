package repository.funko;

import model.Funko;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import repository.curd.CrudRepository;

import java.util.UUID;

public interface FunkoRepository extends CrudRepository<Funko, Long> {

    Flux<Funko> findByName(String nombre);

    Mono<Funko> findyByUuid(UUID uuid);

    Mono<Funko> deleteByUuid(UUID uuid);
    Flux<Funko> findByModel(String model);
    Flux<Funko> findByYear(int myYear);
}
