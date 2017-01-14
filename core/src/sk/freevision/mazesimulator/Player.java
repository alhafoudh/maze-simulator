package sk.freevision.mazesimulator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import net.dermetfan.gdx.graphics.g2d.Box2DSprite;

import java.util.function.BiConsumer;

public class Player {
    public static final float MAX_DISTANCE = 40.0f;
    private final World world;
    private final Sprite sprite;
    private final Body mainBody;
    private final Body leftTireBody;
    private final Body rightTireBody;
    private final ShapeRenderer shapeRenderer;

    private BiConsumer<Integer, Float> onRaycast;
    private float leftPower = 0;
    private float rightPower = 0;

    private RayInfo[] rayInfos;

    public Player(Sprite sprite, World world, Vector2 position) {
        this.sprite = sprite;
        this.world = world;
        this.shapeRenderer = new ShapeRenderer();

        // Main mainBody
        mainBody = createMainBody(position);

        // Tires
        leftTireBody = createTireBody(-3.5f);
        rightTireBody = createTireBody(3.5f);

        createTireJoint(mainBody, leftTireBody);
        createTireJoint(mainBody, rightTireBody);

        rayInfos = new RayInfo[]{
                new RayInfo(),
                new RayInfo(),
                new RayInfo(),
                new RayInfo(),
                new RayInfo(),
                new RayInfo()
        };
    }

    private Body createMainBody(Vector2 position) {
        BodyDef mainBodyDef = new BodyDef();
        mainBodyDef.type = BodyDef.BodyType.DynamicBody;
        mainBodyDef.linearDamping = 15.0f;
        mainBodyDef.angularDamping = 15.0f;
        mainBodyDef.position.set(position);

        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(sprite.getWidth() / 2.0f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circleShape;
        fixtureDef.density = 1;

        Body mainBody = world.createBody(mainBodyDef);
        mainBody.createFixture(fixtureDef);
        mainBody.setUserData(new Box2DSprite(sprite));
        circleShape.dispose();

        return mainBody;
    }

    private Joint createTireJoint(Body mainBody, Body tireBody) {
        PrismaticJointDef jointDef = new PrismaticJointDef();
        jointDef.initialize(mainBody, tireBody, tireBody.getWorldCenter(), new Vector2(1, 0));
        jointDef.enableLimit = true;
        jointDef.lowerTranslation = 0;
        jointDef.upperTranslation = 0;

        return world.createJoint(jointDef);
    }

    private Body createTireBody(float distance) {
        BodyDef tireBodyDef = new BodyDef();
        tireBodyDef.type = BodyDef.BodyType.DynamicBody;

        Vector2 motorPosition = new Vector2(mainBody.getWorldCenter()).add(distance, 0);
        tireBodyDef.position.set(motorPosition);

        PolygonShape tireShape = new PolygonShape();
        tireShape.setAsBox(0.5f, 1.0f);

        FixtureDef tireFixtureDef = new FixtureDef();
        tireFixtureDef.shape = tireShape;
        tireFixtureDef.density = 1;
        tireFixtureDef.isSensor = true;

        Body tireBody = world.createBody(tireBodyDef);
        tireBody.createFixture(tireFixtureDef);

        tireShape.dispose();

        return tireBody;
    }

    private void applyTirePower(Body tireBody, float power) {
        tireBody.applyForce(tireBody.getWorldVector(new Vector2(0, power)), tireBody.getWorldCenter(), true);
    }

    public void setPower(float leftPower, float rightPower) {
        this.leftPower = leftPower;
        this.rightPower = rightPower;
    }

    private void evaluateRay(int rayId, Vector2 basePoint, Vector2 vector) {
        Vector2 raySource = mainBody.getWorldPoint(basePoint).cpy();
//        Vector2 rayTarget = mainBody.getWorldPoint(basePoint.cpy().add(vector)).cpy();
        Vector2 targetPoint = new Vector2(basePoint.x + vector.x, basePoint.y + vector.y);
        Vector2 rayTarget = mainBody.getWorldPoint(targetPoint).cpy();

        RayInfo rayInfo = rayInfos[rayId];
        rayInfo.setFraction(1);
        rayInfo.setPoint(mainBody.getWorldPoint(vector));

        RayCastCallback rayCastCallback = (fixture, point, normal, fraction) -> {
            if (fixture.isSensor())
                return 1;

            if (rayInfo.getFraction() > fraction) {
                rayInfo.setFraction(fraction);

                Vector2 intersectionPoint = raySource.cpy().lerp(rayTarget, fraction);
                rayInfo.setPoint(intersectionPoint);

                float distance = raySource.dst(intersectionPoint);
                rayInfo.setDistance(distance > MAX_DISTANCE ? MAX_DISTANCE : distance);

                rayInfo.setStale(false);
            }

            return fraction;
        };
        world.rayCast(rayCastCallback, raySource, rayTarget);
        renderRay(raySource, rayTarget, Color.ORANGE, 0.2f);
        if (rayInfo.isStale()) {
            onRaycast.accept(rayId, MAX_DISTANCE);
            rayInfo.setPoint(rayTarget);
        }
        else
            onRaycast.accept(rayId, rayInfo.getDistance());
        renderRay(raySource, rayInfo.getPoint(), Color.BLUE, 0.7f);

        rayInfo.setStale(true);
    }

    private void renderRay(Vector2 fromPoint, Vector2 toPoint, Color color, float width) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rectLine(fromPoint, toPoint, width);
        shapeRenderer.circle(fromPoint.x, fromPoint.y, 0.75f, 8);
        shapeRenderer.circle(toPoint.x, toPoint.y, 0.75f, 8);
        shapeRenderer.end();
    }

    public void update() {
        applyTirePower(leftTireBody, leftPower);
        applyTirePower(rightTireBody, rightPower);
    }

    public void render(final Matrix4 projectionMatrix) {
        shapeRenderer.setProjectionMatrix(projectionMatrix);

        float rayLength = 200;

        evaluateRay(0, new Vector2(-3.5f, 2), new Vector2(0, rayLength));
        evaluateRay(1, new Vector2(+3.5f, 2), new Vector2(0, rayLength));
        evaluateRay(2, new Vector2(-1.5f, 4), new Vector2(-rayLength, 0));
        evaluateRay(3, new Vector2(+1.5f, 4), new Vector2(+rayLength, 0));
        evaluateRay(4, new Vector2(-2.5f, 3), new Vector2(-rayLength, rayLength));
        evaluateRay(5, new Vector2(+2.5f, 3), new Vector2(+rayLength, rayLength));
    }

    public Body getMainBody() {
        return mainBody;
    }

    public BiConsumer<Integer, Float> getOnRaycast() {
        return onRaycast;
    }

    public void setOnRaycast(BiConsumer<Integer, Float> onRaycast) {
        this.onRaycast = onRaycast;
    }
}
