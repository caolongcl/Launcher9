package cl.restart.launcher9.desktopPager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import cl.restart.launcher9.AppHelper;
import cl.restart.launcher9.R;
import cl.restart.launcher9.dao.SDao;
import cl.restart.launcher9.model.AppInfo;
import cl.restart.launcher9.model.IInterface;
import cl.restart.launcher9.model.PagerInfo;
import cl.restart.launcher9.utils.DisplayUtils;
import cl.restart.launcher9.utils.LogUtils;

public class AppPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = AppPagerAdapter.class.getSimpleName();

    private Context mContext;
    private PagerInfo mPagerInfo;
    private ConstraintLayout.LayoutParams mItemParams;
    private ItemTouchHelper mItemTouchHelper;
    private boolean mIsEditorMode;
    private IInterface.OnEditDesktopListener mOnEditDesktopListener;
    private SDao mDao;

    public AppPagerAdapter(Context context) {
        mContext = context;
        mDao = ((AppHelper) context).getDao();
    }

    public void setData(PagerInfo pagerInfo) {
        mPagerInfo = pagerInfo;
    }

    public PagerInfo getData() {
        return mPagerInfo;
    }

    public void setItemTouchHelper(ItemTouchHelper helper) {
        mItemTouchHelper = helper;
    }

    public void onPagerHeightChanged(Context context, int height) {
        int itemHeight = DisplayUtils.dip2px(context, 90.f);
        mItemParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, itemHeight);

        int marginStartEnd = DisplayUtils.dip2px(context, 8.f);
        int totalMargin = height - mDao.getDesktopPagerRows() * itemHeight;
        if (totalMargin > 0) {
            int marginTopBottom = (int) (((float) totalMargin / (float) mDao.getDesktopPagerRows()) / 2);
            mItemParams.setMargins(marginStartEnd, marginTopBottom, marginStartEnd, marginTopBottom);
            LogUtils.d(TAG, "onPagerHeightChanged:" + marginTopBottom);
        }
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_page_item, parent, false);

        if (viewType == AppInfo.APP_TYPE) {
            return new AppViewHolder(view);
        } else {
            return new AppFolderViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((OnItemLayoutChanged) holder).onItemLayoutChanged(position, mItemParams);
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
        private TextView mAppName;
        private Animation mAnimationAlpha;
        private Animation mShakeAnimation;

        AppViewHolder(@NonNull View itemView) {
            super(itemView);
            mRootView = itemView;
            mAppIb = (ImageButton) itemView.findViewById(R.id.app_shortcut);
            mDeleteIb = (ImageButton) itemView.findViewById(R.id.app_delete);
            mAppName = (TextView) itemView.findViewById(R.id.app_name);
            mAnimationAlpha = AnimationUtils.loadAnimation(mAppIb.getContext(), R.anim.shortcut_animation_alpha);
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
            mAppName.setText(info.getAppInfo().loadLabel(mRootView.getContext().getPackageManager()));
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
            if (params != null) {
                mRootView.setLayoutParams(params);
            }
        }
    }

    private static class AppFolderViewHolder extends RecyclerView.ViewHolder implements OnBindViewHolder, OnItemLayoutChanged {
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
