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
    private float fov = 60f;

    private boolean orbitMode = false;
    private boolean commandeRoll = false; // false = FPS, true = cockpit

    private Vector3f cible = new Vector3f(0,0,0);
    private float orbitTheta = 0f;
    private float orbitPhi = 0f;
    private float orbitRadius = 1f;

    private float renderDistance = 100f;
    private float renderSimulation = 150f;

    private float rollAngle = 0f; // en degrés
    private static final float EPSILON = 1e-4f;

    public Camera(Vector3f position) {
        this.position = new Vector3f(position);
        this.front = new Vector3f(0,0,-1);
        this.up = new Vector3f(0,1,0);
        this.hautDuMonde = new Vector3f(0,1,0);
        this.droite = new Vector3f();
        this.angleHorizontal = -90f;
        this.angleVertical = 0f;
        updateCameraVectors();
        initOrbitFromCurrentState();
    }

    public void setOrbitMode(boolean active) {
        if (active == orbitMode) return;
        if (active) { initOrbitFromCurrentState(); alignAxesToTarget(); }
        else {
            Vector3f f = new Vector3f(cible).sub(position).normalize();
            angleVertical = (float)Math.toDegrees(Math.asin(f.y));
            angleHorizontal = (float)Math.toDegrees(Math.atan2(f.z, f.x));
            updateCameraVectors();
        }
        orbitMode = active;
    }
    public boolean isOrbitMode() { return orbitMode; }

    public void setCommandeRoll(boolean active) {
        this.commandeRoll = active;
        rollAngle = 0f; // reset pour éviter un décalage visuel
        updateCameraVectors();
    }
    public boolean isCommandeRoll() { return commandeRoll; }

    public Matrix4f getViewMatrix() {
        if (orbitMode) alignAxesToTarget();

        if (!commandeRoll) {
            Vector3f rolledUp = new Vector3f(hautDuMonde);
            if (Math.abs(rollAngle) > EPSILON)
                rolledUp.rotateAxis((float)Math.toRadians(rollAngle), front.x, front.y, front.z);
            return new Matrix4f().lookAt(position, new Vector3f(position).add(front), rolledUp);
        } else {
            return new Matrix4f().lookAt(position, new Vector3f(position).add(front), up);
        }
    }

    public void move(Vector3f offset) {
        if (!orbitMode) position.add(offset);
    }

    public void rotate(float offsetHorizontal, float offsetVertical) {
        if (!orbitMode) {
            angleHorizontal += offsetHorizontal;
            angleVertical += offsetVertical;
            angleVertical = Math.max(-89.9f, Math.min(89.9f, angleVertical));
            updateCameraVectors();
        } else {
            orbitTheta += Math.toRadians(offsetHorizontal);
            orbitPhi += Math.toRadians(offsetVertical);
            float limit = (float)(Math.PI/2-0.001);
            if (orbitPhi>limit) orbitPhi=limit;
            if (orbitPhi<-limit) orbitPhi=-limit;

            float cosPhi = (float)Math.cos(orbitPhi);
            float sinPhi = (float)Math.sin(orbitPhi);
            float cosTh = (float)Math.cos(orbitTheta);
            float sinTh = (float)Math.sin(orbitTheta);

            position.x = cible.x + orbitRadius*cosPhi*cosTh;
            position.y = cible.y + orbitRadius*sinPhi;
            position.z = cible.z + orbitRadius*cosPhi*sinTh;

            alignAxesToTarget();
        }
    }

    public void addRoll(float delta) {
        rollAngle += delta;

        if (commandeRoll) {
            Matrix4f rotation = new Matrix4f().rotate((float)Math.toRadians(delta), front.x, front.y, front.z);
            rotation.transformDirection(up);
            rotation.transformDirection(droite);
            up.normalize();
            droite.normalize();
        }
    }

    public float getRoll() { return rollAngle; }

    private void updateCameraVectors() {
        double yawRad = Math.toRadians(angleHorizontal);
        double pitchRad = Math.toRadians(angleVertical);

        front.x = (float)(Math.cos(yawRad)*Math.cos(pitchRad));
        front.y = (float)(Math.sin(pitchRad));
        front.z = (float)(Math.sin(yawRad)*Math.cos(pitchRad));
        front.normalize();

        if (!commandeRoll) {
            droite = new Vector3f(front).cross(hautDuMonde).normalize();
            if (droite.lengthSquared() < 1e-8f)
                droite = new Vector3f(1,0,0).cross(front).normalize();
            up.set(hautDuMonde);
        } else {
            droite = new Vector3f(front).cross(up).normalize();
            up = new Vector3f(droite).cross(front).normalize();
        }
    }

    private void initOrbitFromCurrentState() {
        Vector3f rel = new Vector3f(position).sub(cible);
        orbitRadius = rel.length();
        if (orbitRadius<1e-6f) orbitRadius=1e-6f;
        rel.div(orbitRadius);
        orbitTheta = (float)Math.atan2(rel.z, rel.x);
        orbitPhi = (float)Math.asin(rel.y);
    }

    private void alignAxesToTarget() {
        front.set(new Vector3f(cible).sub(position).normalize());
        droite.set(new Vector3f(front).cross(hautDuMonde).normalize());
        if (droite.lengthSquared() < 1e-8f)
            droite = new Vector3f(1,0,0).cross(front).normalize();
        up.set(new Vector3f(droite).cross(front).normalize());
    }

    // --- Getters utiles ---
    public Vector3f getPosition(){ return new Vector3f(position); }
    public Vector3f getFront(){ return new Vector3f(front); }
    public Vector3f getDroite(){ return new Vector3f(droite); }
    public Vector3f getUp(){ return new Vector3f(up); }

    public float getYaw(){ return angleHorizontal; }
    public float getPitch(){ return angleVertical; }
    public void setYawPitch(float yawDeg, float pitchDeg){
        angleHorizontal=yawDeg;
        angleVertical=pitchDeg;
        updateCameraVectors();
    }

    public float distanceTo(Vector3f point){ return position.distance(point); }

    public float getRenderDistance(){ return renderDistance; }
    public void setRenderDistance(float d){ renderDistance=d; }

    public float getRenderSimulation(){ return renderSimulation; }
    public void setRenderSimulation(float s){ renderSimulation=s; }

    public float getFov(){ return fov; }
    public void setFov(float fovDeg){ fov=fovDeg; }

    public Matrix4f getProjection(int width, int height) {
        float aspect = (float) width / height;
        return new Matrix4f().perspective(
                (float)Math.toRadians(fov),
                aspect,
                0.1f,
                renderDistance
        );
    }
}
