package learnGL.tools;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private Vector3f position;
    private Vector3f front;
    private Vector3f up;
    private Vector3f droite;
    private Vector3f hautDuMonde;

    // Angles mode libre (yaw/pitch en degrés)
    private float angleHorizontal; // yaw
    private float angleVertical;   // pitch
    private float fov = 60f;       // en degrés

    // Mode orbite
    private boolean orbitMode = false;
    private Vector3f cible = new Vector3f(0, 0, 0);
    private float orbitTheta = 0f;   // azimut (radians)
    private float orbitPhi = 0f;     // élévation (radians)
    private float orbitRadius = 1f;  // distance à la cible

    private float renderDistance = 100f;
    private float renderSimulation = 150f;

    public Camera(Vector3f position) {
        this.position = new Vector3f(position);
        this.front = new Vector3f(0, 0, -1);
        this.up = new Vector3f(0, 1, 0);
        this.hautDuMonde = new Vector3f(0, 1, 0);
        this.droite = new Vector3f();
        this.angleHorizontal = -90.0f;
        this.angleVertical = 0.0f;
        updateCameraVectors();
        // init paramètres d’orbite en cohérence avec la position
        initOrbitFromCurrentState();
    }

    // Définit la cible à regarder en orbite
    public void setCible(Vector3f nouvelleCible) {
        this.cible.set(nouvelleCible);
        initOrbitFromCurrentState();
        if (orbitMode) {
            // en orbite on s'aligne immédiatement vers la nouvelle cible
            alignAxesToTarget();
        }
    }

    // Activer/désactiver mode orbite
    public void setOrbitMode(boolean active) {
        if (active == orbitMode) return;

        if (active) {
            // On entre en orbite : on garde la même position, mais la vision doit pointer vers la cible
            initOrbitFromCurrentState(); // calcule radius/theta/phi depuis position actuelle
            alignAxesToTarget();         // oriente le front vers la cible (sans bouger la position)
        } else {
            // On quitte l'orbite : on conserve la même position et la même vision qu'en orbite
            // → synchroniser yaw/pitch avec la direction actuelle (cible - position)
            Vector3f f = new Vector3f(cible).sub(position).normalize();
            angleVertical   = (float) Math.toDegrees(Math.asin(f.y));          // [-90°, +90°]
            angleHorizontal = (float) Math.toDegrees(Math.atan2(f.z, f.x));    // (-180°, +180°]
            updateCameraVectors(); // met à jour front/droite/up pour le mode libre
        }

        orbitMode = active;
    }

    public Matrix4f getViewMatrix() {
        if (orbitMode) {
            // En orbite : front/up/droite cohérents avec la cible
            alignAxesToTarget();
        }
        Vector3f centre = new Vector3f(position).add(front);
        return new Matrix4f().lookAt(position, centre, up);
    }

    public void move(Vector3f offset) {
        if (!orbitMode) {
            position.add(offset);
        }
    }

    // offsetHorizontal / offsetVertical en DEGRÉS (comme avant)
    public void rotate(float offsetHorizontal, float offsetVertical) {
        if (!orbitMode) {
            // Mode libre : angles accumulés (360° possible si tu ne clamps pas)
            angleHorizontal += offsetHorizontal;
            angleVertical   += offsetVertical;

            // Option FPS : décommente pour limiter le pitch
            angleVertical = Math.max(-89.9f, Math.min(89.9f, angleVertical));

            updateCameraVectors();
        } else {
            // Mode orbite : on fait tourner la position autour de la cible (on ne bouge pas la cible)
            orbitTheta += Math.toRadians(offsetHorizontal);
            orbitPhi   += Math.toRadians(offsetVertical);

            // Évite pile les pôles pour la stabilité numérique
            float epsilon = 0.001f;
            float limit = (float)(Math.PI / 2.0 - epsilon);
            if (orbitPhi > limit)  orbitPhi = limit;
            if (orbitPhi < -limit) orbitPhi = -limit;

            // Repositionner la caméra sur la sphère autour de la cible
            float cosPhi = (float) Math.cos(orbitPhi);
            float sinPhi = (float) Math.sin(orbitPhi);
            float cosTh  = (float) Math.cos(orbitTheta);
            float sinTh  = (float) Math.sin(orbitTheta);

            position.x = cible.x + orbitRadius * cosPhi * cosTh;
            position.y = cible.y + orbitRadius * sinPhi;
            position.z = cible.z + orbitRadius * cosPhi * sinTh;

            // Mettre à jour les axes pour la vue orbitale
            alignAxesToTarget();
        }
    }

    private void updateCameraVectors() {
        // Direction depuis yaw/pitch (angles en degrés)
        double yawRad   = Math.toRadians(angleHorizontal);
        double pitchRad = Math.toRadians(angleVertical);

        front.x = (float) (Math.cos(yawRad) * Math.cos(pitchRad));
        front.y = (float) (Math.sin(pitchRad));
        front.z = (float) (Math.sin(yawRad) * Math.cos(pitchRad));
        front.normalize();

        // Recalcule droite et up
        droite = new Vector3f(front).cross(hautDuMonde).normalize();
        // Si front ≈ hautDuMonde, protège contre un cross quasi nul
        if (droite.lengthSquared() < 1e-8f) {
            // choisir un up alternatif
            droite = new Vector3f(1, 0, 0).cross(front).normalize();
        }
        up = new Vector3f(droite).cross(front).normalize();
    }

    private void initOrbitFromCurrentState() {
        // Calcule radius/theta/phi depuis la position actuelle par rapport à la cible
        Vector3f rel = new Vector3f(position).sub(cible);
        orbitRadius = rel.length();
        if (orbitRadius < 1e-6f) orbitRadius = 1e-6f; // évite division par zéro

        rel.div(orbitRadius); // normalise implicitement
        // azimut sur le plan XZ
        orbitTheta = (float) Math.atan2(rel.z, rel.x);
        // élévation par rapport au plan XZ
        orbitPhi = (float) Math.asin(rel.y); // ∈ [-π/2, π/2]
    }

    private void alignAxesToTarget() {
        // Oriente la caméra vers la cible sans toucher à la position
        front.set(new Vector3f(cible).sub(position).normalize());

        droite.set(new Vector3f(front).cross(hautDuMonde).normalize());
        if (droite.lengthSquared() < 1e-8f) {
            // si on regarde quasi pile en haut/bas, prends un axe latéral stable
            droite = new Vector3f(1, 0, 0).cross(front).normalize();
        }
        up.set(new Vector3f(droite).cross(front).normalize());
    }

    // Getters
    public Vector3f getPosition() { return new Vector3f(position); }
    public Vector3f getFront()    { return new Vector3f(front); }
    public Vector3f getDroite()   { return new Vector3f(droite); }
    public Vector3f getUp()       { return new Vector3f(up); }
    public boolean isOrbitMode()  { return orbitMode; }

    // Optionnel : accéder/modifier les angles libres
    public float getYaw()   { return angleHorizontal; }
    public float getPitch() { return angleVertical; }
    public void setYawPitch(float yawDeg, float pitchDeg) {
        this.angleHorizontal = yawDeg;
        this.angleVertical = pitchDeg;
        updateCameraVectors();
    }

    public float distanceTo(Vector3f point) {
        return position.distance(point);
    }

    public float getRenderDistance() { return renderDistance; }
    public void setRenderDistance(float d) { renderDistance = d; }

    public float getRenderSimulation() { return renderSimulation; }
    public void setRenderSimulation(float s) { renderSimulation = s; }

    public float getFov() {
        return fov;
    }
    public void setFov(float fovDeg) {
        this.fov = fovDeg;
    }

    public Matrix4f getProjection(int width, int height) {
        float aspect = (float) width / height;
        return new Matrix4f().perspective(
                (float) Math.toRadians(fov),
                aspect,
                0.1f,
                renderDistance
        );
    }
    // pas de gyroscope, pas d'inclinaison latérale
    public float getRoll() {
        return 0f; // pas utilisé dans ce jeu
    }
}
