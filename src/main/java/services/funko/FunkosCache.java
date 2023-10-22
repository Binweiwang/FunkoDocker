package services.funko;

import model.Funko;
import services.cache.Cache;

public interface FunkosCache extends Cache<Long, Funko> {
}
