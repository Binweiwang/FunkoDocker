package utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Una clase de adaptador para convertir entre una fecha y una cadena JSON.
 */
public class LocalDateAdapter extends TypeAdapter<LocalDate> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Escribe un valor JSON (una matriz, objeto, cadena, número, booleano o nulo)
     *
     * @param out
     * @param value Puede ser nulo.
     * @throws IOException
     */
    @Override
    public void write(JsonWriter out, LocalDate value) throws IOException {
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
    public LocalDate read(JsonReader in) throws IOException {
        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
            in.nextNull();
            return null;
        } else {
            String dateString = in.nextString();
            return LocalDate.parse(dateString, formatter);
        }
    }
}