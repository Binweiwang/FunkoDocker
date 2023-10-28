package server.exceptions.funkos;

/**
 * FunkoNotStoredException cuando no se guarda un funko
 */
public class FunkoNotStoredException extends FunkoException {
    public FunkoNotStoredException(String message) {
        super(message);
    }
}
