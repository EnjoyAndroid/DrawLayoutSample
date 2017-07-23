package com.lee.drawlayoutsample;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Lee on 2016/11/4.
 */

public class ViewInfo implements Serializable, Parcelable {

    public ViewInfo(int id, float degree) {
        this.id = id;
        this.degree = degree;
    }


    public static final int TYPE_IMAGEVIEW = 0;
    public static final int TYPE_EDITTEXT = 1;

    public int id;
    public float degree;
    public int width;
    public int height;
    public float x;
    public float y;
    //0 imageView 1 EditText
    public int type;
    public int color;
    public int realId;

//    @Override
//    public boolean equals(Object o) {
//        if (o == null) {
//            return false;
//        }
//        if (o instanceof ViewInfo) {
//            ViewInfo info = (ViewInfo) o;
//            if (info.id == id && info.degree == degree && info.width == width && info.height == height
//                    && info.x == x && info.y == y && info.type == type && info.color == color) {
//                return true;
//            }
//        }
//        return false;
//    }

//    @Override
//    public int hashCode() {
//        return Objects.hash(id, degree, width, height, x, y, type, color);
//    }

    public static final Creator<ViewInfo> CREATOR = new Creator<ViewInfo>() {
        @Override
        public ViewInfo createFromParcel(Parcel in) {
            return new ViewInfo(in);
        }

        @Override
        public ViewInfo[] newArray(int size) {
            return new ViewInfo[size];
        }
    };

    @Override
    public String toString() {
        return "ViewInfo{" +
                "id=" + id +
                ", degree=" + degree +
                ", width=" + width +
                ", height=" + height +
                ", x=" + x +
                ", y=" + y +
                ", type=" + type +
                ", color=" + color +
                ", realId=" + realId +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeFloat(this.degree);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeFloat(this.x);
        dest.writeFloat(this.y);
        dest.writeInt(this.type);
        dest.writeInt(this.color);
    }

    protected ViewInfo(Parcel in) {
        this.id = in.readInt();
        this.degree = in.readFloat();
        this.width = in.readInt();
        this.height = in.readInt();
        this.x = in.readFloat();
        this.y = in.readFloat();
        this.type = in.readInt();
        this.color = in.readInt();
    }

}
