package learnGL.tools;

import org.lwjgl.glfw.GLFW;
import org.joml.Vector3f;

public class Commande {
    private Camera camera;
    private long window;
    private float vitesse = 0.05f;
    private float vitesseRotation = 2.0f;

    public Commande(Camera camera, long window) {
        this.camera = camera;
        this.window = window;
    }

    public void update() {
        Vector3f move = new Vector3f();

        // Déplacement
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_Z) == GLFW.GLFW_PRESS) move.add(new Vector3f(camera.getFront()).mul(vitesse));
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_X) == GLFW.GLFW_PRESS) move.add(new Vector3f(camera.getFront()).mul(-vitesse));
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) move.add(new Vector3f(camera.getDroite()).mul(vitesse));
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) move.add(new Vector3f(camera.getDroite()).mul(-vitesse));
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) move.add(new Vector3f(camera.getUp()).mul(vitesse));
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) move.add(new Vector3f(camera.getUp()).mul(-vitesse));

        camera.move(move);

        // Rotation
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS) camera.rotate(-vitesseRotation, 0);
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) camera.rotate(vitesseRotation, 0);
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS) camera.rotate(0, vitesseRotation);
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS) camera.rotate(0, -vitesseRotation);
    }
}
