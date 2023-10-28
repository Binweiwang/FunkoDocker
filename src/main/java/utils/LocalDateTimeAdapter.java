package utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Una clase de adaptador para convertir entre una fecha y una cadena JSON.
 */
public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Escribe un valor JSON (una matriz, objeto, cadena, número, booleano o nulo)
     *
     * @param out
     * @param value Puede ser nulo.
     * @throws IOException
     */
    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(formatter.format(value));
        }
    }

    /**
     * Lee el siguiente valor JSON del JsonReader proporcionado y lo convierte en un objeto Java.
     *
     * @param in JsonReader para leer. Nunca es nulo.
     * @return Puede ser nulo.
     * @throws IOException
     */
    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
            in.nextNull();
            return null;
        } else {
            String dateTimeString = in.nextString();
            return LocalDateTime.parse(dateTimeString, formatter);
        }
    }
}