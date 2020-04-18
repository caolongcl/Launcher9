package cl.restart.launcher9.desktopPager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import cl.restart.launcher9.AppHelper;
import cl.restart.launcher9.dao.SDao;
import cl.restart.launcher9.model.AppInfoLoader;
import cl.restart.launcher9.model.IInterface;
import cl.restart.launcher9.model.PagerInfo;
import cl.restart.launcher9.utils.DisplayUtils;
import cl.restart.launcher9.utils.LogUtils;

public class DesktopPager extends ViewPager {
    private static final String TAG = DesktopPager.class.getSimpleName();

    private List<View> mPagerList = new ArrayList<>();
    private AppInfoLoader mAppLoader;
    private List<OnDesktopChangeListener> mOnDesktopChangeListeners = new ArrayList<>();
    private DesktopAdapter mDesktopAdapter = new DesktopAdapter();
    private int mCurHeight;
    private SDao mDao;

    public DesktopPager(@NonNull Context context) {
        this(context, null);
    }

    public DesktopPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mDao = ((AppHelper) context).getDao();
        getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (mCurHeight == 0) {
                mCurHeight = getHeight();
                relayoutPager(context, mCurHeight);
            }

            if (mCurHeight != getHeight()) {
                mCurHeight = getHeight();
                relayoutPager(context, mCurHeight);
            }
        });
    }

    private void relayoutPager(Context context, int height) {
        LogUtils.d(TAG, "height:" + DisplayUtils.px2dip(context, height));
        RecyclerView recyclerView;
        AppPagerAdapter adapter;
        for (View view : mPagerList) {
            recyclerView = (RecyclerView) view;
            adapter = (AppPagerAdapter) recyclerView.getAdapter();
            if (adapter != null) {
                int itemHeight = DisplayUtils.dip2px(context, 90.f);
                int totalMargin = height - mDao.getDesktopPagerRows() * itemHeight;
                if (totalMargin > 0) {
                    int marginTopBottom = (int) (((float) totalMargin / (float) mDao.getDesktopPagerRows()) / 2);
                    LogUtils.d(TAG, "onPagerHeightChanged:" + marginTopBottom);
                    int marginStartEnd = DisplayUtils.dip2px(context, 8.f);
                    adapter.onPagerHeightChanged(context, height);
                    adapter.notifyDataSetChanged();
                }
            }

        }
    }

    public void init(AppInfoLoader loader) {
        if (loader == null) {
            return;
        }
        mAppLoader = loader;

        View view;
        RecyclerView recyclerView;
        AppPagerAdapter adapter;
        int pagerNum = mAppLoader.getPagerNum();
        mPagerList.clear();
        LogUtils.d(TAG, "page num:" + pagerNum);
        for (int i = 0; i < pagerNum; i++) {
            recyclerView = createPageView();

            adapter = new AppPagerAdapter(getContext());
            adapter.setData(mAppLoader.getPagerInfo(i));

            ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelperCallback(adapter.getData()));
            helper.attachToRecyclerView(recyclerView);
            adapter.setItemTouchHelper(helper);
            adapter.setOnEditDesktopListener((IInterface.OnEditDesktopListener) getContext());

            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), mDao.getDesktopPagerColumns()));
            recyclerView.setAdapter(adapter);

            mPagerList.add(recyclerView);
        }

        mDesktopAdapter.setViewList(mPagerList);
        setAdapter(mDesktopAdapter);

        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mOnDesktopChangeListeners.forEach((listener) -> {
                    if (listener != null) {
                        listener.onDesktopScrolled(position, positionOffset, positionOffsetPixels);
                    }
                });
            }

            @Override
            public void onPageSelected(int position) {
                mOnDesktopChangeListeners.forEach((listener) -> {
                    if (listener != null) {
                        listener.onDesktopChanged(position);
                    }
                });
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                mOnDesktopChangeListeners.forEach((listener) -> {
                    if (listener != null) {
                        listener.onDesktopScrollStateChanged(state);
                    }
                });
            }
        });

        setOffscreenPageLimit(1);
//        setCurrentItem(1);
    }

    public void selectPager(int index) {
        if (index >= 0 && index < mPagerList.size()) {
            setCurrentItem(index);
        }
    }

    public void onBackPressed() {
        forEachAdapter(AppPagerAdapter::stopEditor);
    }

    public void onHomeKeyPressed() {
        forEachAdapter(AppPagerAdapter::stopEditor);
    }

    public void onRecentKeyPressed() {
        forEachAdapter(AppPagerAdapter::stopEditor);
    }

    public void onEditDesktop() {
        forEachAdapter(AppPagerAdapter::startEditor);
    }

    private void forEachAdapter(Consumer<AppPagerAdapter> consumer) {
        mPagerList.forEach(v -> {
            if (v != null) {
                RecyclerView recyclerView = (RecyclerView) v;
                AppPagerAdapter adapter = (AppPagerAdapter) recyclerView.getAdapter();
                if (adapter != null) {
                    consumer.accept(adapter);
                }
            }
        });
    }

    public interface OnDesktopChangeListener {
        void onDesktopScrolled(int position, float positionOffset, int positionOffsetPixels);
        void onDesktopChanged(int index);
        void onDesktopScrollStateChanged(int state);
    }

    public void addOnDesktopChangeListener(OnDesktopChangeListener listener) {
        mOnDesktopChangeListeners.add(listener);
    }

    public void removeOnDesktopChangeListener(OnDesktopChangeListener listener) {
        mOnDesktopChangeListeners.remove(listener);
    }

    // 创建pager
    private RecyclerView createPageView() {
        RecyclerView recyclerView = new RecyclerView(getContext());
        LayoutParams params = new LayoutParams();
        recyclerView.setLayoutParams(params);
        return recyclerView;
    }

    private class ItemTouchHelperCallback extends ItemTouchHelper.Callback {
        private PagerInfo mData;

        public ItemTouchHelperCallback(PagerInfo data) {
            mData = data;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                final int swipeFlags = 0;
                return makeMovementFlags(dragFlags, swipeFlags);
            }
            return 0;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    mData.swap(i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    mData.swap(i, i - 1);
                }
            }

            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            if (adapter != null) {
                adapter.notifyItemMoved(fromPosition, toPosition);
            }

            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }

        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }
    }
}
