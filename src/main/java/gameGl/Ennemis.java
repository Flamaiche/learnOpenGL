package gameGl;

import gameGl.tools.PreVerticesTable;
import learnGL.tools.Shader;
import learnGL.tools.Shape;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Random;

public class Ennemis {
    private Random rand = new Random();
    private float spawnSize = 10.0f;     // Taille de la zone de spawn
    private float exclusionSize = 0.0f;  // Zone interdite autour du joueur

    private Shape corps;
    private Shader shader;
    private Vector3f position;
    private Vector3f direction;
    private Vector3f target;               // point vers lequel l'ennemi se déplace
    private float speed = 5.0f;            // Vitesse de déplacement

    private int vie;
    private int score;

    public Ennemis(Shader shader, float[] centerPlayer) {
        corps = new Shape(Shape.autoAddSlotColor(PreVerticesTable.generateCubeSimple(1f)));
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
        float despawnDistance = 150f; // distance fixe pour éviter téléport
        return position.distance(cameraPos) > despawnDistance;
    }

    public void render(Matrix4f view, Matrix4f projection) {
        Matrix4f model = new Matrix4f().identity().translate(position);

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

    public Shape getCorps() {
        return corps;
    }
}
