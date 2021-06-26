package com.path.utils;

import android.graphics.BlurMaskFilter;

/**
 * @author : xingchong.zhu
 * description :
 * date : 2021/6/26
 * mail : hangchong.zhu@royole.com
 */
public class BlurMaskUtils {
    public static BlurMaskFilter.Blur convertBlurMaskFilter(int mask){
        BlurMaskFilter.Blur blur;
        switch (mask){
            case 0:
                blur = BlurMaskFilter.Blur.NORMAL;
                break;
            case 1:
                blur = BlurMaskFilter.Blur.SOLID;
                break;
            case 2:
                blur = BlurMaskFilter.Blur.OUTER;
                break;
            case 3:
                blur = BlurMaskFilter.Blur.INNER;
                break;
            default:
                blur = BlurMaskFilter.Blur.NORMAL;
                break;
        }
        return blur;
    }
}
