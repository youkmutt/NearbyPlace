package com.example.nearbyplaceapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class mDBList extends SQLiteOpenHelper {
    private static final String TAG = "Leads Detail";
    private static final String TABLE_NAME = "list";
    private static final String COL1 = "name";
    private static final String COL2 = "address";
    private static final String COL3 = "distance";
    private static final String COL4 = "image";

    public mDBList(Context context) {
        super(context, TABLE_NAME,null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL1 + " INT," + COL2 + " INT," + COL3 + " TEXT," + COL4 + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void Drop(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_NAME + "'");
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL1 + " INT," + COL2 + " INT," + COL3 + " TEXT," + COL4 + " TEXT)";
        db.execSQL(createTable);
    }

    public boolean addData(String name,String address,String distance,String img) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, name);
        contentValues.put(COL2, address);
        contentValues.put(COL3, distance);
        contentValues.put(COL4, img);

        long result = db.insert(TABLE_NAME, null, contentValues);
        //if date as inserted incorrectly it will return -1
        if (result == -1)
        { return false; }
        else
        { return true; }
    }

    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }
}
