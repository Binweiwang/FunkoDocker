package server.exceptions.funkos;

/**
 * FunkoException es la base de las excepciones de los funkos
 */
public abstract class FunkoException extends Exception{
    public FunkoException(String message){
        super(message);
    }
}
