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
    private final Vector3f position = new Vector3f();

    private final Shape rayShape;
    private final Matrix4f rayModel = new Matrix4f();
    private final Vector3f tmpVec = new Vector3f();
    private final Vector3f camPos = new Vector3f();

    public Crosshair(Shader shader) {
        this.shader = shader;

        // --- Crosshair visible ---
        float len = 1.0f, gap = 0.5f, t = 0.12f;
        float[] verts = createCrosshairPositions(len, gap, t);
        shape = new Shape(Shape.autoAddSlotColor(verts));
        shape.setColor(1f, 1f, 1f, 1f);
        shape.setShader(shader);

        // --- Rayon ultra-fin ---
        float w = 0.00001f, h = 0.00001f, l = 200f;
        float[] rayVerts = new float[]{
                -w/2, -h/2, 0f,
                w/2, -h/2, 0f,
                w/2,  h/2, 0f,
                -w/2, -h/2, 0f,
                w/2,  h/2, 0f,
                -w/2,  h/2, l
        };
        rayShape = new Shape(Shape.autoAddSlotColor(rayVerts));
        rayShape.setShader(shader);
        rayShape.setColor(1f, 0f, 0f, 0f);
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

    public void setPosition(Vector3f p) { position.set(p); }

    public void render(Matrix4f view, Matrix4f projection, Camera camera, float scale) {
        Vector3f f = camera.getFront();
        Vector3f u = camera.getUp();
        Vector3f r = camera.getDroite();

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

    public void updateHighlightedEnemy(Ennemis[] ennemis, Camera camera) {
        for (Ennemis e : ennemis) e.setHighlighted(false);

        Ennemis closest = null;
        float minDistance = Float.MAX_VALUE;

        // Recalcule le rayon à CHAQUE frame
        camPos.set(camera.getPosition());
        Vector3f f = camera.getFront();
        Vector3f u = camera.getUp();
        Vector3f r = camera.getDroite();

        rayModel.identity()
                .translate(camPos)
                .m00(r.x).m01(r.y).m02(r.z)
                .m10(u.x).m11(u.y).m12(u.z)
                .m20(f.x).m21(f.y).m22(f.z);

        // Test de collision direct rayon <-> ennemis
        for (Ennemis e : ennemis) {
            tmpVec.set(e.getModelMatrix().getTranslation(tmpVec)).sub(camPos);
            float distance = tmpVec.length();

            if (rayShape.intersectsOptimized(e.getCorps(), rayModel, e.getModelMatrix())) {
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
