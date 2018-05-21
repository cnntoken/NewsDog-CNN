package com.objectlife.sidebar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.RelativeLayout;

/**
 * Created by Ryan on 4/15/16.
 */
public class SideBarTipsView extends RelativeLayout {

    private TipTextView mTipsView;

    public SideBarTipsView(Context context) {
        this(context, null);
    }

    public SideBarTipsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SideBarTipsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mTipsView = new TipTextView(context);
        mTipsView.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,16.0f,getResources().getDisplayMetrics()));
        mTipsView.setTextColor(Color.WHITE);
        mTipsView.setGravity(Gravity.CENTER);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,80.0f,getResources().getDisplayMetrics()));
        addView(mTipsView,layoutParams);
        setVisibility(INVISIBLE);
    }


    public void setText(String text,int position, int itemHeight){
        mTipsView.setText(text);
        int viewHeight = mTipsView.getMeasuredHeight();
        int totalOffset = position * itemHeight;
        int topOffset = totalOffset < viewHeight ? 0 : totalOffset - viewHeight;
        LayoutParams layoutParams = (LayoutParams) mTipsView.getLayoutParams();
        layoutParams.topMargin = topOffset;
        mTipsView.setLayoutParams(layoutParams);
    }

}
