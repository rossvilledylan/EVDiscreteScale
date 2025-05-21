package execution;

import objects.*;
import objects.Event.ArrivalEvent;
import objects.Message.BalkMessage;
import objects.Message.EndMessage;
import objects.Message.Message;
import objects.Message.TimingMessage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.HashMap;
import java.util.Collections;

/**
 * This class acts as a central "hub" managing and observing the Station Simulator objects. It keeps a track of the Global
 * Minimum Time, which is the lowest time that all Stations have reached, and is in charge of ending the simulation when all
 * Stations report that they reach the Global End Time. The Monitor also handles moving Arrival Events between stations when
 * one balks.
 */
public class Monitor {
    private final GlobalTime gT;
    private final BlockingQueue<Message> stationToMonitorQueue;
    private final ConcurrentHashMap<String, BlockingQueue<Message>> monitorToStationQueues;
    private final HashMap<String, Instant> stationTimesheet;
    private final HashMap<ArrivalEvent, String> eventMapping; //This hashmap tracks where *arrival events* specifically are sent when a balk message is received.
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

    /**
     * Constructor function to create a Monitor object.
     * @param gT the Global Time object which tracks the beginning and end time of the simulation.
     * @param s the Blocking Queue used by all Stations to communicate with the Monitor
     * @param m a hashmap of message queues associating the names of stations to the queue that they use to listen to messages from the Monitor.
     */
    public Monitor (GlobalTime gT, BlockingQueue<Message> s, ConcurrentHashMap<String, BlockingQueue<Message>> m){
        this.gT = gT;
        this.stationToMonitorQueue = s;
        this.monitorToStationQueues = m;
        this.stationTimesheet = new HashMap<>();
        this.eventMapping = new HashMap<>();
        monitorLoop();
    }

    /**
     * The main loop that keeps the Monitor running. This loop checks for messages from the shared Station to Monitor Queue, then
     * handles those messages based on their type.
     * For timing messages, the Monitor updates its record of the Station sending that timing message's time. It then recalculates
     * the Minimum Global Time, and if that time changed, modifies it in the Global Time object.
     * For Balking Messages, the Monitor first determines the type of Balking message, then handles accordingly. In both cases
     * it sends a message to a Station informing that Station of where to back up to.
     * The Monitor also decides when the simulation is finished, and sends a special message out to all Stations to tell
     * them the stop.
     */
    public void monitorLoop(){
        try {
            while(gT.getGlobalMinimumTime().isBefore(gT.getEndInstant()) || checkMessages()){
                Message msg = stationToMonitorQueue.take();
                if(msg instanceof TimingMessage) {
                    stationTimesheet.put(msg.getSender(), msg.getTimestamp()); //This ensures that a station essentially adds itself, as the first action a station takes in its event loop is to send a message to the monitor
                    Instant nextMinGlobalTime = Collections.min(stationTimesheet.values());
                    if (nextMinGlobalTime != null && !gT.getGlobalMinimumTime().equals(nextMinGlobalTime)) {
                        gT.setGlobalMinimumTime(nextMinGlobalTime);
                        //System.out.println("From the Monitor: global timesheet is as follows:\n" + stationTimesheet.keySet() + "\n" + stationTimesheet.values() + " " + minGlobalTime);
                    }
                }else if (msg instanceof BalkMessage){
                    if(((BalkMessage) msg).getRetread()) { //This handles messages needing to be re-done if a station backtracks
                        String stationToBacktrack = eventMapping.get(((BalkMessage) msg).getEventToLeave());
                        monitorToStationQueues.get(stationToBacktrack).add(msg);
                    }else {
                        ArrayList<String> keys = new ArrayList<>(monitorToStationQueues.keySet());
                        keys.remove(msg.getSender());
                        if(!keys.isEmpty()) {
                            String nextStation = keys.get(random.nextInt(keys.size()));
                            monitorToStationQueues.get(nextStation).add(msg);
                            eventMapping.put(((BalkMessage) msg).getEventToLeave(), nextStation);
                        }
                        //If there is only one station, the car simply leaves and does not get charged.
                    }
                }
            }
            //System.out.println("All stations have reached end of time\n" + gT.getEndInstant() + "\n" + stationTimesheet.values() + "\n" + monitorToStationQueues.keySet());
            for(BlockingQueue<Message> q : monitorToStationQueues.values())
                q.add(new EndMessage(this.gT.getEndInstant(), "Monitor")); //The monitor will only send an End Message when it ends. It uses a special kind of Message so that the Simulators know to continue running even after they get a minTime that is at/after the global end time
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }catch (Exception e){
            System.out.println("Monitor has exception " + e);
        }
    }

    /**
     * This function checks the queue between the Monitor and every Station to determine if any Station has a message it has
     * not yet handled.
     * @return false if there are no outstanding messages to any Station, and true if there are.
     */
    public boolean checkMessages(){//ensures that there are no outstanding messages in each station's queue
        if(monitorToStationQueues.isEmpty())
            return true;
        for (BlockingQueue<Message> q : monitorToStationQueues.values())
            if(!q.isEmpty())
                return true;
        return false;
    }
}
