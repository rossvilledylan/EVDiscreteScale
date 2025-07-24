package execution;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import objects.GlobalTime;
import objects.Message.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileWriter;

import java.util.ArrayList;
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
            String configFilesList = rootNode.get("configFile").asText();
            GlobalTime gT = new GlobalTime(rootNode.get("startTimeHr").asInt(), rootNode.get("startTimeMin").asInt(), rootNode.get("startTimeSec").asInt(), rootNode.get("runtime").asInt());

            //Parallel Version
            ConcurrentHashMap<String, BlockingQueue<Message>> monitorToStationQueues = new ConcurrentHashMap<>();
            BlockingQueue<Message> stationToMonitorQueue = new LinkedBlockingQueue<>();
            long startTime = System.nanoTime();
            long endTime = startTime;
            executor.submit(() -> {
                Thread.currentThread().setName("Monitor");
                new Monitor(gT, stationToMonitorQueue, monitorToStationQueues);
            });
            inputStream = Main.class.getClassLoader().getResourceAsStream("config/"+configFilesList);
            if(inputStream == null){
                throw new IOException("Station config file not found in resources");
            }
            rootNode = mapper.readTree(inputStream);
            JsonNode defaultConig = rootNode.get("defaultConfig");
            ArrayNode stations = (ArrayNode) rootNode.get("stations");
            ArrayList<ObjectNode> fullConfigs = new ArrayList<>();
            for (JsonNode override: stations){
                ObjectNode merged = defaultConig.deepCopy();
                override.fields().forEachRemaining(field -> merged.set(field.getKey(),field.getValue()));
                fullConfigs.add(merged);
            }
            for (ObjectNode fullConfig : fullConfigs){
                BlockingQueue<Message> monitorToStationQueue = new LinkedBlockingQueue<>();
                String stationName = fullConfig.get("name").asText();
                monitorToStationQueues.put(stationName,monitorToStationQueue);
                executor.submit(() -> {
                    Thread.currentThread().setName(stationName);
                    new StationSimulator(fullConfig, gT, stationToMonitorQueue, monitorToStationQueue);
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
            float secTime = (float) pDuration / 1000000000;
            writer.write("The Simulation took " + pDuration + " nanoseconds or " + secTime + " seconds");
            writer.close();

            //System.out.println(time);
        }catch (IOException e){
            System.out.println("The config file cannot be found");
        }catch (NullPointerException e){
            System.out.println("A parameter could not be found: " + e);
        }

    }
}