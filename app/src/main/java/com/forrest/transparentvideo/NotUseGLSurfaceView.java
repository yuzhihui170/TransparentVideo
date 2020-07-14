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
import me.forrest.commonlib.gles.Rectangle;

class NotUseGLSurfaceView extends BaseGLSurfaceView {
    private static String TAG = "PlayViewGL";
    private MyRenderer mRenderer;
    private Rectangle mRect;
    private int mTextureID; // 保存OES纹理ID
    private SurfaceTexture mSurfaceTexture;
    public float[] mSTMatrix = {
            1,0,0,0,
            0,1,0,0,
            0,0,1,0,
            0,0,0,1
    };

    public NotUseGLSurfaceView(Context context) {
        this(context, null);
    }

    public NotUseGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        mRenderer = new MyRenderer();
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        Log.i(TAG, "setRenderer");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        Log.d(TAG, "surfaceDestroyed");
    }

    public SurfaceTexture getSurfaceTexture() {
        return this.mSurfaceTexture;
    }

    class MyRenderer extends BaseGLSurfaceView.BaseRenderer implements SurfaceTexture.OnFrameAvailableListener{

        private boolean updateSurface = false;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // 创建一个矩形绘制对象，使用 OES纹理 带矩阵运算的shader
            mRect = new Rectangle();
            mRect.init(Rectangle.ShaderType.TEXTURE_OES_Simple_TRANS);
            // 生成oes TextureID和 SurfaceTexture
            mTextureID = GlUtil.generateTextureIdOES();
            mSurfaceTexture = new SurfaceTexture(mTextureID);
            mSurfaceTexture.setOnFrameAvailableListener(this);
            updateSurface = false;
            super.onSurfaceCreated(gl, config);
            Log.d(TAG, "onSurfaceCreated");
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            super.onSurfaceChanged(gl, width, height);
            Log.d(TAG, "onSurfaceChanged");
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            synchronized (this) {
                if (updateSurface) {
                    updateSurface = false;
                    mSurfaceTexture.updateTexImage();
                    mSurfaceTexture.getTransformMatrix(mSTMatrix);
                    GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
                    // 设置视频播放窗口大小是1080*1920，由于openGL视窗左下角是原点，为了能在屏幕顶部开始显示，需要将视窗上移 mHeight-1920 个像素
                    GLES20.glViewport(0, 0, 1080, 1920);
                    GLES20.glEnable(GLES20.GL_BLEND);
                    GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_CONSTANT_ALPHA);
                    mRect.drawSelf(mTextureID, GlUtil.IDENTITY_MATRIX, GlUtil.IDENTITY_MATRIX);
                    GLES20.glDisable(GLES20.GL_BLEND);
                }
            }
        }

        @Override
        synchronized public void onFrameAvailable(SurfaceTexture surface) {
            synchronized (this) {
                updateSurface = true;
                requestRender();
            }
        }
    }
}
