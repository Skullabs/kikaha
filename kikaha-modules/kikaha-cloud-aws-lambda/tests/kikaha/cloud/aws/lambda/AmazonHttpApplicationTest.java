package kikaha.cloud.aws.lambda;

import kikaha.cloud.aws.lambda.sample.AccountProducer;
import kikaha.cloud.aws.lambda.sample.SampleResource;
import kikaha.commons.ChainedMap;
import kikaha.core.cdi.DefaultCDI;
import kikaha.urouting.api.Headers;
import kikaha.urouting.api.Mimes;
import lombok.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;
import javax.inject.*;
import javax.swing.*;

import static kikaha.cloud.aws.lambda.Requests.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AmazonHttpApplicationTest {

    static SampleResource resource;
    static AmazonHttpApplication application;

    @BeforeClass
    public static void injectDependencies(){
        val cdi = DefaultCDI.newInstance();

        application = new AmazonHttpApplication(
            cdi.load(AmazonRequestMatcher.class),
            cdi.load(AmazonHttpFilters.class),
            cdi.load(AmazonContentTypeHandlers.class)
        );

        resource = cdi.load( SampleResource.class );
    }

    @Test
    public void canHandleRequestWithManyParameter(){
        val response = application.handleRequest( GET_WITH_PARAMS, null );
        assertEquals( 200, response.statusCode );
        assertEquals(Mimes.JSON, response.getContentType() );
        assertEquals("{\"id\":431}", response.getBody());
    }

    @Test
    public void canHandleRequestWithoutAnyParameter(){
        val response = application.handleRequest( GET_WITHOUT_PARAMETERS, null );
        assertEquals( 200, response.statusCode );
        assertEquals(Mimes.JSON, response.getContentType() );
        assertEquals("\"SampleResource.doGetAndReturnObject\"", response.getBody());
    }

    @Test
    public void canHandleRequestWithSinglePathParameter(){
        val response = application.handleRequest( GET_SINGLE, null );
        assertEquals( 200, response.statusCode );
        assertEquals( Mimes.JSON, response.getContentType() );
        assertEquals("431", response.getBody());
    }

    @Test
    public void canHandlePostRequests(){
        val response = application.handleRequest( POST, null );
        assertEquals( 201, response.statusCode );
        assertNull( response.getContentType() );
        assertNull(response.getBody());
        assertEquals( "/location/", response.getHeaders().get(Headers.LOCATION) );

        assertEquals( resource.getLastExecutedMethod(),"SampleResource.doPost"  );
    }

    @Test
    public void canHandlePutRequests(){
        val response = application.handleRequest( PUT, null );
        assertEquals( 204, response.statusCode );
        assertNull( response.getContentType() );
        assertNull(response.getBody());
        assertEquals( resource.getLastExecutedMethod(),"SampleResource.doPutAndReceiveContext(Account(username=1234),{id=431})"  );
    }

    @Test
    public void canHandlePatchRequests(){
        val response = application.handleRequest( PATCH, null );
        assertEquals( 204, response.statusCode );
        assertNull( response.getContentType() );
        assertNull(response.getBody());
        assertEquals( resource.getLastExecutedMethod(),"SampleResource.doPatch({id=431})"  );
    }

    @Test
    public void canHandleDeleteRequests(){
        val response = application.handleRequest( DELETE, null );
        assertEquals( 204, response.statusCode );
        assertNull( response.getContentType() );
        assertNull(response.getBody());
        assertEquals( resource.getLastExecutedMethod(),"SampleResource.doDeleteObject(431)"  );
    }
}

interface Requests {

    AmazonHttpRequest
        GET_WITHOUT_PARAMETERS = new AmazonHttpRequest().setPath( "/root-uri/" ).setHttpMethod( "GET" ),

        GET_WITH_PARAMS = new AmazonHttpRequest().setPath( "/root-uri/with-params" ).setHttpMethod( "GET" )
            .setHeaders( new ChainedMap<String, String>()
                .set( "JSESSIONID", "1234-431-43143")
                .set( "Cookie", "X=43143" )
            )
            .setQueryStringParameters(
                Collections.singletonMap( "id", "431" )
            ),

        GET_SINGLE = new AmazonHttpRequest().setPath( "/root-uri/single/431" ).setHttpMethod( "GET" ),

        POST = new AmazonHttpRequest().setPath( "/root-uri/" ).setHttpMethod( "POST" )
                .setHeaders( Collections.singletonMap(Headers.CONTENT_TYPE, Mimes.JSON ) )
                .setBody( "{\"id\":431}" ),

        PUT = new AmazonHttpRequest().setPath( "/root-uri/" ).setHttpMethod( "PUT" )
            .setHeaders(
                new ChainedMap<String, String>()
                    .set( AccountProducer.HEADER, "1234")
                    .set(Headers.CONTENT_TYPE, Mimes.JSON ) )
            .setBody( "{\"id\":431}" ),

        DELETE = new AmazonHttpRequest().setPath( "/root-uri/431" ).setHttpMethod( "DELETE" ),

        PATCH = new AmazonHttpRequest().setPath( "/root-uri/" ).setHttpMethod( "PATCH" )
                .setHeaders( Collections.singletonMap(Headers.CONTENT_TYPE, Mimes.JSON ) )
                .setBody( "{\"id\":431}" )
    ;
}