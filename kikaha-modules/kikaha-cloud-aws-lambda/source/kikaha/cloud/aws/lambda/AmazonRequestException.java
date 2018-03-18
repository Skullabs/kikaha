package kikaha.cloud.aws.lambda;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class AmazonRequestException extends RuntimeException {
    public AmazonRequestException( String message ){ super(message);}
    public AmazonRequestException( String message, Throwable cause ){ super(message, cause);}
    public AmazonRequestException( Throwable cause ){ super(cause);}

    public static AmazonRequestException handledException( Throwable cause ) {
        return new AmazonRequestException( cause.getMessage() + "\n" + convertToString(cause), cause );
    }

    static String convertToString( Throwable cause ) {
        try (final Writer writer = new StringWriter() ) {
            cause.printStackTrace(new PrintWriter(writer));
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
