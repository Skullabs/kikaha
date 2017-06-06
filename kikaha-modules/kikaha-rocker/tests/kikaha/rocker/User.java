package kikaha.rocker;

import java.io.Serializable;

public class User implements Serializable {

    private static final long serialVersionUID = -3278587347520465945L;

    final long id;

    public String name;

    public String username;

    public String password;

    public User() {
        this.id = System.currentTimeMillis();
    }
}
