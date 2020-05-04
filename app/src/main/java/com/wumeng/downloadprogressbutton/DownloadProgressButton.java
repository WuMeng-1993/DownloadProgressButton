package com.wumeng.downloadprogressbutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

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

    /**
     * 边框高度
     */
    private float background_strokeWidth;

    /**
     *
     */
    private String mNormalText,mDowningText,mFinishText,mPauseText;


    private int mMaxProgress,mMinProgress;

    private float mProgress = -1,mToProgress;

    /**
     * 背景画笔, 背景边框画笔
     */
    private Paint mBackgroundPaint,mBackgroundBorderPaint;

    /**
     * 按钮文字画笔
     */
    private volatile Paint mTextPaint;

    private int mState = -1;

    /**
     * 4中状态
     */
    public static final int NORMAL = 1,DOWNLOADING = 2,PAUSE = 3,FINISH = 4;

    /**
     * 当前显示的文本
     */
    private CharSequence mCurrentText;

    /**
     * 属性动画
     */
    private ValueAnimator mProgressAnimation;

    /**
     * 动画持续的时间
     */
    private long mAnimationDuration;

    public DownloadProgressButton(Context context) {
        this(context,null);
    }

    public DownloadProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        // isInEditMode: 视图处于编辑模式
        if (!isInEditMode()) {
            initAttrs(context,attrs);
            init();
            setupAnimations();
        }
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.DownloadProgressButton);
        mBackgroundColor = typedArray.getColor(R.styleable.DownloadProgressButton_backgroud_color, Color.parseColor("#6699ff"));
        mBackgroundSecondColor = typedArray.getColor(R.styleable.DownloadProgressButton_background_second_color,Color.LTGRAY);
        mButtonRadius = typedArray.getFloat(R.styleable.DownloadProgressButton_radius,getMeasuredHeight() / 2);
        mTextColor = typedArray.getColor(R.styleable.DownloadProgressButton_text_color,mBackgroundColor);
        mTextCoverColor = typedArray.getColor(R.styleable.DownloadProgressButton_text_covercolor,Color.WHITE);
        background_strokeWidth = typedArray.getDimension(R.styleable.DownloadProgressButton_background_strokeWidth,3F);
        mNormalText = typedArray.getString(R.styleable.DownloadProgressButton_text_normal);
        mDowningText = typedArray.getString(R.styleable.DownloadProgressButton_text_downing);
        mPauseText = typedArray.getString(R.styleable.DownloadProgressButton_text_pause);
        mFinishText = typedArray.getString(R.styleable.DownloadProgressButton_text_finish);
        mAnimationDuration = typedArray.getInt(R.styleable.DownloadProgressButton_animation_duration,500);

        typedArray.recycle();
    }

    private void init() {
        mMaxProgress = 100;
        mMinProgress = 0;
        mProgress = 0;
        if (mNormalText == null) {
            mNormalText = "下载";
        }

        if (mDowningText == null) {
            mNormalText = "";
        }

        if (mPauseText == null) {
            mPauseText = "继续";
        }

        if (mFinishText == null) {
            mFinishText = "使用";
        }

        // 背景画笔
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setStyle(Paint.Style.FILL);

        // 背景边框画笔
        mBackgroundBorderPaint = new Paint();
        mBackgroundBorderPaint.setAntiAlias(true);
        mBackgroundBorderPaint.setStyle(Paint.Style.STROKE);
        mBackgroundBorderPaint.setStrokeWidth(background_strokeWidth);
        mBackgroundBorderPaint.setColor(mBackgroundColor);

        // 按钮文字画笔
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        setLayerType(LAYER_TYPE_SOFTWARE,mTextPaint);

        // 初始化状态为Normal
        setState(NORMAL);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    /**
     *
     */
    private void setupAnimations() {
        mProgressAnimation = ValueAnimator.ofFloat(0,1).setDuration(mAnimationDuration);
        mProgressAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float timePercent = (float) animation.getAnimatedValue();
                mProgress = ((mToProgress - mProgress) * timePercent + mProgress);
                setProgressText(mProgress);
            }
        });
        mProgressAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mProgress == mMaxProgress) {
                    setState(FINISH);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                if (mToProgress < mProgress) {
                    mProgress = mToProgress;
                }
            }
        });
    }

    /**
     * 设置控件状态
     * @param state
     */
    private void setState(int state) {
        // 状态确实有改变
        if (mState != state) {
            switch (state) {
                case NORMAL:
                    mProgress = mToProgress = mMinProgress;
                    setCurrentText(mNormalText);
                    break;
                case PAUSE:
                    setCurrentText(mPauseText);
                    break;
                case FINISH:
                    setCurrentText(mFinishText);
                    mProgress = mMaxProgress;
                    break;
                default:
                    break;
            }
            invalidate();
        }
    }

    /**
     * 获取当前的状态
     * @return
     */
    public int getState() {
        return mState;
    }

    /**
     * 设置当前的文本
     * @param charSequence
     */
    private void setCurrentText(CharSequence charSequence) {
        mCurrentText = charSequence;
        invalidate();
    }

    private void setProgressText(float progress) {
        if (progress <= mMinProgress || progress <= mToProgress || getState() == FINISH) {
            return;
        }
        mToProgress = Math.min(progress,mMaxProgress);
        setState(DOWNLOADING);
        if (mProgressAnimation.isRunning()) {
            mProgressAnimation.end();
            mProgressAnimation.start();
        } else {
            mProgressAnimation.start();
        }
    }

}
