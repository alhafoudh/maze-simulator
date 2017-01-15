package sk.freevision.mazesimulator;

import lombok.Data;

@Data
public class RobotState {
    private float leftMotor = 0;
    private float rightMotor = 0;
    private float[] sensors;
    private float angle = 0;
    private float leftMotorSteps = 0;
    private float rightMotorSteps = 0;

    public RobotState(int numSensors) {
        sensors = new float[numSensors];
        for (int i = 0; i < numSensors; i++) {
            sensors[i] = Float.MAX_VALUE;
        }
    }
}
