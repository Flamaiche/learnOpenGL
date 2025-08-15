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

    // Mode orbite temporaire
    private boolean orbitMode = false;
    private Vector3f cible = new Vector3f(0, 0, 0);
    private float distanceCible;

    public Camera(Vector3f position) {
        this.position = position;
        this.front = new Vector3f(0, 0, -1);
        this.up = new Vector3f(0, 1, 0);
        this.hautDuMonde = new Vector3f(0, 1, 0);
        this.droite = new Vector3f();
        this.angleHorizontal = -90.0f;
        this.angleVertical = 0.0f;
        updateCameraVectors();
    }

    // Activer/désactiver mode orbite selon la touche
    public void setOrbitMode(boolean active) {
        if (orbitMode && !active) {
            // on quitte l'orbite : recalculer angleHorizontal et angleVertical pour la caméra libre
            Vector3f f = new Vector3f(front).normalize();
            angleVertical = (float) Math.toDegrees(Math.asin(f.y));
            angleHorizontal = (float) Math.toDegrees(Math.atan2(f.z, f.x));
        }

        orbitMode = active;

        if (orbitMode) {
            Vector3f dir = new Vector3f(cible).sub(position);
            distanceCible = dir.length();
        }
    }


    public Matrix4f getViewMatrix() {
        if (orbitMode) {
            front.set(new Vector3f(cible).sub(position).normalize());
            droite.set(new Vector3f(front).cross(hautDuMonde).normalize());
            up.set(new Vector3f(droite).cross(front).normalize());
        }
        Vector3f centre = new Vector3f(position).add(front);
        return new Matrix4f().lookAt(position, centre, up);
    }

    public void move(Vector3f offset) {
        if (!orbitMode) position.add(offset);
    }

    public void rotate(float offsetHorizontal, float offsetVertical) {
        if (!orbitMode) {
            angleHorizontal += offsetHorizontal;
            angleVertical += offsetVertical;

            updateCameraVectors();
        } else {
            Vector3f dir = new Vector3f(position).sub(cible);
            float r = dir.length();
            float theta = (float) Math.atan2(dir.z, dir.x);
            float phi = (float) Math.asin(dir.y / r);

            theta += Math.toRadians(offsetHorizontal);
            phi += Math.toRadians(offsetVertical);
            phi = Math.max(-1.5f, Math.min(1.5f, phi));

            position.x = cible.x + r * (float) (Math.cos(phi) * Math.cos(theta));
            position.y = cible.y + r * (float) Math.sin(phi);
            position.z = cible.z + r * (float) (Math.cos(phi) * Math.sin(theta));
        }
    }

    private void updateCameraVectors() {
        front.x = (float) (Math.cos(Math.toRadians(angleHorizontal)) * Math.cos(Math.toRadians(angleVertical)));
        front.y = (float) Math.sin(Math.toRadians(angleVertical));
        front.z = (float) (Math.sin(Math.toRadians(angleHorizontal)) * Math.cos(Math.toRadians(angleVertical)));
        front.normalize();

        droite = new Vector3f(front).cross(hautDuMonde).normalize();
        up = new Vector3f(droite).cross(front).normalize();
    }

    // Getters
    public Vector3f getPosition() { return position; }
    public Vector3f getFront() { return front; }
    public Vector3f getDroite() { return droite; }
    public Vector3f getUp() { return up; }
    public boolean isOrbitMode() { return orbitMode; }
}
