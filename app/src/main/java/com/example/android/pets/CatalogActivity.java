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

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.data.PetDbHelper;
import com.example.android.pets.data.PetProvider;
import com.example.android.pets.data.PetsContract.PetEntry;

import java.util.List;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity {
    //tags that will used in error log message
    private final String LOG_TAG = CatalogActivity.class.getSimpleName();

    //subclass of SqliteOperHelper
    private PetDbHelper mDbHelper;

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
                displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllRows();
                displayDatabaseInfo();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseInfo() {
        //Make a uri to select all rows
        Uri uri = PetEntry.CONTENT_URI;

        //make a projection for columns that we want to be selected
        String[] projection={PetEntry._ID,PetEntry.COLUMN_PET_NAME,
        PetEntry.COLUMN_PET_BREED,PetEntry.COLUMN_PET_GENDER,PetEntry.COLUMN_PET_WEIGHT};

        // create object of class cursor
        Cursor cursor ;
        cursor = getContentResolver().query(uri,projection,null,null,null);
        if (cursor==null){
            Toast.makeText(this, "empty cursor", Toast.LENGTH_SHORT).show();
        }
        //Create new object o listView
        ListView petsListView = (ListView) findViewById(R.id.pets_list);
        //create a new object of PetCursorAdapter
        PetCursorAdaptor adaptor = new PetCursorAdaptor(this,cursor);
        //set adaptor to list view
        petsListView.setAdapter(adaptor);

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

}
