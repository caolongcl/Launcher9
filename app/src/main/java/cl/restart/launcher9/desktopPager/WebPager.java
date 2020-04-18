package cl.restart.launcher9.desktopPager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.widget.FrameLayout;

public class WebPager extends WebView {
    private static final String TAG = "WebPager";

    public WebPager(@NonNull Context context) {
        this(context, null, 0);
    }

    public WebPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WebPager(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initView() {

    }
}
