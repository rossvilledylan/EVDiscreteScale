package objects;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A class to keep track of all the statistics a user may be interested in concerning a single Station.
 * Breaks up the number of charges by their type and if they were completed. Also tracks the number of times an event balked
 * and how many times the station had to backtrack due to an event balking at a different station.
 */
public class StationStats {
    private String stationName;
    private int numFullFastCharges;
    private int numFullSlowCharges;
    private int numPartialFastCharges;
    private int numPartialSlowCharges;
    private int numNoFastCharges;
    private int numNoSlowCharges;
    private int numFaskBalks;
    private int numSlowBalks;
    private int numBacktracks;

    /**
     * Constructor to create a Station Stats object. All stats are set to zero at the beginning of the simulation.
     */
    public StationStats(){
        this.numFullFastCharges = 0;
        this.numFullSlowCharges = 0;
        this.numPartialFastCharges = 0;
        this.numPartialSlowCharges = 0;
        this.numNoFastCharges = 0;
        this.numNoSlowCharges = 0;
        this.numFaskBalks = 0;
        this.numSlowBalks = 0;
        this.numBacktracks = 0;
    }

    /**
     * @return the number of charges on a fast charger that received the full amount of desired energy.
     */
    public int getNumFullFastCharges() {
        return numFullFastCharges;
    }

    /**
     * @return the number of charges on a slow charger that received the full amount of desired energy.
     */
    public int getNumFullSlowCharges(){
        return numFullSlowCharges;
    }

    /**
     * @return the number of charges on a fast charger that received less than, but more than none of, the full amount
     * of desired energy.
     */
    public int getNumPartialFastCharges() {
        return numPartialFastCharges;
    }

    /**
     * @return the number of charges on a slow charger that received less than, but more than none of, the full amount
     * of desired energy.
     */
    public int getNumPartialSlowCharges() {
        return numPartialSlowCharges;
    }

    /**
     * @return the number of charges on a fast charger that received none of the desired energy.
     */
    public int getNumNoFastCharges() {
        return numNoFastCharges;
    }

    /**
     * @return the number of charges on a slow charger that received none of the desired energy.
     */
    public int getNumNoSlowCharges(){
        return numNoSlowCharges;
    }

    /**
     * @return the number of events that desired a fast charge but left before getting onto a charger.
     */
    public int getNumFaskBalks() {
        return numFaskBalks;
    }

    /**
     * @return the number of events that desired a slow charge but left before getting onto a charger.
     */
    public int getNumSlowBalks() {
        return numSlowBalks;
    }

    /**
     * @return the number of times the station had to backtrack in order to accommodate an event which arrived from a
     * different station.
     */
    public int getNumBacktracks(){
        return numBacktracks;
    }

    /**
     * Sets the station name for this stat-taking object.
     * @param stationName the new name of the station being tracked.
     */
    public void setStationName(String stationName){
        this.stationName = stationName;
    }

    /**
     * @param numFullFastCharges the number of charges on a fast charger that received the full amount of desired energy.
     */
    public void setNumFullFastCharges(int numFullFastCharges){
        this.numFullFastCharges = numFullFastCharges;
    }

    /**
     * @param numFullSlowCharges the number of charges on a slow charger that received the full amount of desired energy.
     */
    public void setNumFullSlowCharges(int numFullSlowCharges) {
        this.numFullSlowCharges = numFullSlowCharges;
    }

    /**
     * @param numPartialFastCharges the number of charges on a fast charger that received less than, but more than none of, the full amount
     * of desired energy.
     */
    public void setNumPartialFastCharges(int numPartialFastCharges) {
        this.numPartialFastCharges = numPartialFastCharges;
    }

    /**
     * @param numPartialSlowCharges the number of charges on a slow charger that received less than, but more than none of, the full amount
     * of desired energy.
     */
    public void setNumPartialSlowCharges(int numPartialSlowCharges) {
        this.numPartialSlowCharges = numPartialSlowCharges;
    }

    /**
     * @param numNoFastCharges the number of charges on a fast charger that received none of the desired energy.
     */
    public void setNumNoFastCharges(int numNoFastCharges) {
        this.numNoFastCharges = numNoFastCharges;
    }

    /**
     * @param numNoSlowCharges the number of charges on a slow charger that received none of the desired energy.
     */
    public void setNumNoSlowCharges(int numNoSlowCharges) {
        this.numNoSlowCharges = numNoSlowCharges;
    }

    /**
     * @param numFaskBalks the number of events that desired a fast charge but left before getting onto a charger.
     */
    public void setNumFaskBalks(int numFaskBalks) {
        this.numFaskBalks = numFaskBalks;
    }

    /**
     * @param numSlowBalks the number of events that desired a slow charge but left before getting onto a charger.
     */
    public void setNumSlowBalks(int numSlowBalks) {
        this.numSlowBalks = numSlowBalks;
    }

    /**
     * @param numBacktracks the number of times the station had to backtrack in order to accommodate an event which arrived from a
     * different station.
     */
    public void setNumBacktracks(int numBacktracks){
        this.numBacktracks = numBacktracks;
    }

    /**
     * Prints the statistics captured during the runtime of a station to a file.
     */
    public void printStats(){
        try {
            FileWriter writer = new FileWriter("out/" + stationName + ".txt");
            writer.write("At this station, there were:\n");
            writer.write(this.numFullFastCharges + " fast charges that received all desired energy\n");
            writer.write(this.numFullSlowCharges + " slow charges that received all desired energy\n");
            writer.write(this.numPartialFastCharges + " fast charges that received some desired energy\n");
            writer.write(this.numPartialSlowCharges + " slow charges that received some desired energy\n");
            writer.write(this.numNoFastCharges + " fast charges that received no energy\n");
            writer.write(this.numNoSlowCharges + " slow charges that received no energy\n");
            writer.write(this.numFaskBalks + " fast charges that got impatient\n");
            writer.write(this.numSlowBalks + " slow charges that got impatient\n");
            writer.write(this.numBacktracks + " times backtracked\n");
            writer.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
