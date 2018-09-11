package com.newsdog.mvp.ui.promotion.toast;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.newsdog.ui.R;
import com.newsdog.utils.DeviceUtils;

import static android.view.Gravity.CENTER;


public class PromotionToast {
    private PromotionToast() {
    }

    /**
     * @param context
     */
    public static void showToast(@NonNull Context context, int stringId, int coin) {
        showToast(context, "", "+", context.getString(stringId), coin);
    }


    /**
     * @param context
     */
    public static void showToast(@NonNull Context context, String preFix, int stringId, int coin) {
        showToast(context, "", preFix, context.getString(stringId), coin);
    }

    public static void showToast(@NonNull Context context, int coin){
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, DeviceUtils.dip2px(context, 14), DeviceUtils.dip2px(context, 65));
        View toastView = LayoutInflater.from(context).inflate(R.layout.read_complete_coin_toast_layout, null);
        TextView coinTv = (TextView) toastView.findViewById(R.id.coin_tv);
        coinTv.setText("+" + String.valueOf(coin));

        toast.setView(toastView);
        toast.show();
    }


    /**
     * @param context
     * @param msg
     */
    public static void showToast(@NonNull Context context, String title, String preFix, String msg,  int coin) {
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(CENTER, 0, 0);
        View toastView = LayoutInflater.from(context).inflate(R.layout.promotion_coin_toast_layout, null) ;
        TextView textView = (TextView) toastView.findViewById(R.id.toast_tv);
        textView.setText(msg);

        TextView titleTv = (TextView) toastView.findViewById(R.id.title_tv);
        titleTv.setText(title);
        if (!TextUtils.isEmpty(title)) {
            titleTv.setVisibility(View.VISIBLE);
        } else {
            titleTv.setVisibility(View.GONE);
        }

        TextView coinTextView = (TextView) toastView.findViewById(R.id.coin_tv);
        coinTextView.setText(preFix + coin);

        toast.setView(toastView);
        toast.show();
    }
}
