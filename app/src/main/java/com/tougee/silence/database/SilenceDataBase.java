package com.tougee.silence.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SilenceDataBase extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "silence";

    public static final String TABLE_BIND = "table_bind";
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/bind";
    public static final String ID = "id";
    public static final String NAME = "name";

    public SilenceDataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createBindTable(db);
    }

    private void createBindTable(SQLiteDatabase db) {
        String createSql = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s TEXT)",
                TABLE_BIND, ID, NAME);
        db.execSQL(createSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == 1) {
            db.execSQL("DROP TABLE IF EXITS" + TABLE_BIND);
            onCreate(db);
        }
    }
}
