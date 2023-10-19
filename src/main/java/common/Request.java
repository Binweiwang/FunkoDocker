package common;


public record Request<T>(Type type, T content, String token, String createdAt) {

    public enum Type{
        LOGIN,FIND_ALL_FUNKOS,OBTAIN_FUNKO_COD,OBTAIN_FUNKO_MODEL,OBTAIN_FUNKO_YEAR,SAVE_FUNKO,UPDATE_FUNKO,DELETE_FUNKO,SALIR;
    }
}
