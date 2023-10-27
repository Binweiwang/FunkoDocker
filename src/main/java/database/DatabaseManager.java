package database;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Clase que gestiona la conexión con la base de datos
 */
public class DatabaseManager implements AutoCloseable {
    // Atributos
    private static DatabaseManager instance;
    private final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private final ConnectionFactory connectionFactory;
    private final ConnectionPool pool;
    private String databaseUser;
    private String databasePass;
    private String databaseUrl;
    private boolean databaseInitTables;

    /**
     * Constructor de la clase
     */
    private DatabaseManager() {
        loadProperties();

        connectionFactory = ConnectionFactories.get(databaseUrl);
        ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder(connectionFactory).maxIdleTime(Duration.ofMillis(1000)).maxSize(20).build();

        pool = new ConnectionPool(configuration);
        if (databaseInitTables) {
            initTables();
        }
    }

    /**
     * Singleton de la clase
     *
     * @return una instancia de la clase
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void loadProperties() {
        logger.debug("Cargando fichero de configuración de la base de datos");
        try {
            var pathFile = Paths.get("").toAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "database.properties";
            var props = new Properties();
            props.load(new FileReader(pathFile));

            databaseUser = props.getProperty("database.user", "sa");
            databasePass = props.getProperty("database.password", "");
            databaseUrl = props.getProperty("database.url", "jdbc:h2:./funkos");
            databaseInitTables = Boolean.parseBoolean(props.getProperty("database.initTables", "false"));
            logger.debug("Configurado las properties correctamente");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Método que inicializa las tablas de la base de datos
     */
    public synchronized void initTables() {
        logger.debug("Borrando tablas de la base de datos");
        excuteScript("removeTables.sql").block();
        logger.debug("Creando tablas de la base de datos");
        excuteScript("createTables.sql").block();
    }

    /**
     * Método que ejecuta un script de la base de datos
     *
     * @param scriptSqlFile nombre del fichero de script
     * @return un Mono de tipo Void
     */
    public Mono<Void> excuteScript(String scriptSqlFile) {
        logger.debug("Ejecutando script de la base de datos: " + scriptSqlFile);
        return Mono.usingWhen(connectionFactory.create(), connection -> {
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
        }, Connection::close).then();
    }

    /**
     * Método que devuelve la conexión con la base de datos
     *
     * @return un ConnectionPool
     */
    public ConnectionPool getConnectionPool() {
        return this.pool;
    }

    /**
     * Método que cierra la conexión con la base de datos
     *
     * @throws Exception excepción en caso de que no se pueda cerrar la conexión
     */
    @Override
    public void close() throws Exception {
        logger.debug("Cerrando conexión con la base de datos");
        pool.close();
    }
}

