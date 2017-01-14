package sk.freevision.mazesimulator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.*;

import javax.xml.bind.annotation.XmlAttribute;

public class Wall {

    private static final float WALL_WIDTH = 0.5f;
    private float x1;
    private float y1;

    private float x2;
    private float y2;

    private Body body;
    private ShapeRenderer shapeRenderer;

    public Wall() {
        shapeRenderer = new ShapeRenderer();
    }

    public float getX1() {
        return x1;
    }

    @XmlAttribute(name = "x1")
    public void setX1(float x1) {
        this.x1 = x1;
    }

    public float getY1() {
        return y1;
    }

    @XmlAttribute(name = "y1")
    public void setY1(float y1) {
        this.y1 = y1;
    }

    public float getX2() {
        return x2;
    }

    @XmlAttribute(name = "x2")
    public void setX2(float x2) {
        this.x2 = x2;
    }

    public float getY2() {
        return y2;
    }

    @XmlAttribute(name = "y2")
    public void setY2(float y2) {
        this.y2 = y2;
    }

    public void preparePhysics(World world) {
        BodyDef wallBodyDef = new BodyDef();
        wallBodyDef.type = BodyDef.BodyType.StaticBody;

        wallBodyDef.position.set(0, 0);

        EdgeShape wallShape = new EdgeShape();
        wallShape.set(x1, y1, x2, y2);
        wallShape.setRadius(0.5f);

        FixtureDef wallFixtureDef = new FixtureDef();
        wallFixtureDef.shape = wallShape;
        wallFixtureDef.density = 1f;

        Body wallBody = world.createBody(wallBodyDef);
        wallBody.createFixture(wallFixtureDef);

        wallShape.dispose();

        body = wallBody;
    }

    public void render(Matrix4 projectionMatrix) {
        shapeRenderer.setProjectionMatrix(projectionMatrix);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rectLine(x1, y1, x2, y2, 1.0f, Color.BLACK, Color.BLACK);
        shapeRenderer.end();
    }
}
