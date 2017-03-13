package com.test.youyang.glsplash;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.test.youyang.glsplash.interfaces.IRenderObserver;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by youyang on 16/5/24.
 */
public class PointRender implements GLSurfaceView.Renderer {

    private FloatBuffer mFloatBuffer;
    private FloatBuffer mTextureBuffer;

    private Context mContext;

    private IRenderObserver mIRenderObserver;

    private float mProgress = 0;

    private int mIndex = 0;

    public PointRender(Context context, IRenderObserver observer) {
        mContext = context;
        mIRenderObserver = observer;
    }

    private int[] mResources = new int[2];

    float aPoint = 1f;
    float[] mVertex = new float[]{
            -aPoint, aPoint,
            -aPoint, -aPoint,
            aPoint, -aPoint,

            aPoint, -aPoint,
            aPoint, aPoint,
            -aPoint, aPoint

    };

    float[] mTextureCoord = new float[]{
            0f, 0f,
            0f, 1f,
            1f, 1f,

            1f, 1f,
            1f, 0f,
            0f, 0f
    };

    int[] ids;
    private ColorShaderProgram mColorShaderProgram;

    public void setIndex(int index) {
        mIndex = index;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mColorShaderProgram = new ColorShaderProgram(mContext, mIndex);
        GLES20.glClearColor(0f, 0f, 0f, 0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        mFloatBuffer = ByteBuffer.allocateDirect(mVertex.length * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer().put(mVertex);
        mTextureBuffer = ByteBuffer.allocateDirect(mVertex.length * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer().put(mTextureCoord);
        mFloatBuffer.position(0);
        mTextureBuffer.position(0);

//        mFloatBuffer.position(0);
    }

    public void setProcess(float m) {
        this.mProgress = m;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mIRenderObserver.onSurfaceChanged(width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
//        GLES20.glDrawArrays(GLES20.GL_POINTS,0,1);
        mColorShaderProgram.useProgram();
//        mColorShaderProgram.setUniforms(0.9f, 0.4f, 0.9f);
        mColorShaderProgram.setUniformProgressAndInterpolationPower(mProgress, 5f);

        ids = mIRenderObserver.getTextures();
        if(ids == null ){
            return;
        }

        GLES20.glVertexAttribPointer(mColorShaderProgram.getPositionAttributionLocation(), 2, GLES20.GL_FLOAT, false, 2 * 4, mFloatBuffer);
        GLES20.glVertexAttribPointer(mColorShaderProgram.getTexCoodAttributionLocation(), 2, GLES20.GL_FLOAT, false, 2 * 4, mTextureBuffer);
        GLES20.glEnableVertexAttribArray(mColorShaderProgram.getPositionAttributionLocation());
        GLES20.glEnableVertexAttribArray(mColorShaderProgram.getTexCoodAttributionLocation());

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(mColorShaderProgram.getUniformFromLocation(), 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ids[0]);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glUniform1i(mColorShaderProgram.getUniformToLocation(), 1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ids[1]);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6);
    }

}
