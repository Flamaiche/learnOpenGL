package gameGl.utils;

import org.joml.Matrix4f;

public abstract class Entity {

    /**
     * Met à jour l'état de l'entité (position, rotation, etc.).
     */
    public abstract void update(float deltaTime);

    /**
     * Rend l'entité avec les matrices view et projection données.
     */
    public abstract void render(Matrix4f view, Matrix4f projection);

    /**
     * Nettoie les ressources GPU de l'entité.
     */
    public abstract void cleanup();

    /**
     * Retourne la matrice modèle actuelle de l'entité.
     */
    public abstract Matrix4f getModelMatrix();
}
