package gameGl.utils;

import learnGL.tools.Shader;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Random;

public class Ennemis extends Entity {
    private final Random rand = new Random();
    private Vector3f direction;
    private Vector3f target;

    private int vie;
    private int score;

    public static float speed = 2.5f;
    public static float despawnDistance = 150f;

    private final float spawnSize = 10.0f;
    private final float exclusionSize = 0.0f;

    public Ennemis(Shader shader, float[] centerPlayer, float[] verticesShape) {
        super(shader, verticesShape);
        corps.setColor(0f, 0f, 0f, 1f);

        vie = 1;
        score = 10;

        setDeplacement(centerPlayer);
    }

    public void setDeplacement(float[] centerPlayer) {
        float[] coors = generateSpawn(centerPlayer[0], centerPlayer[1], centerPlayer[2]);
        position.set(coors[0], coors[1], coors[2]);

        float[] targetCoords = generateSpawn(centerPlayer[0], centerPlayer[1], centerPlayer[2]);
        target = new Vector3f(targetCoords[0], targetCoords[1], targetCoords[2]);

        direction = new Vector3f(target).sub(position).normalize();
        modelDirty = true;
    }

    private float[] generateSpawn(float playerX, float playerY, float playerZ) {
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

    @Override
    public void update(float deltaTime) {
        Vector3f deplace = new Vector3f(direction).mul(speed * deltaTime);
        position.add(deplace);
        modelDirty = true;
    }

    public boolean shouldDespawn(Vector3f cameraPos) {
        return position.distance(cameraPos) > despawnDistance;
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

    public static void setDespawnDistance(float d) {
        despawnDistance = d;
    }

    public static void setSpeed(float s) {
        speed = s;
    }
}
