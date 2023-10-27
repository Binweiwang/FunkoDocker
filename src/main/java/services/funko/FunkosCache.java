package services.funko;

import model.Funko;
import services.cache.Cache;

/**
 * Interfaz que contiene los métodos de caché para Funkos
 */
public interface FunkosCache extends Cache<Long, Funko> {
}
