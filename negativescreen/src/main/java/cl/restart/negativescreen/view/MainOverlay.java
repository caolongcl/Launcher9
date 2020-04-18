package cl.restart.negativescreen.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;

import cl.restart.negativescreen.R;

public class MainOverlay extends Dialog {
    public MainOverlay(@NonNull Context context) {
        super(context, R.style.DefaultTheme);
        setCancelable(false);
        setContentView(R.layout.layout_main);

        getWindow().getDecorView().setTranslationX(0);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        dismiss();
    }
}
