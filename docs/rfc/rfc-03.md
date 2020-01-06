# `RFC-03` - RPC-like Rest API 
Provide a simple, but versatile, way to map Rest endpoints in a RPC fashion.

## Tracking issues
- **Pull Request**: [#262](https://github.com/Skullabs/kikaha/pull/262)
- **Issue**: -

## Motivation
There's a lot of discussion about using RPC or Rest endpoint to expose microservice APIs. If we decide to pick one or
another, this decision should be done wisely by considering the pros and cons from both approaches. By observing the
most common Java web frameworks, it's clear that Rest APIs has been the prefered approach - probably
because Rest has been seen as good replacement for SOAP and many people think about RPC as a modern SOAP.

Below are some points that draw my attention in my quick research about this topic. It reforces my previous experience
on this regard by high lighting why RPC is a better approach. It worth notice though that not taking REST into account
would would be seen as an irresponsible approach, once it also have some positive points.

> **REST Pros (and RPC cons)**
> - Semantics are well-known. So you don’t have to teach much things.
> - Schema is explicit. Easier to understand.

> **RPC Pros (and REST cons)**
> - Dealing with irregular data and exceptional situations.
> - Dealing with operation without data.
> - Quickstart. You don’t have to design full schema from first.
> - Fine gained control. You can represent whatever level of operation you need.
> - Gradual improvement. It’s easier to deprecate single function.
> 
> Source: https://qr.ae/TW8y5n

## Solution Overview
The core concepts behind this RPC are:
1. Have a centralized place to find mapped endpoints within the service.
2. Have a flexible API that would be easy to extend and reduce boilerplates.
3. Allow developers to use both semantics (REST and RPC) using the same mapping mechanism.

Below we have a sample Java application exposing a REST endpoint using a syntax more similar to
RPC API's using Kikaha 3.0.0. 

```java
import kikaha.rest.*;
import kikaha.jackson.*;
import kikaha.undertow.*;
import lombok.*;
import lombok.experimental.*;
import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
class App {

    public void main( String[] args )
    {
        val api = Api();
    
        val router = Router.empty()
          .serializers( JacksonSerializer.usingDefaults() )
          .route( Methods.GET, "/users/:city", api::filterUsers )
          .route( Methods.POST, "/users/:city", api::includeUser )
        ;

        UndertowServer.usingDefaults()
          .listenHttpRequests( 8080 )
          .requestHandler( router )
          .start();
    }
}

class Api {

  final Map<String,List<User>> users = new HashMap<>();

  void filterUsers( FilterUserRequest filter, Response response ) {
      val found = users.get( filter.city ).stream()
        .filter( user -> user.name.contains( filter.name ) )
        .collect(Collectors.toList());

      response.send( 200, ContentTypes.JSON, found );
  }
  
  void includeUser( IncludeUser request, Response response )
  {
      users.computeIfAbsent( request.city, k -> new ArrayList<>() )
         .put( request.city, request.user );
      response.send( 201 );
  }
}

@Request
@Value class FilterUserRequest
{
  @PathParam("city") String city;
  @QueryParam("name") String name;
}

@Request
@Value class IncludeUser
{
  @PathParam("city") String city;
  @Body( ContentType.JSON ) User user;
}

@Value class User {
  String name;
  String email;
}
```
