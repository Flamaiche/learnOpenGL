package learnGL.verticesAndColor;

import learnGL.tools.Shader;
import learnGL.tools.Shape;
import learnGL.tools.Texture;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.io.IOException;
import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Dessin d'un triangle avec texture
 */
public class TriangleTexture {

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
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(width, height, "Triangle Texture", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create the GLFW window");

        glfwSetFramebufferSizeCallback(window, (win, w, h) -> glViewport(0, 0, w, h));

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth  = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(window,
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
        glClearColor(0f, 0f, 0f, 1f);

        // Shader pour la texture
        Shader shader = new Shader("shaders/TriangleTextureVertex.glsl",
                "shaders/TriangleTextureFragment.glsl");

        float[] vertices = {
                -0.5f, -0.5f, 0f, 1f, 1f, 0f, 0f, 0f,  // u=0, v=0
                0.5f, -0.5f, 0f, 0f, 0f, 1f, 1f, 0f,  // u=1, v=0
                0.0f,  0.5f, 0f, 0f, 0f, 0.5f, 0.5f, 1f  // u=0.5, v=1
        };

        Shape triangle = new Shape(vertices);
        triangle.setShader(shader);

        // Charger la texture depuis resources
        Texture texture = new Texture("textures/wall.jpg");

        shader.bind();
        int texLoc = glGetUniformLocation(shader.getProgramId(), "ourTexture");
        glUniform1i(texLoc, 0); // texture unit 0
        shader.unbind();

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            shader.bind();
            glActiveTexture(GL_TEXTURE0);
            texture.bind();

            triangle.render();

            texture.unbind();
            shader.unbind();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        triangle.cleanup();
        texture.cleanup();
    }

    public static void main(String[] args) throws IOException {
        new TriangleTexture().run();
    }
}
