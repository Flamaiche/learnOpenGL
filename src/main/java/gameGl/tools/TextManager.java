package gameGl.tools;

import gameGl.utils.HUDText;
import learnGL.tools.Shader;
import java.util.ArrayList;

public class TextManager {
    private final ArrayList<HUDText> texts;
    private boolean debugMode = false;

    public TextManager() {
        texts = new ArrayList<>();

        // --- Crée les textes HUD par défaut ---
        texts.add(new HUDText("Score: 0", 20, 30, 2.5f, 1f, 0f, 0f));
        texts.add(new HUDText("FPS: 0", 20, 60, 1.8f, 0f, 1f, 0f));
        texts.add(new HUDText("Position: 0,0,0", 20, 90, 1.8f, 0f, 1f, 0f));
        texts.add(new HUDText("Orientation: 0,0,0", 20, 120, 1.8f, 0f, 1f, 1f));
        texts.add(new HUDText("Distance cible: 0", 20, 150, 1.8f, 1f, 0.5f, 0f));
        texts.add(new HUDText("Ball: 0/0", 20, 180, 1.8f, 0f, 0.5f, 1f));
        texts.add(new HUDText("Ennemis: 0/0", 20, 210, 1.8f, 1f, 0.5f, 0f));

        // En debugMode false, on masque les textes "Ball" et "Ennemis"
        for (HUDText t : texts) {
            if (t.getContent().startsWith("Ball") || t.getContent().startsWith("Ennemis")) {
                t.setActive(debugMode);
            }
        }
    }

    public void setDebugMode(boolean debug) {
        debugMode = debug;
        // Met à jour tous les textes liés au debug
        for (HUDText t : texts) {
            if (t.getContent().startsWith("Ball") || t.getContent().startsWith("Ennemis")) {
                t.setActive(debugMode);
            }
        }
    }

    /** Met à jour les textes selon les données reçues */
    public void update(int score, float fps, float playerX, float playerY, float playerZ,
                       float pitch, float yaw, float roll,
                       int activeBalls, int totalBalls,
                       int activeEnemies, int totalEnemies,
                       float distanceTarget) {

        for (HUDText t : texts) {
            String c = t.getContent();
            if (c.startsWith("Score")) t.setContent("Score: " + score);
            else if (c.startsWith("FPS")) t.setContent("FPS: " + (int)fps);
            else if (c.startsWith("Position")) t.setContent(String.format("Position: %.1f, %.1f, %.1f", playerX, playerY, playerZ));
            else if (c.startsWith("Orientation")) t.setContent(String.format("Orientation: %.1f, %.1f, %.1f", pitch, yaw, roll));
            else if (c.startsWith("Distance cible")) t.setContent(String.format("Distance cible: %.1f", distanceTarget));
            else if (c.startsWith("Ball")) t.setContent(debugMode ? "Ball: " + activeBalls + "/" + totalBalls
                    : "Ball: " + totalBalls);
            else if (c.startsWith("Ennemis")) t.setContent(debugMode ? "Ennemis: " + activeEnemies + "/" + totalEnemies
                    : "Ennemis: " + totalEnemies);
        }
    }

    /** Affiche tous les textes actifs */
    public void render(Shader shader) {
        for (HUDText t : texts) {
            if (t.isActive()) {
                Text.drawText(shader, t.getContent(), t.getX(), t.getY(), t.getScale(), t.getR(), t.getG(), t.getB());
            }
        }
    }
}
