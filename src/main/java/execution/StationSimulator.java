package execution;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.math3.distribution.BetaDistribution;
import objects.*;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import objects.Event.*;
import objects.Message.BalkMessage;
import objects.Message.EndMessage;
import objects.Message.Message;
import objects.Message.TimingMessage;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.special.Beta;
import org.apache.commons.math3.special.Gamma;

/**
 * The Station Simulator class does the most work out of all the classes. It represents a single Charging Station within the
 * simulation, complete with its own arrival rate, concept of time and message queue to communicate with the Monitor.
 * The Station Simulator is in charge of creating and perpetuating the simulation that is described to it; it uses a single
 * loop to handle a number of events. It also keeps track of certain statistics about the running of these events. The Station
 * Simulator maintains communication with the Monitor to send Arrival Events which have balked to other Stations.
 */
public class StationSimulator {
    private Queue<Event> eventQueue = new PriorityQueue<>(
            (e1, e2) -> e1.getTimestamp().compareTo(e2.getTimestamp())
    ); //This is a priority queue for any kind of event
    private Queue<Event> historyQueue = new PriorityQueue<>(
            (e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp())
    ); //This is a priority queue that tracks the arrival and balk events that have occurred over the course of the simulation. it is REVERSED
    //private ChargingStation station;
    private String stationName;
    private int fastChargers;
    private int fastInUse;
    private int slowChargers;
    private int slowInUse;
    private double fastChargingRate;
    private double slowChargingRate;
    private Queue<ArrivalEvent> fastQueue = new PriorityQueue<>(
            (e1, e2) -> e1.getTimestamp().compareTo(e2.getTimestamp())
    );
    private Queue<ArrivalEvent> slowQueue = new PriorityQueue<>(
            (e1, e2) -> e1.getTimestamp().compareTo(e2.getTimestamp())
    );
    private BlockingQueue<Message> stationToMonitorQueue;
    private BlockingQueue<Message> monitortoStationQueue;
    private GlobalTime gT;
    private Instant stationTime;
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();
    private StationStats sS = new StationStats();
    private GammaDistribution energyDistribution;
    private BetaDistribution timeOfDayDistribution;

    /**
     * Constructor to create a Station Simulator. Reads data from the config file in order to set up a ChargingStation object,
     * which is used to simulate the charging of cars. Also utilizes data from the config file to specialize its simulation,
     * such as the arrival rate, whether to use limited energy mechanics, and the unique name of the Station.
     * @param config the JsonNode which contains all config data from the config file
     * @param gT the Global Time object.
     * @param smQ the message queue which goes from all Stations to the Monitor.
     * @param msQ the message queue which goes from the Monitor to this Station.
     */
    public StationSimulator(JsonNode config, GlobalTime gT, BlockingQueue<Message> smQ, BlockingQueue<Message> msQ){
        this.gT = gT;
        stationTime = gT.getStartInstant();
        try {
            stationName = config.get("name").asText();
            sS.setStationName(stationName);
            fastChargers = config.get("fastChargers").asInt();
            slowChargers = config.get("slowChargers").asInt();
            fastChargingRate = config.get("fastChargingRate").asDouble();
            slowChargingRate = config.get("slowChargingRate").asDouble();

            stationToMonitorQueue = smQ;
            monitortoStationQueue = msQ;

            GenEvent c = new GenEvent(gT.getStartInstant(), config.get("arrivalRate").asInt()); //Arrival rate is cars per hour

            energyDistribution = new GammaDistribution(2.3127598129490075, 3.870663519530382);
            timeOfDayDistribution = new BetaDistribution(4.614972052581306, 3.805085312822052); //

            eventQueue.add(c);
            eventLoop();
            //System.out.println(stationName + " has finished\n" + eventQueue + "\n" + monitortoStationQueue + "\nFast in use: " + fastInUse + "\nSlow in use: " + slowInUse);
            sS.printStats();

        } catch(Error e){
            System.out.println("The requested station file does not exist");
        }
    }


    /**
     * Depreciated constructor to create a Station Simulator, used for creating a Serial simulator. No longer functions
     * with other functions in the file, and does not interact with Monitor.
     * @param name the Station's name.
     * @param types the names of the types of energy the Station can use.
     * @param sources the potential sources from which the Station can draw energy.
     * @param energyAmount the amounts of energy the Station contains, ordered by its type.
     * @param arrivalRate the arrival rate of cars to the station.
     * @param gT the Global Time object.
     */
    public StationSimulator(String name, int[] types, String[] sources, double[][] energyAmount, double arrivalRate, GlobalTime gT){ //Can pass arguments now, I guess
        this.gT = gT;
        stationTime = gT.getStartInstant(); //Set the station's current time

        fastChargers = types[0];
        slowChargers = types[1];
        String[] kinds = new String[types[0]+types[1]];
        for (int i = 0; i < fastChargers; i++)
            kinds[i] = "fast";
        for (int i = fastChargers; i < kinds.length; i++)
            kinds[i] = "slow";

        GenEvent c = new GenEvent(gT.getStartInstant(), arrivalRate); //Create our first eventGenerator event
        eventQueue.add(c);
        eventLoop(); //Begin the event loop
    }

    /**
     * Primary event loop of the Simulator. Iterates through a queue of events, handling each event according to its type.
     * At the end of each iteration, checks for messages from the Monitor, and acts on those messages. When the event queue
     * is empty, the event loop will hold for messages from the Monitor. When an End Message is reached, the loop is broken.
     */
    public void eventLoop(){
        try {
            while(true) {
                while (!eventQueue.isEmpty()) {
                    Event e = eventQueue.remove();
                    if (e instanceof GenEvent & this.stationTime.isBefore(this.gT.getEndInstant())) { //"isBefore" can be used to check if time is semantically before
                        this.stationTime = e.getTimestamp();
                        genEvents(((GenEvent) e).getArrivalRate());
                    } else if (e instanceof ArrivalEvent) {
                        handleArrivalEvent((ArrivalEvent) e);
                    } else if (e instanceof DepartureEvent) {
                        handleDepartureEvent((DepartureEvent) e);
                        switch (((DepartureEvent) e).getStatus()) {
                            case "Uncharged":
                                if (((DepartureEvent) e).getChargeType().equals("fast"))
                                    sS.setNumNoFastCharges(sS.getNumNoFastCharges() + 1);
                                else if (((DepartureEvent) e).getChargeType().equals("slow"))
                                    sS.setNumNoSlowCharges(sS.getNumNoSlowCharges() + 1);
                                break;
                            case "Partially Charged":
                                if (((DepartureEvent) e).getChargeType().equals("fast"))
                                    sS.setNumPartialFastCharges(sS.getNumPartialFastCharges() + 1);
                                else if (((DepartureEvent) e).getChargeType().equals("slow"))
                                    sS.setNumPartialSlowCharges(sS.getNumPartialSlowCharges() + 1);
                                break;
                            case "Fully Charged":
                                if (((DepartureEvent) e).getChargeType().equals("fast"))
                                    sS.setNumFullFastCharges(sS.getNumFullFastCharges() + 1);
                                else if (((DepartureEvent) e).getChargeType().equals("slow"))
                                    sS.setNumFullSlowCharges(sS.getNumFullSlowCharges() + 1);
                                break;
                        }
                    }
                    //Here we check for messages from the Monitor
                    if (!monitortoStationQueue.isEmpty()){
                        Message msg = monitortoStationQueue.take();
                        if (msg instanceof BalkMessage){
                            backtrack(((BalkMessage) msg));
                            //eventQueue.add(((BalkMessage) msg).getBalkEvent());
                        } else if (msg instanceof EndMessage){
                            monitortoStationQueue.add(msg); //Have to make sure the end message does not get lost
                            //System.out.println(stationName + " got a premature EndMessage");
                        }
                    }
                    stationToMonitorQueue.add(new TimingMessage(this.stationTime, this.stationName));
                    //System.out.println(this.stationName + " is operating in the eventloop\n" + eventQueue + "\nFast in use: " + fastInUse + "\nSlow in use: " + slowInUse);
                }
                //System.out.println(stationName + " has exited the eventloop\n"+monitortoStationQueue + "\n" + stationTime + "\n" + minGlobalTime + "\n" + gT.getEndInstant());
                if(monitortoStationQueue.isEmpty()) //Ensure the simulator is only "done" if its event queue AND its message queue are empty
                    stationToMonitorQueue.add(new TimingMessage(gT.getEndInstant(),this.stationName)); //Ensure the monitor knows we're done
                else {
                    Message msg = monitortoStationQueue.take();
                    if (msg instanceof BalkMessage) {
                        backtrack(((BalkMessage) msg));
                    } else if (msg instanceof EndMessage)
                        return;
                }
            }
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    /**
     * This function handles the creation of events. It takes an arrival rate, then calculates an average enter-arrival time,
     * which is used to decide, with a degree of randomness, when each event will be placed in the Event Queue. The properties
     * of every Arrival Event are described here as well, detailing their battery life, the amount of energy they want, and
     * what kind of charge they want.
     * @param arrivalRate the arrival rate of the current Station. This value is best described as the number of cars
     *                    that arrive per day.
     */
    public void genEvents(double arrivalRate){
        long dayInSeconds = 86400;
        for (int i = 0; i< arrivalRate; i++){
            double arrivalTime = -0.042 + timeOfDayDistribution.sample() * 1.110;
            arrivalTime = Math.max(0.0, Math.min(1.0, arrivalTime));
            long secondsIntoDay = (long) (arrivalTime * dayInSeconds);

            Instant currentTime = this.stationTime.plusSeconds(secondsIntoDay);
            double remaining = energyDistribution.sample() * 1000.0;
            eventQueue.add(new ArrivalEvent(currentTime, random.nextDouble() < 0.67 ? "fast" : "slow",remaining));
        }
        GenEvent e = new GenEvent(this.stationTime.plusSeconds(dayInSeconds), arrivalRate);
        eventQueue.add(e);
    }

    /**
     * Handles a given Arrival Event when it reaches the front of the Event Queue. Determines the type of charge desired,
     * then if the event will have to wait for a charger. If all charging slots are filled, then it is placed on a waiting queue.
     * If not, it is placed onto a charging slot, its presence is backed up into the history queue, and the number of relevant
     * charging slots in use is incremented.
     * @param a the Arrival Event that is being handled.
     */
    public void handleArrivalEvent(ArrivalEvent a){
        this.stationTime = a.getTimestamp();
        if(a.getChargeType().equals("fast")){
            if(fastInUse >= fastChargers) {
                fastQueue.add(a);
            }
            else {
                if(a.getTimestamp().plusSeconds(600).isBefore(gT.getEndInstant())) { //also check the event will finish before the simulation closes
                    fastInUse++;
                    historyQueue.add(a); //Since this is going on the charger, add it to the history queue
                    startCharge(a);
                }
            }
        }
        else if(a.getChargeType().equals("slow")){
            if(slowInUse >= slowChargers) {
                slowQueue.add(a);
            }
            else {
                if(a.getTimestamp().plusSeconds(1800).isBefore(gT.getEndInstant())) { //also check the event will finish before the simulation closes
                    slowInUse++;
                    historyQueue.add(a); //Since this is going on the charger, add it to the history queue
                    startCharge(a);
                }
            }
        }
    }

    /**
     * Handles a given Departure Event when it reaches the front of the Event Queue. This function is responsible for
     * maintaining the state of the fast and slow queues, as well as the statistics being recorded for the Station.
     * When a car leaves the Station, the function will first check if there is a car on the relevant queue (fast/slow) that is
     * waiting for a slot; it will then check to see if an arbitrary amount of time has passed according to the Station's
     * time compared to the time the car arrived; if the car has "waited" too long, it will Balk and leave the station.
     * Otherwise, the waiting car will be given a charger and placed on the history queue. If there are no cars waiting or
     * all have balked, then the number of relevant charging slots in use is decremented.
     * This function also handles the majority of stats-taking, recording the number and state of each charge type.
     * @param d the Departure Event that is being handled.
     */
    public void handleDepartureEvent(DepartureEvent d){
        this.stationTime = d.getTimestamp();
        if(d.getChargeType().equals("fast")){
            // Take an event off the fast queue and put it on the charger
            if(!fastQueue.isEmpty()) {
                //This if statement is an "impatience" function that balks at 10 minutes
                ArrivalEvent a = fastQueue.peek();
                while(!fastQueue.isEmpty() && !a.getTimestamp().plusSeconds(600).isAfter(this.stationTime)) {
                    stationToMonitorQueue.add(new BalkMessage(this.stationTime,this.stationName,fastQueue.remove(),false));
                    historyQueue.add(new BalkEvent(this.stationTime, a)); //Add this event to the history queue as an event that left the station
                    sS.setNumFaskBalks(sS.getNumFaskBalks() + 1);
                    if(!fastQueue.isEmpty()){
                        a = fastQueue.peek();
                    }
                }
                if(!fastQueue.isEmpty()){
                    a = fastQueue.remove();
                    if(a.getTimestamp().plusSeconds(600).isBefore(gT.getEndInstant())) { //also check the event will finish before the simulation closes
                        historyQueue.add(a); //Since this is going on the charger, add it to the history queue
                        startCharge(a);         // Start charging
                    }
                    else {
                        fastInUse--;
                    }
                }
                else {
                    fastInUse--;
                }
            }
            else {
                fastInUse--;
            }
        }
        if(d.getChargeType().equals("slow")){
            if (!slowQueue.isEmpty()){
                // Take an event off the slow queue and put it on the charger
                ArrivalEvent a = slowQueue.peek(); // Peek to check the head without removing it
                while (!slowQueue.isEmpty() && !a.getTimestamp().plusSeconds(1800).isAfter(this.stationTime)) {
                    stationToMonitorQueue.add(new BalkMessage(this.stationTime,this.stationName,slowQueue.remove(), false));
                    historyQueue.add(new BalkEvent(this.stationTime, a)); //Add this event to the history queue as an event that left the station
                    sS.setNumSlowBalks(sS.getNumSlowBalks()+1);
                    if (!slowQueue.isEmpty()) {
                        a = slowQueue.peek(); // Update 'a' with the next event
                    }
                }
                if (!slowQueue.isEmpty()) { // Check again if the queue has a valid event
                    a = slowQueue.remove(); // Remove the valid event
                    if (a.getTimestamp().plusSeconds(1800).isBefore(gT.getEndInstant())){ //also check the event will finish before the simulation closes
                        historyQueue.add(a); //Since this is going on the charger, add it to the history queue
                        startCharge(a);         // Start charging
                    }
                    else{
                        slowInUse--;
                    }
                }
                else {
                    slowInUse--;
                }
            }
            else {
                slowInUse--;
            }
        }
    }

    /**
     * Simulates the actual charging of a car based on the information provided by an Arrival Event. Calculates the
     * amount of simulated time is necessary to charge the requested car.
     * After charging the car, a Departure Event is placed on the Event Queue to depict the car leaving the Station.
     * @param a the Arrival Event which is getting its charge.
     */
    public void startCharge(ArrivalEvent a){
        Instant departureTime = this.stationTime.plusSeconds(((long) (a.getChargeDesired() * 3600.0 / (a.getChargeType().equals("fast") ? fastChargingRate : slowChargingRate))));
        DepartureEvent b = new DepartureEvent(departureTime, a.getTimestamp(), this.stationTime, a.getChargeType(), "Fully Charged");
        sS.addEnergyGiven(a.getChargeDesired());
        eventQueue.add(b);
    }

    /**
     * Backtracking function. Resets station to the state that it was in at a given time or event. Reads events out of
     * the history queue and back into the event queue. Will also handle clearing out the history queue of events that
     * happened before the global minimum time.
     * @param balker the message that contains the Arrival Event which is being backtracked to.
     */
    public void backtrack(BalkMessage balker){
        try {
            Instant rewind = balker.getEventToLeave().getTimestamp();
            //We will have to go through the fastQueue, the slowQueue, and the history queue to add events back to the eventQueue.
            //Do fast and slow queues first, they are shorter by design
            PriorityQueue<ArrivalEvent> temporary = new PriorityQueue<>(
                    (e1, e2) -> e1.getTimestamp().compareTo(e2.getTimestamp())
            ); //Holds temporary events just in case there are any that happen before the cutoff time for the fast and slow queus
            Event a;
            while (!fastQueue.isEmpty()) {
                a = fastQueue.remove();
                if (a.getTimestamp().isAfter(rewind))
                    eventQueue.add(a);
                else
                    temporary.add((ArrivalEvent) a);
            }
            if (!temporary.isEmpty()) {
                fastQueue.addAll(temporary);
                temporary.clear();
            }
            while (!slowQueue.isEmpty()) {
                a = slowQueue.remove();
                if (a.getTimestamp().isAfter(rewind))
                    eventQueue.add(a);
                else
                    temporary.add((ArrivalEvent) a);
            }
            if (!temporary.isEmpty()) {
                slowQueue.addAll(temporary);
            }
            a = historyQueue.peek();
            // If statement ensures that there is some history to go back to. Otherwise throws exceptions :/
            // Break instruction ensures that loop is broken when the history queue is empty.
            if(a!=null) {
                while (a.getTimestamp().isAfter(balker.getTimestamp())) {
                    a = historyQueue.remove();
                    if (a instanceof ArrivalEvent) {
                        eventQueue.add(a);
                        sS.subtractEnergyGiven(((ArrivalEvent) a).getChargeDesired());
                    } else if (a instanceof BalkEvent) {
                        monitortoStationQueue.add(new BalkMessage(a.getTimestamp(),this.stationName,((BalkEvent) a).getEventToLeave(),true));
                        //if(this.stationName.equals("Station B"))
                            //System.out.println("B\n" + "Balking Event: " + ((BalkEvent) a) + "\nArrival Event: " + ((BalkEvent) a).getEventToLeave());
                    }
                    if (!historyQueue.isEmpty())
                        a = historyQueue.peek();
                    else
                        break;
                }
            }
            sS.setNumBacktracks(sS.getNumBacktracks() + 1);
            //If this balkMessage is backtracking to an event because that event is being re-done in another station, then remove it from the queue.
            //Else, add the event to the queue, as it is an event
            if(balker.getRetread()) {
                eventQueue.remove(balker.getEventToLeave());
                historyQueue.remove(balker.getEventToLeave());
            }
            else {
                eventQueue.add(balker.getEventToLeave()); //Make sure the traveling message is put on the queue
            }
            //Now we have to make sure the number of slots in use is consistent for the time we are backtracking to
            //The idea is that any Departure events that exist in the eventQueue before the incoming time
            fastInUse = 0;
            slowInUse = 0;
            //eventQueue.removeIf(Event -> Event instanceof DepartureEvent); //Remove all departure events
            Event firstEvent = eventQueue.peek(); //Get the head of the queue to know what new start time is; a new event could come in before or after the station's current time
            Iterator<Event> iter = eventQueue.iterator();//https://stackoverflow.com/questions/18448671/how-to-avoid-concurrentmodificationexception-while-removing-elements-from-arr
            while(iter.hasNext()) {
                Event event = iter.next();
                if (event instanceof DepartureEvent) {
                    if (((DepartureEvent) event).getServiceTime().isAfter(firstEvent.getTimestamp())) //Get only departure events that depict cars that were serviced before the current station time, getting rid of any that are serviced after the new start time.
                        iter.remove();
                    else {
                        if (((DepartureEvent) event).getChargeType().equals("slow"))
                            slowInUse++;
                        else if (((DepartureEvent) event).getChargeType().equals("fast"))
                            fastInUse++;
                    }
                }
            }
            historyQueue.removeIf(ArrivalEvent -> ArrivalEvent.getTimestamp().isBefore(gT.getGlobalMinimumTime())); //remove previous events before global min time
        }catch(Exception e){
            System.out.println(stationName + " " + e);
            e.printStackTrace();
        }
    }
}
