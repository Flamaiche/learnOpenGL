package gameGl;

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
import java.util.ArrayList;
import java.util.Iterator;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBEasyFont.stb_easy_font_print;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class WindowsCreator {

    private long window;
    private int width = 800;
    private int height = 600;

    private Matrix4f projection = new Matrix4f();

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

        glfwSetFramebufferSizeCallback(window, (win, newWidth, newHeight) -> {
            width = newWidth;
            height = newHeight;
            glViewport(0, 0, width, height);
            projection.identity().perspective(
                    (float) Math.toRadians(45.0f),
                    (float) width / height,
                    0.1f,
                    100.0f
            );
        });

        projection.identity().perspective(
                (float) Math.toRadians(45.0f),
                (float) width / height,
                0.1f,
                100.0f
        );

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

        Camera camera = new Camera(new Vector3f(0, 0, 3));
        Commande cmd = new Commande(camera, window);

        Shader ennemisShader = new Shader("shaders/EnnemisVertex.glsl", "shaders/EnnemisFragment.glsl");
        Shader ballShader = new Shader("shaders/EnnemisVertex.glsl", "shaders/EnnemisFragment.glsl");
        Shader crosshairShader = new Shader("shaders/EnnemisVertex.glsl", "shaders/EnnemisFragment.glsl");

        Ennemis[] ennemis = new Ennemis[2];
        for (int i = 0; i < ennemis.length; i++) {
            ennemis[i] = new Ennemis(ennemisShader,
                    new float[]{camera.getPosition().x, camera.getPosition().y, camera.getPosition().z});
        }

        Crosshair crosshair = new Crosshair(crosshairShader);

        ArrayList<Ball> balls = new ArrayList<>();
        double lastShootTime = 0;
        double shootCooldown = 0.3;

        double lastTime = glfwGetTime();
        int score = 0;

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            double currentTime = glfwGetTime();
            float deltaTime = (float) (currentTime - lastTime);
            lastTime = currentTime;

            cmd.update();

            // Update ennemis
            for (Ennemis e : ennemis) {
                e.deplacement(deltaTime);
                e.render(camera.getViewMatrix(), projection);

                if (e.shouldDespawn(camera.getPosition())) {
                    e.setDeplacement(new float[]{
                            camera.getPosition().x,
                            camera.getPosition().y,
                            camera.getPosition().z
                    });
                }
            }

            // Tir
            if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) {
                if (currentTime - lastShootTime >= shootCooldown) {
                    lastShootTime = currentTime;
                    Vector3f spawnPos = new Vector3f(camera.getPosition())
                            .add(new Vector3f(camera.getFront()).mul(0.5f));
                    float baseSize = 0.2f;
                    balls.add(new Ball(ballShader, spawnPos, new Vector3f(camera.getFront()), baseSize));
                }
            }

            // Update balls
            Iterator<Ball> it = balls.iterator();
            while (it.hasNext()) {
                Ball b = it.next();
                b.update(deltaTime);
                b.render(camera.getViewMatrix(), projection);
                score += b.collisionScore(ennemis);

                if (b.shouldDespawn(camera.getPosition())) {
                    b.cleanup();
                    it.remove();
                }
            }

            // Crosshair toujours centré devant la caméra, visible et billboard
            Vector3f crossPos = new Vector3f(camera.getPosition())
                    .add(new Vector3f(camera.getFront()).mul(0.5f)); // proche
            crosshair.setPosition(crossPos);
            crosshair.render(
                    camera.getViewMatrix(),
                    projection,
                    camera.getFront(),
                    camera.getUp(),
                    0.02f // taille visible du crosshair
            );

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        for (Ennemis e : ennemis) e.cleanup();
        for (Ball b : balls) b.cleanup();
        crosshair.cleanup();
    }

    public static void main(String[] args) throws IOException {
        new WindowsCreator().run();
    }
}
