package kikaha.cloud.smart.tracer;

import lombok.*;
import lombok.experimental.Accessors;

import static kikaha.commons.Lang.isUndefined;

/**
 * Created by ibratan on 16/06/2017.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public final class TraceId {
    final String id;

    public boolean isPresent(){
        return !isUndefined( id );
    }
}
