package objects.Event;

import java.time.Instant;

/**
 * Represents an event which occurs during the execution of a simulation.
 * Implemented classes define specific types of events, handling the arrival and departure of cars and metadata around them.
 */
public interface Event {
    /**
     * @return the timestamp that a given event was created with.
     */
    Instant getTimestamp();
}

