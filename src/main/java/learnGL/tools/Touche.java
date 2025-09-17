package learnGL.tools;

import org.lwjgl.glfw.GLFW;

public class Touche {
    private int key;
    private Runnable onPressAction;      // exécuté une seule fois au moment de l'appui
    private Runnable onReleaseAction;    // exécuté une seule fois au relâchement
    private Runnable onHoldAction;       // exécuté à chaque update tant que la touche est pressée

    private boolean wasPressed = false;
    private boolean active = true;

    public Touche(int key, Runnable onPressAction, Runnable onReleaseAction, Runnable onHoldAction) {
        this.key = key;
        this.onPressAction = onPressAction;
        this.onReleaseAction = onReleaseAction;
        this.onHoldAction = onHoldAction;
    }

    public boolean update(long window) {
        boolean inAction = false;
        if (!active) return false;
        boolean pressed = GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;

        if (pressed) {
            if (!wasPressed && onPressAction != null) {
                onPressAction.run(); // appui unique
                inAction = true;
            }
            if (onHoldAction != null) {
                onHoldAction.run(); // action continue
                inAction = true;
            }
        } else {
            if (wasPressed && onReleaseAction != null) {
                onReleaseAction.run(); // relâchement
                inAction = true;
            }
        }

        wasPressed = pressed;
        return inAction;
    }

    public void reset() {
        wasPressed = false;
    }

    // === Getters / Setters ===
    public int getKey() { return key; }
    public void setKey(int key) { this.key = key; }
    public void setActive(boolean active) { this.active = active; }
}
