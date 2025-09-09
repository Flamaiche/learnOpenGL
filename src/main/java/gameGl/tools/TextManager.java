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
    private final float uniformTextScale = 1.5f;

    public TextManager(int initialWidth, int initialHeight) {
        this.windowWidth = initialWidth;
        this.windowHeight = initialHeight;
        initTexts();
    }

    public void setWindowSize(int width, int height) {
        this.windowWidth = width;
        this.windowHeight = height;
    }

    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
        for (TextHUD t : texts) {
            switch (t.getType()) {
                case FPS, POSITION, ORIENTATION, ACTIVE_BALLS, ACTIVE_ENEMIES, DISTANCE_TARGET -> t.setActive(debug);
                default -> {}
            }
        }
    }

    private void initTexts() {
        // HUD joueur (left/top)
        texts.add(new TextHUD(TextHUD.TextType.SCORE, TextHUD.Alignment.LEFT, TextHUD.Alignment.TOP, uniformTextScale, 0.5f, 0f, 0.5f));
        texts.add(new TextHUD(TextHUD.TextType.LIVES, TextHUD.Alignment.LEFT, TextHUD.Alignment.TOP, uniformTextScale, 0.5f, 0f, 0.5f)); // <-- nouveau
        texts.add(new TextHUD(TextHUD.TextType.TIME, TextHUD.Alignment.LEFT, TextHUD.Alignment.TOP, uniformTextScale, 0.5f, 0f, 0.5f));
        texts.add(new TextHUD(TextHUD.TextType.BALLS, TextHUD.Alignment.LEFT, TextHUD.Alignment.TOP, uniformTextScale, 0.5f, 0f, 0.5f));
        texts.add(new TextHUD(TextHUD.TextType.ENEMIES, TextHUD.Alignment.LEFT, TextHUD.Alignment.TOP, uniformTextScale, 0.5f, 0f, 0.5f));

        // Debug (right/top)
        texts.add(new TextHUD(TextHUD.TextType.FPS, TextHUD.Alignment.RIGHT, TextHUD.Alignment.TOP, uniformTextScale, 1f, 0f, 0f));
        texts.add(new TextHUD(TextHUD.TextType.POSITION, TextHUD.Alignment.RIGHT, TextHUD.Alignment.TOP, uniformTextScale, 1f, 0f, 0f));
        texts.add(new TextHUD(TextHUD.TextType.ORIENTATION, TextHUD.Alignment.RIGHT, TextHUD.Alignment.TOP, uniformTextScale, 1f, 0f, 0f));
        texts.add(new TextHUD(TextHUD.TextType.ACTIVE_BALLS, TextHUD.Alignment.RIGHT, TextHUD.Alignment.TOP, uniformTextScale, 1f, 0f, 0f));
        texts.add(new TextHUD(TextHUD.TextType.ACTIVE_ENEMIES, TextHUD.Alignment.RIGHT, TextHUD.Alignment.TOP, uniformTextScale, 1f, 0f, 0f));
        texts.add(new TextHUD(TextHUD.TextType.DISTANCE_TARGET, TextHUD.Alignment.RIGHT, TextHUD.Alignment.TOP, uniformTextScale, 1f, 0f, 0f));

        setDebugMode(false);
    }

    public void update(float deltaTime, int currentWindowWidth, int currentWindowHeight) {
        setWindowSize(currentWindowWidth, currentWindowHeight);
        // Les valeurs des textes sont lues dynamiquement depuis GameData dans getText()
    }

    public void render(Shader shader) {
        GameData data = GameData.getInstance();

        float scaleX = (float) windowWidth / baseWidth;
        float scaleY = (float) windowHeight / baseHeight;
        float uniformScale = Math.min(scaleX, scaleY);

        // Offsets pour top et bottom
        float yOffsetTopLeft = margin * uniformScale;
        float yOffsetTopRight = margin * uniformScale;
        float yOffsetBottomLeft = margin * uniformScale;
        float yOffsetBottomRight = margin * uniformScale;

        for (TextHUD t : texts) {
            if (!t.isActive()) continue;

            String content = t.getText(data);

            // Calcul X
            float renderX = (t.getHAlign() == TextHUD.Alignment.LEFT) ?
                    margin * uniformScale :
                    windowWidth - margin * uniformScale - Text.getTextWidth(content, t.getScale() * uniformScale);

            // Calcul Y selon vAlign
            float renderY;
            if (t.getVAlign() == TextHUD.Alignment.TOP) {
                renderY = (t.getHAlign() == TextHUD.Alignment.LEFT) ? yOffsetTopLeft : yOffsetTopRight;
            } else { // BOTTOM
                renderY = (t.getHAlign() == TextHUD.Alignment.LEFT) ?
                        windowHeight - yOffsetBottomLeft - lineHeight * uniformScale :
                        windowHeight - yOffsetBottomRight - lineHeight * uniformScale;
            }

            // Affichage du texte
            Text.drawText(shader,
                    content,
                    renderX,
                    renderY,
                    t.getScale() * uniformScale,
                    t.getR(), t.getG(), t.getB());

            // Incrément offset pour le prochain texte
            if (t.getVAlign() == TextHUD.Alignment.TOP) {
                if (t.getHAlign() == TextHUD.Alignment.LEFT)
                    yOffsetTopLeft += lineHeight * uniformScale;
                else
                    yOffsetTopRight += lineHeight * uniformScale;
            } else { // BOTTOM
                if (t.getHAlign() == TextHUD.Alignment.LEFT)
                    yOffsetBottomLeft += lineHeight * uniformScale;
                else
                    yOffsetBottomRight += lineHeight * uniformScale;
            }
        }
    }
}
