package com.lee.drawlayoutsample;

import android.os.Parcel;

import java.io.Serializable;

/**
 * Created by Lee on 2016/11/4.
 */

public class EditTextInfo extends ViewInfo implements Serializable {

    public EditTextInfo(int id, float degree) {
        super(id, degree);
    }

    public String text;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.text);
    }

    protected EditTextInfo(Parcel in) {
        super(in);
        this.text = in.readString();
    }

    public static final Creator<EditTextInfo> CREATOR = new Creator<EditTextInfo>() {
        @Override
        public EditTextInfo createFromParcel(Parcel source) {
            return new EditTextInfo(source);
        }

        @Override
        public EditTextInfo[] newArray(int size) {
            return new EditTextInfo[size];
        }
    };
}
