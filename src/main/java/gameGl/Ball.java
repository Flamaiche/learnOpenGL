package gameGl;

import gameGl.tools.PreVerticesTable;
import learnGL.tools.Shader;
import learnGL.tools.Shape;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Random;

public class Ball {
    private Shape corps;
    private Shader shader;
    private Vector3f position;
    private Vector3f direction;
    private float speed = 25.0f;        // vitesse de déplacement
    private float maxDistance = 150f;   // distance de despawn
    private Random rand = new Random();

    private Vector3f rotation = new Vector3f();
    private Vector3f rotationSpeed = new Vector3f();

    private float rotationMultiplier = 2.0f; // pour régler vitesse rotation

    public Ball(Shader shader, Vector3f startPos, Vector3f forwardDir, float baseSize) {
        this.shader = shader;
        this.position = new Vector3f(startPos);
        this.direction = new Vector3f(forwardDir).normalize();

        corps = new Shape(Shape.autoAddSlotColor(PreVerticesTable.generatePyramid(baseSize)));
        corps.setColor(1f, 0f, 0f, 1f);
        corps.setShader(shader);

        rotationSpeed.set(
                rand.nextFloat() * 720f - 360f,
                rand.nextFloat() * 720f - 360f,
                rand.nextFloat() * 720f - 360f
        );
    }

    public void setRotationMultiplier(float multiplier) {
        this.rotationMultiplier = multiplier;
    }

    public void update(float deltaTime) {
        position.add(new Vector3f(direction).mul(speed * deltaTime));

        // applique le multiplicateur sur la rotation
        rotation.x += rotationSpeed.x * deltaTime * rotationMultiplier;
        rotation.y += rotationSpeed.y * deltaTime * rotationMultiplier;
        rotation.z += rotationSpeed.z * deltaTime * rotationMultiplier;
    }

    public boolean shouldDespawn(Vector3f cameraPos) {
        return position.distance(cameraPos) > maxDistance;
    }

    public void render(Matrix4f view, Matrix4f projection) {
        Matrix4f model = getModelMatrix();
        shader.bind();
        shader.setUniformMat4f("view", view);
        shader.setUniformMat4f("projection", projection);
        shader.setUniformMat4f("model", model);

        corps.render();
        shader.unbind();
    }

    public Matrix4f getModelMatrix() {
        return new Matrix4f()
                .translate(position)
                .rotateX((float) Math.toRadians(rotation.x))
                .rotateY((float) Math.toRadians(rotation.y))
                .rotateZ((float) Math.toRadians(rotation.z));
    }

    public void cleanup() {
        corps.cleanup();
    }

    public int collisionScore(Ennemis[] enemies) {
        int score = 0;
        Matrix4f ballModel = getModelMatrix();
        for (Ennemis enemy : enemies) {
            if (haveDestroyed(enemy, ballModel)) {
                score += enemy.getScore();
            }
        }
        return score;
    }

    public boolean haveDestroyed(Ennemis enemy, Matrix4f ballModel) {
        Matrix4f enemyModel = enemy.getModelMatrix();
        // Teste la collision avec la balle
        if (enemy.getCorps().intersectsOptimized(corps, ballModel, enemyModel)) {
            enemy.decrementVie();
            if (enemy.getVie() <= 0) {
                // Déplace l'ennemi hors du monde
                enemy.setDeplacement(new float[]{
                        enemy.getDespawnDistance()*2, enemy.getDespawnDistance()*2, enemy.getDespawnDistance()*2
                });
                return true;
            }
        }
        return false;
    }
}
