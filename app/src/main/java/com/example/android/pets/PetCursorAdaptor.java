package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.data.PetsContract;

public class PetCursorAdaptor extends CursorAdapter {
    public PetCursorAdaptor(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //get object of textViews in list_item.xml file
        TextView nameTextView =(TextView)view.findViewById(R.id.label_pet_name);
        TextView breedTextView = (TextView)view.findViewById(R.id.label_pet_breed);

        //Set text attribute of TextViews to new values in Cursor
        nameTextView.setText(cursor.getString(cursor.getColumnIndex(PetsContract.PetEntry.COLUMN_PET_NAME)));
        breedTextView.setText(cursor.getString(cursor.getColumnIndex(PetsContract.PetEntry.COLUMN_PET_NAME)));
    }
}
