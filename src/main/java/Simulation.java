import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import static java.nio.file.Paths.get;

/**
 * The main class which will run the simulation
 */

public class Simulation {

    static int vehicleRate = 0;
    static SmartCamera camera ;

    /**
     * Main Funtion to run
     * @param args
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, JSONException, InterruptedException {
        readConfigurationFile(args);
        runSimulation();


    }

    /**
     * Function to read Configuration data whether it's a file or strings passed from the command line
     * @param args
     * @throws IOException
     * @throws JSONException
     */
    public static void readConfigurationFile(String[] args) throws IOException, JSONException {
        ArrayList<String> camConfig = new ArrayList();

        //If args is only 1 item, assume it's path to the configuration file
        if(args.length == 1){
            ObjectMapper mapper = new ObjectMapper();
            JSONArray config =mapper.readValue(new File(String.valueOf(get(args[0]))), JSONArray.class);
            camConfig.add(config.getString(0));
            camConfig.add(config.getString(1));
            camConfig.add(config.getString(2));
            camConfig.add(config.getString(3));

            camera = SmartCamera.setupCamera((String[]) camConfig.toArray());
            vehicleRate = config.getInt(4);

        }
        //If args is 5 then it's all the configuration passed through the CMD
        else if(args.length == 5){

            camera = SmartCamera.setupCamera(new String[]{args[0],args[1],args[2],args[3]});
            vehicleRate = Integer.parseInt(args[4]);

        }
    }

    /**
     * Function to run the simulation which will output the rate per car per minute
     *
     * @throws InterruptedException
     * @throws IOException
     */
    public static void runSimulation() throws InterruptedException, IOException, JSONException {
        for (int x = 0;x<3;x++){
            for(int y = 0 ; y<vehicleRate;y++){
                Random rn = new Random();
                Vehicle vehicle = new Vehicle();
                vehicle.setRegistration("ABCDEF");
                vehicle.setCamera(camera);
                vehicle.setType("Car");
                vehicle.setSpeed(rn.nextInt((200-20)+1)+20);
                camera.captureCar(vehicle);
            }
            Thread.sleep(60*1000);
        }
    }
}
