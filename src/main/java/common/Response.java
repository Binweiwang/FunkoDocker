package common;

/**
 * Clase Response que contiene los atributos status, content y createdAt
 *
 * @param status    Estado de la respuesta
 * @param content   Contenido de la respuesta
 * @param createdAt Fecha de creacion
 * @param <T>       Tipo de contenido
 */
public record Response<T>(Status status, T content, String createdAt) {
    public enum Status {
        OK, ERROR, CLOSE, TOKEN
    }
}
