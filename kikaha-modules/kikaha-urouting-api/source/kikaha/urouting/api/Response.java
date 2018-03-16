package kikaha.urouting.api;

import kikaha.core.cdi.helpers.TinyList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.net.URI;
import java.util.List;

import static kikaha.urouting.api.Headers.LOCATION;

/**
 * Interface defining a object that handles a data that will
 * be rendered. There's no intention to be a multi-propose object
 * neither has thread-safety support at all. But it will doing fine in a
 * per-request scope implementation.
 */
public interface Response {

    /**
     * Get the object that should be serialized.
     *
     * @return
     */
    Object entity();

    /**
     * Get the status code.
     *
     * @return
     */
    int statusCode();

    /**
     * Get the encoding.
     *
     * @return
     */
    String encoding();

    /**
     * Get the headers.
     *
     * @return
     */
    Iterable<Header> headers();

    /**
     * Retrieve headers named {@code name}.
     *
     * @param name
     * @return
     */
    // XXX: good enough, but one may improve this... ;)
    default Header header(final String name) {
        for (final Header header : headers())
            if (header.name().equals(name))
                return header;
        return null;
    }

    /**
     * Get the Content-Type defined with this {@link Response}.
     *
     * @return
     */
    default String contentType() {
        final Header header = header("Content-Type");
        return header != null ? header.values().get(0) : null;
    }

    static MutableResponse create() {
        return new DefaultResponse();
    }

    static MutableResponse response(final int statusCode) {
        return new DefaultResponse().statusCode(statusCode);
    }

    static MutableResponse ok() {
        return create().statusCode(200);
    }

    static MutableResponse ok(final Object entity) {
        return ok().entity(entity);
    }

    static MutableResponse noContent() {
        return create().statusCode(204);
    }

    static MutableResponse created() {
        return create().statusCode(201);
    }

    static MutableResponse created(final String location) {
        return create().statusCode(201)
                .header(LOCATION, location);
    }

    static MutableResponse accepted() {
        return create().statusCode(202);
    }

    static MutableResponse movedPermanently() {
        return create().statusCode(301);
    }

    static MutableResponse notModified() {
        return create().statusCode(304);
    }

    static MutableResponse seeOther() {
        return create().statusCode(303);
    }

    static MutableResponse seeOther(final String location) {
        return create().statusCode(303)
                .header(LOCATION, location);
    }

    static MutableResponse temporaryRedirect(final String location) {
        return create().statusCode(307)
                .header(LOCATION, location);
    }

    static MutableResponse temporaryRedirect(final URI location) {
        return temporaryRedirect(location.toString());
    }

    static MutableResponse permanentRedirect(final String location) {
        return create().statusCode(308)
                .header(LOCATION, location);
    }

    static MutableResponse permanentRedirect(final URI location) {
        return permanentRedirect(location.toString());
    }

    static MutableResponse badRequest() {
        return create().statusCode(400);
    }

    static MutableResponse unauthorized() {
        return create().statusCode(401);
    }

    static MutableResponse forbiden() {
        return create().statusCode(403);
    }

    static MutableResponse notFound() {
        return create().statusCode(404);
    }

    static MutableResponse preconditionFailed() {
        return create().statusCode(412);
    }

    static MutableResponse serverError() {
        return create().statusCode(500);
    }

    static MutableResponse serverError(final String string) {
        return create().statusCode(500)
                .entity(string);
    }

    @Getter
    @Setter
    @Accessors(fluent = true)
    @NoArgsConstructor
    class DefaultResponse implements Response, MutableResponse {

        int statusCode = 200;
        @NonNull
        Object entity = "";
        @NonNull
        String encoding = "UTF-8";
        @NonNull
        List<Header> headers = new TinyList<>();

        public MutableResponse headers(final Iterable<Header> headers) {
            this.headers = new TinyList<>();
            for (final Header header : headers)
                this.headers.add(header);
            return this;
        }

        public MutableResponse header(final String name, @NonNull final String value) {
            Header header = header(name);
            if (header == null) {
                header = DefaultHeader.createHeader(name, value);
                headers.add(header);
            } else
                header.add(value);
            return this;
        }
    }
}
