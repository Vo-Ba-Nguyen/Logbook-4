package com.example.logbook4;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class Database extends SQLiteOpenHelper {

    private Context context;
    public static final String DATABASE_NAME = "imageDatabase.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "image_database";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_URL = "url";


    public Database(@Nullable Context context){
        super(context,DATABASE_NAME,null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME +
                "  (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_URL + " TEXT);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

    }
    void addLink(String url){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv =new ContentValues();
        cv.put(COLUMN_URL, url);
        long result =db.insert(TABLE_NAME,null, cv);
        if (result == -1){
            Toast.makeText(context, "Add Failed", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, "Add Successfully!", Toast.LENGTH_SHORT).show();
        }
    }
    Cursor getAllLink(){
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = null;
        if (db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }
}
