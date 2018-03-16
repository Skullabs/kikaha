package kikaha.rocker;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.List;
import javax.inject.Singleton;
import com.fizzed.rocker.*;
import com.fizzed.rocker.runtime.*;
import io.undertow.server.HttpServerExchange;
import kikaha.core.modules.http.ContentType;
import kikaha.urouting.api.*;
import kikaha.urouting.serializers.Serializer;

/**
 * @author <a href="mailto:j.milagroso@gmail.com">Jay Milagroso</a>
 */
@Singleton
@ContentType( Mimes.HTML )
public class RockerSerializer implements Serializer {

    final RockerRuntime runtime = RockerRuntime.getInstance();

    @Override
    public <T> void serialize(T object, HttpServerExchange exchange, String encoding) throws IOException {
        final RockerTemplate template = (RockerTemplate)object;
        final String serialized = serialize(template);
        exchange.getResponseSender().send(serialized);
    }

    public String serialize( final RockerTemplate object ) {
        final Writer writer = new StringWriter();
        serialize( object, writer );
        return writer.toString();
    }

    public void serialize( final RockerTemplate object, final Writer writer ) {
        final String templateName = object.getTemplateName();
        final BindableRockerModel template = this.template(templateName, (Object[]) object.getObjects());
        final ArrayOfByteArraysOutput output = template.render(ArrayOfByteArraysOutput.FACTORY);

        // Convert to array of byte buffers
        final List<byte[]> byteArrays = output.getArrays();
        final int size = byteArrays.size();
        final ByteBuffer[] byteBuffers = new ByteBuffer[size];
        for (int i = 0; i < size; i++) {
            byteBuffers[i] = ByteBuffer.wrap(byteArrays.get(i));
        }

        try {
            writer.write(output.toString());
        } catch (IOException e) {
            throw new IllegalStateException( e );
        }
    }

    private BindableRockerModel template(String templatePath, Object [] arguments) {
        // load model from bootstrap (which may recompile if needed)
        final RockerModel model = runtime.getBootstrap().model(templatePath);
        final BindableRockerModel bindableRockerModel = new BindableRockerModel(templatePath, model.getClass().getCanonicalName(), model);

        if (arguments != null && arguments.length > 0) {
            final String[] argumentNames = getModelArgumentNames(templatePath, model);

            if (arguments.length != argumentNames.length) {
                throw new TemplateBindException(templatePath, model.getClass().getCanonicalName(), "Template requires " + argumentNames.length + " arguments but " + arguments.length + " provided");
            }

            for (int i = 0; i < arguments.length; i++) {
                final String name = argumentNames[i];
                final Object value = arguments[i];
                bindableRockerModel.bind(name, value);
            }
        }

        return bindableRockerModel;
    }

    static private String[] getModelArgumentNames(String templatePath, RockerModel model) {
        try {
            final Field f = model.getClass().getField("ARGUMENT_NAMES");
            return (String[])f.get(null);
        } catch (Exception e) {
            throw new TemplateBindException(templatePath, model.getClass().getCanonicalName(), "Unable to read ARGUMENT_NAMES static field from template");
        }
    }

}
