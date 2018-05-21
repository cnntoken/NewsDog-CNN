package com.objectlife.sidebar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

public class SideBarView extends View {

    private OnSideBarTouchListener listener;
    private List<String> mLetters;
    private int mChoose = -1;
    private Paint mPaint = new Paint();
    private float mTextSize;
    private float mTextSizeChoose;
    private int mTextColor;
    private int mTextColorChoose;
    private int mWidth;
    private int mHeight;
    private int mItemHeight;
    private ViewGroup mRecyclerView;
    private int mFirstTop;

    public SideBarView(Context context) {
        this(context, null);
    }

    public SideBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SideBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public void setRecyclerView(ViewGroup recyclerView) {
        this.mRecyclerView = recyclerView;
    }

    private void init(Context context, AttributeSet attrs) {
        mLetters = Arrays.asList(context.getResources().getStringArray(R.array.quickSideBarLetters));

        mTextColor = Color.BLACK;
        mTextColorChoose = Color.BLACK;
        mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,12.0f,getResources().getDisplayMetrics());
        mTextSizeChoose = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,14.0f,getResources().getDisplayMetrics());
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SideBarView);
            mTextColor = a.getColor(R.styleable.SideBarView_sidebarTextColor, mTextColor);
            mTextColorChoose = a.getColor(R.styleable.SideBarView_sidebarTextColorChoose, mTextColorChoose);
            mTextSize = a.getFloat(R.styleable.SideBarView_sidebarTextSize, mTextSize);
            mTextSizeChoose = a.getFloat(R.styleable.SideBarView_sidebarTextSizeChoose, mTextSizeChoose);
            a.recycle();
        }

        DisplayMetrics dm = new DisplayMetrics();
        dm = getResources().getDisplayMetrics();

        mFirstTop  = (int) (dm.density * 10);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < mLetters.size(); i++) {
            mPaint.setColor(mTextColor);

            mPaint.setAntiAlias(true);
            mPaint.setTextSize(mTextSize);
            if (i == mChoose) {
                mPaint.setColor(mTextColorChoose);
                mPaint.setFakeBoldText(true);
                mPaint.setTypeface(Typeface.DEFAULT_BOLD);
                mPaint.setTextSize(mTextSizeChoose);
            }
            canvas.drawText(mLetters.get(i),getWidth() * 3 / 4, mItemHeight / 2  + (mItemHeight * i) + mFirstTop, mPaint);
            mPaint.reset();
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mHeight = getMeasuredHeight();
        mWidth = getMeasuredWidth();
        mItemHeight = mHeight / mLetters.size();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();
        final int oldChoose = mChoose;
        final int newChoose = (int) (y / mHeight * mLetters.size());

        switch (action) {
            case MotionEvent.ACTION_UP:
                mChoose = -1;
                if (listener != null) {
                    listener.onLetterTouching(false);
                }
                invalidate();
                break;
            default:
                if (oldChoose != newChoose) {
                    if (newChoose >= 0 && newChoose < mLetters.size()) {
                        mChoose = newChoose;
                        if (listener != null) {
                            listener.onLetterChanged(mLetters.get(newChoose), mChoose, mItemHeight);
                        }
                    }
                    invalidate();
                }
                //如果是cancel也要调用onLetterUpListener 通知
                if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    if (listener != null) {
                        listener.onLetterTouching(false);
                        invalidate();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {//按下调用 onLetterDownListener
                    if (listener != null) {
                        listener.onLetterTouching(true);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_HOVER_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (listener != null) {
                        listener.onLetterTouching(true);
                    }
                }

                break;
        }
        return true;
    }

    public OnSideBarTouchListener getListener() {
        return listener;
    }

    public void setOnSideBarTouchListener(OnSideBarTouchListener listener) {
        this.listener = listener;
    }

    public List<String> getLetters() {
        return mLetters;
    }

    public void setLetters(List<String> letters) {
        this.mLetters = letters;
        invalidate();
    }
}

