package kikaha.urouting.it;

import javax.inject.Singleton;
import kikaha.urouting.api.*;

/**
 *
 */
@Path("it/form-auth/authenticated-only")
@Singleton
public class FormResource {

	@GET
	public Response sendOk(){
		return Response.ok();
	}
}
