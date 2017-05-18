package kikaha.rocker;

import lombok.NonNull;

import java.io.Serializable;

public class User implements Serializable {

    private static final long serialVersionUID = -3278587347520465945L;

    final long id;

    @NonNull
    public String name;

    @NonNull
    public String username;

    @NonNull
    public String password;

    public User() {
        this.id = System.currentTimeMillis();
    }
}
