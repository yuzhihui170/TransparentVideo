package me.forrest.commonlib.gles;

import android.opengl.Matrix;

import java.nio.ByteBuffer;

// 存储系统矩阵状态的类
public class MatrixState {

    private float[] mProjMatrix = new float[16];//4x4矩阵 投影用
    private float[] mVMatrix = new float[16];   //摄像机位置朝向9参数矩阵
    private float[] mMVPMatrix = new float[16]; //获取具体物体的总变换矩阵
    private float[] currMatrix;                 //当前变换矩阵

    //保护变换矩阵的栈
    static float[][] mStack = new float[10][16];
    static int stackTop = -1;

    //获取不变换初始矩阵
    public void setInitStack() {
        currMatrix = new float[16];
        Matrix.setRotateM(currMatrix, 0, 0, 1, 0, 0);
    }

    //保护变换矩阵
    public void pushMatrix() {
        stackTop++;
        for (int i = 0; i < 16; i++) {
            mStack[stackTop][i] = currMatrix[i];
        }
    }

    //恢复变换矩阵
    public void popMatrix() {
        for (int i = 0; i < 16; i++) {
            currMatrix[i] = mStack[stackTop][i];
        }
        stackTop--;
    }

    //设置沿xyz轴移动
    public void translate(float x, float y, float z) {
        Matrix.translateM(currMatrix, 0, x, y, z);
    }

	// 设置绕xyz轴移动
	public void rotate(float angle, float x, float y, float z) {
		Matrix.rotateM(currMatrix, 0, angle, x, y, z);
	}

    //设置摄像机
    private ByteBuffer llbb = ByteBuffer.allocateDirect(3 * 4);
    private float[] cameraLocation = new float[3];  //摄像机位置

    public void setCamera(
			float cx,    //摄像机位置x
			float cy,   //摄像机位置y
			float cz,   //摄像机位置z
			float tx,   //摄像机目标点x
			float ty,   //摄像机目标点y
			float tz,   //摄像机目标点z
			float upx,  //摄像机UP向量X分量
			float upy,  //摄像机UP向量Y分量
			float upz   //摄像机UP向量Z分量
	) {
        Matrix.setLookAtM(
                        mVMatrix,
                        0,
                        cx,
                        cy,
                        cz,
                        tx,
                        ty,
                        tz,
                        upx,
                        upy,
                        upz
                );
    }

    // 设置透视投影参数
    public void setProjectFrustum(
		float left,        //near面的left
		float right,       //near面的right
		float bottom,      //near面的bottom
		float top,         //near面的top
		float near,        //near面距离
		float far          //far面距离
    ) {
        Matrix.frustumM(mProjMatrix, 0, left, right, bottom, top, near, far);
    }

    //设置正交投影参数
    public void setProjectOrtho(
		float left,        //near面的left
		float right,       //near面的right
		float bottom,      //near面的bottom
		float top,         //near面的top
		float near,        //near面距离
		float far          //far面距离
    ) {
        Matrix.orthoM(mProjMatrix, 0, left, right, bottom, top, near, far);
    }

    public float[] getFinalMatrix() {
        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, currMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);
        return mMVPMatrix;
    }

    //获取具体物体的变换矩阵
    public float[] getMMatrix() {
        return currMatrix;
    }
}
