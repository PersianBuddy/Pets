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
//import inner class of PetsContract to use it directly inside app
import com.example.android.pets.data.PetDbHelper;
import com.example.android.pets.data.PetsContract.PetEntry;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;



/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    //subclass of SqliteOpenHelper
    PetDbHelper mDbHelper;

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    //global variable for uri of current pet in case u want to edit one
    private Uri mUri;

    //Loader id
    private static final int LOADER_ID=1;

    //a boolean variable that return true if a view has been changed
    private boolean mPetHasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        //initiate subclass of SqliteDbhelper
        mDbHelper = new PetDbHelper(this);
        setupSpinner();

        //get intent if exist
        Intent intent = getIntent();
        if (intent.hasExtra("itemUri")){
            mUri = Uri.parse(intent.getExtras().getString("itemUri")) ;
            setTitle(R.string.edit_page_title);
        }else{
            setTitle(R.string.add_new_pet_page_title);
            //invoke onPrepareOptionsMenu
            invalidateOptionsMenu();
        }

        //create onTouch listener object
        View.OnTouchListener mTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mPetHasChanged = true;
                return false;
            }
        };
        //set touchListener
        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        //Initiate loader for update hint of editText in UI
        getLoaderManager().initLoader(LOADER_ID,null,this);
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    //A method to insert data into database and make an intent
    //into CatalogActivity
    private void savePet(){


        //get values from view objects
        String petName =mNameEditText.getText().toString().trim();
        String petBreed = mBreedEditText.getText().toString().trim();
        String weightString =mWeightEditText.getText().toString().trim();
        Integer petWeight;

        if (!weightString.isEmpty()){
            petWeight= Integer.parseInt(weightString);
        }else{
            petWeight =null;
        }

        //save data into ContentValue object
        ContentValues values =new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME,petName);
        values.put(PetEntry.COLUMN_PET_BREED,petBreed);
        values.put(PetEntry.COLUMN_PET_WEIGHT,petWeight);
        values.put(PetEntry.COLUMN_PET_GENDER,mGender);



        //There are to case of saving pet to database
        //case 1: insert new pet
        //case 2: update an existing pet
        String saveCase = getTitle().toString();
        if (saveCase.equals(getString(R.string.add_new_pet_page_title))){//add new pet
            //uri for return value of insert in PetProvider class
            Uri newRowUri=null;

            try {
                //insert into database using PetProvider class
                newRowUri = getContentResolver().insert(PetEntry.CONTENT_URI,values);
            }catch (IllegalArgumentException e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            //check if insertion was successful
            if (newRowUri ==null){
                Toast.makeText(this, R.string.editor_insert_pet_failed, Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, R.string.editor_insert_pet_successful, Toast.LENGTH_SHORT).show();
                //close the current activity and return to previous one
                finish();
            }
        }else {//update existing pet
            //the number of rows that has been updated
            int rowsUpdated =0;

            try {
                //insert into database using PetProvider class
                rowsUpdated = getContentResolver().update(mUri,values,null,null);
            }catch (IllegalArgumentException e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            //check if insertion was successful
            if (rowsUpdated == 0){
                Toast.makeText(this, "Update has failed", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, " Update has been successful", Toast.LENGTH_SHORT).show();
                //close the current activity and return to previous one
                finish();

            }
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                //insert new pet into database
                savePet();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                if (!mPetHasChanged){
                    // Navigate back to parent activity (CatalogActivity)
                    NavUtils.navigateUpFromSameTask(this);
                }else {//pet data has been changed
                    showUnsavedChangesDialog();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String [] projection ={
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_WEIGHT,
                PetEntry.COLUMN_PET_GENDER
        };

        //return the cursor
        if (mUri!= null){
            return new CursorLoader(this,mUri,projection,null,null,null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor!= null && cursor.moveToFirst()){
            //get pet properties from cursor
            String petName =cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME));
            String petBreed =cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED));
            Integer petWeight = cursor.getInt(cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT));
            Integer petGender = cursor.getInt(cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER));

            mNameEditText.setText(petName);
            mBreedEditText.setText(petBreed);
            mWeightEditText.setText(Integer.toString(petWeight));
            mGenderSpinner.setSelection(petGender);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
//    Let’s make a method which will create the dialog below:

    private void showUnsavedChangesDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //close the current activity and return to previous one
                finish();
            }
        });
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mPetHasChanged){
            super.onBackPressed();
        }else {//pet data has been changed
            showUnsavedChangesDialog();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    // A method for deleting an existing pet from database
    private void deletePet(){
        int rowsDeleted = getContentResolver().delete(mUri,null,null);
        if (rowsDeleted==0){
            Toast.makeText(this, "Unable to Delete this pet", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Pet successfully deleted from database", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    //Create and show a dialog when user click on delete option
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}