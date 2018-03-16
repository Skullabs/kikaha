package kikaha.urouting.unit.samples;

import kikaha.urouting.api.*;
import lombok.*;

import javax.inject.Singleton;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Path("todos")
@Produces(Mimes.PLAIN_TEXT)
@Consumes(Mimes.PLAIN_TEXT)
@Singleton
public class TodoResource {

	final Map<Long, Todo> todos = new HashMap<>();

	@POST
	public Response persistTodo(Todo user) {
		todos.put(user.getId(), user);
		return Response.created("todos/" + user.getId());
	}

	@GET
	@Path( "{id}" )
	public Todo getTodo( @PathParam("id") Long id ) {
		return todos.get(id);
	}

	@Getter
	@EqualsAndHashCode
	@NoArgsConstructor
	@RequiredArgsConstructor
	public static class Todo implements Serializable {

		static final long serialVersionUID = 5059268468661470631L;
		final Long id = System.currentTimeMillis();

		@NonNull
		String name;
		Date date;
	}

}