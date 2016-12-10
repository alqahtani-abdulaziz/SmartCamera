import java.io.Serializable;

/**
 * Class to represent a Vehicle Record
 */

public class Vehicle implements Serializable {
    private String registration;
    private String type;
    private int speed;
    private SmartCamera camera;
    private boolean offender;

    Vehicle(){

    }

    public void setOffender(boolean offender) {
        this.offender = offender;
    }

    public boolean getOffender(){
        return this.offender;
    }

    public void setCamera(SmartCamera camera) {
        this.camera = camera;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setType(String type) {
        this.type = type;
    }

    public SmartCamera getCamera() {
        return camera;
    }

    public String getRegistration() {
        return registration;
    }

    public int getSpeed() {
        return speed;
    }

    public String getType() {
        return type;
    }
}
