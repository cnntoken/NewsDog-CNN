package com.daimajia.slider.library.debug;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.R;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.SliderView;

/**
 * Created by objectlife on 11/1/16.
 * library 开发演示demo
 */

public class DemoActivity extends Activity {

    private SliderLayout mDemoSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        mDemoSlider = (SliderLayout) findViewById(R.id.slider);

        for (int i=0;i<4;i++){
            View contentView = View.inflate(this,R.layout.render_type_default,null);
            ImageView imageView = (ImageView) contentView.findViewById(R.id.daimajia_slider_image);
            imageView.setImageResource(R.drawable.ic_launcher);
            SliderView sliderView = new SliderView(this);
            sliderView.contentView(contentView).description("文字描述").setOnSliderClickListener(new SliderView.OnSliderClickListener() {
                @Override
                public void onSliderClick(SliderView slider) {

                }
            });
            mDemoSlider.addSlider(sliderView);
        }

        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Default);
        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        mDemoSlider.setDuration(4000);
    }


    @Override
    protected void onStop() {
        mDemoSlider.stopAutoCycle();
        super.onStop();
    }
}
