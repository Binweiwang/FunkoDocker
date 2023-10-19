package repository.user;

import common.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;

public class UserRepository {
    private static UserRepository instance;
    private final List<User> users = List.of(
            new User(
                    1,
                    "pepe",
                    BCrypt.hashpw("pepe1234", BCrypt.gensalt(12)),
                    User.Role.ADMIN
            ),
            new User(
                    2,
                    "ana",
                    BCrypt.hashpw("ana1234", BCrypt.gensalt(12)),
                    User.Role.USER
            )
    );
    private UserRepository() {
    }
    public synchronized static UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }
    public Optional<User> findByUsername(String username) {
        return users.stream()
                .filter(user -> user.username().equals(username))
                .findFirst();
    }
    public Optional<User> findById(int id) {
        return users.stream()
                .filter(user -> user.id() == id)
                .findFirst();
    }
}
