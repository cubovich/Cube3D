package cubovich.example.cube3d;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private Cube cube;

    private final float[] vpMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];

    public volatile float angleVertical = 0f;
    public volatile float angleHorizontal = 0f;
    public volatile float eyeZ;


    public float getAngleHorizontal() {
        return angleHorizontal;
    }
    public void setAngleHorizontal(float angle) {
        angleHorizontal = angle;
    }

    public float getAngleVertical() {
        return angleVertical;
    }
    public void setAngleVertical(float angle) {
        angleVertical = angle;
    }


    public float getEyeZ() {
        return eyeZ;
    }
    public void setEyeZ(float n) {
        eyeZ = n;
    }


    public static int loadShader(int type, String shaderCode) {

        // Create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // Add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.5f, 1.0f);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LESS);

        cube = new Cube();
    }

    public void onDrawFrame(GL10 unused) {
        // Redraw background color and control depth
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(
                viewMatrix, 0,
                0, 0, eyeZ,
                0f, 0f, 0f,
                0f, 1.0f, 0.0f
        );

        Matrix.multiplyMM(
                vpMatrix,  // View-Projection matrix
                0,
                projectionMatrix,
                0,
                viewMatrix,
                0
        );

        cube.rotate(angleVertical, angleHorizontal);
        cube.draw(vpMatrix);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // This projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 17);
    }
}
