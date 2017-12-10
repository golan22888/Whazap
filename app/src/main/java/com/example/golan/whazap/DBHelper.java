package com.example.golan.whazap;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by golan on 13/06/2017.
 */

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context) {
        super(context, "DB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXIST CHATS (NAME TEXT PRIMARY KEY, OwnerID TEXT NOT NULL, CREATED TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXIST CHATTERS((ID TEXT PRIMARY KEY, ChatID TEXT NOT NULL, " +
                "OwnerID TEXT NOT NULL, OwnerID TEXT NOT NULL)");
        db.execSQL("CREATE table IF NOT EXIST MESSAGES(ID TEXT PRIMARY KEY AUTOINCREMENT, " +
                "SenderID TEXT NOT NULL, TEXT TEXT NOT NULL, CREATED LONG NOT NULL)");
    }

    public void insertLogInID(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("OwnerID",id);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
