package com.merilonstudio.mycoffeecapsulesinventory.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Environment;

import com.merilonstudio.mycoffeecapsulesinventory.R;
import com.merilonstudio.mycoffeecapsulesinventory.models.Capsule;
import com.merilonstudio.mycoffeecapsulesinventory.models.CapsuleType;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CapsuleHelper extends DatabaseHelper {

    private static final String SEPARATOR = ",";

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

    public int deleteCapsule(Capsule capsule) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            return db.delete(TABLE_CAPSULE, COLUMN_CAPSULE_ID + " = ?",
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

    public boolean exportDatabase() {
        /**First of all we check if the external storage of the device is available for writing.
         * Remember that the external storage is not necessarily the sd card. Very often it is
         * the device storage.
         */
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return false;
        } else {
            //We use the Download directory for saving our .csv file.
            File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            File file;
            PrintWriter printWriter = null;
            try {
                if (Build.VERSION.SDK_INT >= 23) {
                    int permissionCheck = context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        return false;
                    }
                }
                SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd", Locale.getDefault());
                String fileName = "capsules_" + format.format(new Date()) + ".csv";
                file = new File(exportDir, fileName);
                file.createNewFile();
                printWriter = new PrintWriter(new FileWriter(file));

                SQLiteDatabase db = this.getReadableDatabase(); //open the database for reading

                Cursor curCSV = db.rawQuery("SELECT " + COLUMN_CAPSULE_NAME + "," + COLUMN_CAPSULE_QTY + "," + COLUMN_CAPSULE_CONSO
                        + ", capsule_type.capsule_type_name as type_name FROM " + TABLE_CAPSULE + ", " + TABLE_CAPSULE_TYPE + " WHERE capsules." + COLUMN_CAPSULE_TYPE + " = capsule_type." + COLUMN_CAPSULE_TYPE_ID + " ORDER BY " + COLUMN_CAPSULE_NAME + " ASC", null);
                //Write the name of the table and the name of the columns (comma separated values) in the .csv file.
                printWriter.println(stripAccents(context.getResources().getString(R.string.colName))
                        + SEPARATOR + stripAccents(context.getResources().getString(R.string.colQty))
                        + SEPARATOR + stripAccents(context.getResources().getString(R.string.colConso))
                        + SEPARATOR + stripAccents(context.getResources().getString(R.string.colType)));
                while (curCSV.moveToNext()) {
                    String name = stripAccents(curCSV.getString(curCSV.getColumnIndex(COLUMN_CAPSULE_NAME)).replace(SEPARATOR, " "));
                    int qty = curCSV.getInt(curCSV.getColumnIndex(COLUMN_CAPSULE_QTY));
                    int conso = curCSV.getInt(curCSV.getColumnIndex(COLUMN_CAPSULE_CONSO));
                    String type_name = stripAccents(curCSV.getString(curCSV.getColumnIndex("type_name")).replace(SEPARATOR, " "));

                    String record = name + SEPARATOR + qty + SEPARATOR + conso + SEPARATOR + type_name;
                    printWriter.println(record);
                }

                curCSV.close();
                db.close();
            } catch (Exception exc) {
                return false;
            } finally {
                if (printWriter != null) printWriter.close();
            }

            return true;
        }
    }

    private String stripAccents(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

}
