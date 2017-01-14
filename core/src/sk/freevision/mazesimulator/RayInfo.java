package sk.freevision.mazesimulator;

import com.badlogic.gdx.math.Vector2;

public class RayInfo {
    private float fraction = 1;
    private Vector2 point = Vector2.Zero;
    private float distance = Float.MAX_VALUE;
    private boolean stale = true;

    public float getFraction() {
        return fraction;
    }

    public void setFraction(float fraction) {
        this.fraction = fraction;
    }

    public Vector2 getPoint() {
        return point;
    }

    public void setPoint(Vector2 point) {
        this.point = point;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public boolean isStale() {
        return stale;
    }

    public void setStale(boolean stale) {
        this.stale = stale;
    }
}
