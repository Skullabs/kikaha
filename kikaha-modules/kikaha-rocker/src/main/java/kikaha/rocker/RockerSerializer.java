package kikaha.rocker;

import com.fizzed.rocker.BindableRockerModel;
import com.fizzed.rocker.RockerModel;
import com.fizzed.rocker.TemplateBindException;
import com.fizzed.rocker.runtime.RockerRuntime;
import kikaha.config.Config;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;

/**
 * @author <a href="mailto:j.milagroso@gmail.com">Jay Milagroso</a>
 */
@Singleton
@Getter
@Accessors( fluent = true )
public class RockerSerializer {
    private boolean shouldCacheTemplates;

    @Inject
    Config config;

    @PostConstruct
    public void readConfiguration() {
        shouldCacheTemplates = config.getBoolean( "server.rocker.cache-templates" );
    }

    public String serialize( final RockerTemplate object ) {
        final Writer writer = new StringWriter();

        serialize( object, writer );
        return writer.toString();
    }

    public void serialize( final RockerTemplate object, final Writer writer ) {
        final String templateName = object.templateName();

        String rendered = this.template(templateName, (Object[]) object.paramObject())
                .render()
                .toString();
        try {
            writer.write(rendered);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static private BindableRockerModel template(String templatePath, Object [] arguments) {

        // load model from bootstrap (which may recompile if needed)
        RockerModel model = RockerRuntime.getInstance().getBootstrap().model(templatePath);

        BindableRockerModel bindableModel = new BindableRockerModel(templatePath, model.getClass().getCanonicalName(), model);

        if (arguments != null && arguments.length > 0) {
            String[] argumentNames = getModelArgumentNames(templatePath, model);

            if (arguments.length != argumentNames.length) {
                throw new TemplateBindException(templatePath, model.getClass().getCanonicalName(), "Template requires " + argumentNames.length + " arguments but " + arguments.length + " provided");
            }

            for (int i = 0; i < arguments.length; i++) {
                String name = argumentNames[i];
                Object value = arguments[i];
                bindableModel.bind(name, value);
            }
        }

        return bindableModel;
    }

    static private String[] getModelArgumentNames(String templatePath, RockerModel model) {
        try {
            Field f = model.getClass().getField("ARGUMENT_NAMES");
            return (String[])f.get(null);
        } catch (Exception e) {
            throw new TemplateBindException(templatePath, model.getClass().getCanonicalName(), "Unable to read ARGUMENT_NAMES static field from template");
        }
    }

}
