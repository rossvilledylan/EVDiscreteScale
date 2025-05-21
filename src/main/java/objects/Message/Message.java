package objects.Message;

import java.time.Instant;

/**
 * Depicts a message that is being sent either from a Station to the Monitor or to a Station from the Monitor.
 * Implemented classes define specific types of messages, handling time synchronization and Event Balking.
 */
public interface Message {
    /**
     * @return the timestamp that a given message was created with.
     */
    Instant getTimestamp();

    /**
     * @return the name of the Station, or the Monitor, which sent the message.
     */
    String getSender();
}
