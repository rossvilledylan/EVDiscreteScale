package objects.Message;

import java.time.Instant;

/**
 * An implementation of a message which contains information concerning the minimum time of either a specific Station or
 * of the Simulation globally. Used by Stations to inform the Stations to inform the Monitor of their current simulated time
 * and by the Monitor to keep a globally synchronized minimum time across all Stations.
 */
public class TimingMessage implements Message{
    private final Instant timestamp;
    private final String sender;

    /**
     * Constructor for creating a Timing Message.
     * @param t the time the message was created at and the time the recipient is being informed of.
     * @param s the name of the sender of the message
     */
    public TimingMessage(Instant t, String s){
        this.timestamp = t;
        this.sender = s;
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }
    public String getSender(){
        return this.sender;
    }
}
