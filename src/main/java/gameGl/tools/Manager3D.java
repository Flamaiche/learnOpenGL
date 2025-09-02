package gameGl.tools;

import gameGl.utils.Ball;
import gameGl.utils.Ennemis;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Manager3D {

    private Manager3D() {}

    // Update toutes les entités et retourne le score gagné
    public static int updateAll(ArrayList<Ennemis> ennemis, ArrayList<Ball> balls, float deltaTime, Vector3f playerPos) {
        int score = 0;

        for (Ennemis e : ennemis) {
            if (e.shouldDespawn(playerPos)) {
                e.setDeplacement(new float[]{0f,0f,0f});
            }
            e.update(deltaTime);
        }

        for (Ball b : balls) {
            if (!b.isActive()) continue;

            b.update(deltaTime);

            score += b.collisionScore(ennemis.toArray(new Ennemis[0]));
        }

        return score;
    }

    public static void renderAll(ArrayList<Ennemis> ennemis, ArrayList<Ball> balls, Matrix4f view, Matrix4f projection) {
        for (Ennemis e : ennemis) e.render(view, projection);
        for (Ball b : balls) b.render(view, projection);
    }

    public static void cleanupAll(ArrayList<Ennemis> ennemis, ArrayList<Ball> balls) {
        for (Ennemis e : ennemis) e.cleanup();
        for (Ball b : balls) b.cleanup();
    }
}
