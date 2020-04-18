package cl.restart.launcher9.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.widget.EdgeEffect;

public class MyEdgeEffectFactory extends RecyclerView.EdgeEffectFactory {
    @NonNull
    @Override
    protected EdgeEffect createEdgeEffect(@NonNull RecyclerView view, int direction) {
        return new MyEdgeEffect(view.getContext());
    }

    public class MyEdgeEffect extends EdgeEffect {

        /**
         * Construct a new EdgeEffect with a theme appropriate for the provided context.
         *
         * @param context Context used to provide theming and resource information for the EdgeEffect
         */
        public MyEdgeEffect(Context context) {
            super(context);
        }

        @Override
        public boolean draw(Canvas canvas) {
            return true;
        }
    }
}
