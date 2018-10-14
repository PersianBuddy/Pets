/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.data.PetDbHelper;
import com.example.android.pets.data.PetsContract.PetEntry;


/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    //tags that will used in error log message
    private final String LOG_TAG = CatalogActivity.class.getSimpleName();

    //subclass of SqliteOperHelper
    private PetDbHelper mDbHelper;

    //ImageView for empty ListView
    LinearLayout mEmptyList;

    //Adaptor for showing item in Listview
    PetCursorAdaptor mAdaptor;

    //Constant using for CursorLoader id
    private static final int LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        mDbHelper = new PetDbHelper(this);
        mEmptyList =(LinearLayout) findViewById(R.id.empty_list);
        mAdaptor = new PetCursorAdaptor(this,null);

        //Create new object o listView
        ListView petsListView = (ListView) findViewById(R.id.pets_list);
        //set empty textView
        petsListView.setEmptyView(mEmptyList);
        //setAdaptor to ListView
        petsListView.setAdapter(mAdaptor);
        //set empty textView
        petsListView.setEmptyView(mEmptyList);

        //initiate loader
        getLoaderManager().initLoader(LOADER_ID,null,this);
    }

    //A method to insert a dummy row inside database
    private void insertDummyData(){


        //Create Content values to put into database
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME,"Garfield");
        values.put(PetEntry.COLUMN_PET_BREED,"Tabby");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT,14);

        try{
            //insert dummy values into database
            getContentResolver().insert(PetEntry.CONTENT_URI,values);
        }catch (IllegalArgumentException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertDummyData();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllRows();
                //displayDatabaseInfo();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //A method to delete all rows from table
    private void deleteAllRows(){
        int result = getContentResolver().delete(PetEntry.CONTENT_URI,null,null);
        if (result ==0){
            Toast.makeText(this, "Unable to delete rows in table", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "All rows has been deleted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //make a projection for columns that we want to be selected
        String [] projection={PetEntry._ID,PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED};

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed
        //There in no need to create a subclass of CursorLoader (like AsyncLoader) because it's a subclass of AsyncLoader
        return new CursorLoader(this,PetEntry.CONTENT_URI,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Swap the new cursor in. (The framework will take care of closing the
        // old cursor once we return.)
        if (cursor == null){
            mEmptyList.setVisibility(View.VISIBLE);
        }else{
            mEmptyList.setVisibility(View.INVISIBLE);
        }
        mAdaptor.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdaptor.swapCursor(null);
    }
}
