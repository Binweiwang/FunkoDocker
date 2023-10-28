package repository.user;

import common.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;

/**
 * User repository
 */
public class UserRepository {
    // Atributos
    private static UserRepository instance;
    private final List<User> users = List.of(new User(1, "pepe", BCrypt.hashpw("pepe1234", BCrypt.gensalt(12)), User.Role.ADMIN), new User(2, "ana", BCrypt.hashpw("ana1234", BCrypt.gensalt(12)), User.Role.USER));

    /**
     * Constructor de repository de usuario
     */
    private UserRepository() {
    }

    /**
     * Método que devuelve la instancia de repository
     *
     * @return Instancia de repository de usuario
     */
    public synchronized static UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    /**
     * Método que devuelve un usuario
     *
     * @param username Nombre de usuario
     * @return Un usuario
     */
    public Optional<User> findByUsername(String username) {
        return users.stream().filter(user -> user.username().equals(username)).findFirst();
    }

    /**
     * Método que devuelve un usuario
     *
     * @param id Id del usuario
     * @return Un usuario
     */
    public Optional<User> findById(int id) {
        return users.stream().filter(user -> user.id() == id).findFirst();
    }
}
