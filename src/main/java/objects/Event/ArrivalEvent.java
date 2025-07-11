package objects.Event;

import java.time.Instant;

/**
 * An implementation of an event which depicts a vehicle arriving at a charging station.
 * This comes with certain data, informing the Simulator how to handle the event.
 */
public class ArrivalEvent implements Event {
    private final Instant timestamp;
    private final String chargeType;
    private final int chargeDesired;

    /**
     * Constructor for creating an Arrival Event. Utilizes classes from EvLib to represent the car itself and the car's battery.
     * The classes from EvLib are mostly there to interact with the portions of EvLib we are testing, namely the charging. They serve
     * no function otherwise.
     * @param stamp the time that a car arrives at the station.
     * @param chargeType the type of charge that a car desires, either "fast" or "slow".
     * @param remainingAmount the remaining amount of energy in the car's battery, measured in watt-hours.
     * @param desireAmount the amount of energy the car wants from the Station it has arrived at, measured in watt-hours.
     * @param capacityAmount the maximum amount of energy the car can hold, measured in watt-hours.
     */
    public ArrivalEvent(Instant stamp, String chargeType, int remainingAmount, int desireAmount, int capacityAmount){
        this.timestamp = stamp;
        this.chargeType = chargeType;
        this.chargeDesired = desireAmount;
    }

    public Instant getTimestamp(){
        return timestamp;
    }
    /**
     * @return the type of charge that a car desires, either "fast" or "slow".
     */
    public String getChargeType() { return chargeType;}
    /**
     * @return the amount of energy the car wants from the Station it has arrived at, measured in watt-hours.
     */
    public int getChargeDesired() { return this.chargeDesired; }
}
