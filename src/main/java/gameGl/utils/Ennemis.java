package gameGl.utils;

import learnGL.tools.Shader;
import learnGL.tools.Shape;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Random;

import static org.lwjgl.opengl.GL11C.*;

public class Ennemis {
    private Random rand = new Random();
    private float spawnSize = 10.0f;
    private float exclusionSize = 0.0f;

    private Shape corps;
    private Shader shader;
    private Vector3f position;
    private Vector3f direction;
    private Vector3f target;
    public static float speed = 2.5f;
    public static float despawnDistance = 150f;
    private boolean highlighted = false;

    private int vie;
    private int score;

    // Pré-calcul de la matrix
    private final Matrix4f modelMatrix = new Matrix4f();

    public Ennemis(Shader shader, float[] centerPlayer, float[] verticesShape) {
        corps = new Shape(Shape.autoAddSlotColor(verticesShape));
        corps.setColor(0f, 0f, 0f, 1f);
        corps.setShader(shader);
        this.shader = shader;

        vie = 1;
        score = 10;

        setDeplacement(centerPlayer);
        updateModelMatrix(); // initialisation
    }

    public void setDeplacement(float[] centerPlayer) {
        float[] coors = generateSpawn(centerPlayer[0], centerPlayer[1], centerPlayer[2]);
        position = new Vector3f(coors[0], coors[1], coors[2]);

        float[] targetCoords = generateSpawn(centerPlayer[0], centerPlayer[1], centerPlayer[2]);
        target = new Vector3f(targetCoords[0], targetCoords[1], targetCoords[2]);

        direction = new Vector3f(target).sub(position).normalize();
        System.out.println(target + " "  + position + " " + direction);

        updateModelMatrix();
    }

    public float[] generateSpawn(float playerX, float playerY, float playerZ) {
        float x, y, z;
        do {
            x = rand.nextFloat() * (2 * spawnSize) - spawnSize;
            y = rand.nextFloat() * (2 * spawnSize) - spawnSize;
            z = rand.nextFloat() * (2 * spawnSize) - spawnSize;
        } while (
                x > playerX - exclusionSize && x < playerX + exclusionSize &&
                        y > playerY - exclusionSize && y < playerY + exclusionSize &&
                        z > playerZ - exclusionSize && z < playerZ + exclusionSize
        );

        return new float[]{x, y, z};
    }

    public void deplacement(float deltaTime) {
        Vector3f deplace = new Vector3f(direction).mul(speed * deltaTime);
        position.add(deplace);
        updateModelMatrix(); // Mise à jour à chaque frame
    }

    private void updateModelMatrix() {
        modelMatrix.identity().translate(position);
    }

    public boolean shouldDespawn(Vector3f cameraPos) {
        return position.distance(cameraPos) > despawnDistance;
    }

    public void render(Matrix4f view, Matrix4f projection) {
        shader.bind();
        shader.setUniformMat4f("view", view);
        shader.setUniformMat4f("projection", projection);
        shader.setUniformMat4f("model", modelMatrix);

        // Rendu normal
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        corps.setColor(0f, 0f, 0f, 1f);
        corps.render();

        // Outline si highlight
        if (highlighted) {
            Matrix4f outlineModel = new Matrix4f(modelMatrix).scale(1.05f);

            shader.setUniformMat4f("model", outlineModel);

            glEnable(GL_DEPTH_TEST);
            glDepthMask(false);

            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            glLineWidth(2.5f);
            corps.setColor(1f, 0f, 0f, 1f);
            corps.render();

            glDepthMask(true);
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

            shader.setUniformMat4f("model", modelMatrix);
        }

        shader.unbind();
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    public void cleanup() {
        corps.cleanup();
    }

    public Shape getCorps() {
        return corps;
    }

    public int getVie() {
        return vie;
    }

    public void decrementVie() {
        if (vie > 0) vie--;
    }

    public int getScore() {
        return score;
    }

    public float getDespawnDistance() {
        return despawnDistance;
    }

    public void setHighlighted(boolean h) {
        this.highlighted = h;
    }

    public static void setDespawnDistance(float d) {
        despawnDistance = d;
    }

    public static void setSpeed(float s) {
        speed = s;
    }
}
