package learnGL.tools;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private Vector3f position;
    private Vector3f front;
    private Vector3f up;

    public Camera(Vector3f position) {
        this.position = position;
        this.front = new Vector3f(0, 0, -1);
        this.up = new Vector3f(0, 1, 0);
    }

    public Matrix4f getViewMatrix() {
        Vector3f center = new Vector3f(position).add(front);
        return new Matrix4f().lookAt(position, center, up);
    }

    public void move(Vector3f offset) {
        position.add(offset);
    }

    public Vector3f getPosition() {
        return position;
    }
}
