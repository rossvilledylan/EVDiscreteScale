package objects;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A class that keeps track of the Global Simulated Time. This is necessary to inform the Stations and the Monitor of the
 * exact moment the Simulation is meant to start and to end, in terms of simulated time. The Global Time object also tracks
 * the Global Minimum time, as it is an object shared by all Stations and the Monitor.
 */
public class GlobalTime {
    private final Instant startInstant;
    private final Instant endInstant;
    private Instant globalMinimumTime;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Constructor to create a Global Time object. The simulation time will start at the exact moment of real time the
     * Simulation is started at, then run for "runtime" seconds of simulated time.
     * @param runtime the simulated time, in seconds, the simulator is run for.
     */
    public GlobalTime(int runtime){
        this.startInstant = LocalDateTime.now().toInstant(ZoneOffset.UTC);
        this.endInstant = startInstant.plusSeconds(runtime);
        this.globalMinimumTime = this.startInstant;
    }

    /**
     * Constructor to create a Global Time object. Global Time will start a specified hour, minute and second of the day
     * of running's date, then continue for a specified runtime, given in seconds.
     * @param hour the hour the simulation starts at.
     * @param minute the minute the simulation starts at.
     * @param second the second the simulation starts at.
     * @param runtime the simulated time, in seconds, the simulator is to run for.
     */
    public GlobalTime(int hour, int minute, int second, int runtime) {
        LocalDateTime currentDate = LocalDateTime.now(); // get current date
        LocalDateTime customTime = LocalDateTime.of(
                currentDate.getYear(),
                currentDate.getMonth(),
                currentDate.getDayOfMonth(),
                hour,
                minute,
                second
        );
        this.startInstant = customTime.toInstant(ZoneOffset.UTC); // Convert to Instant
        this.endInstant = startInstant.plusSeconds(runtime);
        this.globalMinimumTime = this.startInstant;
    }

    /**
     * @return the instant the Simulation begins at.
     */
    public Instant getStartInstant() {
        return startInstant;
    }

    /**
     * @return the elapsed time, in seconds, the simulation has been running for.
     */
    public long getElapsedSeconds() {
        return Instant.now().getEpochSecond() - startInstant.getEpochSecond();
    }

    /**
     * @return the instant the Simulation will end at.
     */
    public Instant getEndInstant(){
        return endInstant;
    }

    /**
     * Function to return the Global Minimum Time. Locks when a Station or the Monitor is trying to read it.
     * @return the Global Minimum Time.
     */
    public Instant getGlobalMinimumTime(){
        lock.readLock().lock();
        try {
            return globalMinimumTime;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Function to set a new Global Minimum Time. Locks when the Monitor is trying to edit it.
     * @param time the new Global Minimum Time.
     */
    public void setGlobalMinimumTime(Instant time){
        lock.writeLock().lock();
        try{
            this.globalMinimumTime = time;
        } finally {
            lock.writeLock().unlock();
        }
    }
}
