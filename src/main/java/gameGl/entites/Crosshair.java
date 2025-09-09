package gameGl.entites;

import learnGL.tools.Shape;
import learnGL.tools.Shader;
import learnGL.tools.Camera;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11C.*;

public class Crosshair extends Entity2D {

    private final Shape shape;
    private final Shader shader;

    private final Vector3f rayOrigin = new Vector3f();
    private final Vector3f rayDir = new Vector3f();

    // Dimensions actuelles de la fenêtre
    private int lastWidth = 800, lastHeight = 600;

    // Paramètres de base du réticule (en unités OpenGL)
    private final float longueurSegment = 0.04f;    // Longueur des 4 branches
    private final float espaceCentral = 0.02f;      // Espace vide au centre
    private final float epaisseurLigne = 0.005f;    // Épaisseur des lignes

    public Crosshair(Shader shader) {
        this.shader = shader;

        // Création initiale du crosshair centré
        float[] verts = createCrosshairPositions(longueurSegment, espaceCentral, epaisseurLigne);
        verts = Shape.autoAddSlotColor(verts); // Ajout des slots pour la couleur RGBA

        shape = new Shape(verts);
        shape.setColor(1f, 0f, 1f, 1f); // Violet opaque
        shape.setShader(shader);
    }

    /** Génère les vertices du réticule en fonction des dimensions données */
    private static float[] createCrosshairPositions(float len, float gap, float t) {
        float[] v = new float[24 * 3]; // 4 segments * 6 vertices * 3 coordonnées
        int[] idx = new int[]{0};

        float halfT = t * 0.5f;
        float halfGap = gap * 0.5f;

        // Branche gauche
        putRect(v, idx, -(halfGap + len), -halfT, -halfGap, +halfT);
        // Branche droite
        putRect(v, idx, +halfGap, -halfT, +(halfGap + len), +halfT);
        // Branche bas
        putRect(v, idx, -halfT, -(halfGap + len), +halfT, -halfGap);
        // Branche haut
        putRect(v, idx, -halfT, +halfGap, +halfT, +(halfGap + len));

        return v;
    }

    /** Ajoute un rectangle dans le tableau de vertices */
    private static void putRect(float[] a, int[] idx, float x1, float y1, float x2, float y2) {
        int i = idx[0];
        a[i++] = x1; a[i++] = y1; a[i++] = 0f;
        a[i++] = x2; a[i++] = y1; a[i++] = 0f;
        a[i++] = x2; a[i++] = y2; a[i++] = 0f;
        a[i++] = x1; a[i++] = y1; a[i++] = 0f;
        a[i++] = x2; a[i++] = y2; a[i++] = 0f;
        a[i++] = x1; a[i++] = y2; a[i++] = 0f;
        idx[0] = i;
    }

    /** Rendu du crosshair */
    @Override
    public void render(Matrix4f orthoProjection) {
        boolean depth = glIsEnabled(GL_DEPTH_TEST);
        if (depth) glDisable(GL_DEPTH_TEST); // HUD doit être au-dessus de tout

        shader.bind();

        // Correction du ratio pour éviter la déformation
        float scaleX = (float) lastHeight / (float) lastWidth; // ajustement horizontal
        float scaleY = 1.0f;                                   // vertical reste normal

        Matrix4f model = new Matrix4f()
                .identity()
                .scale(scaleX, scaleY, 1.0f); // conserve un crosshair carré

        shader.setUniformMat4f("model", model);
        shader.setUniformMat4f("view", new Matrix4f().identity());
        shader.setUniformMat4f("projection", orthoProjection);

        shape.render();
        shader.unbind();

        if (depth) glEnable(GL_DEPTH_TEST); // réactiver la profondeur
    }

    /** Met à jour dynamiquement la taille du crosshair selon la fenêtre */
    public void update(int width, int height) {
        // Ne rien faire si la taille est inchangée
        if (width == lastWidth && height == lastHeight) return;

        lastWidth = width;
        lastHeight = height;

        // Utilise la plus petite dimension pour garder les proportions
        float minDim = Math.min(width, height);

        // Calcule les dimensions proportionnelles
        float longueur = (minDim / 600f) * longueurSegment;
        float espace = (minDim / 600f) * espaceCentral;
        float epaisseur = (minDim / 600f) * epaisseurLigne;

        // Génération des nouvelles positions
        float[] newVerts = createCrosshairPositions(longueur, espace, epaisseur);
        newVerts = Shape.autoAddSlotColor(newVerts);

        // Mise à jour dans le VBO via Shape
        shape.updatePositions(newVerts);
    }

    /** Détecte et met en surbrillance l'ennemi le plus proche au centre de l'écran */
    public void updateHighlightedEnemy(java.util.ArrayList<Ennemis> ennemis, Camera camera) {
        for (Ennemis e : ennemis) e.setHighlighted(false);

        Ennemis closest = null;
        float minDistance = Float.MAX_VALUE;

        rayOrigin.set(camera.getPosition());
        rayDir.set(camera.getFront()).normalize();

        for (Ennemis e : ennemis) {
            float t = e.getCorps().intersectRayDistance(rayOrigin, rayDir, e.getModelMatrix());
            if (t >= 0 && t < minDistance) {
                minDistance = t;
                closest = e;
            }
        }

        if (closest != null) closest.setHighlighted(true);
    }

    @Override
    public void cleanup() {
        shape.cleanup();
    }
}
