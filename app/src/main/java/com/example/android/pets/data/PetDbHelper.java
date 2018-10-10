package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.pets.data.PetsContract.PetEntry;


public class PetDbHelper extends SQLiteOpenHelper {
    //name of database
    private static final String DATABASE_NAME = "shelter.db";
    //database version
    private static final int DATABASE_VERSION = 1;
    //sql for creating table that used in onCreate method
    private String sqlCreateTable = "CREATE TABLE "+ PetEntry.TABLE_NAME+
            " ("+PetEntry._ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"+
            PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL,"+
            PetEntry.COLUMN_PET_BREED+ " TEXT,"+
            PetEntry.COLUMN_PET_GENDER+ " INTEGER NOT NULL DEFAULT 0,"+
            PetEntry.COLUMN_PET_WEIGHT+ " INTEGER DEFAULT 0);";

    //constructor
    public PetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(sqlCreateTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
