package common;

/**
 * Clase User que contiene los atributos id, username, password y role
 *
 * @param id       Id del usuario
 * @param username Nombre del usuario
 * @param password Contraseña del usuario
 * @param role     Permisos del usuario
 */
public record User(long id, String username, String password, Role role) {
    public enum Role {
        ADMIN, USER
    }
}
