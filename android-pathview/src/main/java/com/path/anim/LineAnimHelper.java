package com.path.anim;

import android.animation.ValueAnimator;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.view.View;

/**
 * @author : xingchong.zhu
 * description : 光束流动效果取当前长度一半
 * date : 2021/6/26
 * mail : hangchong.zhu@royole.com
 */
public class LineAnimHelper extends BaseAnimHelper {
    private final static float PROPORTION = 0.5f;

    public LineAnimHelper() {
        super();
    }

    public LineAnimHelper(View view, Path sourcePath, Path animPath) {
        super(view, sourcePath, animPath);
    }

    public LineAnimHelper(View view, Path sourcePath, Path animPath, long animTime, boolean isInfinite) {
        super(view, sourcePath, animPath, animTime, isInfinite);
    }

    @Override
    public void onPathAnimCallback(View view, Path sourcePath, Path animPath, PathMeasure pathMeasure, ValueAnimator animation) {
        float value = (float) animation.getAnimatedValue();
        //获取一个段落
        float end = pathMeasure.getLength() * value;
        float begin = (float) (end - ((PROPORTION - Math.abs(value - PROPORTION)) * pathMeasure.getLength()));
        animPath.reset();
        animPath.lineTo(0, 0);
        pathMeasure.getSegment(begin, end, animPath, true);
    }
}
