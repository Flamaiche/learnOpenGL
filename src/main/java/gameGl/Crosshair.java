package gameGl;

import learnGL.tools.Shader;
import learnGL.tools.Shape;

import java.io.IOException;

import static org.lwjgl.opengl.GL11C.*;

public class Crosshair {

    private final Shape shape;
    private final Shader shader;

    public Crosshair(float size, float thickness) throws IOException {
        shader = new Shader("shaders/CrosshairVertex.glsl", "shaders/CrosshairFragment.glsl");

        float h = size / 2f;
        float t = thickness / 2f;

        // Ligne horizontale
        float[] horiz = {
                -h, -t, 0.0f,
                h, -t, 0.0f,
                h,  t, 0.0f,
                h,  t, 0.0f,
                -h,  t, 0.0f,
                -h, -t, 0.0f
        };

        // Ligne verticale
        float[] vert = {
                -t, -h, 0.0f,
                t, -h, 0.0f,
                t,  h, 0.0f,
                t,  h, 0.0f,
                -t,  h, 0.0f,
                -t, -h, 0.0f
        };

        float[] vertices = new float[horiz.length + vert.length];
        System.arraycopy(horiz, 0, vertices, 0, horiz.length);
        System.arraycopy(vert, 0, vertices, horiz.length, vert.length);

        shape = new Shape(Shape.autoAddSlotColor(vertices));
        shape.setShader(shader);
        shape.setColor(1f, 0f, 0f, 1f); // rouge
    }

    public void render() {
        // Désactiver depth test si nécessaire
        boolean wasDepthTestEnabled = glIsEnabled(GL_DEPTH_TEST);
        if (wasDepthTestEnabled) glDisable(GL_DEPTH_TEST);

        shader.bind();

        // Projection orthographique pour rester fixe à l’écran
        shader.setUniformMat4f("projection", new org.joml.Matrix4f().ortho2D(-1, 1, -1, 1));
        shader.setUniformMat4f("model", new org.joml.Matrix4f().identity());

        shape.render();
        shader.unbind();

        // Rétablir le depth test si il était activé
        if (wasDepthTestEnabled) glEnable(GL_DEPTH_TEST);
    }

    public void cleanup() {
        shape.cleanup();
        shader.cleanup();
    }
}
