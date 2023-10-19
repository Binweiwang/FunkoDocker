package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import common.Login;
import common.Request;
import common.Response;
import common.User;
import model.Funko;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.user.UserRepository;
import services.funko.FunkoService;
import services.funko.FunkoServiceImp;
import services.token.TokenService;
import utils.LocalDateAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class ClientHandler extends Thread {
    private final Logger logger = LoggerFactory.getLogger(ClientHandler.class.getName());
    private final Socket clientSocket;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,new LocalDateAdapter())
            .create();
    private final FunkoService funkoService;
    private final long clientNumber;
    BufferedReader in;
    PrintWriter out;

    public ClientHandler(Socket socket, long clientNumber, FunkoServiceImp funkoService){
        this.clientSocket = socket;
        this.funkoService = funkoService;
        this.clientNumber = clientNumber;
    }
    public void run(){
        try{
            openConnection();
            String clientInput;
            Request request;

            while(true){
                clientInput = in.readLine();
                logger.debug("Petición recibida: " + clientInput);
                request = gson.fromJson(clientInput, Request.class);
                handleRequest(request);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("uncheked")
    private void handleRequest(Request request) throws IOException {
        logger.debug("Procesando petición: " + request);
        switch (request.type()){
            case LOGIN -> login(request);
            case FIND_ALL_FUNKOS -> findAllFunkos(request);
            case OBTAIN_FUNKO_COD -> findFunkoById(request);
            case OBTAIN_FUNKO_MODEL -> findFunkoByModel(request);
            case OBTAIN_FUNKO_YEAR -> findFunkoByYear(request);
            case SAVE_FUNKO -> saveFunko(request);
            case UPDATE_FUNKO -> updateFunko(request);
            case DELETE_FUNKO -> deleteFunko(request);
            case SALIR -> salir();
            default -> out.println(gson.toJson(new Response<>(Response.Status.ERROR, "Petición no soportada", LocalDateTime.now().toString())));
        }
    }

    private void deleteFunko(Request request) {
     var user = verifyToken(request.token());
     if(user.isPresent() && user.get().role().equals(User.Role.ADMIN)){
         var myId = Long.parseLong(request.content());
         funkoService.deleteById(myId).subscribe(
                 funko -> {
                     logger.debug("Funko eliminado: " + funko);
                     var resJson = gson.toJson(funko);
                     out.println(gson.toJson(new Response<>(Response.Status.OK,resJson, LocalDateTime.now().toString())));
                 },
                 error -> logger.error("Error al eliminar funko: " + error),
                 () -> logger.debug("Funko eliminado")
         );
     }
    }

    private void updateFunko(Request request) {
        var user = verifyToken(request.token());
        if (user.isPresent()){
            Funko funkoToUpdate = gson.fromJson(String.valueOf(request.content()),new TypeToken<Funko>(){}.getType());
            funkoService.update(funkoToUpdate).subscribe(
                    funko -> {
                        logger.debug("Funko actualizado: " + funko);
                        var resJson = gson.toJson(funko);
                        out.println(gson.toJson(new Response<>(Response.Status.OK,resJson, LocalDateTime.now().toString())));
                    },
                    error -> logger.error("Error al actualizar funko: " + error),
                    () -> logger.debug("Funko actualizado")
            );
        }else {
            out.println(gson.toJson(new Response<>(Response.Status.ERROR,"Usuario no encontrado", LocalDateTime.now().toString())));
        }
    }

    private void saveFunko(Request request) {
        var user = verifyToken(request.token());
        if (user.isPresent()){
            Funko funkoToSave = gson.fromJson(String.valueOf(request.content()),new TypeToken<Funko>(){}.getType());
            funkoService.save(funkoToSave).subscribe(
                    funko -> {
                        logger.debug("Funko guardado: " + funko);
                        var resJson = gson.toJson(funko);
                        out.println(gson.toJson(new Response<>(Response.Status.OK,resJson, LocalDateTime.now().toString())));
                    },
                    error -> logger.error("Error al guardar funko: " + error),
                    () -> logger.debug("Funko guardado")
            );
        }else{
            out.println(gson.toJson(new Response<>(Response.Status.ERROR,"Usuario no encontrado", LocalDateTime.now().toString())));
        }
    }

    private void findFunkoByYear(Request request) {
        verifyToken(request.token());
        var myYear = Integer.parseInt(request.content());
        funkoService.findByYear(myYear).collectList().subscribe(
                funkos -> {
                    logger.debug("Enviando funko: " + funkos);
                    var resJson = gson.toJson(funkos);
                    out.println(gson.toJson(new Response<>(Response.Status.OK,resJson, LocalDateTime.now().toString())));
                },
                error -> logger.error("Error al enviar funko: " + error),
                () -> logger.debug("Funkos enviados")
        );
    }

    private void findFunkoByModel(Request request) {
        verifyToken(request.token());
        var myModel = request.content();
        funkoService.findByModel(myModel).collectList().subscribe(
                funkos -> {
                    logger.debug("Enviando funko: " + funkos);
                    var resJson = gson.toJson(funkos);
                    out.println(gson.toJson(new Response<>(Response.Status.OK,resJson, LocalDateTime.now().toString())));
                },
                error -> logger.error("Error al enviar funko: " + error),
                () -> logger.debug("Funkos enviados")
        );
    }

    private void login(Request request) {
        logger.debug("Procesando petición de login: " + request);
        Login login =gson.fromJson(String.valueOf(request.content()),new TypeToken<Login>(){}.getType());

        var user = UserRepository.getInstance().findByUsername(login.username());
        if(user.isEmpty() || !BCrypt.checkpw(login.password(),user.get().password())){
            out.println(gson.toJson(new Response<>(Response.Status.ERROR,"Usuario o contraseña incorrectos", LocalDateTime.now().toString())));
        }
         var token = TokenService.getInstance().createToken(user.get(),Server.TOKEN_SECRET,Server.TOKEN_EXPIRATION);
        out.println(gson.toJson(new Response<>(Response.Status.OK,token, LocalDateTime.now().toString())));
    }

    private void salir() throws IOException {
        out.println(gson.toJson(new Response<>(Response.Status.CLOSE,"Cerrando conexión con el servidor", LocalDateTime.now().toString())));
        closeConnection();
    }

    private void closeConnection() throws IOException {
        logger.debug("Cerrando la conexión con el cliente " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
        out.close();
        in.close();
        clientSocket.close();
    }

    private void findFunkoById(Request request) {
        verifyToken(request.token());
        var myId = Long.parseLong(request.content());
        funkoService.findById(myId).subscribe(
                funko -> {
                    logger.debug("Enviando funko: " + funko);
                    var resJson = gson.toJson(funko);
                    out.println(gson.toJson(new Response<>(Response.Status.OK,resJson, LocalDateTime.now().toString())));
                },
                error -> logger.error("Error al enviar funko: " + error),
                () -> logger.debug("Funkos enviados")
        );
    }

    private void findAllFunkos(Request request) {
        verifyToken(request.token());
        funkoService.findAll().collectList().subscribe(
                funkos -> {
                    logger.debug("Enviando funko: " + funkos);
                    var resJson = gson.toJson(funkos);
                    out.println(gson.toJson(new Response<>(Response.Status.OK,resJson, LocalDateTime.now().toString())));
                },
                error -> logger.error("Error al enviar funko: " + error),
                () -> logger.debug("Funkos enviados")
        );
    }

    private Optional<User> verifyToken(String token) {
        if (TokenService.getInstance().verifyToken(token, Server.TOKEN_SECRET)) {
            logger.debug("Token verificado");
            var claims = TokenService.getInstance().getClaims(token, Server.TOKEN_SECRET);
            var id = claims.get("userid").asInt();
            var user = UserRepository.getInstance().findById(id);
            if (user.isEmpty()){
                logger.error("Usuario no encontrado");
                return Optional.empty();
            } else {
                logger.debug("Usuario encontrado");
                return user;
            }
        }else{
            logger.error("Token no verificado");
        }
        return Optional.empty();
    }

    private void openConnection() throws IOException {
        logger.debug("Conectando con el cliente: " + clientNumber + " : "+ clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }
}
