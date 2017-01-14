package sk.freevision.mazesimulator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;

import javax.script.*;

public class PlayerScript {

    private final Player player;
    private final String script;
    private final javax.script.ScriptEngine engine;

    private ScriptContext scriptContext;
    private RobotState robotState;
    private boolean enabled = false;
    private Invocable invocable;

    public PlayerScript(Player player, String script) {
        this.player = player;
        this.script = script;

        engine = new ScriptEngineManager().getEngineByName("nashorn");
        invocable = (Invocable) engine;
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

    public void update() {
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
