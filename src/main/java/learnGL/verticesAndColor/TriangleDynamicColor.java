package learnGL.verticesAndColor;

import learnGL.tools.Shader;
import learnGL.tools.Shape;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.io.IOException;
import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * + dessin de triangles avec VAO/VBO + coloration de java au cpu
 */

public class TriangleDynamicColor {

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
        glClearColor(1.0f, 1.0f, 0.0f, 0.0f);

        Shader shader = new Shader("shadersTest/TriangleDynamicColorVertex.glsl", "shadersTest/TriangleDynamicColorFragment.glsl");

        float[] verticesTriangle = {
                300f, 100f, 0.0f, 1f, 0f, 0f,
                200f, 100f, 0.0f, 1f, 0f, 1f,
                150f, 200f, 0.0f, 1f, 0f, 1f
        };
        Shape triangle = new Shape(Shape.autoAddSlotTexture(verticesTriangle), width, height);
        triangle.setShader(shader);
        int colorLoc = glGetUniformLocation(shader.getProgramId(), "ourColor");

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Calcul couleur dynamique
            float time = (float) glfwGetTime(); // temps en secondes
            float red   = (float)Math.sin(time * 2.0f) * 0.5f + 0.5f;
            float green = (float)Math.sin(time * 0.7f) * 0.5f + 0.5f;
            float blue  = (float)Math.sin(time * 1.3f) * 0.5f + 0.5f;

            shader.bind();
            glUniform4f(colorLoc, red, green, blue, 1.0f); // R, G, B, Alpha

            triangle.render();
            shader.unbind();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
        triangle.cleanup();
    }


    public static void main(String[] args) throws IOException {
        new TriangleDynamicColor().run();
    }
}
