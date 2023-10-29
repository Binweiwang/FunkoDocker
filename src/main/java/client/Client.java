package client;

import client.exceptions.ClientException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.Login;
import common.Request;
import common.Response;
import model.Funko;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.LocalDateAdapter;
import utils.LocalDateTimeAdapter;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static common.Request.Type.*;
import static server.Server.readConfigFile;

/**
 * Cliente para comunicar con el servidor
 */
public class Client {
    // Atributos
    private static final String HOST = "localhost";
    private static final int PORT = 3000;
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private final Gson gson;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String token;

    /**
     * Constructor
     */
    public Client() {
        this.gson = new Gson();
    }

    /**
     * Método main
     *
     * @param args Argumentos
     */
    public static void main(String[] args) {
        Client client = new Client();
        try {
            client.start();
        } catch (IOException e) {
            logger.debug("Error: " + e.getMessage());
        }
    }

    /**
     * Método para mostrar la respuesta del servidor
     *
     * @param response Respuesta del servidor
     */
    private static void funkoResponse(Response response) {
        System.out.println("🟢 Funko: " + response.content());
    }

    /**
     * Método para iniciar el cliente
     *
     * @throws IOException Excepción de entrada/salida
     */
    private void start() throws IOException {
        try {

            Funko funko = Funko.builder().id(1L).nombre("Funko").precio(12.5).modelo("DISNEY").fecha_lanzamiento(LocalDate.parse("2021-01-01")).build();

            openConnection();
            token = sendRequestLogin();
            findAllFunkos(token);
            findFunkoById(token, "2");
            findFunkoByModel(token, "ANIME");
            findFunkoByYear(token, "2021");
            saveFunko(token, Funko.builder().cod(UUID.randomUUID()).nombre("Funko").modelo("MARVEL").precio(12.5).fecha_lanzamiento(LocalDate.now()).build());
            updateFunko(token, funko);
            deleteFunko(token, "1");
            closeConnection();
            System.exit(0);
        } catch (ClientException ex) {
            logger.debug("Error en el cliente: " + ex.getMessage());
            System.err.println("🔴 Error: " + ex.getMessage());
            closeConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    /**
     * Método para borrar un funko
     *
     * @param token  Token del usuario
     * @param number Número del funko
     * @throws ClientException Excepción del cliente
     * @throws IOException     Excepción de entrada/salida
     */
    private void deleteFunko(String token, String number) throws ClientException, IOException {
        Request<String> request = new Request<>(DELETE_FUNKO, number, token, LocalDateTime.now().toString());
        logger.debug("Petición deleteFunko enviada: " + request);
        out.println(gson.toJson(request));
        Response response = gson.fromJson(in.readLine(), Response.class);
        logger.debug("Respuesta deleteFunko recibida: " + response);
        responseFunko(response);
    }

    /**
     * .
     * Método para mostrar la respuesta del servidor
     *
     * @param response Respuesta del servidor
     * @throws ClientException Excepción del cliente
     */
    private void responseFunko(Response response) throws ClientException {
        switch (response.status()) {
            case OK -> {
                System.out.println("🟢 Funko: " + response.content());
            }
            case ERROR -> {
                System.err.println("🔴 Error: Tipo de respuesta no esperado: " + response.content());
            }
            default -> throw new ClientException("Error: Tipo de respuesta no esperado: " + response.content());
        }
    }

    /**
     * Método para actualizar un funko
     *
     * @param token Token del usuario
     * @param funko Funko que se va a actualizar
     */
    private void updateFunko(String token, Funko funko) {
        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
        var funkoJson = gson.toJson(funko);
        Request request = new Request<>(UPDATE_FUNKO, funkoJson, token, LocalDateTime.now().toString());
        logger.debug("Petición updateFunko enviada: " + request);
        out.println(gson.toJson(request));
        try {
            Response response = gson.fromJson(in.readLine(), Response.class);
            logger.debug("Respuesta updateFunko recibida: " + response);
            responseFunko(response);
        } catch (IOException | ClientException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * @param token Token del usuario
     * @param funko Funko que se va a guardar
     */
    private void saveFunko(String token, Funko funko) {
        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
        var funkoJson = gson.toJson(funko);
        Request request = new Request<>(SAVE_FUNKO, funkoJson, token, LocalDateTime.now().toString());
        logger.debug("Petición saveFunko enviada: " + request);
        out.println(gson.toJson(request));
        try {
            Response response = gson.fromJson(in.readLine(), Response.class);
            logger.debug("Respuesta saveFunko recibida: " + response);
            responseFunko(response);
        } catch (IOException | ClientException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Método para buscar un funko por año
     *
     * @param token Token del usuario
     * @param year  Año del funko
     */
    private void findFunkoByYear(String token, String year) {
        Request<String> request = new Request<>(OBTAIN_FUNKO_YEAR, year, token, LocalDateTime.now().toString());
        logger.debug("Petición findFunkoByYear enviada: " + request);
        out.println(gson.toJson(request));
        try {
            Response response = gson.fromJson(in.readLine(), Response.class);
            logger.debug("Respuesta findFunkoByYear recibida: " + response);
            responseFunko(response);
        } catch (IOException | ClientException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Método para buscar un funko por modelo
     *
     * @param token Token del usuario
     * @param model Modelo del funko
     */
    private void findFunkoByModel(String token, String model) {
        Request<String> request = new Request<>(OBTAIN_FUNKO_MODEL, model, token, LocalDateTime.now().toString());
        logger.debug("Petición findFunkoByModel enviada: " + request);
        out.println(gson.toJson(request));
        try {
            Response response = gson.fromJson(in.readLine(), Response.class);
            logger.debug("Respuesta findFunkoByModel recibida: " + response);
            responseFunko(response);
        } catch (IOException | ClientException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Método para buscar un funko por id
     *
     * @param token Token del usuario
     * @param id    Id del funko
     */
    private void findFunkoById(String token, String id) {
        Request<String> request = new Request<>(OBTAIN_FUNKO_COD, id, token, LocalDateTime.now().toString());
        logger.debug("Petición findFunkoById enviada: " + request);
        out.println(gson.toJson(request));
        try {
            Response response = gson.fromJson(in.readLine(), Response.class);
            logger.debug("Respuesta findFunkoById recibida: " + response);
            responseFunko(response);
        } catch (IOException | ClientException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Método para buscar todos los funkos
     *
     * @param token Token del usuario
     */
    private void findAllFunkos(String token) {
        Request<String> request = new Request<>(FIND_ALL_FUNKOS, null, token, LocalDateTime.now().toString());
        logger.debug("Petición enviada: " + request);
        out.println(gson.toJson(request));
        try {
            Response response = gson.fromJson(in.readLine(), Response.class);
            logger.debug("Respuesta recibida: " + response);
            responseFunko(response);
        } catch (IOException | ClientException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Método para enviar la petición de login
     *
     * @return Devuelve el token del usuario
     */
    private String sendRequestLogin() {
        String myToken = null;
        Request<Login> request = new Request<>(LOGIN, new Login("pepe", "pepe1234"), null, LocalDateTime.now().toString());
        System.out.println("Petición enviada: " + request);
        out.println(gson.toJson(request));
        try {
            Response response = gson.fromJson(in.readLine(), Response.class);
            logger.debug("Respuesta recibida: " + response);
            switch (response.status()) {
                case TOKEN -> {
                    System.out.println("🟢 Mi token es: " + response.content());
                    myToken = response.content().toString();
                }
                case ERROR -> System.err.println("🔴 Error: Tipo de respuesta no esperado: " + response.content());
                default -> closeConnection();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return myToken;
    }

    /**
     * Método para cerrar la conexión
     *
     * @throws IOException Excepción de entrada/salida
     */
    private void closeConnection() throws IOException {
        logger.debug("Cerrando la conexión con el servidor: " + HOST + ":" + PORT);
        System.out.println("🔵 Cerrando Cliente");
        if (in != null) in.close();
        if (out != null) out.close();
        if (socket != null) socket.close();
    }

    /**
     * Método para abrir la conexión
     *
     * @throws IOException Excepción de entrada/salida
     */
    private void openConnection() throws IOException {
        Map<String, String> myConfig = readConfigFile();

        logger.debug("Cargando fichero de propiedades");
        // System.setProperty("javax.net.debug", "ssl, keymanager, handshake"); // Debug
        System.setProperty("javax.net.ssl.trustStore", myConfig.get("keyFile")); // llavero cliente
        System.setProperty("javax.net.ssl.trustStorePassword", myConfig.get("keyPassword")); // clave

        SSLSocketFactory clientFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket) clientFactory.createSocket(HOST, PORT);

        // Opcionalmente podemos forzar el tipo de protocolo -> Poner el mismo que el cliente
        logger.debug("Protocolos soportados: " + Arrays.toString(socket.getSupportedProtocols()));
        socket.setEnabledCipherSuites(new String[]{"TLS_AES_128_GCM_SHA256"});
        socket.setEnabledProtocols(new String[]{"TLSv1.3"});

        logger.debug("Abriendo conexión con el servidor: " + HOST + ":" + PORT);

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        logger.debug("Conexión establecida con el servidor: " + HOST + ":" + PORT);
    }
}
