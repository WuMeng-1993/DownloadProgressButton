package com.wumeng.downloadprogressbutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author WuMeng
 * @date 2020/5/3
 * desc:
 */
public class DownloadProgressButton extends androidx.appcompat.widget.AppCompatTextView {

    public interface OnDownLoadClickListener {

        void clickDownload();

        void clickPause();

        void clickResume();

        void clickFinish();

    }

    public static class SimpleOnDownLoadClickListener implements OnDownLoadClickListener {

        @Override
        public void clickDownload() {

        }

        @Override
        public void clickPause() {

        }

        @Override
        public void clickResume() {

        }

        @Override
        public void clickFinish() {

        }
    }

    /**
     * 背景颜色
     */
    private int mBackgroundColor;

    /**
     * 下载中后半部分后面背景颜色
     */
    private int mBackgroundSecondColor;

    /**
     * 背景画笔, 背景边框画笔
     */
    private Paint mBackgroundPaint, mBackgroundBorderPaint;

    /**
     * 按钮的圆角角度
     */
    private float mButtonRadius;

    /**
     * 按钮文字画笔
     */
    private volatile Paint mTextPaint;

    /**
     * 文字颜色,覆盖后文字颜色
     */
    private int mTextColor, mTextCoverColor;

    /**
     * 不同状态下显示的文字
     */
    private String mNormalText, mDowningText, mFinishText, mPauseText;

    /**
     * 边框高度
     */
    private float background_strokeWidth;

    /**
     * progress的最大值，最小值
     */
    private int mMaxProgress, mMinProgress;

    private float mProgress = -1, mToProgress;

    /**
     * 当前的状态
     */
    private int mState = -1;

    /**
     * 按钮的4中状态
     */
    public static final int NORMAL = 1, DOWNLOADING = 2, PAUSE = 3, FINISH = 4;

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

    private RectF mBackgroundBounds;

    private LinearGradient mProgressBgGradient,mProgressTextGradient;

    /**
     * 当前下载的百分比
     */
    private float mProgressPercent;

    private OnDownLoadClickListener mOnDownLoadClickListener;


    public DownloadProgressButton(Context context) {
        this(context, null);
    }

    public DownloadProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        // isInEditMode: 视图处于编辑模式
        if (!isInEditMode()) {
            initAttrs(context, attrs);
            init();
            setupAnimations();
        }
    }

    /**
     * 获取自定义属性
     * @param context
     * @param attrs
     */
    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DownloadProgressButton);
        mBackgroundColor = typedArray.getColor(R.styleable.DownloadProgressButton_backgroud_color, Color.parseColor("#6699ff"));
        mBackgroundSecondColor = typedArray.getColor(R.styleable.DownloadProgressButton_background_second_color, Color.LTGRAY);
        mButtonRadius = typedArray.getFloat(R.styleable.DownloadProgressButton_radius, getMeasuredHeight() / 2);
        mTextColor = typedArray.getColor(R.styleable.DownloadProgressButton_text_color, mBackgroundColor);
        mTextCoverColor = typedArray.getColor(R.styleable.DownloadProgressButton_text_covercolor, Color.WHITE);
        background_strokeWidth = typedArray.getDimension(R.styleable.DownloadProgressButton_background_strokeWidth, 3F);
        mNormalText = typedArray.getString(R.styleable.DownloadProgressButton_text_normal);
        mDowningText = typedArray.getString(R.styleable.DownloadProgressButton_text_downing);
        mPauseText = typedArray.getString(R.styleable.DownloadProgressButton_text_pause);
        mFinishText = typedArray.getString(R.styleable.DownloadProgressButton_text_finish);
        mAnimationDuration = typedArray.getInt(R.styleable.DownloadProgressButton_animation_duration, 500);

        typedArray.recycle();
    }

    @Override
    public void setTextSize(float size) {
        super.setTextSize(size);
        mTextPaint.setTextSize(size);
        invalidate();
    }

    private void init() {
        mMaxProgress = 100;
        mMinProgress = 0;
        mProgress = 0;
        if (mNormalText == null) {
            mNormalText = "下载";
        }

        if (mDowningText == null) {
            mDowningText = "";
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
        setLayerType(LAYER_TYPE_SOFTWARE, mTextPaint);

        // 初始化状态为Normal
        setState(NORMAL);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnDownLoadClickListener == null) {
                    return;
                }

            }
        });

    }

    /**
     *
     */
    private void setupAnimations() {
        mProgressAnimation = ValueAnimator.ofFloat(0, 1).setDuration(mAnimationDuration);
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
     *
     * @param state
     */
    private void setState(int state) {
        // 状态确实有改变
        if (mState != state) {
            this.mState = state;
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
     *
     * @return
     */
    public int getState() {
        return mState;
    }

    /**
     * 设置当前的文本
     *
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
        mToProgress = Math.min(progress, mMaxProgress);
        setState(DOWNLOADING);
        if (mProgressAnimation.isRunning()) {
            mProgressAnimation.end();
            mProgressAnimation.start();
        } else {
            mProgressAnimation.start();
        }
    }

    /**
     * 绘制
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isInEditMode()) {
            drawing(canvas);
        }
    }

    /**
     * 绘制背景和文字
     * @param canvas
     */
    private void drawing(Canvas canvas) {
        drawBackground(canvas);
        drawTextAbove(canvas);
    }

    /**
     * 绘制背景
     * @param canvas
     */
    private void drawBackground(Canvas canvas) {
        if (mBackgroundBounds == null) {
            mBackgroundBounds = new RectF();
            if (mButtonRadius == 0) {
                mButtonRadius = getMeasuredHeight() / 2;
            }
            mBackgroundBounds.left = background_strokeWidth;
            mBackgroundBounds.top = background_strokeWidth;
            mBackgroundBounds.right = getMeasuredWidth() - background_strokeWidth;
            mBackgroundBounds.bottom = getMeasuredHeight() - background_strokeWidth;
        }

        switch (mState) {
            case NORMAL:
                break;
            case DOWNLOADING:
            case PAUSE:
                mProgressPercent = mProgress / (mMaxProgress + 0f);
                mProgressBgGradient = new LinearGradient(background_strokeWidth,0,
                        getMeasuredWidth() - background_strokeWidth, 0,
                        new int[]{mBackgroundColor,mBackgroundSecondColor},
                        new float[]{mProgressPercent,mProgressPercent + 0.001f},
                        Shader.TileMode.CLAMP
                );
                mBackgroundPaint.setColor(mBackgroundColor);
                mBackgroundPaint.setShader(mProgressBgGradient);
                canvas.drawRoundRect(mBackgroundBounds,mButtonRadius,mButtonRadius,mBackgroundPaint);
                break;
            case FINISH:
                mBackgroundPaint.setShader(null);
                mBackgroundPaint.setColor(mBackgroundColor);
                canvas.drawRoundRect(mBackgroundBounds,mButtonRadius,mButtonRadius,mBackgroundPaint);
                break;
            default:
                break;
        }
        // 绘制边框
        canvas.drawRoundRect(mBackgroundBounds,mButtonRadius,mButtonRadius,mBackgroundBorderPaint);
    }

    /**
     * 绘制文本
     * @param canvas
     */
    private void drawTextAbove(Canvas canvas) {
        mTextPaint.setTextSize(getTextSize());
        final float y = canvas.getHeight() / 2 - (mTextPaint.descent() / 2 + mTextPaint.ascent() / 2);
        if (mCurrentText == null) {
            mCurrentText = "";
        }
        final float textWidth = mTextPaint.measureText(mCurrentText.toString());
        switch (mState) {
            case NORMAL:
                mTextPaint.setShader(null);
                mTextPaint.setColor(mTextColor);
                canvas.drawText(mCurrentText.toString(),(getMeasuredWidth() - textWidth)/2,y,mTextPaint);
                break;
            case DOWNLOADING:
            case PAUSE:
                float w = getMeasuredWidth() -2 * background_strokeWidth;
                float coverLength = w * mProgressPercent;
                float indicator1 = w / 2 - textWidth / 2;
                float indicator2 = w / 2 + textWidth / 2;
                float coverTextLength = textWidth / 2 - w / 2 + coverLength;
                float textProgress = coverTextLength / textWidth;
                if (coverLength <= indicator1) {
                    mTextPaint.setShader(null);
                    mTextPaint.setColor(mTextColor);
                } else if (indicator1 < coverLength && coverLength <= indicator2) {
                    mProgressTextGradient = new LinearGradient((w - textWidth) / 2 + background_strokeWidth,0,
                            (w + textWidth) / 2 + background_strokeWidth,0,
                            new int[]{mTextCoverColor,mTextColor},
                            new float[]{textProgress,textProgress + 0.001f},
                            Shader.TileMode.CLAMP);
                    mTextPaint.setColor(mTextColor);
                    mTextPaint.setShader(mProgressTextGradient);
                } else {
                    mTextPaint.setShader(null);
                    mTextPaint.setColor(mTextCoverColor);
                }
                canvas.drawText(mCurrentText.toString(),(w - textWidth) / 2 + background_strokeWidth,y,mTextPaint);
                break;
            case FINISH:
                mTextPaint.setColor(mTextCoverColor);
                canvas.drawText(mCurrentText.toString(),(getMeasuredWidth() - textWidth)/2,y,mTextPaint);
                break;
            default:
                break;
        }
    }

    /**
     * 设置接口实例
     * @param onDownLoadClickListener
     */
    public void setOnDownLoadClickListener(OnDownLoadClickListener onDownLoadClickListener) {
        this.mOnDownLoadClickListener = onDownLoadClickListener;
    }

    /**
     * 获取接口实例
     * @return
     */
    public OnDownLoadClickListener getmOnDownLoadClickListener() {
        return mOnDownLoadClickListener;
    }


}
