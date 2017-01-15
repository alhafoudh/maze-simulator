package sk.freevision.mazesimulator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import javax.script.*;

public class PlayerScript {

    private final Player player;
    private final String script;
    private final javax.script.ScriptEngine engine;

    private ScriptContext scriptContext;
    private RobotState robotState;
    private boolean enabled = false;
    private Invocable invocable;

    private Vector2 lastLeftMotorPosition;
    private Vector2 lastRightMotorPosition;
    private float leftMotorSteps = 0f;
    private float rightMotorSteps = 0f;

    public PlayerScript(Player player, String script) {
        this.player = player;
        this.script = script;

        engine = new ScriptEngineManager().getEngineByName("nashorn");
        invocable = (Invocable) engine;

        lastLeftMotorPosition = player.getLeftTireBody().getPosition().cpy();
        lastRightMotorPosition = player.getRightTireBody().getPosition().cpy();
    }

    public void start() {
        robotState = new RobotState(6);
        scriptContext = new SimpleScriptContext();

        Bindings bindings = engine.createBindings();
        bindings.put("state", robotState);
        bindings.put("console", new Console());

        scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        engine.setContext(scriptContext);

        try {
            engine.eval(script);
            invocable.invokeFunction("init");
        } catch (ScriptException | NoSuchMethodException e) {
            Gdx.app.error("script", "script init exception", e);
        }

        player.setOnRaycast((sensorId, distance) -> {
            robotState.getSensors()[sensorId] = distance;
        });
    }

    private float calculateMotorSteps(Vector2 currentPosition, Vector2 lastPosition) {
        float motorStepLength = lastPosition.dst(currentPosition);
        lastPosition.set(currentPosition.x, currentPosition.y);
        return motorStepLength;
    }

    public void update() {
        // Calculate motor steps
        leftMotorSteps += calculateMotorSteps(player.getLeftTireBody().getPosition(), lastLeftMotorPosition);
        rightMotorSteps += calculateMotorSteps(player.getRightTireBody().getPosition(), lastRightMotorPosition);
        robotState.setLeftMotorSteps(leftMotorSteps);
        robotState.setRightMotorSteps(rightMotorSteps);

        // Calculate angle
        robotState.setAngle(Math.abs((player.getMainBody().getAngle() * MathUtils.radiansToDegrees) % 360.0f));

        try {
            invocable.invokeFunction("update");
        } catch (ScriptException | NoSuchMethodException e) {
            Gdx.app.error("script", "script update exception", e);
        }

        if (isEnabled())
            player.setPower(robotState.getLeftMotor(), robotState.getRightMotor());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
