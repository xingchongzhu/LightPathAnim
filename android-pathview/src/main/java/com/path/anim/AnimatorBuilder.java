package com.path.anim;

/**
 * @author : xingchong.zhu
 * description :
 * date : 2021/6/22
 * mail : hangchong.zhu@royole.com
 */
import android.view.animation.Animation;

import com.path.androipathview.PathAnimView;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Object for building the animation of the path of this view.
 */
public class AnimatorBuilder extends BaseAnimator{

    /**
     * ObjectAnimator that constructs the animation.
     */
    private final ObjectAnimator anim;

    /**
     * Default constructor.
     *
     * @param pathView The view that must be animated.
     */
    public AnimatorBuilder(final PathAnimView pathView) {
        anim = ObjectAnimator.ofFloat(pathView, "percentage", 0.0f, 1.0f);
    }

    @Override
    public BaseAnimator setInfinite(boolean isInfinite) {
        super.setInfinite(isInfinite);
        if(isInfinite){
            anim.addListener(new AnimatorListenerAdapter(){
                @Override
                public void onAnimationRepeat(Animator animation) {
                    super.onAnimationRepeat(animation);
                }
            });
            anim.setRepeatCount(Animation.INFINITE);
            anim.setRepeatMode(Animation.INFINITE);
        }
        return this;
    }

    /**
     * Starts the animation.
     */
    public void start() {
        anim.setDuration(duration);
        anim.setInterpolator(interpolator);
        anim.setStartDelay(delay);
        anim.start();
    }
}