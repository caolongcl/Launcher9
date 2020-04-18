package cl.restart.launcher9.shortcutBar;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import cl.restart.launcher9.AppHelper;
import cl.restart.launcher9.R;
import cl.restart.launcher9.dao.SDao;
import cl.restart.launcher9.model.AppInfo;
import cl.restart.launcher9.model.IInterface;
import cl.restart.launcher9.model.PagerInfo;
import cl.restart.launcher9.utils.DisplayUtils;
import cl.restart.launcher9.utils.LogUtils;

public class ShortcutBarAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = ShortcutBarAdapter.class.getSimpleName();

    private Context mContext;
    private PagerInfo mPagerInfo;
    private ItemTouchHelper mItemTouchHelper;
    private int mMarginStart;
    private int mMarginTop;
    private int mMarginEnd;
    private int mMarginBottom;
    private int mDeltaMargin;
    private boolean mIsEditorMode;
    private SDao mDao;

    private IInterface.OnEditDesktopListener mOnEditDesktopListener;

    public ShortcutBarAdapter(Context context) {
        mContext = context;
        mDao = ((AppHelper) context).getDao();
    }

    public void setData(PagerInfo pagerInfo) {
        mPagerInfo = pagerInfo;
    }

    public PagerInfo getData() {
        return mPagerInfo;
    }

    public int otherPadding() {
        if (mPagerInfo != null) {
            mMarginStart = mMarginEnd = DisplayUtils.dip2px(mContext, 13f);
            mMarginTop = mMarginBottom = DisplayUtils.dip2px(mContext, 13f);

            int width = mContext.getResources().getDimensionPixelOffset(R.dimen.shortcut_bar_width);
            int itemWidth = mContext.getResources().getDimensionPixelSize(R.dimen.shortcut_bar_item_width);
            LogUtils.d(TAG, "w:" + DisplayUtils.px2dip(mContext, width));

            int delta;
            if (mPagerInfo.size() >= mDao.getShortcutBarVisibleColumns()) {
                delta = width - mDao.getShortcutBarVisibleColumns() * itemWidth;
                mMarginStart = mMarginEnd = (int) ((float) delta / mDao.getShortcutBarVisibleColumns() / 2);
                mDeltaMargin = 0;
                return mDeltaMargin;
            } else {
                delta = width - mPagerInfo.size() * (mMarginStart + mMarginEnd + itemWidth);
                mDeltaMargin = (int) ((float) delta / 2);
                return mDeltaMargin;
            }
        } else {
            return 0;
        }
    }

    public void setItemTouchHelper(ItemTouchHelper helper) {
        mItemTouchHelper = helper;
    }

    public ItemTouchHelper getItemTouchHelper() {
        return mItemTouchHelper;
    }

    public void setOnEditDesktopListener(IInterface.OnEditDesktopListener listener) {
        mOnEditDesktopListener = listener;
    }

    public void startEditor() {
        if (!mIsEditorMode) {
            mIsEditorMode = true;
            notifyDataSetChanged();
        }
    }

    public void stopEditor() {
        if (mIsEditorMode) {
            mIsEditorMode = false;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_shortcutbar_item, parent, false);

        if (viewType == AppInfo.APP_TYPE) {
            return new AppViewHolder(view);
        } else {
            return new AppFolderViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((OnItemLayoutChanged) holder).onItemLayoutChanged(position, null);
        ((OnBindViewHolder) holder).onBindViewHolder(position);
    }

    @Override
    public int getItemCount() {
        return mPagerInfo != null ? mPagerInfo.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return mPagerInfo != null ? mPagerInfo.get(position).getType() : -1;
    }

    private class AppViewHolder extends RecyclerView.ViewHolder implements OnBindViewHolder, OnItemLayoutChanged {
        private static final String TAG = "AppViewHolder";
        private View mRootView;
        private ImageButton mAppIb;
        private ImageButton mDeleteIb;
        private Animation mAnimationAlpha;
        private Animation mShakeAnimation;

        AppViewHolder(@NonNull View itemView) {
            super(itemView);
            mRootView = itemView;
            mAppIb = (ImageButton) itemView.findViewById(R.id.shortcut_bar_item);
            mDeleteIb = (ImageButton) itemView.findViewById(R.id.short_bar_app_delete);
            mAnimationAlpha = AnimationUtils.loadAnimation(mContext, R.anim.shortcut_animation_alpha);
            mShakeAnimation = AnimationUtils.loadAnimation(mContext, R.anim.shortcut_shake);
        }

        @Override
        public void onBindViewHolder(int position) {
            if (mPagerInfo == null) {
                return;
            }

            AppInfo info = (AppInfo) mPagerInfo.get(position);
            if (info == null) {
                return;
            }

            mAppIb.setImageDrawable(info.getAppInfo().activityInfo.loadIcon(mRootView.getContext().getPackageManager()));
            mAppIb.setOnTouchListener((View v, MotionEvent event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.startAnimation(mAnimationAlpha);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return false;
            });

            mAppIb.setOnClickListener(v -> {
                if (!mIsEditorMode) {
                    ResolveInfo resolveInfo = info.getAppInfo();
                    Intent intent = v.getContext().getPackageManager().getLaunchIntentForPackage(resolveInfo.activityInfo.packageName);
                    if (intent != null) {
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ((AppHelper) mContext).startApp(intent);
                    }
                }
            });

            mAppIb.setOnLongClickListener(v -> {
                if (!mIsEditorMode) {
                    if (mItemTouchHelper != null) {
                        mItemTouchHelper.startDrag(this);
                    }
                    if (mOnEditDesktopListener != null) {
                        mOnEditDesktopListener.editDesktop();
                    }
                }
                return true;
            });

            if (mIsEditorMode) {
//                mRootView.startAnimation(mShakeAnimation);
                mDeleteIb.setVisibility(View.VISIBLE);
            } else {
//                mRootView.clearAnimation();
                mDeleteIb.setVisibility(View.GONE);
            }
        }

        @Override
        public void onItemLayoutChanged(int position, ViewGroup.LayoutParams params) {
            LogUtils.d(TAG, "pos:" + position);
            RecyclerView.LayoutParams params1 = (RecyclerView.LayoutParams) mRootView.getLayoutParams();
            if (mPagerInfo.size() >= mDao.getShortcutBarVisibleColumns()) {
                params1.setMargins(mMarginStart, mMarginTop, mMarginEnd, mMarginBottom);
            } else {
                if (position == 0) {
                    params1.setMargins(mMarginStart + mDeltaMargin, mMarginTop, mMarginEnd, mMarginBottom);
                } else if (position == mPagerInfo.size() - 1) {
                    params1.setMargins(mMarginStart, mMarginTop, mMarginEnd + mDeltaMargin, mMarginBottom);
                } else {
                    params1.setMargins(mMarginStart, mMarginTop, mMarginEnd, mMarginBottom);
                }
            }
        }
    }

    private class AppFolderViewHolder extends RecyclerView.ViewHolder implements OnBindViewHolder, OnItemLayoutChanged {
        AppFolderViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void onBindViewHolder(int position) {

        }

        @Override
        public void onItemLayoutChanged(int position, ViewGroup.LayoutParams params) {

        }
    }

    @FunctionalInterface
    interface OnBindViewHolder {
        void onBindViewHolder(int position);
    }

    @FunctionalInterface
    interface OnItemLayoutChanged {
        void onItemLayoutChanged(int position, ViewGroup.LayoutParams params);
    }
}
