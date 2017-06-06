package kikaha.rocker;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author <a href="mailto:j.milagroso@gmail.com">Jay Milagroso</a>
 */
@Singleton
public class RockerSerializerFactory {
    @Inject
    RockerSerializer serializer;

    public RockerSerializer serializer() {
        return serializer;
    }
}