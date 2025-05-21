package execution;

import objects.GlobalTime;
import objects.Message.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileWriter;

import java.util.concurrent.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

/**
 * Main class that handles setup and execution of Simulation.
 */
public class Main {
    /**
     * Reads from the config.json file to determine the runtime of the Simulation and the locations of each station file
     * from which particular data about the Stations to be simulated can be found. The main uses an Executor Service to
     * spawn off the Monitor process, the Monitor's message queues that go between the Monitor and Stations, and each Station
     * as its own spawned process. After spawning off all necessary processes, the main waits on all processes to finish.
     * The main also times the execution and records that value in a file after execution.
     * @param args the arguments from command line. Not currently relevant.
     */
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        //Master config file that points to individual station configs that are passed to stations
        //
        try {
            InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("config/config.json");
            if(inputStream == null){
                throw new IOException("Config file not found in resources");
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(inputStream);
            String[] configFilesList = rootNode.get("configFiles").traverse(mapper).readValueAs(String[].class);
            GlobalTime gT = new GlobalTime(rootNode.get("startTimeHr").asInt(), rootNode.get("startTimeMin").asInt(), rootNode.get("startTimeSec").asInt(), rootNode.get("runtime").asInt());

            //Serial version
            long startTime = System.nanoTime();
            /*for (int i = 0; i<configFilesList.length; i++){
                StationSimulator b = new StationSimulator(configFilesList[i], gT);
            }*/
            long endTime = System.nanoTime();
            long serialDuration = endTime-startTime;

            //Parallel Version
            ConcurrentHashMap<String, BlockingQueue<Message>> monitorToStationQueues = new ConcurrentHashMap<>();
            BlockingQueue<Message> stationToMonitorQueue = new LinkedBlockingQueue<>();
            startTime = System.nanoTime();
            executor.submit(() -> {
                Thread.currentThread().setName("Monitor");
                new Monitor(gT, stationToMonitorQueue, monitorToStationQueues);
            });
            for (String config : configFilesList){
                inputStream = Main.class.getClassLoader().getResourceAsStream("config/"+config);
                if(inputStream == null){
                    throw new IOException("Station config file not found in resources");
                }
                rootNode = mapper.readTree(inputStream);
                BlockingQueue<Message> monitorToStationQueue = new LinkedBlockingQueue<>();
                String stationName = rootNode.get("name").asText();
                monitorToStationQueues.put(stationName,monitorToStationQueue);
                executor.submit(() -> {
                    Thread.currentThread().setName(stationName);
                    new StationSimulator(config, gT, stationToMonitorQueue, monitorToStationQueue);
                });
            }
            executor.shutdown(); // Stop accepting new tasks
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS); // Wait for all tasks to complete
                endTime = System.nanoTime();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            long pDuration = endTime-startTime;
            FileWriter writer = new FileWriter("out/simulatorReport.txt");
            writer.write("The Serial Version took " + serialDuration + " nanoseconds\n");
            writer.write("The Parallel Version took " + pDuration + " nanoseconds");
            writer.close();
            float time = (float) pDuration / 1000000000;
            //System.out.println(time);
        }catch (IOException e){
            System.out.println("The config file cannot be found");
        }

    }
}