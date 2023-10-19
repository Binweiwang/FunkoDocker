package server;

import database.DatabaseManager;
import model.Funko;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import repository.funko.FunkoRepositoryImp;
import services.funko.FunkoService;
import services.funko.FunkoServiceImp;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicLong;

public class Server {
    public static final String TOKEN_SECRET = "MiNombreEsBinwei";
    public static final long TOKEN_EXPIRATION = 10000;
    private static final AtomicLong clientNumber = new AtomicLong(0);
    private final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final FunkoService funkoService = FunkoServiceImp.getInstance(FunkoRepositoryImp.getInstance(DatabaseManager.getInstance()));

    public static void main(String[] args) throws Exception {
        try(ServerSocket serverSocket = new ServerSocket(3000);) {
            System.out.println("🚀 Servidor escuchando en el puerto 3000");
            var funkoService = FunkoServiceImp.getInstance(FunkoRepositoryImp.getInstance(DatabaseManager.getInstance()));
            Flux<Funko> importar = funkoService.importar();
            importar.subscribe(System.out::println);
            while (true) {
                    new ClientHandler(serverSocket.accept(),clientNumber.incrementAndGet(), funkoService).start();
                    if (clientNumber.get() == 10){
                        break;
                    }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            DatabaseManager.getInstance().close();
        }
    }
}
