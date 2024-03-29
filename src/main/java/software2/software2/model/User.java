package software2.software2.model;

/**
 * Class used to create User objects throughout the application.
 */
public class User {
    private int id;
    private String username;
    private String password;

    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public int getId() {return id;}

    public String getUsername() { return username; }

    public void setUsername(String username) {this.username = username;}

    public String getPassword() { return password; }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return (String.valueOf(id));
    }
}
