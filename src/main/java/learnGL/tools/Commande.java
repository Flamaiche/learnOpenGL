package learnGL.tools;

import org.lwjgl.glfw.GLFW;
import org.joml.Vector3f;

public class Commande {
    private Camera camera;
    private long window;
    private float speed = 0.05f;

    public Commande(Camera camera, long window) {
        this.camera = camera;
        this.window = window;
    }

    public void update() {
        Vector3f move = new Vector3f();

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_Z) == GLFW.GLFW_PRESS) move.z -= speed;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_X) == GLFW.GLFW_PRESS) move.z += speed;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) move.x -= speed;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) move.x += speed;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) move.y -= speed;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) move.y += speed;

        camera.move(move);
    }
}
