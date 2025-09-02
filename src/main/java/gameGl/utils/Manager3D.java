package gameGl.utils;

import org.joml.Matrix4f;

import java.util.List;

public class Manager3D {

    private Manager3D() {} // empêche l'instanciation

    /**
     * Met à jour toutes les entités et retourne le score généré par les Ball.
     */
    public static int updateAll(List<Entity> entities, float deltaTime, List<Ennemis> enemies) {
        int score = 0;

        for (Entity e : entities) {
            e.update(deltaTime);

            if (e instanceof Ball b) {
                score += b.collisionScore(enemies.toArray(new Ennemis[0]));
            }
        }

        return score;
    }

    /**
     * Rend toutes les entités 3D.
     */
    public static void renderAll(List<Entity> entities, Matrix4f view, Matrix4f projection) {
        for (Entity e : entities) {
            e.render(view, projection);
        }
    }

    /**
     * Nettoie toutes les entités et vide la liste.
     */
    public static void cleanupAll(List<Entity> entities) {
        for (Entity e : entities) e.cleanup();
        entities.clear();
    }
}
