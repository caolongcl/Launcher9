package cl.restart.launcher9.shortcutBar;

import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Constraints;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import java.util.function.Consumer;

import cl.restart.launcher9.R;
import cl.restart.launcher9.model.AppInfoLoader;
import cl.restart.launcher9.model.IInterface;
import cl.restart.launcher9.model.PagerInfo;
import cl.restart.launcher9.utils.DisplayUtils;
import cl.restart.launcher9.utils.MyEdgeEffectFactory;

public class ShortcutBar extends ConstraintLayout {
    private static final String TAG = "ShortcutBar";

    private AppInfoLoader mAppLoader;
    private WallpaperManager mWallpaperManager;
    private ShortcutBarAdapter mShortcutBarAdapter;

    public ShortcutBar(Context context) {
        this(context, null, 0);
    }

    public ShortcutBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShortcutBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setShortcutBarBlur();
            }
        });
    }

    public void init(AppInfoLoader loader) {
        mAppLoader = loader;
        mWallpaperManager = WallpaperManager.getInstance(getContext());

        RecyclerView recyclerView = new RecyclerView(getContext());

        mShortcutBarAdapter = new ShortcutBarAdapter(getContext());
        mShortcutBarAdapter.setData(mAppLoader.getShortcutPagerInfo());
        mShortcutBarAdapter.setItemTouchHelper(new ItemTouchHelper(new ItemTouchHelperCallback(mShortcutBarAdapter.getData())));
        mShortcutBarAdapter.getItemTouchHelper().attachToRecyclerView(recyclerView);
        mShortcutBarAdapter.setOnEditDesktopListener((IInterface.OnEditDesktopListener) getContext());

        int deltaPadding = mShortcutBarAdapter.otherPadding();
//        recyclerView.setPadding(deltaPadding, 0, deltaPadding, 0);

        ConstraintLayout.LayoutParams params = new Constraints.LayoutParams(
                getContext().getResources().getDimensionPixelOffset(R.dimen.shortcut_bar_width),
                getContext().getResources().getDimensionPixelOffset(R.dimen.shortcut_bar_height));
        params.startToStart = getId();
        params.bottomToBottom = getId();
        params.startToStart = getId();
        params.endToEnd = getId();
        recyclerView.setLayoutParams(params);

        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(manager);
//        recyclerView.setOverScrollMode(OVER_SCROLL_NEVER);
        recyclerView.setEdgeEffectFactory(new MyEdgeEffectFactory());
        recyclerView.setHorizontalFadingEdgeEnabled(true);
        recyclerView.setFadingEdgeLength(DisplayUtils.dip2px(getContext(), 20));
        recyclerView.setAdapter(mShortcutBarAdapter);

        addView(recyclerView);
    }

    public void onWallpaperChanged() {
        setShortcutBarBlur();
    }

    public void onBackPressed() {
        forEachAdapter(ShortcutBarAdapter::stopEditor);
    }

    public void onHomeKeyPressed() {
        forEachAdapter(ShortcutBarAdapter::stopEditor);
    }

    public void onRecentKeyPressed() {
        forEachAdapter(ShortcutBarAdapter::stopEditor);
    }

    private void forEachAdapter(Consumer<ShortcutBarAdapter> consumer) {
        if (mShortcutBarAdapter != null) {
            consumer.accept(mShortcutBarAdapter);
        }
    }

    public void onEditDesktop() {
        mShortcutBarAdapter.startEditor();
    }

    // 模糊shortcut bar
    private void setShortcutBarBlur() {
        WallpaperInfo wallpaperInfo = mWallpaperManager.getWallpaperInfo();
        mWallpaperManager.setWallpaperOffsets(getRootView().getWindowToken(), 0.5f, 0.5f);

        if (wallpaperInfo == null) {
            Drawable wallpaperDrawable = mWallpaperManager.getDrawable();
            Bitmap bitmap = ((BitmapDrawable) wallpaperDrawable).getBitmap();
            DisplayUtils.blur(getContext(), bitmap, this, 100);
        } else {
            Toast.makeText(getContext(), "不支持动态壁纸", Toast.LENGTH_SHORT).show();
        }
    }

    // 图标拖动
    private class ItemTouchHelperCallback extends ItemTouchHelper.Callback {
        private PagerInfo mData;

        public ItemTouchHelperCallback(PagerInfo data) {
            mData = data;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                final int dragFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                final int swipeFlags = 0;
                return makeMovementFlags(dragFlags, swipeFlags);
            }
            return 0;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            RecyclerView.Adapter adapter = recyclerView.getAdapter();

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
