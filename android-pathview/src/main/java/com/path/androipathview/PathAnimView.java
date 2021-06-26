package com.path.androipathview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import com.path.anim.BaseAnimator;
import com.path.listener.ResourceLoaderListener;
import com.path.mylibrary.R;
import com.path.utils.BlurMaskUtils;
import com.path.utils.ColorUtils;
import com.path.utils.SvgUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * PathView is a View that animates paths.
 */
@SuppressWarnings("unused")
public class PathAnimView extends ImageView implements SvgUtils.AnimationStepListener, ResourceLoaderListener {
    /**
     * Logging tag.
     */
    public final String LOG_TAG = getClass().getSimpleName();
    /**
     * The paint for the path.
     */
    protected Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * Utils to catch the paths from the svg.
     */
    protected final SvgUtils svgUtils = new SvgUtils(paint);
    /**
     * All the paths provided to the view. Both from Path and Svg.
     */
    protected List<SvgUtils.SvgPath> paths = new ArrayList<>();

    protected  Path allPath = new Path();

    protected Path mAnimPath;//用于绘制动画的Path

    /**
     * This is a lock before the view is redrawn
     * or resided it must be synchronized with this object.
     */
    protected final Object mSvgLock = new Object();
    /**
     * Thread for working with the object above.
     */
    protected Thread mLoader;

    /**
     * The svg image from the raw directory.
     */
    private int svgResourceId;

    /**
     * The progress of the drawing.
     */
    private float progress = 0f;

    /**
     * If the used colors are from the svg or from the set color.
     */
    protected boolean naturalColors;
    /**
     * If the view is filled with its natural colors after path drawing.
     */
    protected boolean fillAfter;
    /**
     * The view will be filled and showed as default without any animation.
     */
    protected boolean fill;
    /**
     * The solid color used for filling svg when fill is true
     */
    protected int fillColor;
    /**
     * The width of the view.
     */
    protected int width;
    /**
     * The height of the view.
     */
    protected int height;
    /**
     * Will be used as a temporary surface in each onDraw call for more control over content are
     * drawing.
     */
    protected Bitmap mTempBitmap;

    protected BlurMaskFilter blurMaskFilter;
    protected Boolean maskFilter = false;

    /**
     * Will be used as a temporary Canvas for mTempBitmap for drawing content on it.
     */
    protected Canvas mTempCanvas;

    //填充线框背景
    protected boolean fillOutline = true;

    //自动开始动画
    protected boolean autoStart = false;

    //循环动画
    protected boolean infinite = false;

    //循环动画
    protected boolean randColor = false;

    //动画总共执行时间
    private long duration = 0;

    private BaseAnimator animatorBuilder;

    protected float painWidth = 3;

    /**
     * Default constructor.
     *
     * @param context The Context of the application.
     */
    public PathAnimView(Context context) {
        this(context, null);
    }

    /**
     * Default constructor.
     *
     * @param context The Context of the application.
     * @param attrs   attributes provided from the resources.
     */
    public PathAnimView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Default constructor.
     *
     * @param context  The Context of the application.
     * @param attrs    attributes provided from the resources.
     * @param defStyle Default style.
     */
    public PathAnimView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initData();
        getFromAttributes(context, attrs);
    }

    private void initData() {
        //动画路径只要初始化即可
        mAnimPath = new Path();
        paint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStyle(Paint.Style.STROKE);
    }

    /**
     * Get all the fields from the attributes .
     *
     * @param context The Context of the application.
     * @param attrs   attributes provided from the resources.
     */
    private void getFromAttributes(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PathAnimView);
        try {
            if (a != null) {
                paint.setColor(a.getColor(R.styleable.PathAnimView_pathColor, 0xff00ff00));
                outlinePaint.setColor(a.getColor(R.styleable.PathAnimView_outlineColor, 0x3898989));
                painWidth = a.getDimensionPixelSize(R.styleable.PathAnimView_pathWidth, 8);
                outlinePaint.setStrokeWidth(painWidth);
                paint.setStrokeWidth(painWidth);
                svgResourceId = a.getResourceId(R.styleable.PathAnimView_svg, 0);
                naturalColors = a.getBoolean(R.styleable.PathAnimView_naturalColors, false);
                fill = a.getBoolean(R.styleable.PathAnimView_fill, false);
                fillOutline = a.getBoolean(R.styleable.PathAnimView_fillOutline, false);
                autoStart = a.getBoolean(R.styleable.PathAnimView_autoStart, false);
                infinite = a.getBoolean(R.styleable.PathAnimView_infinite, false);
                duration = (long) a.getFloat(R.styleable.PathAnimView_duration, 1000f);
                randColor = a.getBoolean(R.styleable.PathAnimView_randColor, false);
                fillColor = a.getColor(R.styleable.PathAnimView_fillColor, Color.argb(0, 0, 0, 0));
                maskFilter = a.getBoolean(R.styleable.PathAnimView_maskFilter, false);
                if(maskFilter) {
                    blurMaskFilter = new BlurMaskFilter(a.getFloat(R.styleable.PathAnimView_maskFilterRadius, 1f),
                            BlurMaskUtils.convertBlurMaskFilter(a.getInteger(R.styleable.PathAnimView_maskFilterType, 0)));
                }
                if(randColor){
                    paint.setColor(ColorUtils.getRandomColor());
                }
                paint.setMaskFilter(blurMaskFilter);
            }

        } finally {
            if (a != null) {
                a.recycle();
            }
            //to draw the svg in first show , if we set fill to true
            invalidate();
        }
    }

    /**
     * Set paths to be drawn and animated.
     *
     * @param paths - Paths that can be drawn.
     */
    public void setPaths(final List<Path> paths) {
        Log.d(LOG_TAG, "setPaths paths = " + paths);
        for (Path path : paths) {
            this.paths.add(new SvgUtils.SvgPath(path, paint));
        }
        synchronized (mSvgLock) {
            updatePathsPhaseLocked();
        }
    }

    /**
     * Set path to be drawn and animated.
     *
     * @param path - Paths that can be drawn.
     */
    public void setPath(final Path path) {
        Log.d(LOG_TAG, "setPath paths = " + paths);
        paths.add(new SvgUtils.SvgPath(path, paint));
        synchronized (mSvgLock) {
            updatePathsPhaseLocked();
        }
    }

    public List<SvgUtils.SvgPath> getPaths() {
        return paths;
    }

    public void setAnimatorBuilder(BaseAnimator animatorBuilder) {
        this.animatorBuilder = animatorBuilder;
        if(animatorBuilder != null) {
            animatorBuilder.
                    duration((int) duration).
                    setInfinite(infinite).
                    setAuto(autoStart).
                    interpolator(new AccelerateDecelerateInterpolator());
        }
    }

    /**
     * Animate this property. It is the percentage of the path that is drawn.
     * It must be [0,1].
     *
     * @param percentage float the percentage of the path.
     */
    public void setPercentage(float percentage) {
        if (percentage < 0.0f || percentage > 1.0f) {
            throw new IllegalArgumentException("setPercentage not between 0.0f and 1.0f");
        }
        progress = percentage;
        synchronized (mSvgLock) {
            updatePathsPhaseLocked();
        }
        invalidate();
    }

    /**
     * This refreshes the paths before draw and resize.
     */
    private void updatePathsPhaseLocked() {
        final int count = paths.size();
        //Log.d(LOG_TAG,"updatePathsPhaseLocked progress = "+progress);
        for (int i = 0; i < count; i++) {
            SvgUtils.SvgPath svgPath = paths.get(i);
            svgPath.path.reset();
            svgPath.path.lineTo(0.0f, 0.0f);
            svgPath.measure.getSegment(0.0f, svgPath.measure.getLength() * progress, svgPath.path, true);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawSvg(canvas);
        drawAnimaPath(canvas);
    }

    protected void drawAnimaPath(Canvas canvas) {
        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        if(animatorBuilder != null) {
            final int count = paths.size();
            for (int i = 0; i < count; i++) {
                final SvgUtils.SvgPath svgPath = paths.get(i);
                final Path path = svgPath.path;
                final Paint paint1 = naturalColors ? svgPath.paint : paint;
                canvas.drawPath(path, paint1);
            }
        }
        canvas.restore();
    }

    private void drawSvg(Canvas canvas) {
        if (mTempBitmap == null || (mTempBitmap.getWidth() != canvas.getWidth() || mTempBitmap.getHeight() != canvas.getHeight())) {
            mTempBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
            mTempCanvas = new Canvas(mTempBitmap);
        }

        mTempBitmap.eraseColor(0);
        synchronized (mSvgLock) {
            mTempCanvas.save();
            mTempCanvas.translate(getPaddingLeft(), getPaddingTop());
            fill(mTempCanvas);
            if (fillOutline) {
                fillOutline(canvas);
            }

            fillAfter(mTempCanvas);

            mTempCanvas.restore();

            applySolidColor(mTempBitmap);

            canvas.drawBitmap(mTempBitmap, 0, 0, null);
        }
    }

    private void fillOutline(Canvas canvas) {
        final int count = paths.size();
        for (int i = 0; i < count; i++) {
            SvgUtils.SvgPath svgPath = paths.get(i);
            final Path path = svgPath.sourcePath;
            final Paint paint1 = outlinePaint;
            canvas.drawPath(path, paint1);
        }
    }

    @Override
    public void onAnimationStep() {
        invalidate();
    }

    /**
     * If there is svg , the user called setFillAfter(true) and the progress is finished.
     *
     * @param canvas Draw to this canvas.
     */
    protected void fillAfter(final Canvas canvas) {
        if (svgResourceId != 0 && fillAfter && Math.abs(progress - 1f) < 0.00000001) {
            svgUtils.drawSvgAfter(canvas, width, height);
        }
    }

    /**
     * If there is svg , the user called setFill(true).
     *
     * @param canvas Draw to this canvas.
     */
    protected void fill(final Canvas canvas) {
        if (svgResourceId != 0 && fill) {
            svgUtils.drawSvgAfter(canvas, width, height);
        }
    }

    /**
     * If fillColor had value before then we replace untransparent pixels of bitmap by solid color
     *
     * @param bitmap Draw to this canvas.
     */
    protected void applySolidColor(final Bitmap bitmap) {
        if (fill && fillColor != Color.argb(0, 0, 0, 0))
            if (bitmap != null) {
                for (int x = 0; x < bitmap.getWidth(); x++) {
                    for (int y = 0; y < bitmap.getHeight(); y++) {
                        int argb = bitmap.getPixel(x, y);
                        int alpha = Color.alpha(argb);
                        if (alpha != 0) {
                            int red = Color.red(fillColor);
                            int green = Color.green(fillColor);
                            int blue = Color.blue(fillColor);
                            argb = Color.argb(alpha, red, green, blue);
                            bitmap.setPixel(x, y, argb);
                        }
                    }
                }
            }
    }

    @Override
    protected void onSizeChanged(final int w, final int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mLoader != null) {
            try {
                mLoader.join();
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Unexpected error", e);
            }
        }
        if (svgResourceId != 0) {
            mLoader = new Thread(new Runnable() {
                @Override
                public void run() {
                    onLoadStart();
                    svgUtils.load(getContext(), svgResourceId);
                    synchronized (mSvgLock) {
                        width = w - getPaddingLeft() - getPaddingRight();
                        height = h - getPaddingTop() - getPaddingBottom();
                        paths = svgUtils.getPathsForViewport(width, height);
                    }
                    mergePathHelper(paths);
                    updatePathsPhaseLocked();
                    randCreateColor(paths);
                    setPaintMaskFilter(paths);
                    PathAnimView.this.postInvalidate();
                    Log.d(LOG_TAG, "onSizeChanged paths.size = " + paths.size());
                    onLoadComplete();
                }
            }, "SVG Loader");
            mLoader.start();
        }
    }

    /**
     * des 设置画笔光晕
     * @param paths
     */
    private void setPaintMaskFilter(List<SvgUtils.SvgPath> paths) {
        for (SvgUtils.SvgPath svgPath : paths) {
            svgPath.paint.setMaskFilter(blurMaskFilter);
        }
    }

    /**
     * des 随机创建画笔颜色
     * @param paths
     */
    private void randCreateColor(List<SvgUtils.SvgPath> paths) {
        if(randColor) {
            for (SvgUtils.SvgPath svgPath : paths) {
                svgPath.setPatinColor(ColorUtils.getRandomColor());
            }
        }
    }

    /**
     * des 合并路径成一个作为动画
     * @param paths
     */
    public void mergePathHelper(List<SvgUtils.SvgPath> paths){
        allPath.reset();
        allPath.lineTo(0,0);

        for(SvgUtils.SvgPath svgPath:paths){
            allPath.addPath(svgPath.path);
        }

        if(animatorBuilder != null){
            animatorBuilder.setAnimatorPath(this);
        }
        Log.d(LOG_TAG,"resetPathHelper size = "+paths.size());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (svgResourceId != 0) {
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            setMeasuredDimension(widthSize, heightSize);
            return;
        }

        int desiredWidth = 0;
        int desiredHeight = 0;
        final float strokeWidth = paint.getStrokeWidth() / 2;
        for (SvgUtils.SvgPath path : paths) {
            desiredWidth += path.bounds.left + path.bounds.width() + strokeWidth;
            desiredHeight += path.bounds.top + path.bounds.height() + strokeWidth;
        }
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(widthMeasureSpec);

        int measuredWidth, measuredHeight;

        if (widthMode == MeasureSpec.AT_MOST) {
            measuredWidth = desiredWidth;
        } else {
            measuredWidth = widthSize;
        }

        if (heightMode == MeasureSpec.AT_MOST) {
            measuredHeight = desiredHeight;
        } else {
            measuredHeight = heightSize;
        }
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    /**
     * If the real svg need to be drawn after the path animation.
     *
     * @param fillAfter - boolean if the view needs to be filled after path animation.
     */
    public void setFillAfter(final boolean fillAfter) {
        this.fillAfter = fillAfter;
    }

    /**
     * If the real svg need to be drawn without the path animation.
     *
     * @param fill - boolean if the view needs to be filled after path animation.
     */
    public void setFill(final boolean fill) {
        this.fill = fill;
    }

    /**
     * The color for drawing svg in that color if the color be not transparent
     *
     * @param color - the color for filling in that
     */
    public void setFillColor(final int color) {
        this.fillColor = color;
    }

    /**
     * If you want to use the colors from the svg.
     */
    public void setNaturalColors(boolean naturalColors) {
        this.naturalColors = naturalColors;
    }

    /**
     * Get the path color.
     *
     * @return The color of the paint.
     */
    public int getPathColor() {
        return paint.getColor();
    }

    /**
     * Set the path color.
     *
     * @param color -The color to set to the paint.
     */
    public void setPathColor(final int color) {
        paint.setColor(color);
    }

    public void setOutlinePaint(final int color) {
        outlinePaint.setColor(color);
    }

    /**
     * Get the path width.
     *
     * @return The width of the paint.
     */
    public float getPathWidth() {
        return paint.getStrokeWidth();
    }

    /**
     * Set the path width.
     *
     * @param width - The width of the path.
     */
    public void setPathWidth(final float width) {
        paint.setStrokeWidth(width);
    }

    /**
     * Get the svg resource id.
     *
     * @return The svg raw resource id.
     */
    public int getSvgResource() {
        return svgResourceId;
    }

    /**
     * Set the svg resource id.
     *
     * @param svgResource - The resource id of the raw svg.
     */
    public void setSvgResource(int svgResource) {
        svgResourceId = svgResource;
    }

    @Override
    public void onLoadStart() {
        Log.d(LOG_TAG,"onLoadStart");
    }

    @Override
    public void onLoadComplete() {
        Log.d(LOG_TAG,"onLoadComplete");
        if(getHandler() != null) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if(animatorBuilder != null){
                        animatorBuilder.start();
                    }
                }
            });
        }
    }
}
