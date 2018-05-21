package com.objectlife.sidebar;


public interface OnSideBarTouchListener {
    void onLetterChanged(String letter, int position, int itemHeight);
    void onLetterTouching(boolean touching);
}
