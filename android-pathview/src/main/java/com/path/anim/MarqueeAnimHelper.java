package com.path.anim;

import android.animation.ValueAnimator;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.Log;
import android.view.View;

/**
 * @author : xingchong.zhu
 * description : 跑马灯效果
 * date : 2021/6/26
 * mail : hangchong.zhu@royole.com
 */
public class MarqueeAnimHelper extends BaseAnimHelper {
    private final String TAG = getClass().getSimpleName();
    private final static float PROPORTION = 0.02f;
    private Path path = new Path();
    private float end = 0;
    private float begin = 0;
    public MarqueeAnimHelper() {
        super();
    }

    public MarqueeAnimHelper(View view, Path sourcePath, Path animPath) {
        super(view, sourcePath, animPath);
    }

    public MarqueeAnimHelper(View view, Path sourcePath, Path animPath, long animTime, boolean isInfinite) {
        super(view, sourcePath, animPath, animTime, isInfinite);
    }

    @Override
    public void onPathAnimCallback(View view, Path sourcePath, Path animPath, PathMeasure pathMeasure, ValueAnimator animation) {
        float value = (float) animation.getAnimatedValue();
        final float step = pathMeasure.getLength() * PROPORTION;
        //获取一个段落
        final float length = pathMeasure.getLength() * value;
        caculateAnima(length, animPath,step);
    }

    private void caculateLoopAnima(final float startPosition,final float endPosition, Path animPath,final float step) {
        int count = (int) ((endPosition - startPosition) / step)+1;
        for(int i = 0 ; i < count ; i++){
            if(i % 2 != 0){
                resetPath(path);
                begin = startPosition + i * step;
                end = begin + step;
                pathMeasure.getSegment(begin, end, path, true);
                animPath.addPath(path);
            }
        }
    }

    private void caculateAnima(float length, Path animPath,final float step) {
        resetPath(animPath);
        //开始循环
        if(startLoop){
            caculateLoopAnima(length,pathMeasure.getLength(), animPath,step);
        }
        int count = (int) (length / step)+1;
        //Log.d(TAG,"caculateAnima length = "+length+" count = "+count+" step = "+step+" float "+(length / step));
        for(int i = 0 ; i < count ; i++){
            if(i % 2 == 0){
                resetPath(path);
                end = length - i * step;
                begin = end - step;
                pathMeasure.getSegment(begin, end, path, true);
                animPath.addPath(path);
            }
        }
    }

    private void resetPath(Path path){
        path.reset();
        path.lineTo(0, 0);
    }
}

