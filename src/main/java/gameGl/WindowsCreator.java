package gameGl;

import gameGl.tools.PreVerticesTable;
import gameGl.tools.Text;
import learnGL.tools.Camera;
import learnGL.tools.Commande;
import learnGL.tools.Shader;
import org.joml.Matrix4f;
import org.joml.Vector3f;
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

public class WindowsCreator {

    private long window;
    private int width = 800;
    private int height = 600;

    public void run() throws IOException {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(width, height, "Hello World!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(win, true);
        });

        // Callback pour redimensionnement : viewport seulement
        glfwSetFramebufferSizeCallback(window, (win, newWidth, newHeight) -> {
            width = newWidth;
            height = newHeight;
            glViewport(0, 0, width, height);
        });

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
        glfwSwapInterval(1);
        glfwShowWindow(window);
    }

    private void loop() throws IOException {
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glClearColor(1.0f, 1.0f, 0.0f, 0.0f); // fond jaune
        glViewport(0, 0, width, height);

        Camera camera = new Camera(new Vector3f(0, 0, 3));
        Commande cmd = new Commande(camera, window);
        cmd.vitesseRotation = 1.5f;

        Shader ennemisShader = new Shader("shaders/EnnemisVertex.glsl", "shaders/EnnemisFragment.glsl");
        Shader ballShader = new Shader("shaders/DefaultVertex.glsl", "shaders/DefaultFragment.glsl");
        Shader crosshairShader = new Shader("shaders/DefaultVertex.glsl", "shaders/DefaultFragment.glsl");
        Shader textShader = new Shader("shaders/TextVertex.glsl", "shaders/TextFragment.glsl");

        Ennemis.setDespawnDistance(camera.getRenderSimulation());
        Ennemis[] ennemis = new Ennemis[10];
        for (int i = 0; i < ennemis.length; i++) {
            ennemis[i] = new Ennemis(ennemisShader,
                    new float[]{camera.getPosition().x, camera.getPosition().y, camera.getPosition().z},
                    PreVerticesTable.generateCubeSimple(1f));
        }

        Crosshair crosshair = new Crosshair(crosshairShader);

        // --- Pool fixe de balles ---
        Ball.setMaxDistance(camera.getRenderSimulation());
        final int MAX_BALLS = 20;
        Ball[] balls = new Ball[MAX_BALLS];
        for (int i = 0; i < MAX_BALLS; i++) {
            balls[i] = new Ball(ballShader, 0.2f);
        }

        double lastShootTime = 0;
        double shootCooldown = 0.3;
        double lastTime = glfwGetTime();
        int score = 0;

        Matrix4f orthoProjection = new Matrix4f().ortho2D(-1, 1, -1, 1);

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            double currentTime = glfwGetTime();
            float deltaTime = (float) (currentTime - lastTime);
            lastTime = currentTime;

            cmd.update();

            Matrix4f viewMatrix = camera.getViewMatrix();
            Matrix4f projection = camera.getProjection(width, height);

            // --- Tir ---
            if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS && currentTime - lastShootTime >= shootCooldown) {
                lastShootTime = currentTime;
                Vector3f spawnPos = new Vector3f(camera.getPosition()).add(new Vector3f(camera.getFront()).mul(0.5f));

                // Active la première balle inactive du pool
                for (Ball b : balls) {
                    if (!b.isActive()) {
                        b.activate(spawnPos, camera.getFront());
                        break;
                    }
                }
            }

            // --- Update & rendu des balles actives ---
            for (Ball b : balls) {
                if (!b.isActive()) continue;

                b.update(deltaTime);
                b.render(viewMatrix, projection);
                score += b.collisionScore(ennemis);
            }

            // --- Update & rendu des ennemis ---
            for (Ennemis e : ennemis) {
                //e.deplacement(deltaTime);
                e.render(viewMatrix, projection);
            }

            // --- Crosshair ---
            crosshair.updateHighlightedEnemy(ennemis, camera);
            crosshair.render(orthoProjection);

            // --- Texte ---
            Text.drawText(textShader, "Score: " + score, 20, 30, 2.5f, 1f, 0f, 0f);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        // --- Cleanup ---
        for (Ennemis e : ennemis) e.cleanup();
        for (Ball b : balls) b.cleanup();
        crosshair.cleanup();
        Text.cleanup();
    }

    public static void main(String[] args) throws IOException {
        new WindowsCreator().run();
    }
}
