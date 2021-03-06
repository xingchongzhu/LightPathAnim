package com.path.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.Log;

import com.caverock.androidsvg.PreserveAspectRatio;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.util.ArrayList;
import java.util.List;

import static com.path.utils.SvgUtils.SvgPath.MAX_CLIP;

/**
 * Util class to init and get paths from svg.
 */
public class SvgUtils {
    /**
     * It is for logging purposes.
     */
    private static final String LOG_TAG = "SVGUtils";
    /**
     * All the paths with their attributes from the svg.
     */
    private final List<SvgPath> mPaths = new ArrayList<>();
    /**
     * The paint provided from the view.
     */
    private final Paint mSourcePaint;
    /**
     * The init svg.
     */
    private SVG mSvg;

    /**
     * Init the SVGUtils with a paint for coloring.
     *
     * @param sourcePaint - the paint for the coloring.
     */
    public SvgUtils(final Paint sourcePaint) {
        mSourcePaint = sourcePaint;
    }

    /**
     * Loading the svg from the resources.
     *
     * @param context     Context object to get the resources.
     * @param svgResource int resource id of the svg.
     */
    public void load(Context context, int svgResource) {
        if (mSvg != null) 
            return;
        try {
            mSvg = SVG.getFromResource(context, svgResource);
            mSvg.setDocumentPreserveAspectRatio(PreserveAspectRatio.UNSCALED);
        } catch (SVGParseException e) {
            Log.e(LOG_TAG, "Could not load specified SVG resource", e);
        }
    }

    /**
     * Draw the svg to the canvas.
     *
     * @param canvas The canvas to be drawn.
     * @param width  The width of the canvas.
     * @param height The height of the canvas.
     */
    public void drawSvgAfter(final Canvas canvas, final int width, final int height) {
        final float strokeWidth = mSourcePaint.getStrokeWidth();
        rescaleCanvas(width, height, strokeWidth, canvas);
    }

    /**
     * Render the svg to canvas and catch all the paths while rendering.
     *
     * @param width  - the width to scale down the view to,
     * @param height - the height to scale down the view to,
     * @return All the paths from the svg.
     */
    public List<SvgPath> getPathsForViewport(final int width, final int height) {
        final float strokeWidth = mSourcePaint.getStrokeWidth();
        Canvas canvas = new Canvas() {
            private final Matrix mMatrix = new Matrix();

            @Override
            public int getWidth() {
                return width;
            }

            @Override
            public int getHeight() {
                return height;
            }

            @Override
            public void drawPath(Path path, Paint paint) {
                Path dst = new Path();

                //noinspection deprecation
                getMatrix(mMatrix);
                path.transform(mMatrix, dst);
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(strokeWidth);
                mPaths.add(new SvgPath(dst, paint));

                //??????????????????
                resetPath(mPaths);
            }

            private void resetPath(List<SvgPath> paths) {
                List<SvgUtils.SvgPath> tmpList = new ArrayList<>();
                PathMeasure pathMeasure = new PathMeasure();
                for(SvgUtils.SvgPath svgPath : paths){
                    pathMeasure.setPath(svgPath.sourcePath,false);
                    while (pathMeasure.getLength() != 0) {
                        Path path = new Path();
                        pathMeasure.getSegment(0,pathMeasure.getLength(),path,true);
                        tmpList.add(new SvgUtils.SvgPath(path,svgPath.paint));
                        pathMeasure.nextContour();
                    }
                }
                paths.clear();
                paths.addAll(tmpList);
            }
        };

        rescaleCanvas(width, height, strokeWidth, canvas);

        return mPaths;
    }

    /**
     * Rescale the canvas with specific width and height.
     *
     * @param width       The width of the canvas.
     * @param height      The height of the canvas.
     * @param strokeWidth Width of the path to add to scaling.
     * @param canvas      The canvas to be drawn.
     */
    private void rescaleCanvas(int width, int height, float strokeWidth, Canvas canvas) {
        if (mSvg == null) 
            return;
        final RectF viewBox = mSvg.getDocumentViewBox();

        final float scale = Math.min(width
                        / (viewBox.width() + strokeWidth),
                height / (viewBox.height() + strokeWidth));

        canvas.translate((width - viewBox.width() * scale) / 2.0f,
                (height - viewBox.height() * scale) / 2.0f);
        canvas.scale(scale, scale);

        mSvg.renderToCanvas(canvas);
    }

    public static Rect getPathRect(Path path) {
        Region region = new Region();
        region.setPath(path, MAX_CLIP);
       return region.getBounds();
    }

    /**
     * Path with bounds for scalling , length and paint.
     */
    public static class SvgPath {

        /**
         * Region of the path.
         */
        public static final Region REGION = new Region();
        /**
         * This is done for clipping the bounds of the path.
         */
        public static final Region MAX_CLIP =
                new Region(Integer.MIN_VALUE, Integer.MIN_VALUE,
                        Integer.MAX_VALUE, Integer.MAX_VALUE);

        /**
         * The path itself.
         */
        public final Path sourcePath = new Path();
        /**
         * The path itself.
         */
        public  Path path;

        /**
         * The paint to be drawn later.
         */
        public final Paint paint;
        /**
         * The length of the path.
         */
        public float length;
        /**
         * Listener to notify that an animation step has happened.
         */
        AnimationStepListener animationStepListener;
        /**
         * The bounds of the path.
         */
        public final Rect bounds;
        /**
         * The measure of the path, we can use it later to get segment of it.
         */
        public final PathMeasure measure;
        private final int mGradientColor[] = new int[]{Color.TRANSPARENT,Color.RED};
        private final float mColorPropation[] = new float[]{0,1f};
        private LinearGradient linearGradient;
        float startPos[] = new float[2];
        float endPos[] = new float[2];
        /**
         * Constructor to add the path and the paint.
         *
         * @param path  The path that comes from the rendered svg.
         * @param paint The result paint.
         */
        public SvgPath(Path path, Paint paint) {
            this.sourcePath.set(path);
            this.path = path;
            this.paint = paint;

            measure = new PathMeasure(path, false);
            this.length = measure.getLength();
            REGION.setPath(path, MAX_CLIP);
            bounds = REGION.getBounds();

            measure.getPosTan(0,startPos,null);
            measure.getPosTan(0,endPos,null);
        }

        public void setPatinColor(int color){
            mGradientColor[1] = color;
            paint.setColor(color);
            /*Log.d("zxc","setPatinColor  startPos[0] = "+startPos[0]+" startPos[1] = "+startPos[1]+" endPos[0] = "+endPos[0]+" endPos[1] = "+endPos[1]+" bounds " +bounds);
            linearGradient = new LinearGradient(startPos[0],startPos[1] ,
                    endPos[0],endPos[1],
                    mGradientColor,
                    mColorPropation,
                    Shader.TileMode.CLAMP);
            paint.setShader(linearGradient);*/
        }

        /**
         * Sets the animation step listener.
         *
         * @param animationStepListener AnimationStepListener.
         */
        public void setAnimationStepListener(AnimationStepListener animationStepListener) {
            this.animationStepListener = animationStepListener;
        }

        /**
         * Sets the length of the path.
         *
         * @param length The length to be set.
         */
        public void setLength(float length) {
            path.reset();
            measure.getSegment(0.0f, length, path, true);
            path.rLineTo(0.0f, 0.0f);

            if (animationStepListener != null) {
                animationStepListener.onAnimationStep();
            }
        }

        /**
         * @return The length of the path.
         */
        public float getLength() {
            return length;
        }
    }

    public interface AnimationStepListener {

        /**
         * Called when an animation step happens.
         */
        void onAnimationStep();
    }
}
