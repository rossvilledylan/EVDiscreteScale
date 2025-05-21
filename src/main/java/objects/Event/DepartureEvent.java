package objects.Event;

import java.time.Instant;

/**
 * An implementation of an event which depicts a vehicle leaving a charging station after being placed on a charger.
 * This comes with certain data which informs the Station how to track how many successful charges were made.
 */
public class DepartureEvent implements Event {
    private final Instant timestamp;
    private final Instant arrivalTime;
    private final Instant serviceTime;
    private final String chargeType;
    private final String status; // Explains if the car was fully charged, thrown out due to impatience, etc. There are three statuses: Uncharged, Partially Charged and Fully Charged

    /**
     * Constructor for creating a Departure Event.
     * @param stamp the time the car leaves the charging station.
     * @param arrivalTime the time the car originally arrived at the station.
     * @param serviceTime the time the car was placed onto a charger and began receiving energy.
     * @param chargeType the type of charge that a car desires, either "fast" or "slow".
     * @param status the state of a car as it leaves the station; the status can be "Uncharged", "Partially Charged", or "Fully Charged" based on the calculations of the Simulator.
     */
    public DepartureEvent(Instant stamp, Instant arrivalTime, Instant serviceTime, String chargeType, String status){
        this.timestamp = stamp;
        this.arrivalTime = arrivalTime;
        this.serviceTime = serviceTime;
        this.chargeType = chargeType;
        this.status = status;
    }

    public Instant getTimestamp(){
        return timestamp;
    }

    /**
     * @return the time the car originally arrived at the station.
     */
    public Instant getArrivalTime() { return arrivalTime; }

    /**
     * @return the time the car was placed onto a charger and began receiving energy.
     */
    public Instant getServiceTime() { return serviceTime; }

    /**
     * @return the type of charge that a car desires, either "fast" or "slow".
     */
    public String getChargeType() { return chargeType; }

    /**
     * The Departure Event's status determines how it is recorded in the statistics taken by every Simulator.
     * @return the state of a car as it leaves the station; the status can be "Uncharged", "Partially Charged", or "Fully Charged" based on the calculations of the Simulator.
     */
    public String getStatus() { return status; }
}
