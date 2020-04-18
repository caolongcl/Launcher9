// ILauncherOverlayCallback.aidl
package cl.restart.negativescreen;

// Declare any non-default types here with import statements

interface ILauncherOverlayCallback {
    void overlayScrollChanged(float progress);
    void overlayStatusChanged(int status);
}
