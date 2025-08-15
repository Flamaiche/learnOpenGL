package learnGL.treeD;

import learnGL.tools.Camera;
import learnGL.tools.Commande;
import learnGL.tools.Shader;
import learnGL.tools.Shape;
import org.joml.Matrix4f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * + dessin de triangles avec VAO/VBO + coloration de java au cpu
 */

public class CoordinateSystems {

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
        glEnable(GL_DEPTH_TEST); // nécessaire pour la 3D
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);

        Shader shader = new Shader(
                "shaders/CoordinateSystemsVertex.glsl",
                "shaders/CoordinateSystemsFragment.glsl"
        );

        // Camera et commandes
        Camera camera = new Camera(new org.joml.Vector3f(0, 0, 3));
        Commande cmd = new Commande(camera, window);

        // Triangles
        List<Shape> triangles = new ArrayList<>();

        float[] verticesTriangle1 = {
                -0.25f, -0.25f, 0.0f, 1f, 0f, 0f,
                0.25f, -0.25f, 0.0f, 0f, 1f, 0f,
                0.0f,  0.25f, 0.0f, 0f, 0f, 1f
        };
        Shape triangle1 = new Shape(Shape.autoAddSlotTexture(verticesTriangle1));
        triangle1.setShader(shader);
        triangles.add(triangle1);

        float[] verticesTriangle2 = {
                0.3f, -0.2f, 0.0f, 1f, 1f, 0f,
                0.6f, -0.2f, 0.0f, 0f, 1f, 1f,
                0.45f, 0.2f, 0.0f, 1f, 0f, 1f
        };
        Shape triangle2 = new Shape(Shape.autoAddSlotTexture(verticesTriangle2));
        triangle2.setShader(shader);
        triangles.add(triangle2);

        float[] verticesTriangle3 = {
                -1.5f, -0.5f, 0.0f, 0.5f, 0.2f, 0.7f,
                -0.5f, -0.5f, 0.0f, 0.2f, 0.8f, 0.3f,
                -1.0f,  0.5f, 0.0f, 0.7f, 0.1f, 0.9f
        };
        Shape triangle3 = new Shape(Shape.autoAddSlotTexture(verticesTriangle3));
        triangle3.setShader(shader);
        triangles.add(triangle3);

        // Matrice de projection
        org.joml.Matrix4f projection = new org.joml.Matrix4f()
                .perspective((float) Math.toRadians(45.0f), (float) width / height, 0.1f, 100.0f);

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Mise à jour de la caméra avec touches et rotation
            cmd.update();

            // Activation du shader
            shader.bind();

            // Passage des matrices au shader
            shader.setUniformMat4f("view", camera.getViewMatrix());
            shader.setUniformMat4f("projection", projection);

            // Rendu de tous les triangles
            for (Shape s : triangles) {
                org.joml.Matrix4f model = new org.joml.Matrix4f().identity(); // ou transformation spécifique
                shader.setUniformMat4f("model", model);
                s.render();
            }

            shader.unbind();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        // Nettoyage
        for (Shape s : triangles) {
            s.cleanup();
        }
    }



    public static void main(String[] args) throws IOException {
        new CoordinateSystems().run();
    }
}