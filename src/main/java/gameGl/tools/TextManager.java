package gameGl.tools;

import learnGL.tools.Shader;
import java.util.ArrayList;

public class TextManager {

    private final ArrayList<TextHUD> texts = new ArrayList<>();
    private boolean debugMode = false;

    private final int baseWidth = 800;
    private final int baseHeight = 600;

    private int windowWidth;
    private int windowHeight;

    private final float margin = 20f;
    private final float lineHeight = 20f;
    private final float uniformTextScale = 1.5f; // taille fixe

    private double totalElapsedTime = 0.0;

    public TextManager(int initialWidth, int initialHeight) {
        this.windowWidth = initialWidth;
        this.windowHeight = initialHeight;
        initTexts();
    }

    public void setWindowSize(int width, int height) {
        this.windowWidth = width;
        this.windowHeight = height;
    }

    private void initTexts() {
        float yOffset = 0f;

        // HUD joueur (top-left) : violet foncé
        texts.add(new TextHUD("Score: 0", 0, yOffset, uniformTextScale, 0.5f, 0f, 0.5f)); yOffset += lineHeight;
        texts.add(new TextHUD("Temps: 00:00", 0, yOffset, uniformTextScale, 0.5f, 0f, 0.5f)); yOffset += lineHeight;
        texts.add(new TextHUD("Balles: 0", 0, yOffset, uniformTextScale, 0.5f, 0f, 0.5f)); yOffset += lineHeight;
        texts.add(new TextHUD("Ennemis: 0", 0, yOffset, uniformTextScale, 0.5f, 0f, 0.5f));

        // Debug (top-right) : rouge
        yOffset = 0f;
        texts.add(new TextHUD("FPS: 0", 0, yOffset, uniformTextScale, 1f, 0f, 0f)); yOffset += lineHeight;
        texts.add(new TextHUD("Position: 0,0,0", 0, yOffset, uniformTextScale, 1f, 0f, 0f)); yOffset += lineHeight;
        texts.add(new TextHUD("Orientation: 0,0,0", 0, yOffset, uniformTextScale, 1f, 0f, 0f)); yOffset += lineHeight;
        texts.add(new TextHUD("Balles actives: 0/0", 0, yOffset, uniformTextScale, 1f, 0f, 0f)); yOffset += lineHeight;
        texts.add(new TextHUD("Ennemis actifs: 0/0", 0, yOffset, uniformTextScale, 1f, 0f, 0f)); yOffset += lineHeight;
        texts.add(new TextHUD("Distance cible: 0", 0, yOffset, uniformTextScale, 1f, 0f, 0f));

        setDebugMode(false);
    }

    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
        for (TextHUD t : texts) {
            if (isDebugText(t)) t.setActive(debug);
        }
    }

    private boolean isDebugText(TextHUD t) {
        String c = t.getContent();
        return c.startsWith("FPS") || c.startsWith("Position") || c.startsWith("Orientation")
                || c.startsWith("Balles actives") || c.startsWith("Ennemis actifs")
                || c.startsWith("Distance cible");
    }

    public void update(float deltaTime,
                       int score, float fps,
                       float playerX, float playerY, float playerZ,
                       float pitch, float yaw, float roll,
                       int ballsFired, int enemiesKilled,
                       int activeBalls, int maxActiveBalls,
                       int activeEnemies, int maxActiveEnemies,
                       float distanceTarget,
                       int currentWindowWidth,
                       int currentWindowHeight) {

        // Met à jour la taille de la fenêtre
        setWindowSize(currentWindowWidth, currentWindowHeight);

        totalElapsedTime += deltaTime;

        for (TextHUD t : texts) {
            String label = t.getContent();

            if (label.startsWith("Score")) t.setContent("Score: " + score);
            else if (label.startsWith("Temps")) {
                int minutes = (int) (totalElapsedTime / 60);
                int seconds = (int) (totalElapsedTime % 60);
                t.setContent(String.format("Temps: %02d:%02d", minutes, seconds));
            }
            else if (label.startsWith("Balles:")) t.setContent("Balles: " + ballsFired);
            else if (label.startsWith("Ennemis:")) t.setContent("Ennemis: " + enemiesKilled);

            else if (label.startsWith("FPS")) t.setContent("FPS: " + (int) fps);
            else if (label.startsWith("Position")) t.setContent(String.format("Position: %.1f, %.1f, %.1f", playerX, playerY, playerZ));
            else if (label.startsWith("Orientation")) t.setContent(String.format("Orientation: %.1f, %.1f, %.1f", pitch, yaw, roll));
            else if (label.startsWith("Balles actives")) t.setContent("Balles actives: " + activeBalls + "/" + maxActiveBalls);
            else if (label.startsWith("Ennemis actifs")) t.setContent("Ennemis actifs: " + activeEnemies + "/" + maxActiveEnemies);
            else if (label.startsWith("Distance cible")) t.setContent(String.format("Distance cible: %.1f", distanceTarget));
        }
    }

    public void render(Shader shader) {
        float scaleX = (float) windowWidth / baseWidth;
        float scaleY = (float) windowHeight / baseHeight;
        float uniformScale = Math.min(scaleX, scaleY);

        for (TextHUD t : texts) {
            if (!t.isActive()) continue;

            float renderY = t.getY() * uniformScale + margin * uniformScale;
            float renderX;

            if (isDebugText(t)) {
                float textWidth = Text.getTextWidth(t.getContent(), t.getScale() * uniformScale);
                renderX = windowWidth - margin - textWidth;
            } else {
                renderX = margin * uniformScale;
            }

            Text.drawText(shader,
                    t.getContent(),
                    renderX,
                    renderY,
                    t.getScale() * uniformScale,
                    t.getR(), t.getG(), t.getB());
        }
    }
}
