package cubovich.example.cube3d;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Cube {
    private FloatBuffer shapeBuffer;
    private FloatBuffer colorsBuffer;
    private ShortBuffer drawListBuffer;
    private final int program;
    private int mvpMatrixHandle; // Use to access and set the view transformation
    private int positionHandle;
    private int colorHandle;
    static final int COORDS_PER_VERTEX = 3; // Number of coordinates per vertex in array
    static final int COLORS_PER_VERTEX = 4; // Number of color components per vertex in array
    private final int vertexCoordStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex position
    private final int vertexColorStride = COLORS_PER_VERTEX * 4; // 4 bytes per color's component
    private float[] scaleMatrix = new float[16];
    private float[] rotationMatrix = new float[16];
    private float[] translationMatrix = new float[16];
    private float[] modelMatrix = new float[16];

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;"
                    + "attribute vec4 vPosition;"
                    + "attribute vec4 vColor;"
                    + "varying vec4 fColor;"
                    + "void main() {"
                    + "   gl_Position = uMVPMatrix * vPosition;"
                    + "   fColor = vColor;"
                    + "}";

    private final String fragmentShaderCode =
            "precision mediump float;"
                    + "varying vec4 fColor;"
                    + "void main() {"
                    + "  gl_FragColor = fColor;"
                    + "}";



    static final float[] shapeCoords = {
            -0.5f, 0.5f, -0.5f,     // 0. back top left
            -0.5f, -0.5f, -0.5f,    // 1. back bottom left
            0.5f, -0.5f, -0.5f,     // 2. back bottom right
            0.5f, 0.5f, -0.5f,      // 3. back top right
            0.5f, 0.5f, 0.5f,       // 4. front top right
            -0.5f, 0.5f, 0.5f,      // 5. front top left
            -0.5f, -0.5f, 0.5f,     // 6. front bottom left
            0.5f, -0.5f, 0.5f,      // 7. front bottom right
    };

    static float[] vertexColors = {
            1.0f, 0.0f, 0.0f, 1.0f, // red
            0.0f, 1.0f, 0.0f, 1.0f, // green
            0.0f, 0.0f, 1.0f, 1.0f, // blue
            1.0f, 1.0f, 1.0f, 1.0f, // white
            1.0f, 1.0f, 0.0f, 1.0f, // yellow
            0.0f, 1.0f, 1.0f, 1.0f, // cyan
            1.0f, 0.0f, 1.0f, 1.0f, // pink
            0.0f, 0.0f, 0.0f, 1.0f  // black
    };

    private final short[] vertexDrawOrder = {
            // walls
            6, 7, 4, 4, 5, 6,   // 1. front
            6, 5, 1, 1, 5, 0,   // 3. left
            0, 5, 4, 4, 3, 0,   // 2. top
            2, 7, 4, 2, 4, 3,   // 5. right
            1, 2, 6, 2, 7, 6,   // 4. bottom
            0, 2, 1, 3, 2, 0    // 6. back
    };

    public Cube() {
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.setIdentityM(translationMatrix, 0);

        // Initialize vertex byte buffer for shape coordinates
        ByteBuffer sb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                shapeCoords.length * 4);
        sb.order(ByteOrder.nativeOrder());
        shapeBuffer = sb.asFloatBuffer();
        shapeBuffer.put(shapeCoords);
        shapeBuffer.position(0);

        ByteBuffer cb = ByteBuffer.allocateDirect(vertexColors.length * 4);
        cb.order(ByteOrder.nativeOrder());
        colorsBuffer = cb.asFloatBuffer();
        colorsBuffer.put(vertexColors);
        colorsBuffer.position(0);

        // Initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                vertexDrawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(vertexDrawOrder);
        drawListBuffer.position(0);

        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        program = GLES20.glCreateProgram(); // Create empty OpenGL ES Program
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program); // Creates OpenGL ES program executables
    }

    public void rotate(float angleV, float angleH) {
        float[] xAxis = new float[4];
        float[] yAxis = new float[4];
        float[] rotationMatrixT = new float[16];

        // Inverse rotation
        Matrix.transposeM(rotationMatrixT, 0, rotationMatrix, 0);

        // Compute axes
        Matrix.multiplyMV(xAxis, 0, rotationMatrixT, 0, new float[] {-1f, 0f, 0f, 0f}, 0);
        Matrix.multiplyMV(yAxis, 0, rotationMatrixT, 0, new float[] {0f, 1f, 0f, 0f}, 0);

        // Rotate cube
        Matrix.rotateM(rotationMatrix, 0, angleH, yAxis[0], yAxis[1], yAxis[2]);
        Matrix.rotateM(rotationMatrix, 0, angleV, xAxis[0], xAxis[1], xAxis[2]);
    }

    public void draw(float[] vpMatrix) {
        float[] modelMatrixPart = new float[16];
        float[] mvpMatrix = new float[16];

        GLES20.glUseProgram(program);

        // Pass vertex positions
        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, vertexCoordStride, shapeBuffer);

        // Pass vertex colors
        colorHandle = GLES20.glGetAttribLocation(program, "vColor");
        GLES20.glEnableVertexAttribArray(colorHandle);
        GLES20.glVertexAttribPointer(colorHandle, COLORS_PER_VERTEX, GLES20.GL_FLOAT,
                true, vertexColorStride, colorsBuffer);

        // Compute model matrix
        Matrix.multiplyMM(modelMatrixPart, 0, rotationMatrix, 0, scaleMatrix, 0);
        Matrix.multiplyMM(modelMatrix, 0, translationMatrix, 0, modelMatrixPart, 0);

        // Compute model-view-projection matrix
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0);

        // Pass mvp matrix
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        // Drawing
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES,
                vertexDrawOrder.length,
                GLES20.GL_UNSIGNED_SHORT,
                drawListBuffer
        );

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(colorHandle);
    }
}
