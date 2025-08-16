package learnGL.tools;

import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
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
    private Texture texture = null;
    // Transformation CPU
    private Vector3f position = new Vector3f(0, 0, 0);
    private Vector3f rotation = new Vector3f(0, 0, 0); // rotation en radians
    private Vector3f scale = new Vector3f(1, 1, 1);

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
        if (shader != null) {
            shader.setUniformMat4f("model", getModelMatrix());
        }

        if (texture != null) texture.bind();

        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

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

    public boolean estEnCollision(Shape autre) {
        Vector3f[][] triangles1 = obtenirTriangles();
        Vector3f[][] triangles2 = autre.obtenirTriangles();

        for (Vector3f[] t1 : triangles1) {
            for (Vector3f[] t2 : triangles2) {
                if (triangleIntersecte(t1, t2)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Vector3f[][] obtenirTriangles() {
        Vector3f[] transformed = getTransformedVertices(); // <-- utiliser transformés
        int nombreTriangles = vertexCount / 3;
        Vector3f[][] triangles = new Vector3f[nombreTriangles][3];

        for (int i = 0; i < nombreTriangles; i++) {
            for (int j = 0; j < 3; j++) {
                triangles[i][j] = new Vector3f(
                        transformed[i * 3 + j].x,
                        transformed[i * 3 + j].y,
                        transformed[i * 3 + j].z
                );
            }
        }
        return triangles;
    }

    private boolean triangleIntersecte(Vector3f[] t1, Vector3f[] t2) {
        // t1: A,B,C ; t2: P,Q,R
        Vector3f A = t1[0], B = t1[1], C = t1[2];
        Vector3f P = t2[0], Q = t2[1], R = t2[2];

        Vector3f E1 = B.sub(A, new Vector3f());
        Vector3f E2 = C.sub(A, new Vector3f());
        Vector3f N1 = E1.cross(E2, new Vector3f());
        float d1 = -N1.dot(A);

        // Signe des points du triangle 2 par rapport au plan de triangle 1
        float dp0 = N1.dot(P) + d1;
        float dp1 = N1.dot(Q) + d1;
        float dp2 = N1.dot(R) + d1;

        if (dp0 > 0 && dp1 > 0 && dp2 > 0) return false;
        if (dp0 < 0 && dp1 < 0 && dp2 < 0) return false;

        // Plan du triangle 2
        E1 = Q.sub(P, new Vector3f());
        E2 = R.sub(P, new Vector3f());
        Vector3f N2 = E1.cross(E2, new Vector3f());
        float d2 = -N2.dot(P);

        // Signe des points du triangle 1 par rapport au plan de triangle 2
        dp0 = N2.dot(A) + d2;
        dp1 = N2.dot(B) + d2;
        dp2 = N2.dot(C) + d2;

        if (dp0 > 0 && dp1 > 0 && dp2 > 0) return false;
        if (dp0 < 0 && dp1 < 0 && dp2 < 0) return false;

        // On calcule l'intersection de la ligne des plans
        Vector3f D = N1.cross(N2, new Vector3f());

        float max = Math.abs(D.x);
        int index = 0;
        if (Math.abs(D.y) > max) { max = Math.abs(D.y); index = 1; }
        if (Math.abs(D.z) > max) index = 2;

        // Projections sur l’axe dominant
        float[] t1Proj = new float[3];
        float[] t2Proj = new float[3];

        for (int i = 0; i < 3; i++) {
            switch (index) {
                case 0:
                    t1Proj[i] = t1[i].y;
                    t2Proj[i] = t2[i].y;
                    break;
                case 1:
                    t1Proj[i] = t1[i].z;
                    t2Proj[i] = t2[i].z;
                    break;
                case 2:
                    t1Proj[i] = t1[i].x;
                    t2Proj[i] = t2[i].x;
                    break;
            }
        }

        float t1Min = Math.min(Math.min(t1Proj[0], t1Proj[1]), t1Proj[2]);
        float t1Max = Math.max(Math.max(t1Proj[0], t1Proj[1]), t1Proj[2]);
        float t2Min = Math.min(Math.min(t2Proj[0], t2Proj[1]), t2Proj[2]);
        float t2Max = Math.max(Math.max(t2Proj[0], t2Proj[1]), t2Proj[2]);

        return !(t1Max < t2Min || t2Max < t1Min);
    }

    // ---------------- Transformation CPU ----------------
    public void setPosition(Vector3f pos) { this.position.set(pos); }
    public void setRotation(Vector3f rot) { this.rotation.set(rot); }
    public void setScale(Vector3f scl) { this.scale.set(scl); }

    public Vector3f getPosition() { return new Vector3f(position); }
    public Vector3f getRotation() { return new Vector3f(rotation); }
    public Vector3f getScale() { return new Vector3f(scale); }

    /**
     * Retourne la matrice de transformation complète (model) côté CPU
     */
    public Matrix4f getModelMatrix() {
        return new Matrix4f()
                .identity()
                .translate(position)
                .rotateX(rotation.x)
                .rotateY(rotation.y)
                .rotateZ(rotation.z)
                .scale(scale);
    }

    /**
     * Retourne les vertices transformés dans le monde
     */
    public Vector3f[] getTransformedVertices() {
        int vertexCount = vertices.length / FLOATS_PER_VERTEX;
        Vector3f[] worldVertices = new Vector3f[vertexCount];
        Matrix4f model = getModelMatrix();

        for (int i = 0; i < vertexCount; i++) {
            Vector3f v = new Vector3f(
                    vertices[i * FLOATS_PER_VERTEX],       // x
                    vertices[i * FLOATS_PER_VERTEX + 1],   // y
                    vertices[i * FLOATS_PER_VERTEX + 2]    // z
            );
            model.transformPosition(v);
            worldVertices[i] = v;
        }
        return worldVertices;
    }
}
