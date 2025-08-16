package learnGL.treeD;

import learnGL.tools.Camera;
import learnGL.tools.Commande;
import learnGL.tools.Shader;
import learnGL.tools.Shape;
import org.joml.Matrix4f;
import org.joml.Vector3f;
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
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);

        Shader shader = new Shader(
                "shaders/CoordinateSystemsVertex.glsl",
                "shaders/CoordinateSystemsFragment.glsl"
        );

        Camera camera = new Camera(new Vector3f(0, 0, 3));
        Commande cmd = new Commande(camera, window);

        List<Shape> pyramides = new ArrayList<>();
        List<Vector3f> positions = new ArrayList<>();
        List<Float> rotations = new ArrayList<>();

        float[] verticesPyramide = {
                -0.25f, 0.0f, -0.25f, 1f,0f,0f,
                0.25f,0.0f,-0.25f,0f,1f,0f,
                0.25f,0.0f,0.25f,0f,0f,1f,
                -0.25f,0.0f,-0.25f,1f,0f,0f,
                0.25f,0.0f,0.25f,0f,0f,1f,
                -0.25f,0.0f,0.25f,1f,1f,0f,
                -0.25f,0.0f,-0.25f,1f,0f,0f,
                0.25f,0.0f,-0.25f,0f,1f,0f,
                0.0f,0.5f,0.0f,0f,0f,1f,
                0.25f,0.0f,-0.25f,0f,1f,0f,
                0.25f,0.0f,0.25f,0f,0f,1f,
                0.0f,0.5f,0.0f,1f,1f,0f,
                0.25f,0.0f,0.25f,0f,0f,1f,
                -0.25f,0.0f,0.25f,1f,1f,0f,
                0.0f,0.5f,0.0f,1f,0f,1f,
                -0.25f,0.0f,0.25f,1f,1f,0f,
                -0.25f,0.0f,-0.25f,1f,0f,0f,
                0.0f,0.5f,0.0f,0f,1f,1f
        };

        // Création des pyramides mobiles
        for (int i = 0; i < 2; i++) {
            Shape p = new Shape(Shape.autoAddSlotTexture(verticesPyramide));
            p.setShader(shader);
            pyramides.add(p);
            positions.add(new Vector3f(i * 0.8f + 1, 0, 0));
            rotations.add(0f);
        }

        // Création de la pyramide centrale fixe
        Shape pyramideCentre = new Shape(Shape.autoAddSlotTexture(verticesPyramide));
        pyramideCentre.setShader(shader);
        Vector3f posCentre = new Vector3f(0, 0, 0); // centre

        Matrix4f projection = new Matrix4f()
                .perspective((float) Math.toRadians(45.0f), (float) width / height, 0.1f, 100.0f);

        int selected = 0; // pyramide sélectionnée
        float angleMonde = 0f;

        boolean tabPressedLastFrame = false;

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            cmd.update();
            shader.bind();
            shader.setUniformMat4f("view", camera.getViewMatrix());
            shader.setUniformMat4f("projection", projection);

            // Rotation globale autour du monde
            angleMonde += 0.01f;
            if (angleMonde > 6.28f) angleMonde -= 6.28f; // 2*PI

            boolean tabPressed = glfwGetKey(window, GLFW_KEY_TAB) == GLFW_PRESS;
            if (tabPressed && !tabPressedLastFrame) {
                selected = (selected + 1) % pyramides.size();
            }
            tabPressedLastFrame = tabPressed;

            Vector3f pos = positions.get(selected);
            float rot = rotations.get(selected);

            if (glfwGetKey(window, GLFW_KEY_I) == GLFW_PRESS) pos.z -= 0.02f;
            if (glfwGetKey(window, GLFW_KEY_K) == GLFW_PRESS) pos.z += 0.02f;
            if (glfwGetKey(window, GLFW_KEY_J) == GLFW_PRESS) pos.x -= 0.02f;
            if (glfwGetKey(window, GLFW_KEY_L) == GLFW_PRESS) pos.x += 0.02f;
            if (glfwGetKey(window, GLFW_KEY_U) == GLFW_PRESS) rot -= 0.02f;
            if (glfwGetKey(window, GLFW_KEY_O) == GLFW_PRESS) rot += 0.02f;

            rotations.set(selected, rot);
            positions.set(selected, pos);

            // Calcul de collision (distance entre le centre de la pyramide sélectionnée et celle du centre)
            Vector3f delta = new Vector3f(pos).sub(posCentre);
            float distance = delta.length();
            float seuilCollision = 0.6f; // ajuster selon la taille de ta pyramide
            boolean collision = distance < seuilCollision;

            System.out.println("Distance à la pyramide centrale : " + distance + " | Collision : " + collision);

            // Dessin des pyramides mobiles
            for (int i = 0; i < pyramides.size(); i++) {
                Shape s = pyramides.get(i);
                Matrix4f model = new Matrix4f().identity();
                model.rotateY(angleMonde);             // rotation monde
                model.translate(positions.get(i));     // position individuelle
                model.rotateY(rotations.get(i));       // rotation propre
                shader.setUniformMat4f("model", model);
                s.render();
            }

            // Dessin de la pyramide centrale fixe
            Matrix4f modelCentre = new Matrix4f().identity();
            modelCentre.scale(angleMonde*0.3f + 0.1f);
            modelCentre.translate(posCentre); // juste position
            shader.setUniformMat4f("model", modelCentre);
            pyramideCentre.render();

            shader.unbind();
            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        // Nettoyage
        for (Shape s : pyramides) s.cleanup();
        pyramideCentre.cleanup();
    }



    public static void main(String[] args) throws IOException {
        new CoordinateSystems().run();
    }
}