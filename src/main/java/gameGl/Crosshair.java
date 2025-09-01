package gameGl;

import learnGL.tools.Shape;
import learnGL.tools.Shader;
import learnGL.tools.Camera;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11C.*;

public class Crosshair {

    private final Shape shape;
    private final Shader shader;

    // Rayon mathématique
    private final Vector3f rayOrigin = new Vector3f();
    private final Vector3f rayDir = new Vector3f();

    public Crosshair(Shader shader) {
        this.shader = shader;

        // --- Crosshair 2D au centre de l'écran ---
        float len = 0.05f, gap = 0.02f, t = 0.005f;
        float[] verts = createCrosshairPositions(len, gap, t);
        shape = new Shape(Shape.autoAddSlotColor(verts));
        shape.setColor(1f, 1f, 1f, 1f);
        shape.setShader(shader);
    }

    private static float[] createCrosshairPositions(float len, float gap, float t) {
        float[] v = new float[24*3];
        int[] idx = new int[]{0};
        float halfT = t*0.5f, halfGap = gap*0.5f;
        putRect(v, idx, -(halfGap+len), -halfT, -halfGap, +halfT);
        putRect(v, idx, +halfGap, -halfT, +(halfGap+len), +halfT);
        putRect(v, idx, -halfT, -(halfGap+len), +halfT, -halfGap);
        putRect(v, idx, -halfT, +halfGap, +halfT, +(halfGap+len));
        return v;
    }

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

    /** Rendu du crosshair au centre de l'écran (2D) */
    public void render(Matrix4f orthoProjection) {
        boolean depth = glIsEnabled(GL_DEPTH_TEST);
        if (depth) glDisable(GL_DEPTH_TEST);

        shader.bind();
        Matrix4f model = new Matrix4f().identity();
        shader.setUniformMat4f("model", model);
        shader.setUniformMat4f("view", new Matrix4f().identity());
        shader.setUniformMat4f("projection", orthoProjection);
        shape.render();
        shader.unbind();

        if (depth) glEnable(GL_DEPTH_TEST);
    }

    /** Met à jour l'ennemi le plus proche dans le centre de l'écran */
    public void updateHighlightedEnemy(Ennemis[] ennemis, Camera camera) {
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

    public void cleanup() {
        shape.cleanup();
    }
}
