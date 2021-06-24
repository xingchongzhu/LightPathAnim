package com.path.anim;

import android.view.animation.Interpolator;

import com.path.androipathview.PathAnimView;

/**
 * @author : xingchong.zhu
 * description :
 * date : 2021/6/22
 * mail : hangchong.zhu@royole.com
 */
public abstract class BaseAnimator{
    public final String LOG_TAG = getClass().getSimpleName();

    /**
     * Duration of the animation.
     */
    protected int duration = 350;
    /**
     * Interpolator for the time of the animation.
     */
    protected Interpolator interpolator;
    /**
     * The delay before the animation.
     */
    protected int delay = 0;

    /**
     * Animation Infinite
     */
    protected boolean mIsInfinite;

    /**
     * auto start animator
     */
    protected boolean mAuto;

    /**
     * Set the duration of the animation.
     *
     * @param duration - The duration of the animation.
     * @return AnimatorBuilder.
     */
    public BaseAnimator duration(final int duration) {
        this.duration = duration;
        return this;
    }

    public boolean isAuto() {
        return mAuto;
    }

    /**
     * Set the Interpolator.
     *
     * @param interpolator - Interpolator.
     * @return AnimatorBuilder.
     */
    public BaseAnimator interpolator(final Interpolator interpolator) {
        this.interpolator = interpolator;
        return this;
    }

    /**
     * The delay before the animation.
     *
     * @param delay - int the delay
     * @return AnimatorBuilder.
     */
    public BaseAnimator delay(final int delay) {
        this.delay = delay;
        return this;
    }

    /**
     * The Infinite animation.
     *
     * @param isInfinite - true or false
     * @return AnimatorSetBuilder.
     */
    public BaseAnimator setInfinite(final boolean isInfinite) {
        this.mIsInfinite = isInfinite;
        return this;
    }

    /**
     * The auto start animation.
     *
     * @param auto - true or false
     * @return AnimatorSetBuilder.
     */
    public BaseAnimator setAuto(final boolean auto) {
        this.mAuto = auto;
        return this;
    }

    public void setAnimatorPath(PathAnimView pathView) {

    }

    public abstract void start();
}
