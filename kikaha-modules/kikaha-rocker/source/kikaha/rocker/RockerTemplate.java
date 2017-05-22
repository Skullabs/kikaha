package kikaha.rocker;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author <a href="mailto:j.milagroso@gmail.com">Jay Milagroso</a>
 */
@Getter
@Setter
@Accessors( fluent = true )
@NoArgsConstructor
public class RockerTemplate {

    @NonNull
    String templateName;

    @NonNull
    Object objects;

    public RockerTemplate setTemplateName(String name) {
        this.templateName = name;

        return this;
    }

    public RockerTemplate setObjects(Object ... args) {
        this.objects = args;

        return this;
    }

    public String getTemplateName() {
        return this.templateName;
    }

    public Object getObjects() {
        return this.objects;
    }
}
