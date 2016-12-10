// Include the following imports to use Service Bus APIs
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.servicebus.*;
import com.microsoft.windowsazure.services.servicebus.models.*;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
//import java.nio.file.Files;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;


/**
 * Class to represent SmartCamera
 * implements Serializable
 */

public class SmartCamera implements Serializable{

    //Private Variable declaration
    private static String tag = "camera";
    private static String CAMERA_QUEUE_TAG = "CameraMsgs";
    private static String VEHICLE_QUEUE_TAG = "VehicleMsgs";
    private static ServiceBusContract service;
    private String uid;
    private String streetName;
    private String city;
    private int speedLimit;
    private ArrayList<String[]> messages;
    Gson gson;
    /*
    *Create New SmartCamera Object
    * @params UID,StreetName,City,SpeedLimit
    * @throw IOEXceptions when reading from configuration file
    *
     */
    SmartCamera(String uid,String streetName,String city,int speedLimit) throws IOException, JSONException {

        //Assign object data
        gson = new Gson();
        this.uid = uid;
        this.streetName = streetName;
        this.city = city;
        this.speedLimit = speedLimit;
        this.messages = new ArrayList();

        JSONObject object = new JSONObject();
        object.put("uid",this.uid);
        object.put("streetName",this.streetName);
        object.put("city",this.city);
        object.put("speedLimit",this.speedLimit);


        sendMessage(object.toString(),CAMERA_QUEUE_TAG);
    }

    /**
     * Capture details of the vehicle and check if it broke the speed limit
     * @param vehicle
     * @throws IOException
     */

    public void captureCar(Vehicle vehicle) throws IOException, JSONException {

        if(vehicle.getSpeed() > this.speedLimit)
            vehicle.setOffender(true);
        else
            vehicle.setOffender(false);

        JSONObject object = new JSONObject();

        object.put("registration",vehicle.getRegistration());
        object.put("type",vehicle.getType());
        object.put("speed",vehicle.getSpeed());
        object.put("camera",vehicle.getCamera().uid);
        object.put("isOffender",vehicle.getOffender());



        sendMessage(object.toString(),VEHICLE_QUEUE_TAG);

    }

    /**
     * Connect to Azure Service Bus
     */
    private static void connectToServiceBus(){

        //Configuration paramter to connect to Azure

        Configuration config = ServiceBusConfiguration.configureWithSASAuthentication(
                "smarcamera",
                "RootManageSharedAccessKey",
                "h9zP+sPjaennM/CI3rHJzoy+ymsSGJkcJM0/csNp7Vw=",
                ".servicebus.windows.net"

        );

        //Create a connection to the service
        service = ServiceBusService.create(config);
        TopicInfo cameraQueueInfo = new TopicInfo(CAMERA_QUEUE_TAG);
        TopicInfo vehicleQueueInfo = new TopicInfo(VEHICLE_QUEUE_TAG);

        //Try to create queue
        //If error then this means queue already exists
        try
        {


            CreateTopicResult cameraResult = service.createTopic(cameraQueueInfo);
        }
        catch (ServiceException e)
        {

        }

        try {
            CreateTopicResult vehicleResult = service.createTopic(vehicleQueueInfo);

        }catch (ServiceException e){

        }

    }

    /**
     * Send message to the Azure Message Queue.
     * It will send it to the right queue based on the tag attached.
     * If the queue already exists then messages are sent directly to the already created queue
     * IF the queue is doesn't exist then create new queue
     * @param messageObject
     * @param tag
     */
    private void sendMessage(String messageObject,String tag){
        //Try to send a message if there is an error while sending, then add to the offline list of messages
        System.out.println("Sending : " + messageObject + "On Queue: "+tag);
        try{
            BrokeredMessage message = new BrokeredMessage(messageObject);
            service.sendTopicMessage(tag,message);
            sendOfflineMessages();
        }catch (ServiceException e){

            messages.add(new String[] {messageObject,tag});

        }
    }

    /**
     * A static message used to create and return a new SmartCamera instance with the arguments passed through args array
     * @param args
     * @return  SmartCamera
     * @throws IOException
     */
    public static SmartCamera setupCamera(String[] args) throws IOException, JSONException {
        String uid = null;
        String streetName = null;
        String city = null;
        int speedLimit = 0;

        //Connect to ServiceBus
        connectToServiceBus();

        try {
            if (args.length == 1) {
                Gson gson = new Gson();
                String content = new String(Files.readAllBytes(Paths.get(args[0])));
                JSONArray config = new JSONArray(content);

                uid = config.getString(0);
                streetName = config.getString(1);
                city = config.getString(2);
                speedLimit = Integer.parseInt(config.getString(3));


            }else if (args.length == 4){

                uid = args[0];
                streetName = args[1];
                city = args[2];
                speedLimit = Integer.parseInt(args[3]);




            }else
                throw new Exception("Error Reading Configuration data");

        }catch(Exception e){
        System.out.print("Camera initialization error encountered: ");
        System.out.println(e.getMessage());
        System.exit(-1);
        }

        return new SmartCamera(uid,streetName,city,speedLimit);
    }

    /**
     * Send all the remaining offline messages
     * @throws ServiceException
     */
    private void sendOfflineMessages() throws ServiceException {

        BrokeredMessage message;
        if(messages.size() !=0){
            for (int x =0;x<messages.size();x++){
                String[] msg = messages.get(x);
                message = new BrokeredMessage(msg[0]);
                tag = msg[1];
                service.sendTopicMessage(tag,message);
                messages.remove(x);
            }
        }

    }

    /**
     * Override finalize method to make sure that al the remaining offline messages are sent
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        while(messages.size() != 0)
            sendOfflineMessages();
    }
}
