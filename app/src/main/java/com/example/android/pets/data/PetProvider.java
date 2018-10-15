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
import android.widget.Toast;

import com.example.android.pets.R;
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

        //Set Notification for uri so that if it changes it will notify contentResolver to reload the ContentLoader
        //The uri that must be watch in this case is the uri that represent whole table
        //PET_ENTRY.CONTENT_URI or case PETS in mUriMatcher
        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PETS_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        //check sanity of contentValues
        if (contentValues == null || contentValues.size() == 0)return null;
        //sanity check of inputs in contentValues
        //check for gender validation
        Integer gender =contentValues.getAsInteger("gender");
        if (gender ==null || !PetsContract.isValidGender(gender)){
            throw new IllegalArgumentException("The value fo gender of pet must be an integer between 0-2");
        }
        //check validity of weight
        Integer weight =contentValues.getAsInteger("weight");
        if (weight!=null && weight <0){
            throw new IllegalArgumentException("The value of weight must be positive");
        }
        //Check validity of name
        String name= contentValues.getAsString("name");
        if (name== null || name.isEmpty()){
            throw new IllegalArgumentException("Pet require a name");
        }
        //set default value for breed
        String breed = contentValues.getAsString("breed");
        if (breed == null || breed.isEmpty()){
            breed =getContext().getString(R.string.unknown_breed);
            contentValues.put("breed",breed);
        }

        //check validity of uri
        int match = sUriMatcher.match(uri);
        if (match == PETS){
            return insertPet(uri,contentValues);
        }else {//invalid uri for insert a row
            throw new IllegalArgumentException("Invalid Uri for inserting new row "+ uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArg) {
        //create a writable object of database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // a variable that represent the number rows that has been deleted
        int numRowDeleted;

        //check validity of uri
        int match= sUriMatcher.match(uri);
        switch (match){
            case PETS://update multiple row in table
                numRowDeleted = db.delete(PetEntry.TABLE_NAME,selection,selectionArg);
                break;

            case PETS_ID://update a specific row with id=?
                selection =PetEntry._ID +"=?";
                selectionArg = new String[]{String.valueOf(ContentUris.parseId(uri))};
                numRowDeleted = db.delete(PetEntry.TABLE_NAME,selection,selectionArg);
                break;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        //notify contentResolver if uri has been changed
        if (numRowDeleted != 0)getContext().getContentResolver().notifyChange(uri,null);
        return numRowDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArg) {
        //number of rows that has been updated
        int rowsUpdated;

        //sanity check of inputs in contentValues
        if (contentValues == null || contentValues.size() == 0)return 0;
        //Check validity of name
        if (contentValues.containsKey(PetEntry.COLUMN_PET_NAME)){
            String name= contentValues.getAsString("name");
            if (name!= null && name.isEmpty()){
                throw new IllegalArgumentException("Pet require a name");
            }
        }
        //check sanity of contentValues
        //check for gender validation
        if (contentValues.containsKey(PetEntry.COLUMN_PET_GENDER)){
            Integer gender =contentValues.getAsInteger("gender");
            if (gender !=null && !PetsContract.isValidGender(gender)){
                throw new IllegalArgumentException("The value fo gender of pet must be an integer between 0-2");
            }
        }

        //check validity of weight
        if (contentValues.containsKey(PetEntry.COLUMN_PET_WEIGHT)){
            Integer weight =contentValues.getAsInteger("weight");
            if (weight!=null && weight <0){
                throw new IllegalArgumentException("The value of weight must be positive");
            }

        }

        //set default value for breed
        String breed = contentValues.getAsString("breed");
        if (breed!=null && breed.isEmpty()){
            breed = getContext().getString(R.string.unknown_breed);
            contentValues.put("breed",breed);
        }

        //check validity of uri
        int match= sUriMatcher.match(uri);
        switch (match){
            case PETS://update multiple row in table
                rowsUpdated = updatePets(uri,contentValues,selection,selectionArg);
                break;

            case PETS_ID://update a specific row with id=?
                selection =PetEntry._ID +"=?";
                selectionArg = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsUpdated = updatePets(uri,contentValues,selection,selectionArg);
                break;

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }

        //notify contentResolver if table has changed
        //it table changed the loader will be reloaded
        if (rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri,null);

        return rowsUpdated;
    }

    //A method to insert a row into table and return uri of new row
    //the uri inserted inside this method should be correct
    private Uri insertPet(Uri uri,ContentValues values){
        //create a writable object of database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long newRowId = db.insert(PetEntry.TABLE_NAME,null,values);
        if (newRowId == -1){//insertion unsuccessful
            Log.e(lOG_TAG,"insertion unsuccessful for  uri: " + uri);
            return  null;
        }

        //notify contentResolver for change in uri
        //the uri that we are watching is PET_ENTRY.CONTENT_URI
        getContext().getContentResolver().notifyChange(uri,null);

        return ContentUris.withAppendedId(PetEntry.CONTENT_URI,newRowId);
    }

    //A helper method to update a table using PetDbHelper class
    //and return the number of rows that has been affected
    private int updatePets(Uri uri,ContentValues values,String selection, String[] selectionArg){
        //create a writable database object
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        return db.update(PetEntry.TABLE_NAME,values,selection,selectionArg);
    }

}
