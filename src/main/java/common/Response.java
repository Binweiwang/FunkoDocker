package common;

public record Response<T>(Status status, T content, String createdAt) {
    public enum  Status{
        OK, ERROR, CLOSE, TOKEN
    }
}
