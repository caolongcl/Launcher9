package cl.restart.launcher9.pagerIndicator;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import cl.restart.launcher9.R;
import cl.restart.launcher9.model.AppInfoLoader;
import cl.restart.launcher9.utils.LogUtils;

public class PagerIndicator extends LinearLayout {
    private static final String TAG = PagerIndicator.class.getSimpleName();

    private AppInfoLoader mAppLoader;
    private int mCurIndicatorNum;
    private int mPreIndicatorNum;
    private List<ImageView> mIndicators = new ArrayList<>();
    private int mIndicatorSize;
    private int mCurCheckedIndex;
    private int mPreCheckedIndex;

    public PagerIndicator(Context context) {
        this(context, null, 0);
    }

    public PagerIndicator(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagerIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mIndicatorSize = (int) ((float) getHeight() / 2);

                layoutIndicator();
                LogUtils.d(TAG, "PagerIndicator OnGlobalLayoutListener:" + mIndicatorSize);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void init(AppInfoLoader loader) {
        mAppLoader = loader;
        update();
        check(0);
    }

    public void update() {
        if (mAppLoader != null) {
            if (mAppLoader.getPagerNum() <= 1) {
                mCurIndicatorNum = 1;
            } else {
                mCurIndicatorNum = mAppLoader.getPagerNum();
            }
            layoutIndicator();
        }
    }

    public void check(int index) {
        if (index == mCurCheckedIndex) {
            return;
        }

        mPreCheckedIndex = mCurCheckedIndex;
        mCurCheckedIndex = index;

        ImageView preImageView = mIndicators.get(mPreCheckedIndex);
        ImageView curImageView = mIndicators.get(mCurCheckedIndex);
        if (preImageView != null) {
            preImageView.setBackgroundResource(R.drawable.pager_indicator_normal);
        }

        if (curImageView != null) {
            curImageView.setBackgroundResource(R.drawable.pager_indicator_selected);
        }
    }

    private void layoutIndicator() {
        if (mIndicatorSize == 0) {
            return;
        }

        if (mCurIndicatorNum == mPreIndicatorNum) {
            return;
        }

        LogUtils.d(TAG, "layoutIndicator");

        mIndicators.clear();
        removeAllViews();

        for (int i = 0; i < mCurIndicatorNum; i++) {
            ImageView imageView = new ImageView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mIndicatorSize, mIndicatorSize);
            params.setMarginStart(mIndicatorSize / 2);
            params.setMarginEnd(mIndicatorSize / 2);
            imageView.setLayoutParams(params);
            if (mCurCheckedIndex == i) {
                imageView.setBackgroundResource(R.drawable.pager_indicator_selected);
            } else {
                imageView.setBackgroundResource(R.drawable.pager_indicator_normal);
            }

            mIndicators.add(imageView);
            addView(imageView);
        }

        mPreIndicatorNum = mCurIndicatorNum;
    }

    public boolean isMostLeft() {
        return mCurCheckedIndex == 0;
    }
}
