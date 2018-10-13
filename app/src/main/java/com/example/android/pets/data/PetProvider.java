package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.pets.data.PetsContract.PetEntry;


public class PetProvider extends ContentProvider {
    /** Tag for the log messages */
    private static final String lOG_TAG = PetProvider.class.getSimpleName();
    //database helper object
    PetDbHelper mDbHelper;

    //constants that used in uriMatcher patterns
    //constant for pattern that represent whole pet table
    private static final int PETS =100;
    //constant for pattern that represent a row with specific id
    private static final int PETS_ID = 101;

    //global variable UriMatcher that will be used to make uri pattern
    private static UriMatcher sUriMatcher =new UriMatcher(UriMatcher.NO_MATCH);
    //create uri patterns
    static {
        //pattern for whole table
        sUriMatcher.addURI(PetsContract.CONTENT_AUTHORITY,PetsContract.PATH_PETS,PETS);
        //pattern for one row of table with specific id
        sUriMatcher.addURI(PetsContract.CONTENT_AUTHORITY, PetsContract.PATH_PETS+ "/#",PETS_ID);
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        //a readable database to read form it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //Cursor object to be returned
        Cursor cursor;

        //check uri if it matches with any available pattern in  sUriMatcher
        int match = sUriMatcher.match(uri);
        switch (match){
            case PETS://select all object from table
                cursor= db.query(PetEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case PETS_ID:
                selection= PetEntry._ID + " = ?";
                selectionArgs =new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(PetEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Can not query unknown uri" + uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        //check validity of uri
        int match = sUriMatcher.match(uri);
        if (match == PETS){
            return insertPet(uri,contentValues);
        }else {//invalid uri for insert a row
            throw new IllegalArgumentException("Invalid Uri for inserting new row "+ uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    //A method to insert a row into table and return uri of new row
    //the uri inserted inside this method should be correct
    private Uri insertPet(Uri uri,ContentValues values){
        //create a writable object of database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long newRowId = db.insert(PetEntry.TABLE_NAME,null,values);
        if (newRowId == -1){//insertion unsuccessful
            Log.e(lOG_TAG,"insertion unsuccessfull for  uri: " + uri);
            return  null;
        }
        return ContentUris.withAppendedId(PetEntry.CONTENT_URI,newRowId);
    }
}
