package Model;

import java.util.UUID;

public class SessionContext {

    private static final SessionContext INSTANCE = new SessionContext();

    private UUID userId;
    private String username;
    private long balanceCent;
    private UUID roundId;
    private UUID betId;
    
    
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
    
    public UUID getRoundId() {
        return roundId;
    }

    public UUID getBetId() {
        return betId;
    }


    public void setSession(UUID userId, String username, long balanceCent) {
        this.userId = userId;
        this.username = username;
        this.balanceCent = balanceCent;
    }
    
    public void setRoundId(UUID roundId) {
        this.roundId = roundId;
    }

    public void setBetId(UUID betId) {
        this.betId = betId;
    }

    
    public void setBalanceCent(long balanceCent) {
        this.balanceCent = balanceCent;
    }

    public void clear() {
        this.userId = null;
        this.username = null;
        this.balanceCent = 0L;
        this.roundId = null;
        this.betId = null;
    }
}
