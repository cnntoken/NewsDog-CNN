package com.aspsine.irecyclerview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import com.aspsine.irecyclerview.adapter.WrapperAdapter;
import com.aspsine.irecyclerview.anim.SimpleAnimatorListener;
import com.aspsine.irecyclerview.footer.FooterView;
import com.aspsine.irecyclerview.header.RefreshHeader;
import com.aspsine.irecyclerview.header.RefreshHeaderLayout;
import com.aspsine.irecyclerview.listeners.OnLoadMoreListener;
import com.aspsine.irecyclerview.listeners.OnRefreshListener;

/**
 * Created by aspsine on 16/3/3.
 */
public class XRecyclerView extends RecyclerView {
    private static final String TAG = XRecyclerView.class.getSimpleName();

    private static final int STATUS_DEFAULT = 0;
    private static final int STATUS_SWIPING_TO_REFRESH = 1;
    private static final int STATUS_RELEASE_TO_REFRESH = 2;
    private static final int STATUS_REFRESHING = 3;

    public static boolean DEBUG = false;
    private int mStatus;
    private boolean mIsAutoRefreshing;
    private boolean isRefreshEnabled;
    private boolean isLoadMoreEnabled;
    private int mRefreshFinalMoveOffset;

    private OnRefreshListener mOnRefreshListener;
    private OnLoadMoreListener mOnLoadMoreListener;
    /**
     * 下拉刷新 Container
     */
    private RefreshHeaderLayout mRefreshHeaderContainer;
    /**
     * Footer Container
     */
    private FrameLayout mLoadMoreFooterContainer;
    private RefreshHeader mRefreshHeaderView;
    private FooterView mLoadMoreFooterView;

    private int mActivePointerId = -1;
    private int mLastTouchX = 0;
    private int mLastTouchY = 0;
    private ValueAnimator mScrollAnimator;
    private float mDragRate = 1.1f;
    private Handler mHandler = new Handler(Looper.getMainLooper());


    public XRecyclerView(Context context) {
        this(context, null);
    }

    public XRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.XRecyclerView, defStyle, 0);
        @LayoutRes int refreshHeaderLayoutRes = -1;
        @LayoutRes int loadMoreFooterLayoutRes = -1;
        int refreshFinalMoveOffset = -1;
        boolean refreshEnabled;
        boolean loadMoreEnabled;

        try {
            refreshEnabled = a.getBoolean(R.styleable.XRecyclerView_refreshEnabled, false);
            loadMoreEnabled = a.getBoolean(R.styleable.XRecyclerView_loadMoreEnabled, false);
            refreshHeaderLayoutRes = a.getResourceId(R.styleable.XRecyclerView_refreshHeaderLayout, -1);
            loadMoreFooterLayoutRes = a.getResourceId(R.styleable.XRecyclerView_loadMoreFooterLayout, -1);
            refreshFinalMoveOffset = a.getDimensionPixelOffset(R.styleable.XRecyclerView_refreshFinalMoveOffset, -1);
        } finally {
            a.recycle();
        }

        setRefreshEnabled(refreshEnabled);
        setLoadMoreEnabled(loadMoreEnabled);

        if (refreshHeaderLayoutRes != -1) {
            setRefreshHeaderView(refreshHeaderLayoutRes);
        }
        if (loadMoreFooterLayoutRes != -1) {
            setLoadMoreFooterView(loadMoreFooterLayoutRes);
        }
        if (refreshFinalMoveOffset != -1) {
            setRefreshFinalMoveOffset(refreshFinalMoveOffset);
        }
        setStatus(STATUS_DEFAULT);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        if (mRefreshHeaderView != null) {
            if (mRefreshHeaderView.getMeasuredHeight() > mRefreshFinalMoveOffset) {
                mRefreshFinalMoveOffset = 0;
            }
        }
    }

    public void setRefreshEnabled(boolean enabled) {
        this.isRefreshEnabled = enabled;
    }

    public void setLoadMoreEnabled(boolean enabled) {
        this.isLoadMoreEnabled = enabled;
        if (isLoadMoreEnabled) {
            removeOnScrollListener(mOnLoadMoreScrollListener);
            addOnScrollListener(mOnLoadMoreScrollListener);
        } else {
            removeOnScrollListener(mOnLoadMoreScrollListener);
        }
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        this.mOnRefreshListener = listener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        this.mOnLoadMoreListener = listener;
    }

    public void setRefreshing(boolean refreshing) {
        if (mStatus == STATUS_DEFAULT && refreshing) {
            this.mIsAutoRefreshing = true;
            setStatus(STATUS_SWIPING_TO_REFRESH);
            startScrollDefaultStatusToRefreshingStatus();
        } else if (mStatus == STATUS_REFRESHING && !refreshing) {
            this.mIsAutoRefreshing = false;
            startScrollRefreshingStatusToDefaultStatus();
        } else {
            this.mIsAutoRefreshing = false;
            Log.w(TAG, "isLoading = " + refreshing + " current status = " + mStatus);
        }
    }

    public void setRefreshFinalMoveOffset(int refreshFinalMoveOffset) {
        this.mRefreshFinalMoveOffset = refreshFinalMoveOffset;
    }

    public void setRefreshHeaderView(RefreshHeader refreshHeaderView) {
        if (!(refreshHeaderView instanceof RefreshHeader)) {
            throw new ClassCastException("Refresh header view must be an implement of RefreshHeader");
        }

        if (mRefreshHeaderView != null) {
            removeRefreshHeaderView();
        }
        if (mRefreshHeaderView != refreshHeaderView) {
            this.mRefreshHeaderView =  refreshHeaderView;
            ensureRefreshHeaderContainer();
            mRefreshHeaderContainer.addView((View) refreshHeaderView);
        }
    }

    public void setRefreshHeaderView(@LayoutRes int refreshHeaderLayoutRes) {
        ensureRefreshHeaderContainer();
        final View refreshHeader = LayoutInflater.from(getContext()).inflate(refreshHeaderLayoutRes, mRefreshHeaderContainer, false);
        if (refreshHeader != null && refreshHeader instanceof RefreshHeader) {
            setRefreshHeaderView((RefreshHeader)refreshHeader);
        } else {
            throw new IllegalArgumentException("XRecyclerVie's header must be RefreshHeader !!!");
        }
    }

    public void setLoadMoreFooterView(FooterView loadMoreFooterView) {
        if (mLoadMoreFooterView != null) {
            removeLoadMoreFooterView();
        }
        if (mLoadMoreFooterView != loadMoreFooterView) {
            this.mLoadMoreFooterView = loadMoreFooterView;
            ensureLoadMoreFooterContainer();
            mLoadMoreFooterContainer.addView((View)loadMoreFooterView);
            setLoadMoreEnabled(true);
        }
    }

    public void setLoadMoreFooterView(@LayoutRes int loadMoreFooterLayoutRes) {
        ensureLoadMoreFooterContainer();
        final View loadMoreFooter = LayoutInflater.from(getContext()).inflate(loadMoreFooterLayoutRes, mLoadMoreFooterContainer, false);
        if (loadMoreFooter != null && loadMoreFooter instanceof FooterView) {
            setLoadMoreFooterView((FooterView) loadMoreFooter);
        }else {
            throw new IllegalArgumentException("XRecyclerVie's FooterView must be FooterView !!!");
        }
    }


    WrapperAdapter mWrapperAdapter;

    @Override
    public void setAdapter(Adapter adapter) {
        ensureRefreshHeaderContainer();
        ensureLoadMoreFooterContainer();
        mWrapperAdapter = new WrapperAdapter(adapter, mRefreshHeaderContainer, mLoadMoreFooterContainer) ;
        super.setAdapter(mWrapperAdapter);
    }


    @Override
    public Adapter getAdapter() {
        final WrapperAdapter wrapperAdapter = (WrapperAdapter) super.getAdapter();
        return wrapperAdapter.getAdapter();
    }


    private void ensureRefreshHeaderContainer() {
        if (mRefreshHeaderContainer == null) {
            mRefreshHeaderContainer = new RefreshHeaderLayout(getContext());
            mRefreshHeaderContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
        }
    }

    private void ensureLoadMoreFooterContainer() {
        if (mLoadMoreFooterContainer == null) {
            mLoadMoreFooterContainer = new FrameLayout(getContext());
            mLoadMoreFooterContainer.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    private void removeRefreshHeaderView() {
        if (mRefreshHeaderContainer != null) {
            mRefreshHeaderContainer.removeView((View)mRefreshHeaderView);
        }
    }

    private void removeLoadMoreFooterView() {
        if (mLoadMoreFooterContainer != null) {
            mLoadMoreFooterContainer.removeView((View) mLoadMoreFooterView);
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        final int action = MotionEventCompat.getActionMasked(e);
        final int actionIndex = MotionEventCompat.getActionIndex(e);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mActivePointerId = MotionEventCompat.getPointerId(e, 0);
                mLastTouchX = (int) (MotionEventCompat.getX(e, actionIndex) + 0.5f);
                mLastTouchY = (int) (MotionEventCompat.getY(e, actionIndex) + 0.5f);
                mDownY = mLastTouchY;
            }
            break;

            case MotionEvent.ACTION_POINTER_DOWN: {
                mActivePointerId = MotionEventCompat.getPointerId(e, actionIndex);
                mLastTouchX = (int) (MotionEventCompat.getX(e, actionIndex) + 0.5f);
                mLastTouchY = (int) (MotionEventCompat.getY(e, actionIndex) + 0.5f);
            }
            break;

            case MotionEventCompat.ACTION_POINTER_UP: {
                onPointerUp(e);
            }
            break;
        }

        return super.onInterceptTouchEvent(e);
    }

    int mDownY ;
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        final int action = MotionEventCompat.getActionMasked(e);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final int index = MotionEventCompat.getActionIndex(e);
                mActivePointerId = MotionEventCompat.getPointerId(e, 0);
                mLastTouchX = getMotionEventX(e, index);
                mLastTouchY = getMotionEventY(e, index);
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                final int index = MotionEventCompat.findPointerIndex(e, mActivePointerId);
                if (index < 0) {
                    Log.e(TAG, "Error processing scroll; pointer index for id " + index + " not found. Did any MotionEvents get skipped?");
                    return false;
                }

                final int x = getMotionEventX(e, index);
                final int y = getMotionEventY(e, index);

                final int dx = x - mLastTouchX;
                int dy = y - mLastTouchY;
                dy = (int)(dy / mDragRate);

                mLastTouchX = x;
                mLastTouchY = y;

                final boolean triggerCondition = isEnabled() && isRefreshEnabled && mRefreshHeaderView != null && isFingerDragging() && canTriggerRefresh();
                if (DEBUG) {
                    Log.i(TAG, "triggerCondition = " + triggerCondition + "; mStatus = " + mStatus + "; dy = " + dy);
                }
                if (triggerCondition) {

                    final int refreshHeaderContainerHeight = mRefreshHeaderContainer.getMeasuredHeight();
                    final int refreshHeaderViewHeight = (int) getRefreshHeaderHeight();

                    if (dy > 0 && mStatus == STATUS_DEFAULT) {
                        setStatus(STATUS_SWIPING_TO_REFRESH);
                        mRefreshHeaderView.onStart(false, refreshHeaderViewHeight, mRefreshFinalMoveOffset);
                    } else if (dy < 0) {
                        if (mStatus == STATUS_SWIPING_TO_REFRESH && refreshHeaderContainerHeight <= 0) {
                            setStatus(STATUS_DEFAULT);
                        }
                        if (mStatus == STATUS_DEFAULT) {
                            break;
                        }
                    }

                    if (mStatus == STATUS_SWIPING_TO_REFRESH || mStatus == STATUS_RELEASE_TO_REFRESH) {
                        if (refreshHeaderContainerHeight >= refreshHeaderViewHeight) {
                            setStatus(STATUS_RELEASE_TO_REFRESH);
                            mRefreshHeaderView.onReleaseToRefresh();
                        } else {
                            setStatus(STATUS_SWIPING_TO_REFRESH);
                            fingerMove(dy);
                        }
                        return true;
                    }
                }
            }
            break;

            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(e);
                mActivePointerId = MotionEventCompat.getPointerId(e, index);
                mLastTouchX = getMotionEventX(e, index);
                mLastTouchY = getMotionEventY(e, index);
            }
            break;

            case MotionEventCompat.ACTION_POINTER_UP: {
                onPointerUp(e);
            }
            break;

            case MotionEvent.ACTION_UP: {
                onFingerUpStartAnimating();
                // touch to load more
                if ( canLoadMore() ) {
                    doLoadMore();
                }
            }
            break;

            case MotionEvent.ACTION_CANCEL: {
                onFingerUpStartAnimating();
            }
            break;
        }
        return super.onTouchEvent(e);
    }

    private boolean canLoadMore() {
        boolean notRefresh = isNotRefreshing() ;
        boolean notLoading = isLoadMoreNotRunning();
        boolean canLoadMore = isBottom(getLastItemPosition()) ;
        return notRefresh && notLoading
                && mStatus == STATUS_DEFAULT
                && isFlingUp() && canLoadMore && isLoadMoreEnabled ;
    }

    private boolean isFingerDragging() {
        return getScrollState() == SCROLL_STATE_DRAGGING;
    }

    public boolean canTriggerRefresh() {
        final Adapter adapter = getAdapter();
        if ((adapter == null || adapter.getItemCount() <= 0) && isLoadMoreNotRunning()) {
            return true;
        }
        View firstChild = getChildAt(0);
        int position = getChildLayoutPosition(firstChild);
        if (position == 0) {
            if (firstChild.getTop() == mRefreshHeaderContainer.getTop() && isLoadMoreNotRunning()) {
                return true;
            }
        }
        return false;
    }

    public boolean isLoadMoreNotRunning() {
        return mLoadMoreFooterView == null || (mLoadMoreFooterView != null && !mLoadMoreFooterView.isLoading());
    }

    private boolean isNotRefreshing() {
        return mRefreshHeaderView == null || (mRefreshHeaderView != null && !mRefreshHeaderView.isRefreshing());
    }

    private int getMotionEventX(MotionEvent e, int pointerIndex) {
        return (int) (MotionEventCompat.getX(e, pointerIndex) + 0.5f);
    }

    private int getMotionEventY(MotionEvent e, int pointerIndex) {
        return (int) (MotionEventCompat.getY(e, pointerIndex) + 0.5f);
    }

    private void onFingerUpStartAnimating() {
        if (mStatus == STATUS_RELEASE_TO_REFRESH) {
            startScrollReleaseStatusToRefreshingStatus();
        } else if (mStatus == STATUS_SWIPING_TO_REFRESH) {
            startScrollSwipingToRefreshStatusToDefaultStatus();
        }
    }


    private void onPointerUp(MotionEvent e) {
        final int actionIndex = MotionEventCompat.getActionIndex(e);
        if (MotionEventCompat.getPointerId(e, actionIndex) == mActivePointerId) {
            // Pick a new pointer to pick up the slack.
            final int newIndex = actionIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(e, newIndex);
            mLastTouchX = getMotionEventX(e, newIndex);
            mLastTouchY = getMotionEventY(e, newIndex);
        }
    }

    private void fingerMove(int dy) {
        int ratioDy = (int) (dy * 0.5f + 0.5f);
        int offset = mRefreshHeaderContainer.getMeasuredHeight();
        int finalDragOffset = mRefreshFinalMoveOffset;

        int nextOffset = offset + ratioDy;
        if (finalDragOffset > 0) {
            if (nextOffset > finalDragOffset) {
                ratioDy = finalDragOffset - offset;
            }
        }

        if (nextOffset < 0) {
            ratioDy = -offset;
        }
        move(ratioDy);
    }

    private void move(int dy) {
        if (dy != 0) {
            float height = mRefreshHeaderContainer.getMeasuredHeight() + dy;
            if (getRefreshHeaderHeight() != 0 && (height > getRefreshHeaderHeight())) {
                height = getRefreshHeaderHeight();
            }
            setRefreshHeaderContainerHeight(height);
            mRefreshHeaderView.onMove(false, false, (int) height, height / getRefreshHeaderHeight());
        }
    }


    public void setDragRate(float rate) {
        this.mDragRate = rate;
    }

    private void setRefreshHeaderContainerHeight(float height) {
        mRefreshHeaderContainer.getLayoutParams().height = (int) height;
        mRefreshHeaderContainer.requestLayout();
    }

    private void startScrollDefaultStatusToRefreshingStatus() {
        if ( mRefreshHeaderView == null ) {
            return;
        }
        mRefreshHeaderView.onStart(true, (int) getRefreshHeaderHeight(), mRefreshFinalMoveOffset);

        int targetHeight = (int) getRefreshHeaderHeight();
        int currentHeight = mRefreshHeaderContainer.getMeasuredHeight();
        startScrollAnimation(400, new AccelerateInterpolator(), currentHeight, targetHeight);
    }

    private void startScrollSwipingToRefreshStatusToDefaultStatus() {
        final int targetHeight = 0;
        final int currentHeight = mRefreshHeaderContainer.getMeasuredHeight();
        startScrollAnimation(300, new DecelerateInterpolator(), currentHeight, targetHeight);
    }

    private void startScrollReleaseStatusToRefreshingStatus() {
        if ( mRefreshHeaderView == null ) {
            return;
        }
        mRefreshHeaderView.onRelease();

        final int targetHeight = (int) getRefreshHeaderHeight();
        final int currentHeight = mRefreshHeaderContainer.getMeasuredHeight();
        startScrollAnimation(300, new DecelerateInterpolator(), currentHeight, targetHeight);
    }

    private void startScrollRefreshingStatusToDefaultStatus() {
        if ( mRefreshHeaderView == null ) {
            return;
        }
        mRefreshHeaderView.onComplete();

        final int targetHeight = 0;
        final int currentHeight = mRefreshHeaderContainer.getMeasuredHeight();
        startScrollAnimation(400, new DecelerateInterpolator(), currentHeight, targetHeight);
    }



    private void startScrollAnimation(final int time, final Interpolator interpolator, int value, int toValue) {
        if (mScrollAnimator == null) {
            mScrollAnimator = new ValueAnimator();
        }
        //cancel
        mScrollAnimator.removeAllUpdateListeners();
        mScrollAnimator.removeAllListeners();
        mScrollAnimator.cancel();

        //reset new value
        mScrollAnimator.setFloatValues(value, toValue);
        mScrollAnimator.setDuration(time);
        mScrollAnimator.setInterpolator(interpolator);
        mScrollAnimator.addUpdateListener(mAnimatorUpdateListener);
        mScrollAnimator.addListener(mAnimationListener);
        mScrollAnimator.start();
    }

    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            final float height = (float) animation.getAnimatedValue();
            setRefreshHeaderContainerHeight(height);
            switch (mStatus) {
                case STATUS_SWIPING_TO_REFRESH: {
                    final float ration = height / getRefreshHeaderHeight();
                    mRefreshHeaderView.onMove(false, true, (int) height, ration);
                    if (ration == 1.0f) {
                        mRefreshHeaderView.onReleaseToRefresh();
                    }
                }
                break;

                case STATUS_RELEASE_TO_REFRESH: {
                    mRefreshHeaderView.onMove(false, true, (int) height, height / getRefreshHeaderHeight());
                }
                break;

                case STATUS_REFRESHING: {
                    mRefreshHeaderView.onMove(true, true, (int) height, height / getRefreshHeaderHeight());
                }
                break;
            }

        }
    };

    private Animator.AnimatorListener mAnimationListener = new SimpleAnimatorListener() {
        @Override
        public void onAnimationEnd(Animator animation) {
            int lastStatus = mStatus;

            switch (mStatus) {
                case STATUS_SWIPING_TO_REFRESH: {
                    if (mIsAutoRefreshing) {
                        mRefreshHeaderContainer.getLayoutParams().height = (int) getRefreshHeaderHeight();
                        mRefreshHeaderContainer.requestLayout();
                        setStatus(STATUS_REFRESHING);
                        if (mOnRefreshListener != null) {
                            mOnRefreshListener.onRefresh();
                            mRefreshHeaderView.onRefresh();
                        }
                    } else {
                        mRefreshHeaderContainer.getLayoutParams().height = 0;
                        mRefreshHeaderContainer.requestLayout();
                        setStatus(STATUS_DEFAULT);
                    }
                }
                break;

                case STATUS_RELEASE_TO_REFRESH: {
                    mRefreshHeaderContainer.getLayoutParams().height = (int) getRefreshHeaderHeight();
                    mRefreshHeaderContainer.requestLayout();
                    setStatus(STATUS_REFRESHING);
                    if (mOnRefreshListener != null) {
                        mOnRefreshListener.onRefresh();
                        mRefreshHeaderView.onRefresh();
                    }
                }
                break;

                case STATUS_REFRESHING: {
                    mIsAutoRefreshing = false;
                    mRefreshHeaderContainer.getLayoutParams().height = 0;
                    mRefreshHeaderContainer.requestLayout();
                    setStatus(STATUS_DEFAULT);
                    mRefreshHeaderView.onReset();
                }
                break;
            }
            if (DEBUG) {
                Log.i(TAG, "onAnimationEnd " + getStatusLog(lastStatus) + " -> " + getStatusLog(mStatus) + " ;refresh view height:" + mRefreshHeaderContainer.getMeasuredHeight());
            }
        }
    };

    private ScrollListener mOnLoadMoreScrollListener = new ScrollListener();

    private void setStatus(int status) {
        this.mStatus = status;
        if (DEBUG) {
            printStatusLog();
        }
    }

    private void printStatusLog() {
        Log.i(TAG, getStatusLog(mStatus));
    }

    private String getStatusLog(int status) {
        final String statusLog;
        switch (status) {
            case STATUS_DEFAULT:
                statusLog = getContext().getString(R.string.status_default);
                break;

            case STATUS_SWIPING_TO_REFRESH:
                statusLog = getContext().getString(R.string.status_swiping_to_refresh);
                break;

            case STATUS_RELEASE_TO_REFRESH:
                statusLog = getContext().getString(R.string.status_release_to_refresh);
                break;

            case STATUS_REFRESHING:
                statusLog = getContext().getString(R.string.status_refreshing);
                break;
            default:
                statusLog = getContext().getString(R.string.status_illegal);
                break;
        }
        return statusLog;
    }


    public void showRefreshHeader() {
        if (mRefreshHeaderView != null ) {
            mRefreshHeaderContainer.getLayoutParams().height = mRefreshHeaderView.getMeasuredHeight();
            mRefreshHeaderContainer.requestLayout();
            mRefreshHeaderView.onRefresh();
            mStatus = STATUS_REFRESHING;
        }
    }

    private float getRefreshHeaderHeight() {
        return mRefreshHeaderView != null ? mRefreshHeaderView.getMeasuredHeight() : 0 ;
    }


    /**
     * 获取最后item的position
     *
     * @return
     */
    int getLastItemPosition() {
        LayoutManager layoutManager = getLayoutManager();
        int lastVisibleItemPosition;
        if (layoutManager instanceof GridLayoutManager) {
            lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
        }  else {
            lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        }
        return lastVisibleItemPosition;
    }

    /**
     * 是否是底部
     *
     * @param lastVisibleItemPosition
     * @return
     */
    private boolean isBottom(int lastVisibleItemPosition) {
        LayoutManager layoutManager = getLayoutManager();
        int itemCount = layoutManager.getItemCount();
        int childCount = layoutManager.getChildCount();

//        boolean lastOneShowing =  childCount > 0 && lastVisibleItemPosition >= itemCount - 1 && itemCount >= childCount && isNotRefreshing() ;
//        if ( lastOneShowing ) {
//            int itemBottom = this.getBottom();
//            int bottom = this.getBottom();
//            ViewHolder holder = this.findViewHolderForAdapterPosition(lastVisibleItemPosition) ;
//            if ( holder != null ) {
//                itemBottom = holder.itemView.getBottom();
//            }
////            Log.e("", "### bottom " + itemBottom + ", x bottom : " + bottom) ;
//            return itemBottom - bottom <= 20 ;
//        }
//        return false;

        int itemIndex = Math.max(1, itemCount - 3) ;
        // 滚动到倒数第二位时就触发加载更多, 避免用户等待load more
        return childCount > 0 && lastVisibleItemPosition >= itemIndex && isNotRefreshing();
    }


    private boolean isFlingUp() {
        return mDownY - mLastTouchY > 30 ;
    }


    private void doLoadMore() {
        if ( mOnLoadMoreListener != null ) {
            if ( mLoadMoreFooterView != null ) {
                mLoadMoreFooterView.setStatus(FooterView.Status.LOADING);
            }
            mOnLoadMoreListener.onLoadMore();

//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if ( mLoadMoreFooterView.isLoading()
//                            && getLastItemPosition() >= getLayoutManager().getItemCount() - 1 ) {
//                        smoothScrollToPosition(mWrapperAdapter.getItemCount());
//                    }
//                }
//            }, 80);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDetachedFromWindow();
    }

    public void loadMoreComplete() {
        if ( mLoadMoreFooterView != null ) {
            mLoadMoreFooterView.setStatus(FooterView.Status.GONE);
        }
    }

    public boolean isRefreshing() {
        return mStatus != STATUS_DEFAULT;
    }


    @Override
    public boolean awakenScrollBars() {
        return super.awakenScrollBars();
    }

    /**
     *
     */
    private class ScrollListener extends OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            int visibleItemCount = layoutManager.getChildCount();

            boolean triggerCondition = visibleItemCount > 0
                    && newState == RecyclerView.SCROLL_STATE_IDLE;

            if (triggerCondition && canLoadMore()) {
                doLoadMore();
            }
        }
    }
}
