package cubovich.example.cube3d;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class MyGLSurfaceView  extends GLSurfaceView {

    private final MyGLRenderer renderer;

    private final float TOUCH_SCALE_FACTOR = 0.3f;
    private float previousX;
    private float previousY;

    private ScaleGestureDetector scaleDetector;
    private float scaleFactor = 1f;

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        scaleDetector.onTouchEvent(e);
        if (scaleDetector.isInProgress()) {
            return true;
        }

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - previousX;
                float dy = y - previousY;

                renderer.setAngleHorizontal(
                        //renderer.getAngleHorizontal()
                        + (dx * TOUCH_SCALE_FACTOR)
                );
                renderer.setAngleVertical(
                        //renderer.getAngleVertical()
                        + (dy * TOUCH_SCALE_FACTOR)
                );

                requestRender();
                break;
        }

        previousX = x;
        previousY = y;
        return true;
    }

    public MyGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        renderer = new MyGLRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        renderer.setEyeZ(-6f);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        float lastEyeZ;
        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            scaleFactor = 1f;
            lastEyeZ = renderer.getEyeZ();
            return true;
        }


        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();

            float newEyeZ = lastEyeZ * 1/scaleFactor;

            if (newEyeZ > -4f) {
                renderer.setEyeZ(-4f);
            }
            else if (newEyeZ < -15f) {
                renderer.setEyeZ(-15f);
            }
            else {
                renderer.setEyeZ(newEyeZ);
            }

            requestRender();
            return true;
        }
    }
}

