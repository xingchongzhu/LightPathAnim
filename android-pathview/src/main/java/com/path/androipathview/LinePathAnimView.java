package com.path.androipathview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.path.anim.BaseAnimHelper;
import com.path.mylibrary.R;
import com.path.utils.SvgUtils;

import java.util.List;

/**
 * @author : xingchong.zhu
 * description :
 * date : 2021/6/24
 * mail : hangchong.zhu@royole.com
 */
public class LinePathAnimView extends PathAnimView {

    protected BaseAnimHelper mPathAnimHelper;//Path动画工具类
    //单点时间
    protected long stepDuaration = 0;
    private Paint linePath = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean shader = false;
    private float shaderSize = 0;

    public LinePathAnimView(Context context) {
        this(context,null);
    }

    public LinePathAnimView(Context context, AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public LinePathAnimView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getFromAttributes(context,attrs);
        init();
    }

    private void init() {
        linePath.setStrokeWidth(painWidth);
    }

    private void getFromAttributes(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LineAnimView);
        try {
            if (a != null) {
                stepDuaration = (long) a.getFloat(R.styleable.LineAnimView_stepDuaration, 1f);
                linePath.setColor(a.getColor(R.styleable.LineAnimView_linePathColor, 0x3898989));
                shaderSize = a.getFloat(R.styleable.LineAnimView_lineShaderSize, 1f);
                shader = a.getBoolean(R.styleable.LineAnimView_lineShader, false);
            }
        } finally {
            if (a != null) {
                a.recycle();
            }
            //to draw the svg in first show , if we set fill to true
            invalidate();
        }
    }

    public PathAnimView setPathAnimHelper(BaseAnimHelper pathAnimHelper) {
        mPathAnimHelper = pathAnimHelper;
        initPatherHelper();
        return this;
    }

    public BaseAnimHelper getPathAnimHelper() {
        return mPathAnimHelper;
    }

    private void initPatherHelper() {
        if(mPathAnimHelper != null) {
            mPathAnimHelper.setView(this).
                    animPath(mAnimPath).
                    infinite(infinite).
                    auto(autoStart).
                    interpolator(new AccelerateDecelerateInterpolator()).
                    setStepDuaration(stepDuaration);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mPathAnimHelper != null) {
            Paint paint1 = paint;
            if(naturalColors) {
                if (paths.size() > mPathAnimHelper.getCurrentCount()) {
                    paint1 = paths.get(mPathAnimHelper.getCurrentCount()).paint;
                }
            }

            canvas.drawPath(mAnimPath, paint1);
        }
    }

    @Override
    public void resetPathHelper(List<SvgUtils.SvgPath> paths) {
        super.resetPathHelper(paths);
        if(mPathAnimHelper == null) {
            mPathAnimHelper = new BaseAnimHelper(this, allPath, mAnimPath);
        }else{
            mPathAnimHelper.setSourcePath(allPath).initStartAndEndPosition(allPath);
        }
    }

    @Override
    public void onLoadComplete() {
        super.onLoadComplete();
        Log.d(LOG_TAG,"onLoadComplete");
        if(getHandler() != null) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (mPathAnimHelper != null && mPathAnimHelper.isAuto()) {
                        mPathAnimHelper.startAnim();
                    }
                }
            });
        }
    }
}
