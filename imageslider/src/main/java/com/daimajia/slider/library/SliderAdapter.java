package com.daimajia.slider.library;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.slider.library.SliderTypes.SliderView;

import java.util.ArrayList;

/**
 * A slider adapter
 */
public class SliderAdapter extends PagerAdapter{

    private Context mContext;
    private ArrayList<SliderView> mImageContents;

    public SliderAdapter(Context context){
        mContext = context;
        mImageContents = new ArrayList<>();
    }

    public void addSlider(SliderView slider){
        mImageContents.add(slider);
        notifyDataSetChanged();
    }

    public SliderView getSliderView(int position){
        if(position < 0 || position >= mImageContents.size()){
            return null;
        }else{
            return mImageContents.get(position);
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void removeSlider(SliderView slider){
        if(mImageContents.contains(slider)){
            mImageContents.remove(slider);
            notifyDataSetChanged();
        }
    }

    public void removeSliderAt(int position){
        if(mImageContents.size() > position){
            mImageContents.remove(position);
            notifyDataSetChanged();
        }
    }

    public void removeAllSliders(){
        mImageContents.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mImageContents.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        SliderView b = mImageContents.get(position);
        View v = b.getContentView();
        container.addView(v);
        return v;
    }
}
