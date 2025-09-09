package learnGL.tools;

import org.lwjgl.glfw.GLFW;
import org.joml.Vector3f;

public class Commande {
    private Camera camera;
    private long window;
    public static float vitesse = 0.05f;
    public static float vitesseRotation = 2.0f;

    // souris
    private float mouseSensitivity = 0.1f;
    private double lastMouseX;
    private double lastMouseY;
    private boolean firstMouseInput = true;
    private boolean mouseLocked = true;

    // tir
    private double lastShootTime = 0;
    private double shootCooldown = 0.3;

    public Commande(Camera camera, long window) {
        this.camera = camera;
        this.window = window;

        // souris capturée au début
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);

        // callback souris
        GLFW.glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            if (!mouseLocked) return;

            if (firstMouseInput) {
                lastMouseX = xpos;
                lastMouseY = ypos;
                firstMouseInput = false;
            }

            double deltaX = xpos - lastMouseX;
            double deltaY = lastMouseY - ypos;

            lastMouseX = xpos;
            lastMouseY = ypos;

            camera.rotate((float)(deltaX * mouseSensitivity), (float)(deltaY * mouseSensitivity));
        });
    }

    public void update() {
        // switch souris TAB
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_TAB) == GLFW.GLFW_PRESS) {
            if (mouseLocked) {
                GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
                mouseLocked = false;
            } else {
                firstMouseInput = true;
                GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
                mouseLocked = true;
            }
            while (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_TAB) == GLFW.GLFW_PRESS)
                GLFW.glfwPollEvents();
        }

        // fermer fenêtre Échap
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            GLFW.glfwSetWindowShouldClose(window, true);
        }

        // mode orbite espace
        camera.setOrbitMode(GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS);

        // déplacement WASD + shift/ctrl
        Vector3f move = new Vector3f();
        if (!camera.isOrbitMode()) {
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) move.add(new Vector3f(camera.getFront()).mul(vitesse)); // avant
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) move.add(new Vector3f(camera.getFront()).mul(-vitesse)); // arrière
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) move.add(new Vector3f(camera.getDroite()).mul(vitesse)); // droite
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) move.add(new Vector3f(camera.getDroite()).mul(-vitesse)); // gauche
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) move.add(new Vector3f(camera.getUp()).mul(vitesse)); // haut
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS) move.add(new Vector3f(camera.getUp()).mul(-vitesse)); // bas
        }
        camera.move(move);

        // rotation flèches
        float rotH = 0, rotV = 0;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS) rotH -= vitesseRotation;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) rotH += vitesseRotation;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS) rotV += vitesseRotation;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS) rotV -= vitesseRotation;
        camera.rotate(rotH, rotV);
    }

    // renvoie true si le joueur peut tirer
    public boolean canShoot() {
        double currentTime = GLFW.glfwGetTime();
        if (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS
                && currentTime - lastShootTime >= shootCooldown) {
            lastShootTime = currentTime;
            return true;
        }
        return false;
    }
}
