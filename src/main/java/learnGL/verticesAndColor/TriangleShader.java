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
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * dessin d’un triangle avec VAO/VBO avec shader ajoutant une couleur fixe
 * forcer la couleur du triangle avec le shader ne fonctionne plus a cause de shape qui demande une couleur
 */
@Deprecated
public class TriangleShader {

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
        // Initialisation des fonctions OpenGL
        GL.createCapabilities();

        // Définir la couleur de fond (noir ici)
        glClearColor(0f, 0f, 0f, 0f);

        // 1. Chargement des shaders
        Shader shader = new Shader("shaders/TriangleShaderVertex.glsl", "shaders/TriangleShaderFragment.glsl");

        // 2. Définition des sommets du triangle (coordonnées logiques ou normalisées)
        float[] vertices = {
                0.0f,  0.5f, 0.0f,  // sommet haut
                -0.5f, -0.5f, 0.0f,  // sommet bas gauche
                0.5f, -0.5f, 0.0f   // sommet bas droite
        };

        // 3. Création de la forme
        Shape triangle = new Shape(Shape.autoAddSlotColor(vertices));

        // 4. Associer le shader à la shape
        triangle.setShader(shader);

        // Boucle principale
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            triangle.render();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        // Nettoyage
        triangle.cleanup();
        shader.cleanup();
    }

    public static void main(String[] args) throws IOException {
        new TriangleShader().run();
    }
}
