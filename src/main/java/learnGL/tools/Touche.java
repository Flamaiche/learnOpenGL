package learnGL.tools;

import org.lwjgl.glfw.GLFW;

public class Touche {
    private int key;
    private Runnable onPressAction;      // exécuté une seule fois au moment de l'appui
    private Runnable onReleaseAction;    // exécuté une seule fois au relâchement
    private Runnable onHoldAction;       // exécuté à chaque update tant que la touche est pressée

    private boolean wasPressed = false;

    public Touche(int key, Runnable onPressAction, Runnable onReleaseAction, Runnable onHoldAction) {
        this.key = key;
        this.onPressAction = onPressAction;
        this.onReleaseAction = onReleaseAction;
        this.onHoldAction = onHoldAction;
    }

    public void update(long window) {
        boolean pressed = GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;

        if (pressed) {
            if (!wasPressed && onPressAction != null) {
                onPressAction.run(); // appui unique
            }
            if (onHoldAction != null) {
                onHoldAction.run(); // action continue
            }
        } else {
            if (wasPressed && onReleaseAction != null) {
                onReleaseAction.run(); // relâchement
            }
        }

        wasPressed = pressed;
    }

    public void reset() {
        wasPressed = false;
    }

    // === Getters / Setters ===
    public int getKey() { return key; }
    public void setKey(int key) { this.key = key; }
}
