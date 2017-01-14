package sk.freevision.mazesimulator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import net.dermetfan.gdx.graphics.g2d.Box2DSprite;

public class MazeSimulator extends ApplicationAdapter {
    public enum SimulationSpeed {
        NORMAL,
        SLOW,
        FAST
    }

    public enum ControlOwner {
        USER,
        AI
    }

    public static final String SCRIPT_FILE_NAME = "script.js";
    public static final String PLAYER_TEXTURE = "player.png";
    private static final float DEFAULT_ZOOM_LEVEL = 0.1f;
    private static final float ZOOM_LEVEL_STEP = 0.005f;
    private static final Vector2 STARTING_POSITION = new Vector2(10, 9);

    private World world;
    private SpriteBatch batch;
    private Maze maze;
    private Player player;
    private Box2DDebugRenderer debugRenderer;
    private OrthographicCamera camera;

    private PlayerScript playerScript;

    private float leftPower = 0;
    private float rightPower = 0;

    private SimulationSpeed simulationSpeed = SimulationSpeed.NORMAL;
    private ControlOwner controlOwner = ControlOwner.AI;

    @Override
    public void create() {
        debugRenderer = initializeDebugRenderer();

        world = new World(new Vector2(0, 0), true);
        batch = new SpriteBatch();

        camera = initializeCamera();
        camera.update();

        maze = initializeMaze("maze2");
        if (maze != null)
            maze.preparePhysics(world);

        player = new Player(new Sprite(new Texture(PLAYER_TEXTURE)), world, STARTING_POSITION);

        playerScript = initializeScript();
        playerScript.start();
    }

    private OrthographicCamera initializeCamera() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        OrthographicCamera camera = new OrthographicCamera(w, h);
        camera.zoom = DEFAULT_ZOOM_LEVEL;
        return camera;
    }

    private Maze initializeMaze(String mazeName) {
        try {
            return Maze.load(String.format("mazes/%s.svg", mazeName));
        } catch (Exception e) {
            Gdx.app.error("maze", "Failed to load maze", e);
            Gdx.app.exit();
        }
        return null;
    }

    private PlayerScript initializeScript() {
        return new PlayerScript(player, Gdx.files.internal(SCRIPT_FILE_NAME).readString());
    }

    private Box2DDebugRenderer initializeDebugRenderer() {
        Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
        debugRenderer.setDrawVelocities(true);
        debugRenderer.setDrawJoints(true);
        debugRenderer.setDrawBodies(true);
        debugRenderer.setDrawContacts(true);
        return debugRenderer;
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        // Input handling
        handleInput();

        // Camera manipulation
        camera.position.set(player.getMainBody().getWorldCenter(), 0);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Update player
        playerScript.update();
        if (controlOwner == ControlOwner.USER) {
            player.setPower(leftPower, rightPower);
            playerScript.setEnabled(false);
        } else {
            playerScript.setEnabled(true);
        }
        player.update();

        // Perform simulation
        switch (simulationSpeed) {
            case NORMAL:
                deltaTime *= 1.0f;
                break;
            case SLOW:
                deltaTime *= 0.25f;
                break;
            case FAST:
                deltaTime *= 2.0f;
                break;
        }
        world.step(deltaTime, 6, 2);

        // Render
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        Box2DSprite.draw(batch, world);
        batch.end();

        player.render(camera.combined);
        maze.render(camera.combined);

        // Render debug
        debugRenderer.render(world, camera.combined);
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            Gdx.app.exit();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0)) {
            camera.zoom = DEFAULT_ZOOM_LEVEL;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            camera.zoom = 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT_BRACKET)) {
            camera.zoom += ZOOM_LEVEL_STEP;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT_BRACKET)) {
            camera.zoom -= ZOOM_LEVEL_STEP;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.translate(-3, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.translate(3, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.translate(0, -3, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.translate(0, 3, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            simulationSpeed = SimulationSpeed.SLOW;
        } else if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            simulationSpeed = SimulationSpeed.FAST;
        } else {
            simulationSpeed = SimulationSpeed.NORMAL;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            resetPlayer();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.O)) {
            controlOwner = (controlOwner == ControlOwner.USER ? ControlOwner.AI : ControlOwner.USER);
        }

        handleUserControl();
    }

    private void handleUserControl() {
        float currentLeftPower = 0;
        float currentRightPower = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            currentLeftPower = 5000;
            currentRightPower = 5000;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            currentLeftPower = -5000;
            currentRightPower = -5000;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            currentRightPower += 2500;
            currentLeftPower -= 2500;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            currentRightPower -= 2500;
            currentLeftPower += 2500;
        }

        leftPower = currentLeftPower;
        rightPower = currentRightPower;
    }

    private void resetPlayer() {
        Body playerMainBody = player.getMainBody();
        playerMainBody.setTransform(STARTING_POSITION, 0);
        playerMainBody.setAngularVelocity(0);
        playerMainBody.setLinearVelocity(0, 0);
    }

}
