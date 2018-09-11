package com.newsdog.mvp.ui.promotion.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.newsdog.config.PromotionConfig;
import com.newsdog.ui.R;
import com.newsdog.utils.DeviceUtils;


public class CheckInLayout extends FrameLayout {
    private View mLeftTopStar;
    private View mRightBottomStar;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public CheckInLayout(Context context) {
        this(context, null);
    }

    public CheckInLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckInLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateView(context);
    }

    @TargetApi(value = 21)
    public CheckInLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflateView(context);
    }


    private void inflateView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.checkin_entry_layout, this);
        mLeftTopStar = findViewById(R.id.star_left_top);
        mRightBottomStar = findViewById(R.id.star_right_bottom);
    }

    private boolean isClicked = false;
    private int mAnimTimes = 0;

    @Override
    public boolean performClick() {
        isClicked = true;
        return super.performClick();
    }

    public void reset() {
        isClicked = false;
        mAnimTimes = 0;
        mHandler.removeCallbacksAndMessages(null);
        clearAnimation();
    }

    boolean intercept = false ;

    public void interceptAnimation(boolean intercept) {
        this.intercept =  intercept;
    }

    public void showAnimation() {
        if (!PromotionConfig.isPromotionEnable() ) {
            this.setVisibility(GONE);
            return;
        }
        this.setVisibility(VISIBLE);
        if (isClicked || mAnimTimes >= 2) {
            dismissStar(mLeftTopStar);
            dismissStar(mRightBottomStar);
            return;
        }

        final int yOffset = DeviceUtils.dip2px(getContext(), 15);
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(this, "translationY", 0, yOffset);
        anim1.setInterpolator(new DecelerateInterpolator());
        anim1.setDuration(2000);


        final ObjectAnimator anim2 = ObjectAnimator.ofFloat(this, "translationY", yOffset, 0);
        anim2.setInterpolator(new DecelerateInterpolator());
        anim2.setDuration(2000);


        anim1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                showStar(mLeftTopStar);
                mRightBottomStar.setVisibility(GONE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if ( intercept ) {
                    return;
                }
                anim2.start();
            }
        });
        anim1.start();


        anim2.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                showStar(mRightBottomStar);
                mLeftTopStar.setVisibility(GONE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if ( intercept ) {
                    return;
                }
                mAnimTimes++;
                if (mAnimTimes >= 2 ) {
                    dismissStar(mLeftTopStar);
                    dismissStar(mRightBottomStar);
                    return;
                }

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if ( intercept ) {
                            return;
                        }
                        showAnimation();
                    }
                }, 50);
            }
        });
    }


    private void showStar(final View star) {
        star.setVisibility(VISIBLE);
        ObjectAnimator animatorScaleX = ObjectAnimator.ofFloat(star, "scaleX", 0, 1.6f, 1.2f);
        ObjectAnimator animatorScaleY = ObjectAnimator.ofFloat(star, "scaleY", 0, 1.6f, 1.2f);
        AnimatorSet scaleSet = new AnimatorSet();
        scaleSet.setDuration(800);
        scaleSet.playTogether(animatorScaleX, animatorScaleY);
        scaleSet.setInterpolator(new AccelerateInterpolator());
        scaleSet.start();
    }

    private void dismissStar(final View star) {
        ObjectAnimator animatorScaleX = ObjectAnimator.ofFloat(star, "scaleX", 0);
        ObjectAnimator animatorScaleY = ObjectAnimator.ofFloat(star, "scaleY", 0);
        AnimatorSet scaleSet = new AnimatorSet();
        scaleSet.setDuration(800);
        scaleSet.playTogether(animatorScaleX, animatorScaleY);
        scaleSet.setInterpolator(new AccelerateInterpolator());
        scaleSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                star.setVisibility(GONE);
            }
        });
        scaleSet.start();
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacksAndMessages(null);
    }
}
