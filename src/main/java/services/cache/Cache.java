package services.cache;

import reactor.core.publisher.Mono;

/**
 * Interfaz que contiene los métodos de caché
 *
 * @param <K> Tipo de key
 * @param <V> Tipo de value
 */
public interface Cache<K, V> {
    Mono<Void> put(K key, V value);

    Mono<V> get(K key);

    Mono<Void> remove(K key);

    void clear();

    void shutdown();
}
