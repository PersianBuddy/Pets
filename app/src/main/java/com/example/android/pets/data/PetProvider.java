package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


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
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
