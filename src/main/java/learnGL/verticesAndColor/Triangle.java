package learnGL.verticesAndColor;

import learnGL.tools.Shape;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * + dessin de triangles avec VAO/VBO
 */

public class Triangle {

    // Handle de la fenêtre GLFW
    private long window;
    // Taille de la fenêtre
    private int width = 800;
    private int height = 600;

    public void run() {
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

    private void loop() {
        // Initialisation des fonctions OpenGL
        GL.createCapabilities();

        // Définir la couleur de fond (jaune ici)
        glClearColor(1.0f, 1.0f, 0.0f, 0.0f);

        // Initialiser VAO/VBO avec les données du triangle
        Shape[] shapes = new Shape[4];
        float[] verticesRectangle = { // decoupage par triangle
                750f, 550f, 0.0f,
                750f, 450f, 0.0f,
                650f, 450f, 0.0f,

                650f, 450f, 0.0f,
                650f, 550f, 0.0f,
                750f, 550f, 0.0f
        };
        shapes[0] = new Shape(Shape.autoAddSlotColor(verticesRectangle), width, height);
        float[] verticesTriangle = {
                100f, 100f, 0.0f,
                200f, 100f, 0.0f,
                150f, 200f, 0.0f
        };
        for (int j = 1; j < shapes.length; j++) {
            shapes[j] = new Shape(Shape.autoAddSlotColor(verticesTriangle), width, height);
            for (int i = 0; i < verticesTriangle.length; i+=3) {
                verticesTriangle[i] += 100f;
            }
        }

        // Boucle principale
        while (!glfwWindowShouldClose(window)) {
            // Effacer l'écran
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            for (Shape shape : shapes) {
                shape.render(); // Dessiner le triangle
            }

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
        // Nettoyage
        for (Shape shape : shapes) {
            shape.cleanup();
        }
    }


    public static void main(String[] args) {
        new Triangle().run();
    }
}
