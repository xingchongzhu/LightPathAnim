package com.path.androipathview;


import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


import com.path.bean.StarBean;
import com.path.mylibrary.R;
import com.path.utils.ColorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class StarView extends View {
    private static final String TAG = "StarView";

    private Paint paint;
    private Paint paintHead;
    private Paint paintHeadLight;
    private Paint paintHeadLight2;

    private int bw = 0;
    private int bh = 0;

    private double sweep = 340;//角度
    private int count = 100;
    private double pointCount = 100;
    private long duaration = 0;
    private List<StarBean> list = new ArrayList<>();
    private boolean isStart = false;
    private boolean init = false;

    private Timer playTimer;
    private ValueAnimator mValueAnimator;

    public StarView(Context context) {
        this(context, null);
    }

    public StarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        getFromAttributes(context,attrs);
    }

    private void getFromAttributes(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StarAnimView);
        try {
            if (a != null) {
                sweep = a.getInteger(R.styleable.StarAnimView_starsweep, 340);
                pointCount = count = a.getInteger(R.styleable.StarAnimView_starcount, 100);
                duaration = (long) a.getFloat(R.styleable.StarAnimView_starduration, 300);
            }
        } finally {
            if (a != null) {
                a.recycle();
            }
            //to draw the svg in first show , if we set fill to true
            invalidate();
        }
        Log.d(TAG,"sweep = "+sweep+" pointCount = "+pointCount+" duaration = "+duaration);
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        LinearGradient linearGradient = new LinearGradient(
                0f, 0f, 0f, 1000,
                new int[]{Color.TRANSPARENT, Color.RED},
                new float[]{0f, 1f},
                Shader.TileMode.CLAMP
        );
        paint.setShader(linearGradient);

        paintHead = new Paint();
        paintHead.setStyle(Paint.Style.FILL);
        paintHead.setAntiAlias(true);
        paintHead.setColor(Color.WHITE);

        paintHeadLight = new Paint();
        paintHeadLight.setStyle(Paint.Style.FILL);
        paintHeadLight.setAntiAlias(true);
        paintHeadLight.setColor(Color.argb(100, 255, 255, 255));

        paintHeadLight2 = new Paint();
        paintHeadLight2.setStyle(Paint.Style.FILL);
        paintHeadLight2.setAntiAlias(true);
        paintHeadLight2.setColor(Color.argb(30, 255, 255, 255));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!init) {
            bw = getMeasuredWidth();
            bh = getMeasuredHeight();
            initStar();
            initPoint();

            start();

            init = true;
        }
    }

    private void initStar() {
        for (int i = 0; i < count; i++) {
            int color = ColorUtils.getRandomColor();
            StarBean bean = getStarBean();
            bean.setColor(color);
            list.add(bean);
        }
    }

    public StarBean getStarBean() {
        StarBean bean = new StarBean();
//        setBeanXY(bean);
        bean.setX((float) (Math.random() * bw));
        bean.setY((float) (Math.random() * bh));
        bean.setR((float) (Math.random() * 3 + 0.3f));
        return bean;
    }

    public void initPoint() {
        for (int i = 0; i < pointCount; i++) {
            StarBean bean = getStarBean();
            bean.setPoint(true);
            bean.setColor(Color.WHITE);
            bean.setR((float) (Math.random() * 1.5 + 0.1f));
            list.add(bean);
        }
    }

    private void setBeanXY(StarBean bean) {
        //int color = ColorUtils.getRandomColor();
        //bean.setColor(color);
        if (Math.random() >= 0.5) {
            bean.setX(0);
            bean.setY((float) (Math.random() * bh));
        } else {
            bean.setX((float) (Math.random() * bw));
            bean.setY(0);
        }
    }

    public void addStar(int count) {
        for (int i = 0; i < count; i++) {
            StarBean bean = getStarBean();
            list.add(bean);
        }
    }

    public void addStar(int count, int color) {
        for (int i = 0; i < count; i++) {
            StarBean bean = getStarBean();
            bean.setColor(color);
            list.add(bean);
        }
    }

    @SuppressLint("WrongConstant")
    public void start() {
        if(mValueAnimator != null){
            mValueAnimator.cancel();
        }

        mValueAnimator = ValueAnimator.ofFloat(0f, 1f);
        mValueAnimator.setDuration(duaration);
        mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);//无限循环
        mValueAnimator.setRepeatMode(ValueAnimator.INFINITE);//
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                for (int i = 0; i < list.size(); i++) {
                    list.get(i).setX(list.get(i).getX() + list.get(i).getvX());
                    list.get(i).setY(list.get(i).getY() + list.get(i).getvY());

                    if (list.get(i).getX() < 0
                            || list.get(i).getX() > (bw + Math.sin(sweep) * list.get(i).getL())
                            || list.get(i).getY() > (bh + Math.cos(sweep) * list.get(i).getL())
                            || list.get(i).getY() < 0) {
                        setBeanXY(list.get(i));
                    }
                }
                invalidate();
            }
        });
        mValueAnimator.start();
    }

    public void stop() {
        if(mValueAnimator != null){
            mValueAnimator.cancel();
        }
    }

    private Path getPath(float x, float y, double sweep, float r, StarBean bean) {
        Path path = new Path();

        float l = bean.getL();
        float startX = (float) (x + r * Math.cos(sweep));
        float startY = (float) (y - r * Math.sin(sweep));
        float twoX = (float) (x - r * Math.cos(sweep));
        float twoY = (float) (y + r * Math.sin(sweep));
        float threeX = (float) (x - l * Math.sin(sweep));
        float threeY = (float) (y - l * Math.cos(sweep));

        path.moveTo(startX, startY);
        path.lineTo(twoX, twoY);
        path.lineTo(threeX, threeY);
        path.lineTo(startX, startY);
        path.close();

        LinearGradient linearGradient = new LinearGradient(
                threeX, threeY, x, y,
                new int[]{Color.TRANSPARENT, bean.getColor()},
                new float[]{0f, 1f},
                Shader.TileMode.CLAMP
        );
        paint.setShader(linearGradient);
        return path;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);

        for (int i = 0; i < list.size(); i++) {
            StarBean bean = list.get(i);
            if (!bean.isPoint()) {
                Path path = getPath(bean.getX(), bean.getY(), sweep, bean.getR(), bean);
                canvas.drawPath(path, paint);
                canvas.drawCircle(bean.getX(), bean.getY(), bean.getHeadRLight2(), paintHeadLight2);
                canvas.drawCircle(bean.getX(), bean.getY(), bean.getHeadRLight(), paintHeadLight);
                canvas.drawCircle(bean.getX(), bean.getY(), bean.getHeadR(), paintHead);
            } else {
                canvas.drawCircle(bean.getX(), bean.getY(), bean.getR(), paintHead);
            }
        }

    }
}