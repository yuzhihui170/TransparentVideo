package com.forrest.transparentvideo;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class BaseGLSurfaceView extends GLSurfaceView {
    private static String TAG = "GLSurfaceView";
    private boolean surfaceViewAvailable;
    protected BaseGLSurfaceViewListener baseGLSurfaceViewListener;
    protected int mWidth = 0;
    protected int mHeight = 0;

    // 用于获取GLSurfaceView生命周期
    public interface BaseGLSurfaceViewListener {
        void surfaceCreated();
        void surfaceChanged();
        void surfaceDestroyed();
    }

    protected void setBaseGLSurfaceViewListener(BaseGLSurfaceViewListener listener) {
        this.baseGLSurfaceViewListener = listener;
    }

    protected boolean isSurfaceViewAvailable() {
        return this.surfaceViewAvailable;
    }

    public BaseGLSurfaceView(Context context) {
        super(context);
    }

    public BaseGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        if (baseGLSurfaceViewListener != null) {
            baseGLSurfaceViewListener.surfaceDestroyed();
        }
        surfaceViewAvailable = false;
        Log.d(TAG, "surfaceDestroyed");
    }

    abstract class BaseRenderer implements Renderer {

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            if (baseGLSurfaceViewListener != null) {
                baseGLSurfaceViewListener.surfaceCreated();
            }
            surfaceViewAvailable = true;
            Log.d(TAG, "onSurfaceCreated");
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            mWidth = width;
            mHeight = height;
            if (baseGLSurfaceViewListener != null) {
                baseGLSurfaceViewListener.surfaceChanged();
            }
            Log.d(TAG, "onSurfaceChanged mWidth:"+ mWidth + " mHeight:"+mHeight);
        }

        @Override
        public void onDrawFrame(GL10 gl) {

        }
    }
}
