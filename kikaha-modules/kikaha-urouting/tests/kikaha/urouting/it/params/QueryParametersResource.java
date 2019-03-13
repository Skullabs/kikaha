package kikaha.urouting.it.params;

import com.fasterxml.jackson.databind.ObjectMapper;
import kikaha.urouting.api.*;
import lombok.val;

import javax.inject.Singleton;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * @author: miere.teixeira
 */
@Path( "it/parameters/query" )
@Singleton
public class QueryParametersResource {

	@GET
	public String pathParameterEchoWithGETMethod(@QueryParam( "id" ) long id,
												 @QueryParam("localDate") LocalDate localDate,
												 @QueryParam("localDateTime") LocalDateTime localDateTime,
												 @QueryParam("zonedDateTime") ZonedDateTime zonedDateTime) {
		return createResponseMessage(id, localDate, localDateTime, zonedDateTime);
	}

	@POST
	public String pathParameterEchoWithPOSTMethod(@QueryParam( "id" ) long id,
												   @QueryParam("localDate") LocalDate localDate,
												   @QueryParam("localDateTime") LocalDateTime localDateTime,
												   @QueryParam("zonedDateTime") ZonedDateTime zonedDateTime) {
		return createResponseMessage(id, localDate, localDateTime, zonedDateTime);
	}

	@PUT
	public String pathParameterEchoWithPUTMethod(@QueryParam( "id" ) long id,
												  @QueryParam("localDate") LocalDate localDate,
												  @QueryParam("localDateTime") LocalDateTime localDateTime,
												  @QueryParam("zonedDateTime") ZonedDateTime zonedDateTime) {
		return createResponseMessage(id, localDate, localDateTime, zonedDateTime);
	}

	@DELETE
	public String pathParameterEchoWithDELETEMethod(@QueryParam( "id" ) long id,
													@QueryParam("localDate") LocalDate localDate,
													@QueryParam("localDateTime") LocalDateTime localDateTime,
													@QueryParam("zonedDateTime") ZonedDateTime zonedDateTime) {
		return createResponseMessage(id, localDate, localDateTime, zonedDateTime);
	}

	@PATCH
	public String pathParameterEchoWithPATCHMethod(@QueryParam( "id" ) long id,
												   @QueryParam("localDate") LocalDate localDate,
												   @QueryParam("localDateTime") LocalDateTime localDateTime,
												   @QueryParam("zonedDateTime") ZonedDateTime zonedDateTime) {
		return createResponseMessage(id, localDate, localDateTime, zonedDateTime);
	}

	String createResponseMessage(long id, LocalDate localDate, LocalDateTime localDateTime, ZonedDateTime zonedDateTime){
		val response = new HashMap<>();
		response.put( "id", id );
		response.put( "localDate", localDate == null ? null : DateTimeFormatter.ISO_LOCAL_DATE.format(localDate));
		response.put( "localDateTime", localDateTime == null ? null : DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime));
		response.put( "zonedDateTime", zonedDateTime == null ? null : DateTimeFormatter.ISO_ZONED_DATE_TIME.format(zonedDateTime));
		return asString( response );
	}

	private String asString(Object response) {
		try {
			return new ObjectMapper().writeValueAsString(response);
		} catch ( Exception e ){
			throw new RuntimeException( e );
		}
	}

}
