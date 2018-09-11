package com.aspsine.irecyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 *
 * todo: 注意, 如果某一个 XRecyclerView的 Header 不只一个的话, 那么对应的要覆写 getRealAdapterPosition() 函数
 * Created by aspsine on 16/3/12.
 */
public abstract class XViewHolder extends RecyclerView.ViewHolder {

    public XViewHolder(View itemView) {
        super(itemView);
    }

    /**
     * 获取 ViewHolder在 XRecyclerView 中的位置.
     *
     * 默认情况下 XRecyclerView 只有有一个 Header, 因此需要减1. 需要与 {@link WrapperAdapter#getHeaderCount()} 的数量一致.
     *
     * @return
     */
    public int getRealAdapterPosition() {
        return getAdapterPosition() - 1;
    }
}
