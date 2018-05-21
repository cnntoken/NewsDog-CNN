package com.newsdog.facebook;

/**
 * Created by newsdog on 6/12/16.
 */

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * 用户类
 */
public class User implements Parcelable {
    public String id = "";
    public String name = "";
    public String portraitUrl = "";
    public int gender;
    public int age;
    /**
     * 用户状态
     */
    public int status;
    /**
     * 用户token
     */
    public String token = "";
    /**
     * 用户来源,例如source为device则表示使用设备注册的用户,fb为facebook登录用户
     */
    public String source = "";
    public boolean hasRegistered = false;

    public User() {
    }


    protected User(Parcel in) {
        name = in.readString();
        portraitUrl = in.readString();
        id = in.readString();
        gender = in.readInt();
        age = in.readInt();
        status = in.readInt();
        token = in.readString();
        source = in.readString();
    }


    public boolean isValid() {
        return !TextUtils.isEmpty(id) && !TextUtils.isEmpty(name) ;
    }

    @Override
    public String toString() {
        return "User{" + "status=" + status + ", id='" + id + '\'' + ", name='" + name + '\'' + ", portraitUrl='" + portraitUrl + '\'' + ", gender=" + gender + ", token='" + token + '\'' + ", source='" + source + '\'' + '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(portraitUrl);
        dest.writeString(id);
        dest.writeInt(gender);
        dest.writeInt(age);
        dest.writeInt(status);
        dest.writeString(token);
        dest.writeString(source);
    }


    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

}
