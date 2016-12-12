package com.paocorp.mycoffeecapsules.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.paocorp.mycoffeecapsules.R;

public class DatabaseHelper extends SQLiteOpenHelper {

    protected Context context;
    protected static final int DATABASE_VERSION = 1;
    protected static final String DATABASE_NAME = "dbcaps";

    // Logcat tag
    protected static final String LOG = "DatabaseHelper";

    // TABLES NAMES
    protected static final String TABLE_CAPSULE = "capsules";
    protected static final String TABLE_CAPSULE_TYPE = "capsule_type";

    // CAPSULES COLUMNS NAMES
    protected static final String COLUMN_CAPSULE_ID = "capsules_id";
    protected static final String COLUMN_CAPSULE_NAME = "capsules_name";
    protected static final String COLUMN_CAPSULE_QTY = "capsules_qty";
    protected static final String COLUMN_CAPSULE_IMG = "capsules_img";
    protected static final String COLUMN_CAPSULE_CONSO = "capsules_conso";
    protected static final String COLUMN_CAPSULE_TYPE = "capsules_type";
    protected static final String COLUMN_CAPSULE_NOTIF = "capsules_notif";

    protected static final String COLUMN_CAPSULE_TYPE_ID = "capsule_type_id";
    protected static final String COLUMN_CAPSULE_TYPE_NAME = "capsule_type_name";

    // CAPSULES TABLE CREATE STATEMENT
    private static final String TABLE_CREATE_CAPSULES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_CAPSULE + " (" +
                    COLUMN_CAPSULE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    COLUMN_CAPSULE_NAME + " TEXT, " +
                    COLUMN_CAPSULE_QTY + " INTEGER, " +
                    COLUMN_CAPSULE_IMG + " TEXT, " +
                    COLUMN_CAPSULE_CONSO + " INTEGER DEFAULT 0, " +
                    COLUMN_CAPSULE_TYPE + " INTEGER, " +
                    COLUMN_CAPSULE_NOTIF + " INTEGER)";

    // CAPSULES TYPE TABLE CREATE STATEMENT
    private static final String TABLE_CREATE_CAPSULE_TYPE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_CAPSULE_TYPE + " (" +
                    COLUMN_CAPSULE_TYPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    COLUMN_CAPSULE_TYPE_NAME + " TEXT)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /******************** TEST ENV ONLY ***********************/
        //db.execSQL("DROP TABLE IF EXISTS capsules");
        //db.execSQL("DROP TABLE IF EXISTS capsule_type");
        /******************** TEST ENV ONLY ***********************/
        db.execSQL(TABLE_CREATE_CAPSULE_TYPE);
        db.execSQL(TABLE_CREATE_CAPSULES);
    }

    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void startImportation() {
        SQLiteDatabase db = this.getWritableDatabase();
        /******************** TEST ENV ONLY ***********************/
        //db.execSQL("DROP TABLE IF EXISTS capsules");
        //db.execSQL("DROP TABLE IF EXISTS capsule_type");
        db.execSQL(TABLE_CREATE_CAPSULE_TYPE);
        db.execSQL(TABLE_CREATE_CAPSULES);
        /******************** TEST ENV ONLY ***********************/

        db.execSQL(importCapsules());
        db.execSQL(importCapsulesTypes());
        closeDB();
    }


    private String importCapsules() {
        return "INSERT INTO capsules (capsules_id, capsules_name, capsules_img, capsules_type, capsules_conso, capsules_notif) VALUES \n" +
                "(1, 'Kazaar', 'kazaar', '2', '0', '0')," +
                "(2, 'Ristretto', 'ristretto', '2', '0', '0')," +
                "(3, 'Dharkan', 'dharkan', '2', '0', '0')," +
                "(4, 'Arpeggio', 'arpeggio', '2', '0', '0')," +
                "(5, 'Roma', 'roma', '2', '0', '0')," +
                "(6, 'Livanto', 'livanto', '1', '0', '0')," +
                "(7, 'Capriccio', 'capriccio', '1', '0', '0')," +
                "(8, 'Volluto', 'volluto', '1', '0', '0')," +
                "(9, 'Cosi', 'cosi', '1', '0', '0')," +
                "(10, 'Envivo Lungo', 'envivo_lungo', '3', '0', '0')," +
                "(11, 'Fortissio Lungo', 'fortissio_lungo', '3', '0', '0')," +
                "(12, 'Vivalto Lungo', 'vivalto_lungo', '3', '0', '0')," +
                "(13, 'Linizio Lungo', 'linizio_Lungo', '3', '0', '0')," +
                "(14, 'Arpeggio Decaffeinato', 'arpeggio_decaffeinato', '4', '0', '0')," +
                "(15, 'Vivalto Lungo Decaffeinato', 'vivalto_lungo_decaffeinato', '4', '0', '0')," +
                "(16, 'Indriya from India', 'cap_indriya', '5', '0', '0')," +
                "(17, 'Rosabaya de Columbia', 'cap_rosabaya', '5', '0', '0')," +
                "(18, 'Dulsao do Brasil', 'cap_dulsao', '5', '0', '0')," +
                "(19, 'Bukeela ka Ethiopia', 'cap_bukeela', '5', '0', '0')," +
                "(20, 'Caramelito', 'cap_caramelito', '6', '0', '0')," +
                "(21, 'Ciocattino', 'cap_ciocattino', '6', '0', '0')," +
                "(22, 'Vanilio', 'cap_vanilio', '6', '0', '0')," +
                "(23, 'Variations Linzer Torte', 'cap_linzer_torte', '7', '0', '0')," +
                "(24, 'Variations Sachertorte', 'cap_sachertorte', '7', '0', '0')," +
                "(25, 'Variations Apfelstrudel', 'cap_apfelstrudel', '7', '0', '0')";
    }

    private String importCapsulesTypes() {
        return "INSERT INTO capsule_type (capsule_type_id, capsule_type_name) VALUES \n" +
                "(1, '" + context.getResources().getString(R.string.espresso) + "'),\n" +
                "(2, '" + context.getResources().getString(R.string.intenso) + "'),\n" +
                "(3, '" + context.getResources().getString(R.string.lungo) + "'),\n" +
                "(4, '" + context.getResources().getString(R.string.decaffeinato) + "'),\n" +
                "(5, '" + context.getResources().getString(R.string.pureOrigin) + "'),\n" +
                "(6, '" + context.getResources().getString(R.string.variations) + "'),\n" +
                "(7, '" + context.getResources().getString(R.string.variations_limited) + "'),\n" +
                "(8, '" + context.getResources().getString(R.string.customCapsules) + "')";
    }
}


