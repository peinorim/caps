package com.paocorp.mycoffeecapsules.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Capsule implements Parcelable {

    private int id;
    private String name;
    private int qty;
    private String img;
    private int type;
    private int conso;
    private int notif;

    public Capsule() {
    }

    public int getConso() {
        return conso;
    }

    public void setConso(int conso) {
        this.conso = conso;
    }

    public boolean getNotif() {
        return notif == 1;
    }

    public void setNotif(int notif) {
        this.notif = notif;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    protected Capsule(Parcel in) {
        id = in.readInt();
        name = in.readString();
        qty = in.readInt();
        img = in.readString();
        type = in.readInt();
        conso = in.readInt();
        notif = in.readInt();
    }

    public static final Creator<Capsule> CREATOR = new Creator<Capsule>() {
        @Override
        public Capsule createFromParcel(Parcel in) {
            return new Capsule(in);
        }

        @Override
        public Capsule[] newArray(int size) {
            return new Capsule[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeInt(qty);
        parcel.writeString(img);
        parcel.writeInt(type);
        parcel.writeInt(conso);
        parcel.writeInt(notif);
    }
}
