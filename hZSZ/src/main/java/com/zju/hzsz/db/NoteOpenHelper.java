package com.zju.hzsz.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Wangli on 2017/3/17.
 */

public class NoteOpenHelper extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "note";
    public static final int VERSION = 1;
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String TIME = "time";
    public static final String ID = "_id";



    public NoteOpenHelper(Context context) {
        super(context, TABLE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("create table if not exists " + TABLE_NAME + " ("
                + ID + " integer primary key autoincrement,"
                + CONTENT + " text not null,"
                + TITLE + " text not null,"
                + TIME + " text not null)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
