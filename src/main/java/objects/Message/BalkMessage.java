package objects.Message;

import objects.Event.ArrivalEvent;

import java.time.Instant;

/**
 * An implementation of a message which depicts a vehicle leaving a Station, going to the monitor, and expecting to be sent to another
 * Station. These messages are essentially wrappers for the Arrival Events that want to leave the Station, and contain the
 * Arrival Event in question as a variable.
 * Balk Messages are also used to inform the Monitor of Events that had previously been sent out, but must now be recalculated due to
 * the Station it left from backtracking.
 */
public class BalkMessage implements Message{
    private final Instant timestamp;
    private final String sender;
    private final ArrivalEvent eventToLeave;
    private final boolean retread; //This boolean is set to 0 if an event is leaving the station for the first time; it is set to 1 if the event is already at another station and needs to be backtracked to and removed from that station's eventQueue

    /**
     * Constructor for creating a Balk Message.
     * @param i the time the Event in question decided to leave the Station.
     * @param s the name of the Station that the Event is leaving from.
     * @param a the Arrival Event that has decided to leave the Station.
     * @param b the status of whether the Event in question is leaving the Station for the first time, false, or if it is
     *          a record of a previous message that Balked and informing the Monitor to tell the Station that Event went to
     *          to backtrack, true.
     */
    public BalkMessage(Instant i, String s, ArrivalEvent a, boolean b){
        this.timestamp = i;
        this.sender = s;
        this.eventToLeave = a;
        this.retread = b;
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }
    public String getSender(){
        return this.sender;
    }

    /**
     * @return the Arrival Event that has decided to leave the Station.
     */
    public ArrivalEvent getEventToLeave(){
        return this.eventToLeave;
    }

    /**
     * @return the status of whether the Event in question is leaving the Station for the first time, false, or if it is
     *          a record of a previous message that Balked and informing the Monitor to tell the Station that Event went to
     *          to backtrack, true.
     */
    public boolean getRetread(){
        return this.retread;
    }
}
