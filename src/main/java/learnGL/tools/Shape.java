package learnGL.tools;

import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

public class Shape {
    private final float[] vertices;
    private final int vaoId;
    private final int vboId;
    private final int vertexCount;
    public static int drawMode = GL_TRIANGLES;

    private static final int FLOATS_PER_VERTEX = 8; // 3 pos + 3 couleur + 2 texture
    private static final int FLOAT_SIZE_BYTES = 4;

    private Shader shader = null;
    private Texture texture = null;  // Texture associée

    public Shape(float[] vertices) {
        this.vertexCount = vertices.length / FLOATS_PER_VERTEX;
        this.vertices = vertices;

        // Création VAO
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // Création VBO
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        // Remplissage VBO
        FloatBuffer buffer = MemoryUtil.memAllocFloat(vertices.length);
        buffer.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        MemoryUtil.memFree(buffer);

        // Attributs
        // 0 : position (x, y, z)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, FLOATS_PER_VERTEX * FLOAT_SIZE_BYTES, 0);
        glEnableVertexAttribArray(0);

        // 1 : couleur (r, g, b)
        glVertexAttribPointer(1, 3, GL_FLOAT, false, FLOATS_PER_VERTEX * FLOAT_SIZE_BYTES, 3 * FLOAT_SIZE_BYTES);
        glEnableVertexAttribArray(1);

        // 2 : texture (u, v)
        glVertexAttribPointer(2, 2, GL_FLOAT, false, FLOATS_PER_VERTEX * FLOAT_SIZE_BYTES, 6 * FLOAT_SIZE_BYTES);
        glEnableVertexAttribArray(2);

        // Nettoyage
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public Shape(float[] logicalVertices, int logicalWidth, int logicalHeight) {
        this(convertLogicalToNormalized(logicalVertices, logicalWidth, logicalHeight));
    }

    public void setShader(Shader shader) {
        this.shader = shader;
    }

    public Shader getShader() {
        return shader;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public Texture getTexture() {
        return texture;
    }

    public float[] getVertices() {
        return vertices;
    }

    public void render() {
        if (texture != null) texture.bind();

        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0); // position
        glEnableVertexAttribArray(1); // couleur
        glEnableVertexAttribArray(2); // texture coord

        glDrawArrays(drawMode, 0, vertexCount);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);

        if (texture != null) texture.unbind();
    }

    public void cleanup() {
        glDisableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vboId);
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
        // Shader et texture gérés séparément
    }

    public float[] center() {
        float[] center = new float[3];
        for (int i = 0; i < vertexCount; i++) {
            center[0] += vertices[i * FLOATS_PER_VERTEX];
            center[1] += vertices[i * FLOATS_PER_VERTEX + 1];
            center[2] += vertices[i * FLOATS_PER_VERTEX + 2];
        }
        center[0] /= vertexCount;
        center[1] /= vertexCount;
        center[2] /= vertexCount;
        return center;
    }

    public Shape clone() {
        Shape cloneShape = new Shape(vertices.clone());
        cloneShape.setShader(this.shader);
        cloneShape.setTexture(this.texture);
        return cloneShape;
    }

    // Conversion logique -> NDC
    public static float[] convertLogicalToNormalized(float[] logicalVertices, int logicalWidth, int logicalHeight) {
        float[] normalizedVertices = new float[logicalVertices.length];
        int vertexCount = logicalVertices.length / FLOATS_PER_VERTEX;

        for (int i = 0; i < vertexCount; i++) {
            float x_log = logicalVertices[i * FLOATS_PER_VERTEX];
            float y_log = logicalVertices[i * FLOATS_PER_VERTEX + 1];
            float z_log = logicalVertices[i * FLOATS_PER_VERTEX + 2];

            float x_ndc = (x_log / (logicalWidth / 2f)) - 1f;
            float y_ndc = (y_log / (logicalHeight / 2f)) - 1f;

            normalizedVertices[i * FLOATS_PER_VERTEX] = x_ndc;
            normalizedVertices[i * FLOATS_PER_VERTEX + 1] = y_ndc;
            normalizedVertices[i * FLOATS_PER_VERTEX + 2] = z_log;

            // Couleur
            normalizedVertices[i * FLOATS_PER_VERTEX + 3] = logicalVertices[i * FLOATS_PER_VERTEX + 3];
            normalizedVertices[i * FLOATS_PER_VERTEX + 4] = logicalVertices[i * FLOATS_PER_VERTEX + 4];
            normalizedVertices[i * FLOATS_PER_VERTEX + 5] = logicalVertices[i * FLOATS_PER_VERTEX + 5];

            // Texture
            normalizedVertices[i * FLOATS_PER_VERTEX + 6] = logicalVertices[i * FLOATS_PER_VERTEX + 6];
            normalizedVertices[i * FLOATS_PER_VERTEX + 7] = logicalVertices[i * FLOATS_PER_VERTEX + 7];
        }

        return normalizedVertices;
    }

    // Ajout automatique de slot couleur
    public static float[] autoAddSlotColor(float[] vertices) {
        float[] verticesFullSlot = new float[vertices.length / 3 * FLOATS_PER_VERTEX];
        for (int i = 0; i < verticesFullSlot.length / FLOATS_PER_VERTEX; i++) {
            verticesFullSlot[i * FLOATS_PER_VERTEX] = vertices[i * 3];
            verticesFullSlot[i * FLOATS_PER_VERTEX + 1] = vertices[i * 3 + 1];
            verticesFullSlot[i * FLOATS_PER_VERTEX + 2] = vertices[i * 3 + 2];
            verticesFullSlot[i * FLOATS_PER_VERTEX + 3] = 1.0f;
            verticesFullSlot[i * FLOATS_PER_VERTEX + 4] = 1.0f;
            verticesFullSlot[i * FLOATS_PER_VERTEX + 5] = 1.0f;
        }
        return verticesFullSlot;
    }

    // Ajout automatique de slot texture
    public static float[] autoAddSlotTexture(float[] vertices) {
        float[] verticesFullSlot = new float[vertices.length / 6 * FLOATS_PER_VERTEX]; // 3 pos + 3 couleur
        for (int i = 0; i < verticesFullSlot.length / FLOATS_PER_VERTEX; i++) {
            verticesFullSlot[i * FLOATS_PER_VERTEX] = vertices[i * 6];
            verticesFullSlot[i * FLOATS_PER_VERTEX + 1] = vertices[i * 6 + 1];
            verticesFullSlot[i * FLOATS_PER_VERTEX + 2] = vertices[i * 6 + 2];
            verticesFullSlot[i * FLOATS_PER_VERTEX + 3] = vertices[i * 6 + 3];
            verticesFullSlot[i * FLOATS_PER_VERTEX + 4] = vertices[i * 6 + 4];
            verticesFullSlot[i * FLOATS_PER_VERTEX + 5] = vertices[i * 6 + 5];
            verticesFullSlot[i * FLOATS_PER_VERTEX + 6] = 0.0f;
            verticesFullSlot[i * FLOATS_PER_VERTEX + 7] = 0.0f;
        }
        return verticesFullSlot;
    }

// ----------------------
// UTILITAIRES VECTORIELS
// ----------------------

    // Soustraction de vecteurs
    private float[] sub(float[] a, float[] b) {
        return new float[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    // Produit vectoriel
    private float[] cross(float[] a, float[] b) {
        return new float[]{a[1]*b[2] - a[2]*b[1],
                a[2]*b[0] - a[0]*b[2],
                a[0]*b[1] - a[1]*b[0]};
    }

    // Produit scalaire
    private float dot(float[] a, float[] b) {
        return a[0]*b[0] + a[1]*b[1] + a[2]*b[2];
    }

    // Longueur d’un vecteur
    private float length(float[] v) {
        return (float)Math.sqrt(dot(v,v));
    }

    // Projection d’un triangle sur un axe (0=x,1=y,2=z)
    private float[] projectOnAxisRaw(int axis, float[] v0, float[] v1, float[] v2) {
        float p0 = v0[axis], p1 = v1[axis], p2 = v2[axis];
        return new float[]{Math.min(p0, Math.min(p1,p2)), Math.max(p0, Math.max(p1,p2))};
    }

    // Vérifie si deux intervalles se chevauchent
    private boolean intervalsOverlap(float min1, float max1, float min2, float max2) {
        return !(max1 < min2 || max2 < min1);
    }

// ----------------------
// TRIANGLE-TRIANGLE INTERSECTION
// ----------------------

    // Test exact d’intersection entre deux triangles (Möller)
// Utilise uniquement des float[] pour éviter la création répétitive de Vector3f
    private boolean triTriIntersectRaw(float[] V, int vi, float[] U, int ui) {
        float[] V0 = {V[vi * FLOATS_PER_VERTEX], V[vi * FLOATS_PER_VERTEX + 1], V[vi * FLOATS_PER_VERTEX + 2]};
        float[] V1 = {V[(vi+1) * FLOATS_PER_VERTEX], V[(vi+1) * FLOATS_PER_VERTEX + 1], V[(vi+1) * FLOATS_PER_VERTEX + 2]};
        float[] V2 = {V[(vi+2) * FLOATS_PER_VERTEX], V[(vi+2) * FLOATS_PER_VERTEX + 1], V[(vi+2) * FLOATS_PER_VERTEX + 2]};

        float[] U0 = {U[ui * FLOATS_PER_VERTEX], U[ui * FLOATS_PER_VERTEX + 1], U[ui * FLOATS_PER_VERTEX + 2]};
        float[] U1 = {U[(ui+1) * FLOATS_PER_VERTEX], U[(ui+1) * FLOATS_PER_VERTEX + 1], U[(ui+1) * FLOATS_PER_VERTEX + 2]};
        float[] U2 = {U[(ui+2) * FLOATS_PER_VERTEX], U[(ui+2) * FLOATS_PER_VERTEX + 1], U[(ui+2) * FLOATS_PER_VERTEX + 2]};

        float[] N1 = cross(sub(V1, V0), sub(V2, V0));
        float[] N2 = cross(sub(U1, U0), sub(U2, U0));

        if (length(N1) == 0 || length(N2) == 0) return false;

        float du0 = dot(N1, sub(U0, V0));
        float du1 = dot(N1, sub(U1, V0));
        float du2 = dot(N1, sub(U2, V0));

        float dv0 = dot(N2, sub(V0, U0));
        float dv1 = dot(N2, sub(V1, U0));
        float dv2 = dot(N2, sub(V2, U0));

        float EPSILON = 1e-6f;
        if (Math.abs(du0) < EPSILON) du0 = 0;
        if (Math.abs(du1) < EPSILON) du1 = 0;
        if (Math.abs(du2) < EPSILON) du2 = 0;
        if (Math.abs(dv0) < EPSILON) dv0 = 0;
        if (Math.abs(dv1) < EPSILON) dv1 = 0;
        if (Math.abs(dv2) < EPSILON) dv2 = 0;

        if (du0 * du1 > 0 && du0 * du2 > 0) return false;
        if (dv0 * dv1 > 0 && dv0 * dv2 > 0) return false;

        float[] D = cross(N1, N2);
        int max = Math.abs(D[0]) > Math.abs(D[1]) ? 0 : 1;
        max = Math.abs(D[2]) > Math.abs(D[max]) ? 2 : max;

        float[] tri1 = projectOnAxisRaw(max, V0, V1, V2);
        float[] tri2 = projectOnAxisRaw(max, U0, U1, U2);

        return intervalsOverlap(tri1[0], tri1[1], tri2[0], tri2[1]);
    }

// ----------------------
// COLLISION SHAPE-SHAPE
// ----------------------

    // Test si cette shape intersecte une autre shape
// Utilise bounding box pour un early out rapide, puis test triangle par triangle
    public boolean intersectsOptimized(Shape other) {
        // Bounding box rapide pour early out
        float[] minA = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
        float[] maxA = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
        float[] minB = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
        float[] maxB = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};

        for (int i = 0; i < vertexCount; i++) {
            for (int j = 0; j < 3; j++) {
                float v = vertices[i * FLOATS_PER_VERTEX + j];
                minA[j] = Math.min(minA[j], v);
                maxA[j] = Math.max(maxA[j], v);
            }
        }

        int otherVertexCount = other.vertices.length / FLOATS_PER_VERTEX;
        for (int i = 0; i < otherVertexCount; i++) {
            for (int j = 0; j < 3; j++) {
                float v = other.vertices[i * FLOATS_PER_VERTEX + j];
                minB[j] = Math.min(minB[j], v);
                maxB[j] = Math.max(maxB[j], v);
            }
        }

        // Si les boxes ne se touchent pas, pas besoin de test triangle
        if (maxA[0] < minB[0] || minA[0] > maxB[0] ||
                maxA[1] < minB[1] || minA[1] > maxB[1] ||
                maxA[2] < minB[2] || minA[2] > maxB[2]) {
            return false;
        }

        // Test triangle par triangle
        for (int i = 0; i < vertexCount; i += 3) {
            for (int j = 0; j < otherVertexCount; j += 3) {
                if (triTriIntersectRaw(vertices, i, other.vertices, j)) {
                    return true;
                }
            }
        }

        return false;
    }

}