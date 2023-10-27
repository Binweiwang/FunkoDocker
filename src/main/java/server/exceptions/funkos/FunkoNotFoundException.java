package server.exceptions.funkos;

/**
 * FunkoNotFoundException cuando no se encuentra un funko
 */
public class FunkoNotFoundException extends FunkoException{
    public FunkoNotFoundException(String message) {
        super(message);
    }
}
