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

        if (key == 256) { // GLFW_KEY_ESCAPE = 256
            System.out.println("=== ESCAPE DEBUG ===");
            System.out.println("pressed: " + pressed);
            System.out.println("wasPressed: " + wasPressed);
            System.out.println("active: " + active);
            System.out.println("onPressAction != null: " + (onPressAction != null));
        }

        if (pressed) {
            System.out.println("Touche pressée: " + key + " , wasPressed: " + wasPressed);

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
    public boolean isActive() { return active; }
    public boolean wasPressed() { return wasPressed; }
}
