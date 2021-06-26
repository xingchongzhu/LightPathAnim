package com.path.anim;

import android.animation.ValueAnimator;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.view.View;

/**
 * @author : xingchong.zhu
 * description : 光束流动从0-1填满整条path
 * date : 2021/6/26
 * mail : hangchong.zhu@royole.com
 */
public class SequentAnimHelper extends BaseAnimHelper {

    public SequentAnimHelper() {
        super();
    }

    public SequentAnimHelper(View view, Path sourcePath, Path animPath) {
        super(view, sourcePath, animPath);
    }

    public SequentAnimHelper(View view, Path sourcePath, Path animPath, long animTime, boolean isInfinite) {
        super(view, sourcePath, animPath, animTime, isInfinite);
    }

    @Override
    public void onPathAnimCallback(View view, Path sourcePath, Path animPath, PathMeasure pathMeasure, ValueAnimator animation) {
        float value = (float) animation.getAnimatedValue();
        //获取一个段落
        pathMeasure.getSegment(0, pathMeasure.getLength() * value, animPath, true);
    }
}
