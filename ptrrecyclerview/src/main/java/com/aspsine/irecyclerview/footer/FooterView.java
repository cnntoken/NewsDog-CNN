package com.aspsine.irecyclerview.footer;

/**
 * Created by mrsimple on 21/3/17.
 */
public interface FooterView {

    public enum Status {
        GONE, LOADING, ERROR, THE_END
    }

    void setStatus(Status status) ;
    boolean canLoadMore()  ;
    boolean isLoading() ;
}
