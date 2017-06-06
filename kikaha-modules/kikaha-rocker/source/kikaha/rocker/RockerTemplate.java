package kikaha.rocker;

/**
 * @author <a href="mailto:j.milagroso@gmail.com">Jay Milagroso</a>
 */

public class RockerTemplate {

    String templateName;

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
