package com.paocorp.mycoffeecapsules.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.paocorp.mycoffeecapsules.models.Capsule;
import com.paocorp.mycoffeecapsules.models.CapsuleType;

import java.util.ArrayList;
import java.util.HashMap;

public class CapsuleHelper extends DatabaseHelper {

    public CapsuleHelper(Context context) {
        super(context);
    }

    public ArrayList<Capsule> getAllCapsulesByType(int type) {

        ArrayList<Capsule> allCapsules = new ArrayList<Capsule>();

        String selectQuery = "SELECT * FROM " + TABLE_CAPSULE + " WHERE " + COLUMN_CAPSULE_TYPE + " = " + type + " ORDER BY " + COLUMN_CAPSULE_NAME + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Capsule capsule = new Capsule();
                capsule.setId(c.getInt(c.getColumnIndex(COLUMN_CAPSULE_ID)));
                capsule.setName(c.getString(c.getColumnIndex(COLUMN_CAPSULE_NAME)));
                capsule.setQty(c.getInt(c.getColumnIndex(COLUMN_CAPSULE_QTY)));
                capsule.setImg(c.getString(c.getColumnIndex(COLUMN_CAPSULE_IMG)));
                capsule.setConso(c.getInt(c.getColumnIndex(COLUMN_CAPSULE_CONSO)));
                capsule.setNotif(c.getInt(c.getColumnIndex(COLUMN_CAPSULE_NOTIF)));
                capsule.setType(c.getInt(c.getColumnIndex(COLUMN_CAPSULE_TYPE)));
                allCapsules.add(capsule);
            } while (c.moveToNext());
        }
        c.close();
        return allCapsules;
    }

    public Capsule getCapsuleById(int id) {
        String selectQuery = "SELECT * FROM " + TABLE_CAPSULE + " WHERE " + COLUMN_CAPSULE_ID + " = " + id;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            Capsule capsule = new Capsule();
            capsule.setId(c.getInt(c.getColumnIndex(COLUMN_CAPSULE_ID)));
            capsule.setName(c.getString(c.getColumnIndex(COLUMN_CAPSULE_NAME)));
            capsule.setQty(c.getInt(c.getColumnIndex(COLUMN_CAPSULE_QTY)));
            capsule.setImg(c.getString(c.getColumnIndex(COLUMN_CAPSULE_IMG)));
            capsule.setConso(c.getInt(c.getColumnIndex(COLUMN_CAPSULE_CONSO)));
            capsule.setNotif(c.getInt(c.getColumnIndex(COLUMN_CAPSULE_NOTIF)));
            capsule.setType(c.getInt(c.getColumnIndex(COLUMN_CAPSULE_TYPE)));
            c.close();
            return capsule;
        }
        return null;
    }

    public int updateCapsule(Capsule capsule) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CAPSULE_QTY, capsule.getQty());
        values.put(COLUMN_CAPSULE_CONSO, capsule.getConso());
        values.put(COLUMN_CAPSULE_NOTIF, capsule.getNotif());

        try {
            return db.update(TABLE_CAPSULE, values, COLUMN_CAPSULE_ID + " = ?",
                    new String[]{String.valueOf(capsule.getId())});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long insertCustomCapsule(Capsule capsule) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CAPSULE_NAME, capsule.getName());
        values.put(COLUMN_CAPSULE_IMG, capsule.getImg());
        values.put(COLUMN_CAPSULE_QTY, capsule.getQty());
        values.put(COLUMN_CAPSULE_CONSO, capsule.getConso());
        values.put(COLUMN_CAPSULE_NOTIF, capsule.getNotif());
        values.put(COLUMN_CAPSULE_TYPE, "8");

        try {
            if (!capsule.getName().trim().isEmpty()) {
                return db.insert(TABLE_CAPSULE, null, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public ArrayList<Capsule> getSearchCapsules(CapsuleType type, String query) {
        String selectQuery = "SELECT * FROM " + TABLE_CAPSULE + " WHERE " + COLUMN_CAPSULE_TYPE + " = " + type.getId() + " AND LOWER(" + COLUMN_CAPSULE_NAME + ") LIKE '%" + query.toLowerCase() + "%'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        ArrayList<Capsule> allCapsules = new ArrayList<Capsule>();

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Capsule capsule = new Capsule();
                capsule.setId(c.getInt(c.getColumnIndex(COLUMN_CAPSULE_ID)));
                capsule.setName(c.getString(c.getColumnIndex(COLUMN_CAPSULE_NAME)));
                capsule.setQty(c.getInt(c.getColumnIndex(COLUMN_CAPSULE_QTY)));
                capsule.setImg(c.getString(c.getColumnIndex(COLUMN_CAPSULE_IMG)));
                capsule.setConso(c.getInt(c.getColumnIndex(COLUMN_CAPSULE_CONSO)));
                capsule.setNotif(c.getInt(c.getColumnIndex(COLUMN_CAPSULE_NOTIF)));
                capsule.setType(c.getInt(c.getColumnIndex(COLUMN_CAPSULE_TYPE)));
                allCapsules.add(capsule);
            } while (c.moveToNext());
        }
        c.close();

        return allCapsules;
    }


}
