package com.forrest.transparentvideo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import me.forrest.commonlib.gles.GlUtil;
import me.forrest.commonlib.gles.MatrixState;
import me.forrest.commonlib.gles.Rectangle;

// 用于绘制预览和透明视频的GLSurfaceView
public class PreviewGLSurfaceView extends BaseGLSurfaceView implements SurfaceHolder.Callback {
    private final static String TAG = "PreviewGL";
    private final Object mSync = new Object();
    private MyRenderer mRenderer;

    public PreviewGLSurfaceView(Context context) {
        this(context, null);
    }

    public PreviewGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        mRenderer = new MyRenderer();
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
//      setPreserveEGLContextOnPause(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        mRenderer.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        mRenderer.onPause();
        Log.d(TAG, "onPause");
    }

    public SurfaceTexture getSurfaceTexture() {
        return mRenderer.surfaceTexture;
    }

    public SurfaceTexture getSurfaceTextureForVideo() {
        return mRenderer.surfaceTextureForVideo;
    }

    class MyRenderer extends BaseRenderer implements SurfaceTexture.OnFrameAvailableListener {

        private Rectangle rectCamera;
        private Rectangle rectVideo;
        private int texId;
        private int texIdForVideo;
        private SurfaceTexture surfaceTexture;
        private SurfaceTexture surfaceTextureForVideo;
        private boolean updateSurface = false;

        private void onResume() {
        }

        private void onPause() {
        }

        float[] currMatrix;
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            rectCamera = new Rectangle();
            rectCamera.init(Rectangle.ShaderType.TEXTURE_OES_Matrix);
            texId = GlUtil.generateTextureIdOES();
            surfaceTexture = new SurfaceTexture(texId);
            surfaceTexture.setOnFrameAvailableListener(this);

            rectVideo = new Rectangle();
            rectVideo.init(Rectangle.ShaderType.TEXTURE_OES_Matrix_TRANS);
            texIdForVideo = GlUtil.generateTextureIdOES();
            surfaceTextureForVideo = new SurfaceTexture(texIdForVideo);
            surfaceTextureForVideo.setOnFrameAvailableListener(this);

            synchronized (mSync) {
                updateSurface = false;
            }
            super.onSurfaceCreated(gl, config);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            super.onSurfaceChanged(gl, width, height);
            // 由于Camera方式和屏幕方向不一致，需要对预览画面旋转即可正常显示。
            MatrixState matrixState = new MatrixState();
            matrixState.setInitStack();
            matrixState.rotate(90, 0, 0, 1);
            currMatrix = matrixState.getMMatrix();
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            synchronized (mSync) {
                if (updateSurface) {
                    updateSurface = false;
                    surfaceTexture.updateTexImage();
                    surfaceTextureForVideo.updateTexImage();
                    GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
                    GLES20.glViewport(0, mHeight-1920, 1080, 1920);
                    GLES20.glEnable(GLES20.GL_BLEND);
                    // src dst
                    // 表示源颜色乘以自身的alpha 值，目标颜色乘以1.0减去源颜色的alpha值，
                    // 这样一来，源颜色的alpha值越大，则产生的新颜色中源颜色所占比例就越大，而目标颜色所占比例则减 小。
                    // 这种情况下，我们可以简单的将源颜色的alpha值理解为“不透明度”。这也是混合时最常用的方式。
                    GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
                    rectCamera.drawSelf(texId, currMatrix, GlUtil.IDENTITY_MATRIX);
                    rectVideo.drawSelf(texIdForVideo, GlUtil.IDENTITY_MATRIX, GlUtil.IDENTITY_MATRIX);
                    GLES20.glDisable(GLES20.GL_BLEND);
                }
            }
        }

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            synchronized (mSync) {
                updateSurface = true;
                requestRender();
            }
        }
    }

}
