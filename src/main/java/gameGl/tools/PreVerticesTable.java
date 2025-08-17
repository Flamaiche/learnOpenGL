package gameGl.tools;

public class PreVerticesTable {

    public static float[] generateCubeSimple(float n) {
        float h = n / 2f;

        return new float[]{
                // Face avant
                -h, -h,  h,   h, -h,  h,   h,  h,  h,
                h,  h,  h,  -h,  h,  h,  -h, -h,  h,
                // Face arrière
                -h, -h, -h,  -h,  h, -h,   h,  h, -h,
                h,  h, -h,   h, -h, -h,  -h, -h, -h,
                // Face gauche
                -h, -h, -h,  -h, -h,  h,  -h,  h,  h,
                -h,  h,  h,  -h,  h, -h,  -h, -h, -h,
                // Face droite
                h, -h, -h,   h,  h, -h,   h,  h,  h,
                h,  h,  h,   h, -h,  h,   h, -h, -h,
                // Face haute
                -h,  h, -h,  -h,  h,  h,   h,  h,  h,
                h,  h,  h,   h,  h, -h,  -h,  h, -h,
                // Face basse
                -h, -h, -h,   h, -h, -h,   h, -h,  h,
                h, -h,  h,  -h, -h,  h,  -h, -h, -h
        };
    }

    public static float[] generatePyramid(float baseSize, float height) {
        float h = baseSize / 2f;

        return new float[]{
                // Base (2 triangles)
                -h, 0, -h,   h, 0, -h,   h, 0,  h,
                h, 0,  h,  -h, 0,  h,  -h, 0, -h,

                // Face avant
                -h, 0,  h,    h, 0,  h,    0, height, 0,
                // Face arrière
                -h, 0, -h,    0, height, 0,    h, 0, -h,
                // Face gauche
                -h, 0, -h,   -h, 0,  h,    0, height, 0,
                // Face droite
                h, 0, -h,    h, 0,  h,    0, height, 0
        };
    }

}
