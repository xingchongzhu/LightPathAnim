package com.path.anim;

/**
 * @author : xingchong.zhu
 * description :
 * date : 2021/6/22
 * mail : hangchong.zhu@royole.com
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import com.path.androipathview.PathAnimView;
import com.path.utils.SvgUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Object for building the sequential animation of the paths of this view.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AnimatorSetBuilder extends BaseAnimator{

    /**
     * List of ObjectAnimator that constructs the animations of each path.
     */
    private final List<Animator> animators = new ArrayList<>();
    /**
     * The animator that can animate paths sequentially
     */
    private AnimatorSet animatorSet = new AnimatorSet();

    /**
     * The list of paths to be animated.
     */
    private List<SvgUtils.SvgPath> paths;

    /**
     * Default constructor.
     *
     * @param pathView The view that must be animated.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public AnimatorSetBuilder(final PathAnimView pathView) {
        setAnimatorPath(pathView);
    }

    @Override
    public void setAnimatorPath(PathAnimView pathView) {
        paths = pathView.getPaths();
        Log.d(LOG_TAG,"AnimatorSetBuilder paths "+paths.size());
        for (SvgUtils.SvgPath path : paths) {
            path.setAnimationStepListener(pathView);
            ObjectAnimator animation = ObjectAnimator.ofFloat(path, "length", 0.0f, path.getLength());
            animators.add(animation);
        }
        animatorSet.playSequentially(animators);
    }

    @Override
    public BaseAnimator setInfinite(final boolean isInfinite) {
        super.setInfinite(isInfinite);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animatorSet.start();
            }
        });
        return this;
    }

    /**
     * Starts the animation.
     */
    public void start() {
        Log.d(LOG_TAG, "start");
        resetAllPaths();
        animatorSet.cancel();
        animatorSet.setDuration(duration);
        animatorSet.setInterpolator(interpolator);
        animatorSet.setStartDelay(delay);
        animatorSet.start();
    }

    /**
     * Sets the length of all the paths to 0.
     */
    private void resetAllPaths() {
        for (SvgUtils.SvgPath path : paths) {
            path.setLength(0);
        }
    }
}
