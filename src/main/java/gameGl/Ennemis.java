package gameGl;

import learnGL.tools.Shader;
import learnGL.tools.Shape;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Random;

import static org.lwjgl.opengl.GL11C.*;

public class Ennemis {
    private Random rand = new Random();
    private float spawnSize = 10.0f;     // Taille de la zone de spawn
    private float exclusionSize = 0.0f;  // Zone interdite autour du joueur

    private Shape corps;
    private Shader shader;
    private Vector3f position;
    private Vector3f direction;
    private Vector3f target;               // point vers lequel l'ennemi se déplace
    private float speed = 2.5f;            // Vitesse de déplacement
    private float despawnDistance = 150f; // distance fixe pour éviter téléport
    private boolean highlighted = false;

    private int vie;
    private int score;

    public Ennemis(Shader shader, float[] centerPlayer, float[] verticesShape) {
        corps = new Shape(Shape.autoAddSlotColor(verticesShape));
        corps.setColor(0f, 0f, 0f, 1f);
        corps.setShader(shader);
        this.shader = shader;

        vie = 1;
        score = 10;

        setDeplacement(centerPlayer);
    }

    public void setDeplacement(float[] centerPlayer) {
        // position aléatoire
        float[] coors = generateSpawn(centerPlayer[0], centerPlayer[1], centerPlayer[2]);
        position = new Vector3f(coors[0], coors[1], coors[2]);

        // cible aléatoire
        float[] targetCoords = generateSpawn(centerPlayer[0], centerPlayer[1], centerPlayer[2]);
        target = new Vector3f(targetCoords[0], targetCoords[1], targetCoords[2]);

        // direction vers la cible
        direction = new Vector3f(target).sub(position).normalize();
        System.out.println(target + " "  + position + " " + direction);
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
        // déplacement vers la cible
        Vector3f deplace = new Vector3f(direction).mul(speed * deltaTime);
        position.add(deplace);
    }

    public boolean shouldDespawn(Vector3f cameraPos) {
        return position.distance(cameraPos) > despawnDistance;
    }

    public void render(Matrix4f view, Matrix4f projection) {
        Matrix4f model = getModelMatrix();

        shader.bind();
        shader.setUniformMat4f("view", view);
        shader.setUniformMat4f("projection", projection);
        shader.setUniformMat4f("model", model);

        // Rendu normal
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        corps.setColor(0f, 0f, 0f, 1f);
        corps.render();

        // Outline si highlight
        if (highlighted) {
            // Gonfler légèrement le mesh
            Matrix4f outlineModel = new Matrix4f(model).scale(1.05f);

            shader.setUniformMat4f("model", outlineModel);

            glEnable(GL_DEPTH_TEST);        // toujours tester la profondeur
            glDepthMask(false);             // mais ne PAS écrire dans le depth buffer

            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            glLineWidth(2.5f);
            corps.setColor(1f, 0f, 0f, 1f);
            corps.render();

            glDepthMask(true);              // réactiver écriture
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

            // remettre le model normal
            shader.setUniformMat4f("model", model);
        }


        shader.unbind();
    }


    public Matrix4f getModelMatrix() {
        return new Matrix4f()
                .translate(position);
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
        if (vie > 0) {
            vie--;
        }
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
}
