package objects.Event;

import java.time.Instant;

/**
 * An implementation of an event which is placed in an event queue every hour, signifying when new Arrival Events will be generated
 * and placed in the event queue. Contains an arrival rate which describes how many cars are expected to arrive in an hour.
 */
public class GenEvent implements Event{
    private final Instant timestamp;
    private final double arrivalRate;

    /**
     * Constructor for creating a Generator Event.
     * @param stamp the time the Generator Event takes place.
     * @param arrivalRate the amount of cars that arrive at the station per hour.
     */
    public GenEvent(Instant stamp, double arrivalRate){
        this.timestamp = stamp;
        this.arrivalRate = arrivalRate;
    }

    public Instant getTimestamp(){
        return timestamp;
    }

    /**
     * @return the amount of cars that arrive at the station per hour.
     */
    public double getArrivalRate(){ return arrivalRate; }
}
