package repository.funko;

import database.DatabaseManager;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Row;
import model.Funko;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

public class FunkoRepositoryImp implements FunkoRepository {
    private static FunkoRepositoryImp instance;
    private final Logger logger = LoggerFactory.getLogger(FunkoRepositoryImp.class);
    private final ConnectionPool connectionFactory;
    private FunkoRepositoryImp(DatabaseManager databaseManager) {
        this.connectionFactory = databaseManager.getConnectionPool();
    }

    public static FunkoRepositoryImp getInstance(DatabaseManager db) {
        if (instance == null) {
            instance = new FunkoRepositoryImp(db);
        }
        return instance;
    }
    @Override
    public Flux<Funko> findAll() {
        logger.debug("Buscando todos los funkos");
        String sql = "SELECT * FROM FUNKOS";
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(sql).execute())
                        .flatMap(result -> result.map((row, rowMetadata) ->
                                getFunko(row)
                        )),
                Connection::close
        );
    }

    private static Funko getFunko(Row row) {
        return getFunko(Funko.builder()
                .id(row.get("id", Long.class))
                .cod(row.get("cod", UUID.class))
                .nombre(row.get("nombre", String.class))
                .modelo(row.get("modelo", String.class))
                .precio(row.get("precio", Double.class))
                .fecha_lanzamiento(row.get("fecha_lanzamiento", LocalDate.class)));
    }

    private static Funko getFunko(Funko.FunkoBuilder row) {
        return row
                .build();
    }

    @Override
    public Mono<Funko> findById(Long id) {
        logger.debug("Buscando funko por id: " + id);
        String sql = "SELECT * FROM FUNKOS WHERE id = ?";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .bind(0, id)
                        .execute()
                ).flatMap(result -> Mono.from(result.map((row, rowMetadata) ->
                    getFunko(row)
                ))),
                Connection::close
        );
    }

    @Override
    public Flux<Funko> findByName(String nombre) {
        logger.debug("Buscando todos los funkos por nombre" + nombre);
        String sql = "SELECT * FROM FUNKOS WHERE nombre LIKE ?";
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(sql).bind(0, nombre).execute())
                        .flatMap(result -> result.map((row, rowMetadata) ->
                            getFunko(row)
                        )),
                Connection::close
        );
    }

    @Override
    public Mono<Funko> findyByUuid(UUID uuid) {
        logger.debug("Buscando funko por uuid: " + uuid);
        String sql = "SELECT * FROM FUNKOS WHERE cod = ?";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .bind(0, uuid)
                        .execute()
                ).flatMap(result -> Mono.from(result.map((row, rowMetadata) ->
                    getFunko(row)
                ))),
                Connection::close
        );
    }

    @Override
    public Mono<Funko> deleteByUuid(UUID uuid) {
        logger.debug("Borrando funko por uuid: " + uuid);
        String sql = "DELETE FROM FUNKOS WHERE cod = ?";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .bind(0, uuid)
                        .execute()
                ).then(Mono.just(Funko.builder().cod(uuid).build())),
                Connection::close
        );
    }

    @Override
    public Flux<Funko> findByModel(String model) {
        logger.debug("Buscando todos los funkos por modelo" + model);
        String sql = "SELECT * FROM FUNKOS WHERE modelo LIKE ?";
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(sql)
                        .bind(0, model)
                        .execute()
                ).flatMap(result -> result.map((row, rowMetadata) ->
                    getFunko(row)
                )),
                Connection::close
        );
    }

    @Override
    public Flux<Funko> findByYear(int myYear) {
        logger.debug("Buscando todos los funkos por año" + myYear);
        String sql  = "SELECT * FROM FUNKOS WHERE YEAR(fecha_lanzamiento) = ?";
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(sql)
                        .bind(0, myYear)
                        .execute()
                ).flatMap(result -> result.map((row, rowMetadata) ->
                    getFunko(row)
                )),
                Connection::close
        );
    }


    @Override
    public Mono<Funko> save(Funko funko) {
        logger.debug("Guardando funko: " + funko);
        String sql = "INSERT INTO FUNKOS (cod, nombre, modelo, precio, fecha_lanzamiento) VALUES (?, ?, ?, ?, ?)";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .bind(0, funko.getCod())
                        .bind(1, funko.getNombre())
                        .bind(2, funko.getModelo())
                        .bind(3, funko.getPrecio())
                        .bind(4, funko.getFecha_lanzamiento())
                        .execute()
                        ).then(Mono.just(funko)),
                Connection::close
        );
    }

    @Override
    public Mono<Funko> update(Funko funko) {
        logger.debug("Actualizando funko: " + funko);
        String sql = "UPDATE FUNKOS SET nombre = ?, modelo = ?, precio = ?, FECHA_LANZAMIENTO = ? WHERE id = ?";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .bind(0, funko.getNombre())
                        .bind(1, funko.getModelo())
                        .bind(2, funko.getPrecio())
                        .bind(3, funko.getFecha_lanzamiento())
                        .bind(4, funko.getId())
                        .execute()
                ).then(Mono.just(funko)),
                Connection::close
        );
    }

    @Override
    public Mono<Boolean> deleteById(Long id) {
        logger.debug("Borrando funko por id: " + id);
        String sql = "DELETE FROM FUNKOS WHERE id = ?";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .bind(0, id)
                        .execute()
                ).flatMapMany(Result::getRowsUpdated)
                        .hasElements(),
                Connection::close
        );
    }


    @Override
    public Mono<Void> deleteAll() {
        logger.debug("Borrando todos los funkos");
        String sql = "DELETE FROM FUNKOS";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql).execute()
        ).then(),
                Connection::close
        );

    }
}
