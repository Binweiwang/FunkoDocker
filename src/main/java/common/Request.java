package common;

/**
 * Clase Request que contiene los atributos type, content, token y createdAt
 *
 * @param type      Accion que puede realizar el cliente
 * @param content   Contenido que se envia al servidor
 * @param token     Token de autenticacion
 * @param createdAt Fecha de creacion
 * @param <T>       Tipo de contenido
 */
public record Request<T>(Type type, T content, String token, String createdAt) {
    public enum Type {
        LOGIN, FIND_ALL_FUNKOS, OBTAIN_FUNKO_COD, OBTAIN_FUNKO_MODEL, OBTAIN_FUNKO_YEAR, SAVE_FUNKO, UPDATE_FUNKO, DELETE_FUNKO, SALIR
    }
}
