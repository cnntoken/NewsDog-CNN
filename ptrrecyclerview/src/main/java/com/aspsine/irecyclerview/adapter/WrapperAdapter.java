package com.aspsine.irecyclerview.adapter;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.aspsine.irecyclerview.header.RefreshHeaderLayout;

/**
 * Adapter中的数据分布如下所示: 第一位是 header, 然后是真实数据,最后是 footer .
 * ==================
 * | refresh header |
 * |     data0      |
 * |     data1      |
 * |     data2      |
 * |     data3      |
 * |     data4      |
 * |     data5      |
 * |     data6      |
 * |     data7      |
 * |  footer view   |
 * ==================
 * Created by aspsine on 16/3/12.
 */
public class WrapperAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected static final int REFRESH_HEADER = Integer.MIN_VALUE;
    protected static final int LOAD_MORE_FOOTER = Integer.MAX_VALUE;

    private final RecyclerView.Adapter mAdapter;
    private final RefreshHeaderLayout mRefreshHeaderContainer;
    private final FrameLayout mLoadMoreFooterContainer;


    public WrapperAdapter(RecyclerView.Adapter adapter, RefreshHeaderLayout refreshHeaderContainer, FrameLayout loadMoreFooterContainer) {
        this.mAdapter = adapter;
        this.mRefreshHeaderContainer = refreshHeaderContainer;
        this.mLoadMoreFooterContainer = loadMoreFooterContainer;
        mAdapter.registerAdapterDataObserver(mObserver);
    }


    private RecyclerView.AdapterDataObserver mObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            WrapperAdapter.this.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            WrapperAdapter.this.notifyItemRangeChanged(positionStart + getHeaderCount(), itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            WrapperAdapter.this.notifyItemRangeChanged(positionStart + getHeaderCount(), itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            WrapperAdapter.this.notifyItemRangeInserted(positionStart + getHeaderCount(), itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            WrapperAdapter.this.notifyItemRangeRemoved(positionStart + getHeaderCount(), itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            WrapperAdapter.this.notifyDataSetChanged();
        }
    };


    public RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return REFRESH_HEADER;
        } else if (1 <= position && position < mAdapter.getItemCount() + getFooterCount()) {
            return mAdapter.getItemViewType(position - getHeaderCount());
        }  else if (position == mAdapter.getItemCount() + getFooterCount()) {
            return LOAD_MORE_FOOTER;
        }
        return 0;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == REFRESH_HEADER) {
            return new SimpleViewHolder(mRefreshHeaderContainer);
        }else if (viewType == LOAD_MORE_FOOTER) {
            return new SimpleViewHolder(mLoadMoreFooterContainer);
        } else {
            return mAdapter.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (0 < position && position < mAdapter.getItemCount() + getHeaderCount()) {
            mAdapter.onBindViewHolder(holder, position - getHeaderCount());
        }
    }


    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    WrapperAdapter wrapperAdapter = (WrapperAdapter) recyclerView.getAdapter();
                    if (isFullSpanType(wrapperAdapter.getItemViewType(position))) {
                        return gridLayoutManager.getSpanCount();
                    } else if (spanSizeLookup != null) {
                        return spanSizeLookup.getSpanSize(position - getHeaderCount());
                    }
                    return 1;
                }
            });
        }
    }


    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getAdapterPosition();
        int type = getItemViewType(position);
        if (isFullSpanType(type)) {
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) layoutParams;
                lp.setFullSpan(true);
            }
        }
    }

    private boolean isFullSpanType(int type) {
        return type == REFRESH_HEADER || type == LOAD_MORE_FOOTER;
    }

    @Override
    public int getItemCount() {
        // 多了一个 header 和 footer
        return mAdapter.getItemCount() + getHeaderCount() + getFooterCount();
    }


    protected int getHeaderCount() {
        return 1;
    }


    protected int getFooterCount() {
        return 1;
    }


    /**
     *
     */
    static class SimpleViewHolder extends RecyclerView.ViewHolder {

        public SimpleViewHolder(View itemView) {
            super(itemView);
        }
    }
}
