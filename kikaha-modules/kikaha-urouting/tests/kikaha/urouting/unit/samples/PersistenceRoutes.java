package kikaha.urouting.unit.samples;

import kikaha.urouting.api.*;
import kikaha.urouting.unit.User;

import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
@Path( "{contentType}/users" )
public class PersistenceRoutes {

	ExecutorService executor = Executors.newSingleThreadExecutor();

	@PUT
	@Path( "{id}" )
	public Response update(
			@PathParam( "id" ) final Long id, final User user ) {
		return null;
	}

	@POST
	public Response create( final User user ) {
		return null;
	}

	@DELETE
	@Path( "{id}" )
	public void delete( @Context final User user ) {
	}

	@GET
	@Path( "async" )
	public void doAsyncSearch( final AsyncResponse response ) {
		executor.submit( () -> response.write( Response.notModified() ) );
	}

	@GET
	@Path( "async/{{id}}" )
	@Produces( Mimes.JSON )
	public void doAsyncSearchById(
		final AsyncResponse response, @PathParam( "id" ) final Long id ) {
		executor.submit( () -> response.write( Response.notModified() ) );
	}

	@POST
	@Path("form")
	public String doSomethingWithFormData( @FormParam( "name" ) String name ){
		return name;
	}
}
