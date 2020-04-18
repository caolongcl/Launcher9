package cl.restart.negativescreen.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.WindowManager;

public class ParamsWrap implements Parcelable {

    private WindowManager.LayoutParams mLayoutParams;

    public ParamsWrap() {}

    private ParamsWrap(Parcel in) {
        mLayoutParams = in.readParcelable(WindowManager.LayoutParams.class.getClassLoader());
    }

    public WindowManager.LayoutParams getLayoutParams() {
        return mLayoutParams;
    }

    public void setLayoutParams(WindowManager.LayoutParams layoutParams) {
        mLayoutParams = layoutParams;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mLayoutParams, flags);
    }

    public static final Creator<ParamsWrap> CREATOR = new Creator<ParamsWrap>() {
        @Override
        public ParamsWrap createFromParcel(Parcel source) {
            return new ParamsWrap(source);
        }

        @Override
        public ParamsWrap[] newArray(int size) {
            return new ParamsWrap[size];
        }
    };
}
