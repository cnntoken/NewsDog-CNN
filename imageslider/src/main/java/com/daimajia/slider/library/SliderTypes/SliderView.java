package com.daimajia.slider.library.SliderTypes;

import android.content.Context;
import android.view.View;

import com.daimajia.slider.library.R;

/**
 * When you want to make your own slider view, you must extends from this class.
 * SliderView provides some useful methods.
 * if you want to show progressbar, you just need to set a progressbar id as @+id/loading_bar.
 */
public class SliderView {
    private Context mContext;

    private OnSliderClickListener mOnSliderClickListener;

    private String mDescription;

    private View mContentView;

    public SliderView(Context context) {
        mContext = context;
    }

    /**
     * the description of a slider image.
     * @param description
     * @return
     */
    public SliderView description(String description){
        mDescription = description;
        return this;
    }

    public String getDescription(){
        return mDescription;
    }


    public SliderView contentView(View contentView){
        mContentView = contentView;
        if (mContentView != null){
            bindEventAndShow(mContentView);
        }
        return this;
    }

    public View getContentView(){
        return mContentView;
    }

    public Context getContext(){
        return mContext;
    }

    /**
     * set a slider image click listener
     * @param l
     * @return
     */
    public SliderView setOnSliderClickListener(OnSliderClickListener l){
        mOnSliderClickListener = l;
        return this;
    }

    /**
     * When you want to implement your own slider view, please call this method in the end in `getContentView()` method
     * @param v the whole view
     */
    private void bindEventAndShow(final View v){
        final SliderView me = this;
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSliderClickListener != null) {
                    mOnSliderClickListener.onSliderClick(me);
                }
            }
        });
        if (v.findViewById(R.id.loading_bar) != null) {
            v.findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
        }
   }

    public interface OnSliderClickListener {
        public void onSliderClick(SliderView slider);
    }

}
