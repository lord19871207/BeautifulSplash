package com.test.youyang.glsplash;

import android.app.ActivityManager;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;
import android.widget.Toast;

import com.test.youyang.glsplash.interfaces.IPictureProvider;
import com.test.youyang.glsplash.interfaces.IRenderObserver;

/**
 * Created by youyang on 16/7/24.
 */
public class GLSwitchView extends GLSurfaceView implements IPictureProvider, IRenderObserver {

    private static final String TAG = "GLSwitchView";
    private boolean isMoved;
    private float process = 0.0f;
    private int[] mSize;
//    private float moveX = 0;
    private int[] mDrawables = new int[]{};
    private Scroller mScroller;

    private PointRender mRender;

    private static int TOUCH_SLOP;

    private Context mContext;
    private boolean mNeedToChange = true;

    private int mCurrentIndex = 0;
    private int mTempCurrentIndex = 0;
    private boolean mAnimate = false;

    public GLSwitchView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mScroller = new Scroller(context, new DecelerateInterpolator());
        mFlingRunnable = new FlingRunnable();
        mSize = getScreenSize(context);
        TOUCH_SLOP = ViewConfiguration.get(mContext).getScaledTouchSlop();
        mRender = new PointRender(context, this);
        if (isSupportES2(context)) {
            //设定egl版本
            setEGLContextClientVersion(2);
            //设置渲染器，设置完这一步之后相当于开启了 另一条渲染线程
            setRenderer(mRender);
            //设置渲染模式
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        } else {
            Toast.makeText(context, "该手机不支持opengl es 2.0", Toast.LENGTH_LONG).show();
            return;
        }
    }


    /**
     * 检验是否支持opengl 2.0
     */
    private boolean isSupportES2(Context context) {
        return ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                .getDeviceConfigurationInfo().reqGlEsVersion >= 0x20000;
    }

    /**
     * 方法描述：获取屏幕宽高
     */
    public static int[] getScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay()
                .getMetrics(localDisplayMetrics);
        int mScreenHeight = localDisplayMetrics.heightPixels;
        int mScreenWidth = localDisplayMetrics.widthPixels;
        int[] size = {mScreenWidth, mScreenHeight};
        return size;
    }

    public GLSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(handleTouchCommon(event)){
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                break;
            case MotionEvent.ACTION_MOVE:
                if(isMoved){
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            process = Math.abs(mMoveX / mSize[0]);
                            mRender.setProcess(process);
                            Log.d("youyang", "process :" + process);
                            requestRender();
                        }
                    });
                }


                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                startScollAnimation(isMoved);
                isMoved = false;
                requestRender();
                Log.d("youyang","mCurrentIndex:"+mCurrentIndex  +" , mNeedToChange:"+mNeedToChange);
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 上一页下一页的趋势 0空闲状态；1 上一页；2 下一页；3菜单；4长按事件
     */
    private int mScrollDirection = DIRECTION_VOID;

    private int mTempScrollDirection = DIRECTION_VOID;
    /**
     * 空闲状态or点击状态
     */
    public static final int DIRECTION_VOID = 0x0004;
    /**
     * 上一页
     */
    public static final int DIRECTION_LAST = 0x0005;
    /**
     * 下一页o
     */
    public static final int DIRECTION_NEXT = 0x0006;

    /**
     * touch事件down时的y轴坐标
     */
    private float mTouchDownY;
    /**
     * 一次完整touch事件中上一次的x轴坐标
     */
    private float mLastTouchX;
    /**
     * 一次完整touch事件中上一次的y轴坐标
     */
    private float mLastTouchY;

    /**
     * touch事件down时的x轴坐标
     */
    private float mTouchDownX;
    private float mPreY;
    private float mCurrentY;
    private float mDy;
    private float mPreX;
    private float mCurrentX;
    private float mDx;
    private float mMoveX = 0;
    private float mMoveY;
    private FlingRunnable mFlingRunnable;
    private boolean flag = true;

    private boolean isTouchCancle;

    /**
     * 处理触摸事件的公共部分
     */
    private boolean handleTouchCommon(MotionEvent event) {

        if(mAnimate){
            Log.d("youyang","mAnimate:"+mAnimate);
            return true;
        }

        mCurrentX = event.getX();
        mCurrentY = event.getY();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mScrollDirection = DIRECTION_VOID;
                mTouchDownX = event.getX();
                mTouchDownY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                mMoveX = event.getX();
                mMoveY = event.getY();
                mDy = mCurrentY - mPreY;
                mDx = mCurrentX - mPreX;
                if (Math.abs(mTouchDownX - mMoveX) > TOUCH_SLOP
                        || Math.abs(mTouchDownY - mMoveY) > TOUCH_SLOP) {
                    // 移动超过阈值，则表示移动了
                    isMoved = true;
                }
                mPreY = mCurrentY;
                mPreX = mCurrentX;
                if (isMoved) {
                    if (flag) {
                        flag = false;
                        mNeedToChange = true;
                        if (mDx > 0) {
                            mScrollDirection = DIRECTION_LAST;
                            if (mCurrentIndex != 0) {
                                mCurrentIndex--;
                                isTouchCancle = false;
                            } else {
                                Toast.makeText(mContext,"已经翻到首页",Toast.LENGTH_SHORT).show();
                                Log.d(TAG,"已经翻到首页");
                                isTouchCancle = true;
                                return true;
                            }
                        } else {
                            mScrollDirection = DIRECTION_NEXT;
                            if (mCurrentIndex != mDrawables.length - 1) {
                                mCurrentIndex++;
                                isTouchCancle = false;
                            }else {
                                Toast.makeText(mContext,"已经翻到末页",Toast.LENGTH_SHORT).show();
                                Log.d(TAG,"已经翻到末页");
                                isTouchCancle = true;
                                return true;
                            }
                        }

                    }

                    if(isTouchCancle){
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLastTouchX = event.getX();
                mLastTouchY = event.getY();
                if (!isMoved) {
                    mScrollDirection = getDirection(mLastTouchX);
                }
                mTempScrollDirection = mScrollDirection;
                flag = true;
                break;
            default:
                break;
        }
        return false;
    }

    private int getDirection(float lastTouchX) {

        if (lastTouchX < mSize[0] / 2f) {
            mScrollDirection = DIRECTION_LAST;
        } else {
            mScrollDirection = DIRECTION_NEXT;
        }
        return mScrollDirection;
    }


    private void startScollAnimation(boolean isTouch) {
        int distance = 0;

        if (!isTouch) {

            if (mScrollDirection == DIRECTION_LAST) {
                mMoveX = 0;
                mFlingRunnable.startByTouch(mSize[0]);
            } else if (mScrollDirection == DIRECTION_NEXT) {
                mMoveX = mSize[0];
                mFlingRunnable.startByTouch(-mSize[0]);
            }
        } else {
            // 翻下一页
            if (mScrollDirection == DIRECTION_NEXT) {
                if (mDx <= 0) {
                    // 手左右来回划，最后滑的方向是从右往左
//                distance = mTouchDownX - mLastTouchX > 0 ? (int) (mTouchDownX - mLastTouchX)
//                        - mWidth : (int) (mLastTouchX - mWidth);
                    distance = (mTouchDownX - mLastTouchX > 0 ? (int) (mTouchDownX - mLastTouchX)
                            - mSize[0] : (int) (-mSize[0] + mTouchDownX - mLastTouchX));
                } else {
                    distance = mTouchDownX - mLastTouchX > 0 ? (int) (mTouchDownX - mLastTouchX) : 0;
                }
            } else if (mScrollDirection == DIRECTION_LAST) {
                // 手左右来回划，最后滑的方向是从右往左
                if (mDx < 0) {
//                distance = mTouchDownX - mLastTouchX < 0 ? (int) (mTouchDownX - mLastTouchX) : 0;
                    distance = -(int)mLastTouchX;
                } else {

                    distance = mSize[0] - (int)mLastTouchX;
                }
            }
            mFlingRunnable.startByTouch(distance);
        }
        mAnimate = true;

    }

    @Override
    public void setPictureList(int[] drawables) {
        mDrawables = drawables;
    }

    @Override
    public void setStyleIndex(int styleIndex) {
        mRender.setIndex(styleIndex);
    }

    private int[] mTextureIds;

    @Override
    public int[] getTextures() {
        if (mNeedToChange) {
            if(mScrollDirection == DIRECTION_NEXT){
                mTextureIds = TextureHelper.loadTexture(mContext, mDrawables[mCurrentIndex], mDrawables[mTempCurrentIndex]);
            } else {
                mTextureIds = TextureHelper.loadTexture(mContext, mDrawables[mTempCurrentIndex], mDrawables[mCurrentIndex]);
            }
            mNeedToChange = false;
        }
        return mTextureIds;
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        mSize[0] = width;
        mSize[1] = height;
    }

    class FlingRunnable implements Runnable {
        private static final int TOUCH_ANIMATION_DURATION = 350;
        private static final int MIN_ANIMATION_DURATION = 1500;
        private int mLastFlingX;

        /**
         * 移除消息队列里的上个动作
         */
        private void startCommon() {
            removeCallbacks(this);
        }

        /**
         * 另外开启一个线程来横向滑动书页
         *
         * @param distance 滑动的距离
         */
        public void startByTouch(int distance) {
            startUsingDistance(distance, TOUCH_ANIMATION_DURATION);
        }

        /**
         * 异步书页偏移的 实际执行方法
         *
         * @param distance 偏移距离
         * @param during   持续时间
         */
        public void startUsingDistance(int distance, int during) {
            if (distance == 0){
                mAnimate = false;
                return;
            }
            startCommon();
            mLastFlingX = 0;
            // 起始点为（0，0），x偏移量为 -distance ，y的偏移量为 0，持续时间
            mScroller.startScroll(0, 0, -distance, 0, Math.max(MIN_ANIMATION_DURATION, Math.abs(distance) * during / mSize[0]));
            post(this);
        }

        /**
         * 停止滑动
         */
        private void endFling() {
            mScroller.forceFinished(true);
            mNeedToChange = true;
            mTempCurrentIndex = mCurrentIndex;
            mAnimate = false;
        }

        @Override
        public void run() {

            boolean more = mScroller.computeScrollOffset();// 返回true的话则动画还没有结束
            final int x = mScroller.getCurrX();// 返回滚动时 当前的x坐标
//            Log.d("youyang", "mScroller.getCurrX()" + x);
            int delta = mLastFlingX - x;
            if (delta != 0) {
                mMoveX += delta;
                Log.d("youyang", "mSize[0]" + mSize[0] + ", mSize[1]:" + mSize[1]);

                float process = Math.abs(mMoveX) / (mSize[0]);
                Log.d("youyang", "process" + process);
                mRender.setProcess(process);
                requestRender();
            }
            if (more) {
                mLastFlingX = x;
                post(this);
            } else {
                endFling();
            }

        }
    }
}
