package com.test.youyang.beautifulsplash;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.test.youyang.glsplash.GLSwitchView;
import com.test.youyang.glsplash.PointRender;

/**
 * 着色器 是 软件与GPU硬件的桥梁，可以通过着色器去控制GPU去进行高性能的绘制。 如果不使用着色器，我们就只能通过cpu去进行渲染，两者的效率天差地别。
 *
 * 这节课开始接触着色器，用着色器 绘制一个普通的顶点Point 1.学会写简单的着色器 2.了解如何加载着色器 3.学会如何在java代码中动态给着色器的属性赋值
 */

public class MainActivity extends Activity{

    GLSwitchView mView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mView = new GLSwitchView(this);
        int index = getIntent().getIntExtra("index",0);
        mView.setStyleIndex(index);
        mView.setPictureList(new int[]{R.drawable.img_loading,R.drawable.img_loadings});
        setContentView(mView);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mView.onPause();
    }

}
