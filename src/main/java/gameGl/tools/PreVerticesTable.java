package gameGl.tools;

import gameGl.utils.Ball;
import gameGl.utils.Ennemis;
import gameGl.utils.Entity2D;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class PreVerticesTable {

    public static float[] generateCubeSimple(float n) {
        float h = n / 2f;

        return new float[]{
                // Face avant
                -h, -h,  h,   h, -h,  h,   h,  h,  h,
                h,  h,  h,  -h,  h,  h,  -h, -h,  h,
                // Face arrière
                -h, -h, -h,  -h,  h, -h,   h,  h, -h,
                h,  h, -h,   h, -h, -h,  -h, -h, -h,
                // Face gauche
                -h, -h, -h,  -h, -h,  h,  -h,  h,  h,
                -h,  h,  h,  -h,  h, -h,  -h, -h, -h,
                // Face droite
                h, -h, -h,   h,  h, -h,   h,  h,  h,
                h,  h,  h,   h, -h,  h,   h, -h, -h,
                // Face haute
                -h,  h, -h,  -h,  h,  h,   h,  h,  h,
                h,  h,  h,   h,  h, -h,  -h,  h, -h,
                // Face basse
                -h, -h, -h,   h, -h, -h,   h, -h,  h,
                h, -h,  h,  -h, -h,  h,  -h, -h, -h
        };
    }

    public static float[] generatePyramid(float baseSize, float height) {
        float h = baseSize / 2f;

        return new float[]{
                // Base (2 triangles)
                -h, 0, -h,   h, 0, -h,   h, 0,  h,
                h, 0,  h,  -h, 0,  h,  -h, 0, -h,

                // Face avant
                -h, 0,  h,    h, 0,  h,    0, height, 0,
                // Face arrière
                -h, 0, -h,    0, height, 0,    h, 0, -h,
                // Face gauche
                -h, 0, -h,   -h, 0,  h,    0, height, 0,
                // Face droite
                h, 0, -h,    h, 0,  h,    0, height, 0
        };
    }

    public static float[] generatePyramid(float sideLength) {
        // Calcul de la hauteur d'un triangle équilatéral
        float hTriangle = (float) (Math.sqrt(3) / 2 * sideLength);

        // Hauteur de la pyramide tétraédrique
        float height = (float) (Math.sqrt(2.0 / 3.0) * sideLength);

        // Sommets de la base (triangle équilatéral) dans le plan XY
        Vector3f v0 = new Vector3f(-sideLength / 2, 0, -hTriangle / 3);
        Vector3f v1 = new Vector3f(sideLength / 2, 0, -hTriangle / 3);
        Vector3f v2 = new Vector3f(0, 0, 2 * hTriangle / 3);

        // Sommet supérieur (apex)
        Vector3f apex = new Vector3f(0, height, 0);

        return new float[]{
                // Base (triangle)
                v0.x, v0.y, v0.z,
                v1.x, v1.y, v1.z,
                v2.x, v2.y, v2.z,

                // Face 1
                v0.x, v0.y, v0.z,
                v1.x, v1.y, v1.z,
                apex.x, apex.y, apex.z,

                // Face 2
                v1.x, v1.y, v1.z,
                v2.x, v2.y, v2.z,
                apex.x, apex.y, apex.z,

                // Face 3
                v2.x, v2.y, v2.z,
                v0.x, v0.y, v0.z,
                apex.x, apex.y, apex.z
        };
    }

    /**
     * Manager pour tous les objets 2D fixes à l'écran.
     */
    public static class Manager2D {

        public static void updateAll(ArrayList<? extends Entity2D> entities) {
            for (Entity2D e : entities) {
                e.update();
            }
        }

        public static void renderAll(ArrayList<? extends Entity2D> entities, Matrix4f orthoProjection) {
            for (Entity2D e : entities) {
                e.render(orthoProjection);
            }
        }

        public static void cleanupAll(ArrayList<? extends Entity2D> entities) {
            for (Entity2D e : entities) {
                e.cleanup();
            }
        }
    }

    public static class Manager3D {

        private Manager3D() {}

        // Update toutes les entités et retourne le score gagné
        public static int updateAll(ArrayList<Ennemis> ennemis, ArrayList<Ball> balls, float deltaTime) {
            int score = 0;

            for (Ennemis e : ennemis) {
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
}
