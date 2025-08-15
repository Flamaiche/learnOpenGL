package learnGL.tools;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private Vector3f position;
    private Vector3f front;
    private Vector3f up;
    private Vector3f droite;
    private Vector3f hautDuMonde;

    private float angleHorizontal;
    private float angleVertical;

    public Camera(Vector3f position) {
        this.position = position;
        this.front = new Vector3f(0, 0, -1);
        this.up = new Vector3f(0, 1, 0);
        this.hautDuMonde = new Vector3f(0, 1, 0);
        this.droite = new Vector3f();
        this.angleHorizontal = -90.0f; // regarde vers -Z par défaut
        this.angleVertical = 0.0f;
        updateCameraVectors();
    }

    public Matrix4f getViewMatrix() {
        Vector3f centre = new Vector3f(position).add(front);
        return new Matrix4f().lookAt(position, centre, up);
    }

    public void move(Vector3f offset) {
        position.add(offset);
    }

    public void rotate(float offsetHorizontal, float offsetVertical) {
        angleHorizontal += offsetHorizontal;
        angleVertical += offsetVertical;

        if (angleVertical > 89.0f) angleVertical = 89.0f;
        if (angleVertical < -89.0f) angleVertical = -89.0f;

        updateCameraVectors();
    }

    private void updateCameraVectors() {
        front.x = (float) (Math.cos(Math.toRadians(angleHorizontal)) * Math.cos(Math.toRadians(angleVertical)));
        front.y = (float) Math.sin(Math.toRadians(angleVertical));
        front.z = (float) (Math.sin(Math.toRadians(angleHorizontal)) * Math.cos(Math.toRadians(angleVertical)));
        front.normalize();

        droite = new Vector3f(front).cross(hautDuMonde).normalize();
        up = new Vector3f(droite).cross(front).normalize();
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getFront() {
        return front;
    }

    public Vector3f getDroite() {
        return droite;
    }

    public Vector3f getUp() {
        return up;
    }
}
