package com.wumeng.downloadprogressbutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;

/**
 * @author WuMeng
 * @date 2020/5/3
 * desc:
 */
public class DownloadProgressButton extends androidx.appcompat.widget.AppCompatTextView {

    /**
     * 背景颜色
     */
    private int mBackgroundColor;

    /**
     * 下载中后半部分后面背景颜色
     */
    private int mBackgroundSecondColor;

    /**
     * 按钮的角度
     */
    private float mButtonRadius;

    /**
     * 文字颜色
     */
    private int mTextColor;

    /**
     * 覆盖后颜色
     */
    private int mTextCoverColor;

    public DownloadProgressButton(Context context) {
        this(context,null);
    }

    public DownloadProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        // isInEditMode: 视图处于编辑模式
        if (!isInEditMode()) {
            initAttrs(context,attrs);
        }
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.DownloadProgressButton);
        mBackgroundColor = a.getColor(R.styleable.DownloadProgressButton_backgroud_color, Color.parseColor("#6699ff"));
        mBackgroundSecondColor = a.getColor(R.styleable.DownloadProgressButton_background_second_color,Color.LTGRAY);
        mButtonRadius = a.getFloat(R.styleable.DownloadProgressButton_radius,getMeasuredHeight() / 2);
        mTextColor = a.getColor(R.styleable.DownloadProgressButton_text_color,mBackgroundColor);
        mTextCoverColor = a.getColor(R.styleable.DownloadProgressButton_text_covercolor,Color.WHITE);
        a.recycle();
    }


    public DownloadProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}
