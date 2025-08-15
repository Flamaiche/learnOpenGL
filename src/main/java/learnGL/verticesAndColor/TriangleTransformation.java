package learnGL.verticesAndColor;

import learnGL.tools.Shader;
import learnGL.tools.Shape;
import org.joml.Matrix4f;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.io.IOException;
import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * + dessin de triangles avec VAO/VBO + transformation
 */

public class TriangleTransformation {

    // Handle de la fenêtre GLFW
    private long window;
    // Taille de la fenêtre
    private int width = 800;
    private int height = 600;

    public void run() throws IOException {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");
        init();
        loop();

        // Nettoyage
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Gestion des erreurs GLFW
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configuration de la fenêtre
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(width, height, "Hello World!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetFramebufferSizeCallback(window, (win, width, height) -> {
            glViewport(0, 0, width, height);
        });

        // Fermer la fenêtre avec la touche Échap
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });

        // Centrer la fenêtre
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // V-Sync
        glfwShowWindow(window);
    }

    private void loop() throws IOException {
        GL.createCapabilities();
        glClearColor(0f, 0f, 0f, 1f); // fond noir

        Shader shader = new Shader("shaders/TriangleTransformationVertex.glsl", "shaders/TriangleTransformationFragment.glsl");

        // Triangle de base
        float[] baseVertices = {
                200f + 233.33f, 200f + 133.33f, 0.0f, 1f, 0f, 0f, // rouge
                100f + 233.33f, 200f + 133.33f, 0.0f, 0f, 1f, 0f, // vert
                200f + 233.33f, 100f + 133.33f, 0.0f, 0f, 0f, 1f  // bleu
        };

        Shape[] triangles = new Shape[20];
        for (int i = 0; i < triangles.length; i++) {
            triangles[i] = new Shape(Shape.autoAddSlotTexture(baseVertices.clone()), width, height);
            triangles[i].setShader(shader);
        }
        float phase = 0.1f; // phase de décalage pour les transformations (plus c'est petit, moins les triangles sont espacés)
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            shader.bind();

            float time = (float) glfwGetTime();

            // On divise le temps en 10 segments
            for (int i = 0; i < triangles.length; i++) {
                transformation(triangles[i], time - phase * i);
            }

            shader.unbind();
            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        for (Shape t : triangles) {
            t.cleanup();
        }
    }

    public void transformation(Shape shape, float time) {
        float[] triCenter = shape.center();
        Matrix4f transform = new Matrix4f();
        transform = transform.translate(triCenter[0], triCenter[1], triCenter[2]); // translation pour centrer le triangle

        transform = transform.rotateZ(time);
        transform = transform.scale((float)Math.sin(time * 2.0f) * 0.5f + 1f);
        transform = transform.translate((float)Math.sin(time * 2.0f) * 0.5f, 0f, 0f);

        transform = transform.translate(-triCenter[0], -triCenter[1], -triCenter[2]); // translation inverse pour remettre le triangle à sa position d'origine
        // envoyer la matrice au shader
        int transformLoc = glGetUniformLocation(shape.getShader().getProgramId(), "transform");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(transformLoc, false, transform.get(stack.mallocFloat(16)));
        }
        shape.render();

    }


    public static void main(String[] args) throws IOException {
        new TriangleTransformation().run();
    }
}

