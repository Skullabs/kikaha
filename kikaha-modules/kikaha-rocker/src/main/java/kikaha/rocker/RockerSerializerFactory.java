package kikaha.rocker;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author <a href="mailto:j.milagroso@gmail.com">Jay Milagroso</a>
 */
@Slf4j
@Singleton
public class RockerSerializerFactory {
    @Inject
    RockerSerializer serializer;

    public RockerSerializer serializer() {
        return serializer;
    }
}