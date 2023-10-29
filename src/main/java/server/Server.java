package server;

import database.DatabaseManager;
import model.Funko;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import repository.funko.FunkoRepositoryImp;
import services.funko.FunkoService;
import services.funko.FunkoServiceImp;
import utils.PropertiesReader;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Clase que representa el servidor
 */
public class Server {
    // Atributos
    public static final String TOKEN_SECRET = "MiNombreEsBinwei";
    public static final long TOKEN_EXPIRATION = 10000;
    private static final AtomicLong clientNumber = new AtomicLong(0);
    private static final FunkoService funkoService = FunkoServiceImp.getInstance(FunkoRepositoryImp.getInstance(DatabaseManager.getInstance()));
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final int PUERTO = 3000;

    /**
     * Método main
     *
     * @param args argumentos
     * @throws Exception excepción en caso de error
     */
    public static void main(String[] args) throws Exception {
        try  {
            // Cargamos las propiedades
            var myConfig = readConfigFile();
            // Nos anunciamos como socket

            logger.debug("Configurando TSL");
            // System.setProperty("javax.net.debug", "ssl, keymanager, handshake"); // Depuramos
            System.setProperty("javax.net.ssl.keyStore", myConfig.get("keyFile")); // Llavero
            System.setProperty("javax.net.ssl.keyStorePassword", myConfig.get("keyPassword")); // Clave de acceso


            // Nos anunciamos como servidor de tipo SSL
            SSLServerSocketFactory serverFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket serverSocket = (SSLServerSocket) serverFactory.createServerSocket(PUERTO);

            // Opcionalmente podemos forzar el tipo de protocolo -> Poner el mismo que el cliente
            logger.debug("Protocolos soportados: " + Arrays.toString(serverSocket.getSupportedProtocols()));
            serverSocket.setEnabledCipherSuites(new String[]{"TLS_AES_128_GCM_SHA256"});
            serverSocket.setEnabledProtocols(new String[]{"TLSv1.3"});

            System.out.println("🚀 Servidor escuchando en el puerto 3000");
            var funkoService = FunkoServiceImp.getInstance(FunkoRepositoryImp.getInstance(DatabaseManager.getInstance()));
            Flux<Funko> importar = funkoService.importar();
            importar.subscribe(System.out::println);
            while (true) {
                new ClientHandler(serverSocket.accept(), clientNumber.incrementAndGet(), funkoService).start();
                if (clientNumber.get() == 10) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        } finally {
            DatabaseManager.getInstance().close();
        }
    }
    public static Map<String, String>   readConfigFile() {
        try {
            logger.debug("Leyendo el fichero de propiedades");

            PropertiesReader properties = new PropertiesReader("server.properties");

            String keyFile = properties.getProperty("keyFile");
            String keyPassword = properties.getProperty("keyPassword");
            String tokenSecret = properties.getProperty("tokenSecret");
            String tokenExpiration = properties.getProperty("tokenExpiration");

            // Comprobamos que no estén vacías
            if (keyFile.isEmpty() || keyPassword.isEmpty()) {
                throw new IllegalStateException("Hay errores al procesar el fichero de propiedades o una de ellas está vacía");
            }

            // Comprobamos el fichero de la clave
            if (!Files.exists(Path.of(keyFile))) {
                throw new FileNotFoundException("No se encuentra el fichero de la clave");
            }

            Map<String, String> configMap = new HashMap<>();
            configMap.put("keyFile", keyFile);
            configMap.put("keyPassword", keyPassword);
            configMap.put("tokenSecret", tokenSecret);
            configMap.put("tokenExpiration", tokenExpiration);

            return configMap;
        } catch (FileNotFoundException e) {
            logger.error("Error en clave: " + e.getLocalizedMessage());
            System.exit(1);
            return null; // Este retorno nunca se ejecutará debido a System.exit(1)
        } catch (IOException e) {
            logger.error("Error al leer el fichero de propiedades: " + e.getLocalizedMessage());
            return null;
        }
    }
}

