package gameGl.tools;

public class TextHUD {
    private String content;
    private final float x, y;
    private final float scale;
    private final float r, g, b;
    private boolean active;

    public TextHUD(String content, float x, float y, float scale, float r, float g, float b) {
        this.content = content;
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.r = r;
        this.g = g;
        this.b = b;
        this.active = true;
    }

    public void setContent(String content) { this.content = content; }
    public String getContent() { return content; }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getScale() { return scale; }
    public float getR() { return r; }
    public float getG() { return g; }
    public float getB() { return b; }

    public void setActive(boolean active) { this.active = active; }
    public boolean isActive() { return active; }
}
