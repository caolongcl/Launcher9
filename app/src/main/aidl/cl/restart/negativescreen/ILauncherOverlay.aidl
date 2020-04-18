// ILauncherOverlay.aidl
package cl.restart.negativescreen;

// Declare any non-default types here with import statements
//import android.view.WindowManager.LayoutParams;
import cl.restart.negativescreen.ILauncherOverlayCallback;
import cl.restart.negativescreen.model.ParamsWrap;

interface ILauncherOverlay{
    void closeOverlay(int options);
    void endScroll();
    String getVoiceSearchLanguage();
    boolean isVoiceDetectionRunning();
    void onPause();
    void onResume();
    void onScroll(float progress);
    void openOverlay(int options);
    void requestVoiceDetection(boolean start);
    void startScroll();
    void windowAttached(in ParamsWrap attrs, in ILauncherOverlayCallback callbacks, int options);
    void windowDetached(boolean isChangingConfigurations);
}
