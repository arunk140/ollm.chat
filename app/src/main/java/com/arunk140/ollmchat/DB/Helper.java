package com.arunk140.ollmchat.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class Helper extends SQLiteOpenHelper {
    static final String DB_NAME = "OLLM_CHAT_APP.DB";
    static final int DB_VERSION = 1;

    static final String configTableName = "config";
    static final String configKeyCol = "title";
    static final String configValueCol = "value";


    public Helper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +configTableName+ " ("+configKeyCol+" TEXT NOT NULL PRIMARY KEY, "+configValueCol+" TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
