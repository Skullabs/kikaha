package kikaha.commons;

import lombok.Data;

import java.util.Date;

@Data
public class Cookie {

    private final String name;
    private String value;
    private String path;
    private String domain;
    private Integer maxAge;
    private Date expires;
    private boolean discard;
    private boolean secure;
    private boolean httpOnly;
    private int version = 0;
    private String comment;

    public Cookie(final String name, final String value) {
        this.name = name;
        this.value = value;
    }
}