package objects.Event;

import java.time.Instant;

/**
 * An implementation of an event which depicts a point in time when the energy reserves of a Station are to be recharged.
 * Recharge Events are only used when a Station Simulator has "useEnergyMechanics" set to "true" in its config file.
 * The method used to depict recharging a Station is based on the EvLib implementation.
 */
public class RechargeEvent implements Event{
    private final Instant timestamp;
    private final double[] charges;

    /**
     * Constructor for a Recharge Event.
     * @param stamp the time a Recharge Event takes place.
     * @param charges the array of values, in watt-hours, which are used to refill the station's array of sources of energy.
     */
    public RechargeEvent(Instant stamp, double[] charges){
        this.timestamp = stamp;
        this.charges = charges;
    }

    public Instant getTimestamp(){
        return timestamp;
    }

    /**
     * @return the array of values, in watt-hours, which are used to refill the station's array of sources of energy.
     */
    public double[] getCharges(){
        return charges;
    }
}
