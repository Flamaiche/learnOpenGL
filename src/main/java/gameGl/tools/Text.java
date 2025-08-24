package gameGl.tools;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBEasyFont;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import learnGL.tools.Shader;

public class Text {
    private static int vao, vbo;
    private static boolean initialized = false;

    private static void init() {
        if (initialized) return;
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        initialized = true;
    }

    public static void drawText(Shader shader, String text,
                                float x, float y, float scale,
                                float r, float g, float b) {
        if (text == null || text.isEmpty()) return;
        init();

        ByteBuffer charBuffer = BufferUtils.createByteBuffer(99999);
        int quads = STBEasyFont.stb_easy_font_print(0, 0, text, null, charBuffer);
        FloatBuffer fb = charBuffer.asFloatBuffer();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, fb, GL_DYNAMIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 16, 0);

        shader.bind();

        // Projection ortho dynamique
        int[] viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);
        int winWidth = viewport[2];
        int winHeight = viewport[3];

        Matrix4f ortho = new Matrix4f().ortho(0f, winWidth, winHeight, 0f, -1f, 1f);
        shader.setUniformMat4f("projection", ortho);

        int locOffset = glGetUniformLocation(shader.getProgramId(), "offset");
        int locScale = glGetUniformLocation(shader.getProgramId(), "scale");
        int locColor = glGetUniformLocation(shader.getProgramId(), "textColor");

        glUniform2f(locOffset, x, y);
        glUniform1f(locScale, scale);
        glUniform3f(locColor, r, g, b);

        glDrawArrays(GL_QUADS, 0, quads * 4);

        glDisableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        shader.unbind();
    }

    public static void cleanup() {
        if (!initialized) return;
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
        initialized = false;
    }
}
