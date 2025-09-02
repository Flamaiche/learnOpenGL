package gameGl.utils;

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

    public static float speed = 25f;
    public static float maxDistance = 150f;
    public static float rotationMultiplier = 2f;

    private boolean active = false;
    private final Random rand = new Random();

    // Stocke la matrice pour éviter de la recalculer à chaque render
    private final Matrix4f modelMatrix = new Matrix4f();
    private boolean modelDirty = true;

    public Ball(Shader shader, float baseSize) {
        this.shader = shader;
        corps = new Shape(Shape.autoAddSlotColor(PreVerticesTable.generatePyramid(baseSize)));
        corps.setColor(1f, 0f, 0f, 1f);
        corps.setShader(shader);
    }

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
        modelDirty = true; // on doit recalculer la matrice
    }

    public void deactivate() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public void update(float deltaTime) {
        if (!active) return;

        position.fma(speed * deltaTime, direction);
        rotation.x += rotationSpeed.x * deltaTime * rotationMultiplier;
        rotation.y += rotationSpeed.y * deltaTime * rotationMultiplier;
        rotation.z += rotationSpeed.z * deltaTime * rotationMultiplier;

        if (position.length() > maxDistance) active = false;

        modelDirty = true; // la position/rotation a changé
    }

    public void render(Matrix4f view, Matrix4f projection) {
        if (!active) return;

        if (modelDirty) updateModelMatrix();

        shader.bind();
        shader.setUniformMat4f("view", view);
        shader.setUniformMat4f("projection", projection);
        shader.setUniformMat4f("model", modelMatrix);
        corps.render();
        shader.unbind();
    }

    private void updateModelMatrix() {
        modelMatrix.identity()
                .translate(position)
                .rotateX((float) Math.toRadians(rotation.x))
                .rotateY((float) Math.toRadians(rotation.y))
                .rotateZ((float) Math.toRadians(rotation.z));
        modelDirty = false;
    }

    public Matrix4f getModelMatrix() {
        if (modelDirty) updateModelMatrix();
        return modelMatrix;
    }

    public void cleanup() {
        corps.cleanup();
    }

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
                active = false;
                return true;
            }
        }
        return false;
    }

    public static void setMaxDistance(float maxDistance) {
        Ball.maxDistance = maxDistance;
    }

    public static void setSpeed(float speed) {
        Ball.speed = speed;
    }

    public static void setRotationMultiplier(float rotationMultiplier) {
        Ball.rotationMultiplier = rotationMultiplier;
    }
}
