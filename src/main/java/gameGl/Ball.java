package gameGl;

import gameGl.tools.PreVerticesTable;
import learnGL.tools.Shader;
import learnGL.tools.Shape;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Random;

public class Ball {
    private final Shape corps;
    private final Shader shader;

    private final Vector3f position = new Vector3f();
    private final Vector3f direction = new Vector3f();
    private final Vector3f rotation = new Vector3f();
    private final Vector3f rotationSpeed = new Vector3f();

    private float speed = 25f;
    private float maxDistance = 150f;
    private float rotationMultiplier = 2f;

    private boolean active = false; // état actif ou non
    private final Random rand = new Random();

    public Ball(Shader shader, float baseSize) {
        this.shader = shader;
        corps = new Shape(Shape.autoAddSlotColor(PreVerticesTable.generatePyramid(baseSize)));
        corps.setColor(1f, 0f, 0f, 1f);
        corps.setShader(shader);
    }

    /** Réactive et initialise la balle */
    public void activate(Vector3f startPos, Vector3f forwardDir) {
        position.set(startPos);
        direction.set(forwardDir).normalize();

        rotation.set(0f, 0f, 0f);
        rotationSpeed.set(
                rand.nextFloat() * 720f - 360f,
                rand.nextFloat() * 720f - 360f,
                rand.nextFloat() * 720f - 360f
        );

        active = true;
    }

    /** Désactive la balle (pour le pool) */
    public void deactivate() {
        active = false;
    }

    /** Vérifie si la balle est active */
    public boolean isActive() {
        return active;
    }

    /** Mise à jour de la position et rotation */
    public void update(float deltaTime) {
        if (!active) return;

        position.fma(speed * deltaTime, direction); // position += direction * speed * dt

        rotation.x += rotationSpeed.x * deltaTime * rotationMultiplier;
        rotation.y += rotationSpeed.y * deltaTime * rotationMultiplier;
        rotation.z += rotationSpeed.z * deltaTime * rotationMultiplier;

        if (position.length() > maxDistance) active = false; // auto-désactivation si trop loin
    }

    /** Rendu de la balle */
    public void render(Matrix4f view, Matrix4f projection) {
        if (!active) return;

        Matrix4f model = getModelMatrix();
        shader.bind();
        shader.setUniformMat4f("view", view);
        shader.setUniformMat4f("projection", projection);
        shader.setUniformMat4f("model", model);

        corps.render();
        shader.unbind();
    }

    /** Matrice de transformation pour le rendu */
    public Matrix4f getModelMatrix() {
        return new Matrix4f()
                .translate(position)
                .rotateX((float) Math.toRadians(rotation.x))
                .rotateY((float) Math.toRadians(rotation.y))
                .rotateZ((float) Math.toRadians(rotation.z));
    }

    /** Libération des ressources */
    public void cleanup() {
        corps.cleanup();
    }

    /** Collision contre ennemis et mise à jour du score */
    public int collisionScore(Ennemis[] enemies) {
        if (!active) return 0;

        int score = 0;
        Matrix4f ballModel = getModelMatrix();
        for (Ennemis enemy : enemies) {
            if (haveDestroyed(enemy, ballModel)) {
                score += enemy.getScore();
            }
        }
        return score;
    }

    /** Vérifie collision et décrémente vie ennemis */
    private boolean haveDestroyed(Ennemis enemy, Matrix4f ballModel) {
        Matrix4f enemyModel = enemy.getModelMatrix();
        if (enemy.getCorps().intersectsOptimized(corps, ballModel, enemyModel)) {
            enemy.decrementVie();
            if (enemy.getVie() <= 0) {
                enemy.setDeplacement(new float[]{
                        enemy.getDespawnDistance() * 2,
                        enemy.getDespawnDistance() * 2,
                        enemy.getDespawnDistance() * 2
                });
                active = false; // désactive la balle après destruction
                return true;
            }
        }
        return false;
    }
}
