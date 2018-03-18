package kikaha.cloud.aws.lambda.sample;

import kikaha.cloud.aws.lambda.AmazonHttpRequest;
import kikaha.urouting.api.*;
import lombok.Getter;

import javax.inject.Singleton;
import java.util.Collections;

/**
 * The only propose this resource class exists it to force the APT
 * to generate {@code routing classes} for each method. If the APT
 * would not able to generate and compile classes, it will break the
 * build plan of this module.
 */
@Singleton
@Path("root-uri")
@SuppressWarnings("unused")
public class SampleResource {

    @Getter String lastExecutedMethod;

	@GET
	public Object doGetAndReturnObject() {
        return lastExecutedMethod = "SampleResource.doGetAndReturnObject";
	}

	@GET
    @Path( "with-params" )
	public Response doGetAndReturnObjectWithParams(
		@CookieParam("JSESSIONID") String sessionId,
	    @HeaderParam("Cookie") String cookie,
	    @QueryParam("id") int id
	) {
		lastExecutedMethod = "SampleResource.doGetAndReturnObjectWithParams("+sessionId+","+cookie+","+id+")";
        return Response.ok()
            .entity( Collections.singletonMap( "id", id ) )
            .contentType( Mimes.JSON );
    }

	@GET
	@Path("single/{id}")
	public Long doGetAndReturnObjectById( @PathParam("id") Long id ) {
	    lastExecutedMethod = "SampleResource.doGetAndReturnObjectById("+id+")";
        return id;
	}

	@POST
	public Response doPost( Object body ) {
        lastExecutedMethod = "SampleResource.doPost";
		return Response.created( "/location/" );
	}

	@PUT
	public void doPutAndReceiveContext(@Context Account account, Object body){
        lastExecutedMethod = "SampleResource.doPutAndReceiveContext("+ account+","+body+")";
	}

    @PATCH
    public void doPatch(Object body){
        lastExecutedMethod = "SampleResource.doPatch("+body+")";
    }

    @DELETE @Path("{id}")
    public void doDeleteObject( @PathParam("id") Long id ){
        lastExecutedMethod = "SampleResource.doDeleteObject("+id+")";
	}
}
