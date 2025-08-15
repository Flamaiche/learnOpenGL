package learnGL.tools;

import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;

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
}
