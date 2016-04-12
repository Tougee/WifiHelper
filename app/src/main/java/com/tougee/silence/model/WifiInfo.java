package com.tougee.silence.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import static com.tougee.silence.database.SilenceDataBase.NAME;

public class WifiInfo implements Parcelable {

    public String mName;

    public WifiInfo(String name) {
        mName = name;
    }

    public WifiInfo(Cursor c) {
        mName = c.getString(c.getColumnIndex(NAME));
    }

    protected WifiInfo(Parcel in) {
        mName = in.readString();
    }

    public static final Creator<WifiInfo> CREATOR = new Creator<WifiInfo>() {
        @Override
        public WifiInfo createFromParcel(Parcel in) {
            return new WifiInfo(in);
        }

        @Override
        public WifiInfo[] newArray(int size) {
            return new WifiInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
    }
}
