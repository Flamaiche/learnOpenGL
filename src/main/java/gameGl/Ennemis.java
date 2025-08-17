package gameGl;

import gameGl.tools.PreVerticesTable;
import learnGL.tools.Shader;
import learnGL.tools.Shape;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Random;

public class Ennemis {
    private Random rand = new Random();
    private float spawnSize = 5.0f;     // Taille de la zone de spawn
    private float exclusionSize = 2.0f; // Zone interdite autour du joueur

    private Shape corps;
    private Shader shader;
    private Vector3f position;
    private Vector3f direction;

    private int vie;
    private int score;

    public Ennemis(Shape corps, int vie, int score, Vector3f direction, Shader shader, float[] centerPlayer) {
        this.corps = corps;
        this.vie = vie;
        this.score = score;
        this.direction = direction;
        this.shader = shader;
        this.position = new Vector3f(corps.center()[0], corps.center()[1], corps.center()[2]);
        corps.setShader(shader);
    }

    public Ennemis(Shader shader, float[] centerPlayer) {
        // shape
        corps = new Shape(Shape.autoAddSlotColor(PreVerticesTable.generateCubeSimple(1f)));
        float[] coors = generateSpawn(centerPlayer[0], centerPlayer[1], centerPlayer[2]);
        position = new Vector3f(coors[0], coors[1], coors[2]);

        corps.setColor(0f, 0f, 0f, 1f); // Couleur noire
        corps.setShader(shader);
        this.shader = shader;

        // direction aléatoire
        float[] direc = generateSpawn(centerPlayer[0], centerPlayer[1], centerPlayer[2]);
        direction = new Vector3f(direc[0], direc[1], direc[2]);

        vie = 1;
        score = 10;
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

    public Shape getCorps() {
        return corps;
    }

    public void render(Matrix4f view, Matrix4f projection) {
        Matrix4f model = new Matrix4f()
                .identity()
                .translate(position); // applique la position de l’ennemi

        shader.bind();
        shader.setUniformMat4f("view", view);
        shader.setUniformMat4f("projection", projection);
        shader.setUniformMat4f("model", model);

        corps.render();
        shader.unbind();
    }

    public void cleanup() {
        corps.cleanup();
    }

    public void deplacement() {
        // TODO: bouger selon la direction si besoin
    }
}
