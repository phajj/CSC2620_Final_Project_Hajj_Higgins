package shared;

public class UserCredentials {

    private String username;
    private String password;
    private String keyword;

    public UserCredentials(String username, String password, String keyword) {
        this.username = username;
        this.password = password;
        this.keyword = keyword;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getKeyword() { return keyword; }
}
