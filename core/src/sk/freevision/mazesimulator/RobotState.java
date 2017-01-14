package sk.freevision.mazesimulator;

public class RobotState {
    private float leftMotor = 0;
    private float rightMotor = 0;

    private float[] sensors;

    public RobotState(int numSensors) {
        sensors = new float[numSensors];
        for (int i = 0; i < numSensors; i++) {
            sensors[i] = Float.MAX_VALUE;
        }
    }

    public float getLeftMotor() {
        return leftMotor;
    }

    public void setLeftMotor(float leftMotor) {
        this.leftMotor = leftMotor;
    }

    public float getRightMotor() {
        return rightMotor;
    }

    public void setRightMotor(float rightMotor) {
        this.rightMotor = rightMotor;
    }

    public float[] getSensors() {
        return sensors;
    }

    public void setSensors(float[] sensors) {
        this.sensors = sensors;
    }
}
