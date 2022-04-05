package chat.model;

public class UserSession {
    private String username;
    private String sessionId;
    private String status;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
