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

    // Retourne la position d’un vertex sous forme de Vector3f
    private Vector3f getVertexPos(int index) {
        return new Vector3f(
                vertices[index * FLOATS_PER_VERTEX],
                vertices[index * FLOATS_PER_VERTEX + 1],
                vertices[index * FLOATS_PER_VERTEX + 2]
        );
    }

    // Test triangle-triangle exact (Möller)
    private boolean triTriIntersect(Vector3f V0, Vector3f V1, Vector3f V2,
                                    Vector3f U0, Vector3f U1, Vector3f U2) {
        Vector3f E1 = new Vector3f(V1).sub(V0);
        Vector3f E2 = new Vector3f(V2).sub(V0);
        Vector3f N1 = new Vector3f(E1).cross(E2);

        Vector3f F1 = new Vector3f(U1).sub(U0);
        Vector3f F2 = new Vector3f(U2).sub(U0);
        Vector3f N2 = new Vector3f(F1).cross(F2);

        if (N1.length() == 0 || N2.length() == 0) return false;

        float du0 = N1.dot(new Vector3f(U0).sub(V0));
        float du1 = N1.dot(new Vector3f(U1).sub(V0));
        float du2 = N1.dot(new Vector3f(U2).sub(V0));

        float dv0 = N2.dot(new Vector3f(V0).sub(U0));
        float dv1 = N2.dot(new Vector3f(V1).sub(U0));
        float dv2 = N2.dot(new Vector3f(V2).sub(U0));

        float EPSILON = 1e-6f;
        if (Math.abs(du0) < EPSILON) du0 = 0;
        if (Math.abs(du1) < EPSILON) du1 = 0;
        if (Math.abs(du2) < EPSILON) du2 = 0;
        if (Math.abs(dv0) < EPSILON) dv0 = 0;
        if (Math.abs(dv1) < EPSILON) dv1 = 0;
        if (Math.abs(dv2) < EPSILON) dv2 = 0;

        if (du0 * du1 > 0 && du0 * du2 > 0) return false;
        if (dv0 * dv1 > 0 && dv0 * dv2 > 0) return false;

        Vector3f D = new Vector3f(N1).cross(N2);
        int max = 0;
        float absX = Math.abs(D.x), absY = Math.abs(D.y), absZ = Math.abs(D.z);
        if (absY > absX) max = 1;
        if (absZ > ((max == 0) ? absX : absY)) max = 2;

        float[] tri1 = projectOnAxis(max, V0, V1, V2);
        float[] tri2 = projectOnAxis(max, U0, U1, U2);

        return intervalsOverlap(tri1[0], tri1[1], tri2[0], tri2[1]);
    }

    private float[] projectOnAxis(int axis, Vector3f v0, Vector3f v1, Vector3f v2) {
        float p0 = (axis == 0) ? v0.x : (axis == 1 ? v0.y : v0.z);
        float p1 = (axis == 0) ? v1.x : (axis == 1 ? v1.y : v1.z);
        float p2 = (axis == 0) ? v2.x : (axis == 1 ? v2.y : v2.z);
        float min = Math.min(p0, Math.min(p1, p2));
        float max = Math.max(p0, Math.max(p1, p2));
        return new float[]{min, max};
    }

    private boolean intervalsOverlap(float min1, float max1, float min2, float max2) {
        return !(max1 < min2 || max2 < min1);
    }

    // Test collision entre deux shapes
    public boolean intersectsOptimized(Shape other) {
        // Bounding box rapide pour early out
        float[] minA = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
        float[] maxA = new float[]{-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
        float[] minB = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
        float[] maxB = new float[]{-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};

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
            float[] t1v0 = {vertices[i * FLOATS_PER_VERTEX], vertices[i * FLOATS_PER_VERTEX + 1], vertices[i * FLOATS_PER_VERTEX + 2]};
            float[] t1v1 = {vertices[(i+1) * FLOATS_PER_VERTEX], vertices[(i+1) * FLOATS_PER_VERTEX + 1], vertices[(i+1) * FLOATS_PER_VERTEX + 2]};
            float[] t1v2 = {vertices[(i+2) * FLOATS_PER_VERTEX], vertices[(i+2) * FLOATS_PER_VERTEX + 1], vertices[(i+2) * FLOATS_PER_VERTEX + 2]};

            for (int j = 0; j < otherVertexCount; j += 3) {
                float[] t2v0 = {other.vertices[j * FLOATS_PER_VERTEX], other.vertices[j * FLOATS_PER_VERTEX + 1], other.vertices[j * FLOATS_PER_VERTEX + 2]};
                float[] t2v1 = {other.vertices[(j+1) * FLOATS_PER_VERTEX], other.vertices[(j+1) * FLOATS_PER_VERTEX + 1], other.vertices[(j+1) * FLOATS_PER_VERTEX + 2]};
                float[] t2v2 = {other.vertices[(j+2) * FLOATS_PER_VERTEX], other.vertices[(j+2) * FLOATS_PER_VERTEX + 1], other.vertices[(j+2) * FLOATS_PER_VERTEX + 2]};

                if (triTriIntersect(
                        new Vector3f(t1v0[0], t1v0[1], t1v0[2]),
                        new Vector3f(t1v1[0], t1v1[1], t1v1[2]),
                        new Vector3f(t1v2[0], t1v2[1], t1v2[2]),
                        new Vector3f(t2v0[0], t2v0[1], t2v0[2]),
                        new Vector3f(t2v1[0], t2v1[1], t2v1[2]),
                        new Vector3f(t2v2[0], t2v2[1], t2v2[2])
                )) {
                    return true;
                }
            }
        }

        return false;
    }

}