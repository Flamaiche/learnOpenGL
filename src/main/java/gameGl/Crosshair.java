package gameGl;

import learnGL.tools.Shape;
import learnGL.tools.Shader;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11C.*;

public class Crosshair {

    private final Shape shape;
    private final Shader shader;
    private final Vector3f position = new Vector3f();

    // Rayon invisible pour les collisions
    private final Shape rayShape;
    private final Matrix4f rayModel = new Matrix4f();

    public Crosshair(Shader shader) {
        this.shader = shader;

        // géométrie du crosshair
        float len = 1.0f;
        float gap = 0.5f;
        float t   = 0.12f;

        float[] verts = createCrosshairPositions(len, gap, t);
        this.shape = new Shape(Shape.autoAddSlotColor(verts));
        this.shape.setColor(1f, 1f, 1f, 1f);
        this.shape.setShader(shader);

        // Rayon pour collision : long rectangle très fin
        float size = 0.2f;
        float length = 100f;
        float[] rayVerts = new float[]{
                -size/2, -size/2, 0f,
                size/2, -size/2, 0f,
                size/2,  size/2, 0f,
                -size/2, -size/2, 0f,
                size/2,  size/2, 0f,
                -size/2,  size/2, length
        };
        this.rayShape = new Shape(Shape.autoAddSlotColor(rayVerts));
        this.rayShape.setShader(shader);
        this.rayShape.setColor(1f, 0f, 0f, 0f); // invisible alpha=0
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

    private static float[] createCrosshairPositions(float len, float gap, float t) {
        float[] v = new float[24*3];
        int[] idx = new int[]{0};
        float halfT = t*0.5f;
        float halfGap = gap*0.5f;
        // gauche
        putRect(v, idx, -(halfGap+len), -halfT, -halfGap, +halfT);
        // droite
        putRect(v, idx, +halfGap, -halfT, +(halfGap+len), +halfT);
        // bas
        putRect(v, idx, -halfT, -(halfGap+len), +halfT, -halfGap);
        // haut
        putRect(v, idx, -halfT, +halfGap, +halfT, +(halfGap+len));
        return v;
    }

    public void setPosition(Vector3f p) {
        this.position.set(p);
    }

    public void render(Matrix4f view, Matrix4f projection, Vector3f cameraFront, Vector3f cameraUp, float scale) {
        Vector3f f = new Vector3f(cameraFront).normalize();
        Vector3f r = new Vector3f();
        cameraUp.cross(f, r).normalize();
        Vector3f u = new Vector3f(f).cross(r);

        Matrix4f model = new Matrix4f()
                .identity()
                .translate(position)
                .m00(r.x).m01(r.y).m02(r.z)
                .m10(u.x).m11(u.y).m12(u.z)
                .m20(f.x).m21(f.y).m22(f.z)
                .scale(scale);

        boolean depth = glIsEnabled(GL_DEPTH_TEST);
        if (depth) glDisable(GL_DEPTH_TEST);

        shader.bind();
        shader.setUniformMat4f("model", model);
        shader.setUniformMat4f("view", view);
        shader.setUniformMat4f("projection", projection);

        shape.render();

        shader.unbind();
        if (depth) glEnable(GL_DEPTH_TEST);
    }

    /**
     * Highlight de l'ennemi visé via un rayon invisible
     */
    public void updateHighlightedEnemy(Ennemis[] ennemis, Vector3f cameraPos, Vector3f cameraFront) {
        for (Ennemis e : ennemis) e.setHighlighted(false);

        Ennemis closest = null;
        float minDistance = Float.MAX_VALUE;

        // Rayon : position = caméra
        rayModel.identity().translate(cameraPos);
        // Orientation : aligné sur cameraFront
        Vector3f f = new Vector3f(cameraFront).normalize();
        Vector3f up = new Vector3f(0,1,0);
        Vector3f right = new Vector3f();
        up.cross(f, right).normalize();
        Vector3f u = new Vector3f(f).cross(right);
        rayModel.m00(right.x); rayModel.m01(right.y); rayModel.m02(right.z);
        rayModel.m10(u.x);     rayModel.m11(u.y);     rayModel.m12(u.z);
        rayModel.m20(f.x);     rayModel.m21(f.y);     rayModel.m22(f.z);

        for (Ennemis e : ennemis) {
            if (rayShape.intersectsOptimized(e.getCorps(), rayModel, e.getModelMatrix())) {
                float distance = new Vector3f(e.getModelMatrix().getTranslation(new Vector3f()))
                        .sub(cameraPos).length();
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = e;
                }
            }
        }

        if (closest != null) closest.setHighlighted(true);
    }

    public void cleanup() {
        shape.cleanup();
        rayShape.cleanup();
    }
}
