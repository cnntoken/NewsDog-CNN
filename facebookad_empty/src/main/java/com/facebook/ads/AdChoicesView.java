package com.facebook.ads;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by objectlife on 5/5/17.
 */

public class AdChoicesView extends RelativeLayout {
    public AdChoicesView(Context context) {
        super(context);
    }

    public AdChoicesView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdChoicesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AdChoicesView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public AdChoicesView(Context var1, NativeAd var2, boolean var3) {
        this(var1);
    }
}
