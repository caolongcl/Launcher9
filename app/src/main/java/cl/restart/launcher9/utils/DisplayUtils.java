package cl.restart.launcher9.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.view.View;

public class DisplayUtils {
    private static Bitmap mBlurDst;

    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static int getDisplayWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getDisplayHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen",
                "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getNavigationBarHeight(Context context) {
        int resourceId = 0;
        int rid = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        if (rid != 0) {
            resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            return context.getResources().getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }

    private static Bitmap getDstArea(Context context, Bitmap bitmap, View view) {
        int width = getDisplayWidth(context);
        int height = getDisplayHeight(context);
        LogUtils.d("display size", "w:" + width + ", h:" + height);
        float screenRatio = (float) width / (float) height;
        float bitmapRatio = (float) bitmap.getWidth() / (float) bitmap.getHeight();

        Bitmap dstArea = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dstArea);

        float scaleW;
        float scaleH;

        float deltaW;
        float deltaH;
        if (bitmapRatio >= screenRatio) {
            scaleH = (float) height / (float) bitmap.getHeight();
            deltaW = ((float) width - (float) bitmap.getWidth()) / 2;
//            canvas.scale(scaleH, scaleH);
            canvas.translate(-view.getLeft() + deltaW, -view.getTop());
            LogUtils.e("dst", "-left:" + (-view.getLeft()) + ", deltaW:" + deltaW);
        } else {
            scaleW = (float) width / (float) bitmap.getWidth();
            deltaH = ((float) height - (float) bitmap.getHeight() * scaleW) / 2;
            canvas.scale(scaleW, scaleW);
            canvas.translate(-view.getLeft(), -view.getTop() + deltaH);
            LogUtils.e("dst", "-top:" + (-view.getTop()) + ", deltaH:" + deltaH);
        }
        canvas.drawBitmap(bitmap, 0, 0, null);
        return dstArea;
    }

    private static float[] radiusArray = {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};

    private static void setRadius(float leftTop, float rightTop, float rightBottom, float leftBottom) {
        radiusArray[0] = leftTop;
        radiusArray[1] = leftTop;
        radiusArray[2] = rightTop;
        radiusArray[3] = rightTop;
        radiusArray[4] = rightBottom;
        radiusArray[5] = rightBottom;
        radiusArray[6] = leftBottom;
        radiusArray[7] = leftBottom;
    }

    private static Bitmap getRoundDstArea(@NonNull Context context, @NonNull Bitmap bitmap, View view) {
        Bitmap dstArea = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Path path = new Path();
        Paint paint = new Paint();
        RectF rectF = new RectF(0, 0, dstArea.getWidth(), dstArea.getHeight());
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        Canvas canvas = new Canvas(dstArea);
        float radius = dip2px(context, 34.f);
        setRadius(radius, radius, radius, radius);
        path.addRoundRect(rectF, radiusArray, Path.Direction.CW);
        canvas.drawRoundRect(rectF, radius, radius, paint);
        canvas.clipPath(path);
        canvas.translate(-view.getLeft(), -view.getTop());

        canvas.drawBitmap(bitmap, 0, 0, null);
        return dstArea;
    }

    public static Bitmap getRoundDstAreaAfterBlur(@NonNull Context context, @NonNull Bitmap bitmap) {
        Bitmap dstArea = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        RectF rectF = new RectF(0, 0, dstArea.getWidth(), dstArea.getHeight());

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setAlpha(32);
        paint.setAntiAlias(true);

        float radius = dip2px(context, 34.f);
        setRadius(radius, radius, radius, radius);

        Path path = new Path();
        path.addRoundRect(rectF, radiusArray, Path.Direction.CW);

        Canvas canvas = new Canvas(dstArea);
        canvas.clipPath(path);
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.drawRoundRect(rectF, radius, radius, paint);
        return dstArea;
    }

    public static void blur(Context context, Bitmap bitmap, View view, float radius) {
        Bitmap dstArea = getDstArea(context, bitmap, view);
        Bitmap dstBlur = blurByGauss(dstArea, (int) radius);
        if (mBlurDst != null && !mBlurDst.isRecycled()) {
            mBlurDst.recycle();
        }
        mBlurDst = getRoundDstAreaAfterBlur(context, dstBlur);

        view.setBackground(new BitmapDrawable(context.getResources(), mBlurDst));

        dstBlur.recycle();
        dstArea.recycle();
//        bitmap.recycle();
    }

    private static Bitmap zoomImage(Bitmap srcBitmap, float scale) {
        float width = srcBitmap.getWidth();
        float height = srcBitmap.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 缩放图片动作
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(srcBitmap, 0, 0, (int) width, (int) height, matrix, true);
    }

    private static Bitmap blurByGauss(Bitmap srcBitmap, int radius) {

        Bitmap bitmap = srcBitmap.copy(srcBitmap.getConfig(), true);

        if (radius < 1) {
            radius = 5;
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int temp = 256 * divsum;
        int dv[] = new int[temp];
        for (i = 0; i < temp; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        return bitmap;
    }
}
