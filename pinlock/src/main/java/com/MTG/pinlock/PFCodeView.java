package com.MTG.pinlock;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aleksandr Nikiforov on 2018/02/07.
 */

public class PFCodeView extends LinearLayout {

    private List<CheckBox> mCodeViews = new ArrayList<>();
    private String mCode = "";
    private int mCodeLength = PinLockConfiguration.Config.CODE_LENGTH_DEFAULT;
    private int mCodeLengthSuccess = PinLockConfiguration.Config.CODE_LENGTH_DEFAULT;
    private OnPFCodeListener mListener;
    private int mSelector = 0;

    public PFCodeView(Context context) {
        super(context);
        init();
    }

    public PFCodeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PFCodeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_code_pf_lockscreen, this);
        setUpCodeViews();
    }

    public void setCodeLength(int codeLength) {
        mCodeLength = codeLength;
        mCodeLengthSuccess = codeLength;
        setUpCodeViews();
    }

    public void setCodeLengthSuccess(int codeLengthSuccess) {
        mCodeLengthSuccess = codeLengthSuccess;
    }

    private void setUpCodeViews() {
        removeAllViews();
        mCodeViews.clear();
        mCode = "";
        for (int i = 0; i < mCodeLength; i++) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            CheckBox view = (CheckBox) inflater.inflate(R.layout.view_pf_code_checkbox, null);
            if (mSelector != 0) {
                view.setButtonDrawable(mSelector);
            }
            LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int margin = getResources().getDimensionPixelSize(R.dimen.code_fp_margin);
            layoutParams.setMargins(margin, margin, margin, margin);
            view.setLayoutParams(layoutParams);
            view.setChecked(false);
            addView(view);
            mCodeViews.add(view);
        }
        if (mListener != null) {
            mListener.onCodeNotCompleted("");
        }
    }

    public void setCheckbox(int selector) {
        mSelector = selector;
        setUpCodeViews();
        invalidate();
    }

    public int input(String number) {
        if (mCode.length() == mCodeLengthSuccess) {
            return mCode.length();
        }
        mCodeViews.get(mCode.length()).toggle();
        mCode += number;
        if (mCode.length() == mCodeLengthSuccess && mListener != null) {
            mListener.onCodeCompleted(mCode);
        }
        return mCode.length();
    }

    public int delete() {
        if (mListener != null) {
            mListener.onCodeNotCompleted(mCode);
        }
        if (mCode.length() == 0) {
            return mCode.length();
        }
        mCode = mCode.substring(0, mCode.length() - 1);
        mCodeViews.get(mCode.length()).toggle();
        return mCode.length();
    }

    public void clearCode() {
        if (mListener != null) {
            mListener.onCodeNotCompleted(mCode);
        }
        mCode = "";
        for (CheckBox codeView : mCodeViews) {
            codeView.setChecked(false);
        }
    }

    public int getInputCodeLength() {
        return mCode.length();
    }

    public String getCode() {
        return mCode;
    }

    public void setListener(OnPFCodeListener listener) {
        mListener = listener;
    }

    public interface OnPFCodeListener {

        void onCodeCompleted(String code);

        void onCodeNotCompleted(String code);

    }
}
