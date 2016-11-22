package com.paocorp.mycoffeecapsules.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.paocorp.mycoffeecapsules.models.CapsuleType;

import java.util.ArrayList;

public class CapsuleTypeHelper extends DatabaseHelper {

    public CapsuleTypeHelper(Context context) {
        super(context);
    }

    public ArrayList<CapsuleType> getAllCapsuleTypes() {

        ArrayList<CapsuleType> allCapsuleTypes = new ArrayList<CapsuleType>();

        String selectQuery = "SELECT * FROM " + TABLE_CAPSULE_TYPE + " ORDER BY " + COLUMN_CAPSULE_TYPE_NAME + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                CapsuleType type = new CapsuleType();
                type.setId(c.getInt(c.getColumnIndex(COLUMN_CAPSULE_TYPE_ID)));
                type.setName(c.getString(c.getColumnIndex(COLUMN_CAPSULE_TYPE_NAME)));
                allCapsuleTypes.add(type);
            } while (c.moveToNext());
        }
        c.close();
        return allCapsuleTypes;
    }

    public CapsuleType getCapsuleTypeById(int id) {
        String selectQuery = "SELECT * FROM " + TABLE_CAPSULE_TYPE + " WHERE " + COLUMN_CAPSULE_TYPE_ID + " = " + id;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            CapsuleType type = new CapsuleType();
            type.setId(c.getInt(c.getColumnIndex(COLUMN_CAPSULE_TYPE_ID)));
            type.setName(c.getString(c.getColumnIndex(COLUMN_CAPSULE_TYPE_NAME)));
            c.close();
            return type;
        } else {
            c.close();
            return null;
        }
    }


}
