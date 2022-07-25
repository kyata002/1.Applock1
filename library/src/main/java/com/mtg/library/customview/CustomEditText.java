package com.mtg.library.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.appcompat.widget.AppCompatEditText;

public class CustomEditText extends AppCompatEditText {
    private OnHideKeyboardListener mOnHideKeyboardListener;

    public CustomEditText(Context context) {
        super(context);
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnHideKeyboardListener(OnHideKeyboardListener onHideKeyboardListener) {
        mOnHideKeyboardListener = onHideKeyboardListener;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (mOnHideKeyboardListener != null) {
                mOnHideKeyboardListener.onHideKeyboard();
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public interface OnHideKeyboardListener {
        void onHideKeyboard();
    }
}
