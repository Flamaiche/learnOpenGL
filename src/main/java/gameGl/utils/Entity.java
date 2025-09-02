package gameGl.utils;

import learnGL.tools.Shape;
import learnGL.tools.Shader;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public abstract class Entity {
    protected final Shape corps;
    protected final Shader shader;
    protected final Vector3f position = new Vector3f();
    protected final Matrix4f modelMatrix = new Matrix4f();
    protected boolean modelDirty = true;
    protected boolean highlighted = false;

    public Entity(Shader shader, float[] verticesShape) {
        this.shader = shader;
        this.corps = new Shape(Shape.autoAddSlotColor(verticesShape));
        this.corps.setShader(shader);
    }

    /** Mise à jour spécifique (déplacement, rotation, etc.) */
    public abstract void update(float deltaTime);

    /** Rendu générique */
    public void render(Matrix4f view, Matrix4f projection) {
        if (modelDirty) updateModelMatrix();

        shader.bind();
        shader.setUniformMat4f("view", view);
        shader.setUniformMat4f("projection", projection);
        shader.setUniformMat4f("model", modelMatrix);
        corps.render();

        if (highlighted) {
            Matrix4f outlineModel = new Matrix4f(modelMatrix).scale(1.05f);
            shader.setUniformMat4f("model", outlineModel);
            corps.render();
            shader.setUniformMat4f("model", modelMatrix);
        }

        shader.unbind();
    }

    /** Nettoyage mémoire */
    public void cleanup() {
        corps.cleanup();
    }

    /** Recalcule la matrice de modèle */
    protected void updateModelMatrix() {
        modelMatrix.identity().translate(position);
        modelDirty = false;
    }

    public Matrix4f getModelMatrix() {
        if (modelDirty) updateModelMatrix();
        return modelMatrix;
    }

    public Shape getCorps() {
        return corps;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setHighlighted(boolean h) {
        this.highlighted = h;
    }
}
