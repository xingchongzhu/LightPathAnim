package com.path.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import static com.path.utils.SvgUtils.getPathRect;

/**
 * 介绍：一个自定义View Path动画的工具类
 * <p>
 * 一个SourcePath 内含多段（一段）Path，循环取出每段Path，并做一个动画,
 * <p>
 * 默认动画时间1500ms，无限循环
 * 可以通过构造函数修改这两个参数
 * <p>
 * 对外暴露 startAnim() 和 stopAnim()两个方法
 * <p>
 * 子类可通过重写onPathAnimCallback（）方法，对animPath进行再次操作，从而定义不同的动画效果
 * 作者：zhangxutong
 * 邮箱：zhangxutong@imcoming.com
 * 时间： 2016/11/2.
 */

public class BaseAnimHelper {
    private static final String TAG = "BaseAnimHelper";
    protected static final long mDefaultAnimTime = 2;//默认动画总时间
    private static final long MIN_DRUATION = 200;

    protected View mView;//执行动画的View
    protected Path mSourcePath;//源Path
    protected Path mAnimPath;//用于绘制动画的Path
    protected long stepDuaration;//单位时间
    protected boolean mIsInfinite;//是否无限循环
    protected boolean mAuto;
    protected Interpolator interpolator = new AccelerateDecelerateInterpolator();

    protected ValueAnimator mAnimator;//动画对象
    protected boolean startLoop = false;

    protected Rect mBoundRect = new Rect();

    PathMeasure pathMeasure = new PathMeasure();
    private float mAllLength = 0;//总长度
    private int mAllCount = 0;
    private int mCurrentCount = 0;
    protected AnimaLoopCallback mAnimaLoopCallback;

    /**
     * INIT FUNC
     **/
    public BaseAnimHelper(){
        this(null, null, null, mDefaultAnimTime, true);
    }

    public BaseAnimHelper(View view, Path sourcePath, Path animPath) {
        this(view, sourcePath, animPath, mDefaultAnimTime, true);
    }

    public BaseAnimHelper(View view, Path sourcePath, Path animPath, long stepDuaration, boolean isInfinite) {
        if (view == null || sourcePath == null || animPath == null) {
            Log.e(TAG, "BaseAnimHelper init error: view 、sourcePath、animPath can not be null");
            return;
        }
        mView = view;
        mSourcePath = sourcePath;
        mAnimPath = animPath;
        this.stepDuaration = stepDuaration;
        mIsInfinite = isInfinite;

        initStartAndEndPosition(mSourcePath);
    }

    public BaseAnimHelper initStartAndEndPosition(Path sourcePath) {
        PathMeasure pathMeasure = new PathMeasure();
        pathMeasure.setPath(sourcePath, false);
        //这里仅仅是为了 计算一下每一段的duration

        mBoundRect = getPathRect(sourcePath);

        Log.d(TAG,"initStartAndEndPosition rect "+mBoundRect);
        return this;
    }

    public void setAnimaLoopCallback(AnimaLoopCallback mAnimaLoopCallback) {
        this.mAnimaLoopCallback = mAnimaLoopCallback;
    }

    /**
     * GET SET FUNC
     **/
    public View getView() {
        return mView;
    }

    public BaseAnimHelper setView(View view) {
        mView = view;
        return this;
    }

    public Path getSourcePath() {
        return mSourcePath;
    }

    public BaseAnimHelper setSourcePath(Path sourcePath) {
        mSourcePath = sourcePath;
        initStartAndEndPosition(mSourcePath);
        return this;
    }

    public Path getAnimPath() {
        return mAnimPath;
    }

    public BaseAnimHelper animPath(Path animPath) {
        mAnimPath = animPath;
        return this;
    }

    public BaseAnimHelper setStepDuaration(long stepDuaration) {
        this.stepDuaration = stepDuaration;
        return this;
    }

    public boolean isInfinite() {
        return mIsInfinite;
    }

    public BaseAnimHelper auto(boolean mAuto) {
        this.mAuto = mAuto;
        return this;
    }

    public boolean isAuto() {
        return mAuto;
    }

    public BaseAnimHelper infinite(boolean infinite) {
        mIsInfinite = infinite;
        return this;
    }

    public BaseAnimHelper interpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
        return this;
    }

    public int getCurrentCount() {
        return mCurrentCount;
    }

    /**
     * 执行动画
     */
    public void startAnim() {
        startAnim(mView, mSourcePath, mAnimPath, mIsInfinite);
    }

    /**
     * 一个SourcePath 内含多段Path，循环取出每段Path，并做一个动画
     * 自定义动画的总时间
     * 和是否循环
     *
     * @param view           需要做动画的自定义View
     * @param sourcePath     源Path
     * @param animPath       自定义View用这个Path做动画
     * @param isInfinite     是否无限循环
     */
    protected void startAnim(View view, Path sourcePath, Path animPath, boolean isInfinite) {
        if (view == null || sourcePath == null || animPath == null) {
            return;
        }
        //pathMeasure.setPath(sourcePath, false);
        //先重置一下需要显示动画的path
        animPath.reset();
        animPath.lineTo(0, 0);
        pathMeasure.setPath(sourcePath, false);
        //这里仅仅是为了 计算一下每一段的duration
        mAllCount = 0;
        mAllLength = 0;
        while (pathMeasure.getLength() != 0) {
            mAllLength += pathMeasure.getLength();
            pathMeasure.nextContour();
            mAllCount++;
        }
        //经过上面这段计算duration代码的折腾 需要重新初始化pathMeasure
        pathMeasure.setPath(sourcePath, false);
        loopAnim(view, sourcePath, animPath, pathMeasure, getDuration(pathMeasure.getLength(),stepDuaration), isInfinite);
    }

    private long getDuration(float length,float step){
        return Math.max((long) (length * step),MIN_DRUATION);
    }

    /**
     * 循环取出每一段path ，并执行动画
     *
     * @param animPath    自定义View用这个Path做动画
     * @param pathMeasure 用于测量的PathMeasure
     */
    protected void loopAnim(final View view, final Path sourcePath, final Path animPath,
                            final PathMeasure pathMeasure, final long duration, final boolean isInfinite) {
        Log.d(TAG,"loopAnim mAllLength = "+mAllLength+" duration = "+duration+" stepDuaration = "+stepDuaration+" mCurrentCount = "+mCurrentCount+" getLength = "+pathMeasure.getLength());
        //动画正在运行的话，先stop吧。万一有人要使用新动画呢，（正经用户不会这么用。）
        stopAnim();
        mAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        mAnimator.setInterpolator(interpolator);
        mAnimator.setDuration(duration);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                onPathAnimCallback(view, sourcePath, animPath, pathMeasure, animation);
                //通知View刷新自己
                view.invalidate();
            }
        });

        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if(nextAnima(sourcePath,animPath,animation,isInfinite)) {
                    loopAnim(view,sourcePath,animPath,pathMeasure,getDuration(pathMeasure.getLength(),stepDuaration),isInfinite);
                    startLoop = true;
                    if(mAnimaLoopCallback != null){
                        mAnimaLoopCallback.startLoop();
                    }
                }
            }
        });
        mAnimator.start();
    }

    private boolean nextAnima(final Path sourcePath, final Path animPath, Animator animation, final boolean isInfinite){
        mCurrentCount++;
        pathMeasure.getSegment(0, pathMeasure.getLength(), animPath, true);
        //绘制完一条Path之后，再绘制下一条
        pathMeasure.nextContour();
        //长度为0 说明一次循环结束
        if (pathMeasure.getLength() == 0) {
            mCurrentCount = 0;
            if (isInfinite) {//如果需要循环动画
                animPath.reset();
                animPath.lineTo(0, 0);
                pathMeasure.setPath(sourcePath, false);
                return true;
            } else {//不需要就停止（因为repeat是无限 需要手动停止）
                animPath.reset();
                animPath.lineTo(0, 0);
                return false;
            }
        }
        return true;
    }

    /**
     * 停止动画
     */
    public void stopAnim() {
        //Log.e("TAG", "stopAnim: ");
        if (null != mAnimator && mAnimator.isRunning()) {
            mAnimator.end();
        }
    }

    /**
     * 用于子类继承搞事情，对animPath进行再次操作的函数
     *
     * @param view
     * @param sourcePath
     * @param animPath
     * @param pathMeasure
     */
    public void onPathAnimCallback(View view, Path sourcePath, Path animPath, PathMeasure pathMeasure, ValueAnimator animation) {
        float value = (float) animation.getAnimatedValue();
        //获取一个段落
        pathMeasure.getSegment(0, pathMeasure.getLength() * value, animPath, true);
    }

    public static interface AnimaLoopCallback{
        void startLoop();
    }
}
