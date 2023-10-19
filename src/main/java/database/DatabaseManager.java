package database;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.stream.Collectors;

public class DatabaseManager implements AutoCloseable {
    private static DatabaseManager instance;
    private final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private final ConnectionFactory connectionFactory;
    private final ConnectionPool pool;
    private String databaseUser = "binwei";
    private String databasePass = "binwei";
    private String databaseUrl = "r2dbc:h2:file:///./funkos";
    private boolean databaseInitTables = true;

    private DatabaseManager() {
        connectionFactory = ConnectionFactories.get(databaseUrl);
        ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration
                .builder(connectionFactory)
                .maxIdleTime(Duration.ofMillis(1000))
                .maxSize(20)
                .build();

        pool = new ConnectionPool(configuration);
        if (databaseInitTables) {
            initTables();
        }
    }
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public synchronized void initTables() {
        logger.debug("Borrando tablas de la base de datos");
        excuteScript("removeTables.sql").block();
        logger.debug("Creando tablas de la base de datos");
        excuteScript("createTables.sql").block();
    }

    public Mono<Void> excuteScript(String scriptSqlFile) {
        logger.debug("Ejecutando script de la base de datos: " + scriptSqlFile);
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> {
                    logger.debug("Creando conexión con la base de datos");
                    String scriptContent = null;
                    try {
                        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(scriptSqlFile)) {
                            if (inputStream == null) {
                                return Mono.error(new IOException("No se ha encontrado el fichero de script de inicialización de la base de datos"));
                            } else {
                                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                                    scriptContent = reader.lines().collect(Collectors.joining("\n"));
                                }
                            }
                        }
                        Statement statement = connection.createStatement(scriptContent);
                        return Mono.from(statement.execute());
                    } catch (IOException e) {
                        return Mono.error(e);
                    }
                },
                Connection::close
        ).then();
    }
    public ConnectionPool getConnectionPool() {
        return this.pool;
    }

    @Override
    public void close() throws Exception {
        logger.debug("Cerrando conexión con la base de datos");
        pool.close();
    }
}

