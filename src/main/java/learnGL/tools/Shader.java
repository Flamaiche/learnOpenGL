package learnGL.tools;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL20.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

public class Shader {
    private int programId;
    private int vertexShaderId;
    private int fragmentShaderId;

    // Garder le code shader en mémoire
    private String vertexCode;
    private String fragmentCode;

    // Constructeur : on donne les chemins des fichiers shader (relatifs dans resources)
    public Shader(String vertexPath, String fragmentPath) throws IOException {
        // Lire les fichiers une fois et stocker le code
        vertexCode = readFileFromResources(vertexPath);
        fragmentCode = readFileFromResources(fragmentPath);

        // Compiler et lier les shaders
        compile();
    }

    private String readFileFromResources(String fileName) throws IOException {
        try (InputStream in = Shader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (in == null) {
                throw new IOException("Resource not found: " + fileName);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }

    public int getProgramId() {
        return programId;
    }

    private void compile() {
        // Créer et compiler le vertex shader
        vertexShaderId = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShaderId, vertexCode);
        glCompileShader(vertexShaderId);
        if (glGetShaderi(vertexShaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Erreur compilation vertex shader : " + glGetShaderInfoLog(vertexShaderId));
        }

        // Créer et compiler le fragment shader
        fragmentShaderId = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShaderId, fragmentCode);
        glCompileShader(fragmentShaderId);
        if (glGetShaderi(fragmentShaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Erreur compilation fragment shader : " + glGetShaderInfoLog(fragmentShaderId));
        }

        // Créer le programme, attacher les shaders et linker
        programId = glCreateProgram();
        glAttachShader(programId, vertexShaderId);
        glAttachShader(programId, fragmentShaderId);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            System.err.println("Erreur linkage shader : " + glGetProgramInfoLog(programId));
        }
    }

    // Activer ce shader pour dessiner
    public void bind() {
        glUseProgram(programId);
    }

    // Désactiver les shaders (utiliser programme par défaut)
    public void unbind() {
        glUseProgram(0);
    }

    // Nettoyer toutes les ressources
    public void cleanup() {
        unbind();
        glDetachShader(programId, vertexShaderId);
        glDetachShader(programId, fragmentShaderId);
        glDeleteShader(vertexShaderId);
        glDeleteShader(fragmentShaderId);
        glDeleteProgram(programId);
    }

    public void setUniformMat4f(String name, Matrix4f matrix) {
        int location = glGetUniformLocation(programId, name);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        matrix.get(buffer);
        glUniformMatrix4fv(location, false, buffer);
    }
}
