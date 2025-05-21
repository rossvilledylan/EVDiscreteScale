package objects.Message;

import java.time.Instant;

/**
 * Depicts a message that is sent to signal to a Station that the simulation is over.
 */
public class EndMessage implements Message{
    private final Instant timestamp;
    private final String sender;

    /**
     * Constructor to create an End Message
     * @param i the time the End Message is sent at
     * @param s the sender of the End Messsage, typically the Monitor
     */
    public EndMessage(Instant i, String s){
        this.timestamp = i;
        this.sender = s;
    }
    public Instant getTimestamp() {
        return this.timestamp;
    }
    public String getSender() {
        return this.sender;
    }
}
