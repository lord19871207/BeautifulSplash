package com.test.youyang.glsplash.interfaces;

/**
 * Created by youyang on 2017/3/6.
 */

public interface IRenderObserver {
    int[] getTextures();

    void onSurfaceChanged(int width, int height);
}
