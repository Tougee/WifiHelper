package com.tougee.silence.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.tougee.silence.database.SilenceDataBase;

import static android.content.UriMatcher.NO_MATCH;
import static com.tougee.silence.database.SilenceDataBase.CONTENT_TYPE;
import static com.tougee.silence.database.SilenceDataBase.TABLE_BIND;

public class SilenceProvider extends ContentProvider {

    public static final String AUTHORITY = "com.tougee.silence.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final int BIND = 0x01;

    private static final UriMatcher sUriMatcher;
    SilenceDataBase mDb;

    static {
        sUriMatcher = new UriMatcher(NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, TABLE_BIND, BIND);
    }

    @Override
    public boolean onCreate() {
        mDb = new SilenceDataBase(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDb.getWritableDatabase();
        return database.query(getTableName(uri), projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case BIND:
                return CONTENT_TYPE;
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase database = mDb.getWritableDatabase();
        long value = database.insert(getTableName(uri), null, values);
        return Uri.withAppendedPath(CONTENT_URI, String.valueOf(value));
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDb.getWritableDatabase();
        return database.delete(getTableName(uri), selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDb.getWritableDatabase();
        return database.update(getTableName(uri), values, selection, selectionArgs);
    }

    private String getTableName(Uri uri) {
        String tableName;
        switch (sUriMatcher.match(uri)) {
            case BIND:
                tableName = TABLE_BIND;
                break;
            default:
                throw new IllegalArgumentException("Unknown table name");
        }
        return tableName;
    }
}
