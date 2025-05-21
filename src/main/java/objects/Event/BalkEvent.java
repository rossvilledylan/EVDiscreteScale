package objects.Event;

import java.time.Instant;

/**
 * An implementation of an event which depicts a vehicle leaving a charging station after Balking.
 * Balking refers to when an arrival event is accepted into a Station off of its event queue, but is not placed onto a charger
 * after an arbitrary length of time. At that point the vehicle "decides" to leave and try another station without being charged at
 * its original station.
 */
public class BalkEvent implements Event{
    private final Instant timestamp;
    private final ArrivalEvent eventToLeave;

    /**
     * Constructor for creating a Balk Event.
     * @param i the time that an Arrival Event decides to leave a Simulator.
     * @param a the Arrival Event that is leaving the Simulator.
     */
    public BalkEvent(Instant i, ArrivalEvent a){
        this.timestamp = i;
        this.eventToLeave = a;
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }

    /**
     * @return the Arrival Event that is leaving the Simulator.
     */
    public ArrivalEvent getEventToLeave(){
        return this.eventToLeave;
    }
}
