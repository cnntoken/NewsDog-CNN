package com.objectlife.sidebar;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Ryan on 4/15/16.
 */
public class TipTextView extends TextView {

    private int mCornerRadius;
    private Path mBackgroundPath = new Path();
    private RectF mBackgroundRect = new RectF();
    private Paint mBackgroundPaint;
    private int mWidth;

    public TipTextView(Context context) {
        super(context);
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setColor(Color.parseColor("#E9002C"));
    }

    public TipTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TipTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mWidth = getWidth();
        mCornerRadius = (int) (mWidth * 0.5);
        float[] radii;

        mBackgroundRect.set(0, 0, mWidth, mWidth);
        if (isRtl()) {
            radii = new float[]{mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, 0, 0};
        } else {
            radii = new float[]{mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, 0, 0, mCornerRadius, mCornerRadius};
        }
        mBackgroundPath.addRoundRect(mBackgroundRect, radii, Path.Direction.CW);
        canvas.drawPath(mBackgroundPath, mBackgroundPaint);
        super.onDraw(canvas);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private boolean isRtl() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) &&
                (getContext().getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }
}
