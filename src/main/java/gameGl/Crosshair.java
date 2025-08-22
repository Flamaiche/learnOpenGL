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

    // Géométrie “unitaire” : 4 segments courts (style FPS) autour d’un gap central.
    // On applique l’échelle finale via la matrice modèle pour garder une taille écran constante.
    public Crosshair(Shader shader) {
        this.shader = shader;

        // longueur d’un bras, gap au centre, épaisseur (en unités “modèle”)
        float len = 1.0f;     // longueur de bras
        float gap = 0.5f;     // trou au centre
        float t   = 0.12f;    // épaisseur

        float[] verts = createCrosshairPositions(len, gap, t);
        // IMPORTANT : on passe UNIQUEMENT des positions à autoAddSlotColor
        this.shape = new Shape(Shape.autoAddSlotColor(verts));
        this.shape.setColor(1f, 1f, 1f, 1f);
        this.shape.setShader(shader);
    }

    private static void putRect(float[] a, int[] idx, float x1, float y1, float x2, float y2) {
        // deux triangles (x1,y1)-(x2,y1)-(x2,y2) et (x1,y1)-(x2,y2)-(x1,y2)
        int i = idx[0];
        // tri 1
        a[i++] = x1; a[i++] = y1; a[i++] = 0f;
        a[i++] = x2; a[i++] = y1; a[i++] = 0f;
        a[i++] = x2; a[i++] = y2; a[i++] = 0f;
        // tri 2
        a[i++] = x1; a[i++] = y1; a[i++] = 0f;
        a[i++] = x2; a[i++] = y2; a[i++] = 0f;
        a[i++] = x1; a[i++] = y2; a[i++] = 0f;
        idx[0] = i;
    }

    private static float[] createCrosshairPositions(float len, float gap, float t) {
        // 4 rectangles (segments), 6 sommets chacun -> 24 sommets -> 24 * 3 floats
        float[] v = new float[24 * 3];
        int[] idx = new int[]{0};

        float halfT = t * 0.5f;
        float halfGap = gap * 0.5f;

        // segment gauche : x in [-(halfGap+len), -halfGap], y in [-halfT, +halfT]
        putRect(v, idx, -(halfGap + len), -halfT, -halfGap, +halfT);

        // segment droit : x in [+halfGap, +halfGap+len], y in [-halfT, +halfT]
        putRect(v, idx, +halfGap, -halfT, +(halfGap + len), +halfT);

        // segment bas : y in [-(halfGap+len), -halfGap], x in [-halfT, +halfT]
        putRect(v, idx, -halfT, -(halfGap + len), +halfT, -halfGap);

        // segment haut : y in [+halfGap, +halfGap+len], x in [-halfT, +halfT]
        putRect(v, idx, -halfT, +halfGap, +halfT, +(halfGap + len));

        return v;
    }

    public void setPosition(Vector3f p) {
        this.position.set(p);
    }


    public void render(Matrix4f view, Matrix4f projection, Vector3f cameraFront, Vector3f cameraUp, float scale) {
        // Crée un repère “billboard” face caméra
        Vector3f f = new Vector3f(cameraFront).normalize();    // avant
        Vector3f r = new Vector3f();
        cameraUp.cross(f, r).normalize();                     // droite
        Vector3f u = new Vector3f(f).cross(r);                // up corrigé

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


    public void cleanup() {
        shape.cleanup();
        shader.cleanup();
    }
}
