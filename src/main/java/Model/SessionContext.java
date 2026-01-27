package Model;

import java.util.UUID;

public class SessionContext {

    private static final SessionContext INSTANCE = new SessionContext();

    private UUID userId;
    private String username;
    private long balanceCent;

    private SessionContext() {
    }

    public static SessionContext getInstance() {
        return INSTANCE;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public long getBalanceCent() {
        return balanceCent;
    }

    public void setSession(UUID userId, String username, long balanceCent) {
        this.userId = userId;
        this.username = username;
        this.balanceCent = balanceCent;
    }

    public void setBalanceCent(long balanceCent) {
        this.balanceCent = balanceCent;
    }

    public void clear() {
        this.userId = null;
        this.username = null;
        this.balanceCent = 0L;
    }
}
