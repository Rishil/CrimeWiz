package com.github.rishil.crimewiz.features;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.github.rishil.crimewiz.R;
import com.github.rishil.crimewiz.base.BaseActivity;
import com.github.rishil.crimewiz.core.util.DatabaseHelper;

public class FilterActivity extends BaseActivity {

    private ListView categoriesListView, yearListView, monthListView;
    private DatePicker datePicker;
    private Date minDate, maxDate; // of data

    private TextView categoriesTextView, yearTextView, monthTextView, noDataTextView;
    private Button cancelBtn, saveBtn, backBtn;

    private ArrayList<String> selectedCategories, selectedYears, selectedMonths;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        sharedPreferences.edit().putBoolean("inFilterView", true).apply();

        categoriesTextView = findViewById(R.id.categoriesTextView);
        yearTextView = findViewById(R.id.yearTextView);
        monthTextView = findViewById(R.id.monthTextView);

        categoriesListView = findViewById(R.id.categoriesListView);
        yearListView = findViewById(R.id.yearListView);
        monthListView = findViewById(R.id.monthListView);

        cancelBtn = findViewById(R.id.cancelBtn);
        saveBtn = findViewById(R.id.saveBtn);
        backBtn = findViewById(R.id.backBtn);

        noDataTextView = findViewById(R.id.noDataTextView);


        try {
            populateLists();
        } catch (Exception e){
            e.printStackTrace();
            hideUi();
        }
        //test

    }

    @Override
    protected void onResume() {
        sharedPreferences.edit().putBoolean("inFilterView", true).apply();
        super.onResume();

        Cursor mCursor = sqLiteDatabase.rawQuery("SELECT * FROM " + DatabaseHelper.CRIME_TABLE, null);

        if (mCursor.moveToFirst())
        {
            // DO SOMETHING WITH CURSOR
            showUi();
        } else
        {
            // I AM EMPTY
            hideUi();
        }

        mCursor.close();

    }

    private void hideUi(){
        categoriesListView.setVisibility(View.GONE);
        categoriesTextView.setVisibility(View.GONE);
        yearTextView.setVisibility(View.GONE);
        yearListView.setVisibility(View.GONE);
        monthTextView.setVisibility(View.GONE);
        monthListView.setVisibility(View.GONE);

        noDataTextView.setVisibility(View.VISIBLE);
        cancelBtn.setVisibility(View.GONE);
        saveBtn.setVisibility(View.GONE);

        backBtn.setVisibility(View.VISIBLE);

    }

    private void showUi(){
        categoriesListView.setVisibility(View.VISIBLE);
        categoriesTextView.setVisibility(View.VISIBLE);
        categoriesListView.setVisibility(View.VISIBLE);
        yearListView.setVisibility(View.VISIBLE);
        monthListView.setVisibility(View.VISIBLE);
        monthTextView.setVisibility(View.VISIBLE);

        noDataTextView.setVisibility(View.GONE);
        cancelBtn.setVisibility(View.VISIBLE);
        saveBtn.setVisibility(View.VISIBLE);

        backBtn.setVisibility(View.GONE);

    }

    private void populateLists(){
        showUi();
        loadCategories();
        loadYears();
        try {
            loadMonths();
        } catch (Exception e){
            Log.e("FilterActivity", e.getMessage());
        }
    }

    private void loadCategories(){
        // load categories
        final ArrayList<String> categoryList = databaseHelper.getCategories();
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice, categoryList);
        categoriesListView.setAdapter(categoryAdapter);

        selectedCategories = new ArrayList<>();

        Set<String> set = sharedPreferences.getStringSet("saved_categories", null);
        if (set != null) {
            selectedCategories.addAll(set);
        }

        // tick if contained
        for (int i = 0; i < categoryList.size(); i++){
            String itemName = categoriesListView.getItemAtPosition(i).toString();
            if (selectedCategories.contains(itemName)){
                categoriesListView.setItemChecked(i, true);
            } else {
                categoriesListView.setItemChecked(i, false);
            }
        }
        categoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                SparseBooleanArray sbArray = categoriesListView.getCheckedItemPositions();
                selectedCategories.clear();
                int i = 0 ;
                while (i < sbArray.size()) {
                    if (sbArray.valueAt(i)) {
                        selectedCategories.add(categoryList.get(sbArray.keyAt(i)));
                    }
                    i++ ;
                }
            }
        });
    }

    private void loadYears(){
        // load years
        final ArrayList<String> yearList = databaseHelper.getYears();
        yearListView = findViewById(R.id.yearListView);
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice, yearList);
        yearListView.setAdapter(yearAdapter);

        selectedYears = new ArrayList<>();

        Set<String> yearSet = sharedPreferences.getStringSet("saved_years", null);
        if (yearSet != null) {
            selectedYears.addAll(yearSet);
        }
        // tick if contained
        for (int i = 0; i < yearList.size(); i++){
            String itemName = yearListView.getItemAtPosition(i).toString();
            if (selectedYears.contains(itemName)){
                yearListView.setItemChecked(i, true);
            } else {
                yearListView.setItemChecked(i, false);
            }
        }
        yearListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                SparseBooleanArray sbArray = yearListView.getCheckedItemPositions();
                selectedYears.clear();
                int i = 0 ;
                while (i < sbArray.size()) {
                    if (sbArray.valueAt(i)) {
                        selectedYears.add(yearList.get(sbArray.keyAt(i)));
                    }
                    i++;
                }
            }
        });
    }

    private void loadMonths(){
        // load years
        final ArrayList<String> monthList = databaseHelper.getMonths();
        monthListView = findViewById(R.id.monthListView);
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice, monthList);
        monthListView.setAdapter(monthAdapter);

        selectedMonths = new ArrayList<>();

        Set<String> monthSet = sharedPreferences.getStringSet("saved_months", null);
        if (monthSet != null) {
            selectedMonths.addAll(monthSet);
        }
        // tick if contained
        for (int i = 0; i < monthList.size(); i++){
            String itemName = monthListView.getItemAtPosition(i).toString();
            if (selectedMonths.contains(itemName)){
                monthListView.setItemChecked(i, true);
            } else {
                monthListView.setItemChecked(i, false);
            }
        }
        monthListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                SparseBooleanArray sbArray = monthListView.getCheckedItemPositions();
                selectedMonths.clear();
                int i = 0 ;
                while (i < sbArray.size()) {
                    if (sbArray.valueAt(i)) {
                        selectedMonths.add(monthList.get(sbArray.keyAt(i)));
                    }
                    i++;
                }

            }
        });

    }

    public void onClickSave(View v){
        giveHapticFeedback();
        // save categories
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> savedCategoriesSet = new HashSet<>(selectedCategories);
        editor.putStringSet("saved_categories", savedCategoriesSet);

        Set<String> savedYearsSet = new HashSet<>(selectedYears);
        editor.putStringSet("saved_years", savedYearsSet);

        Set<String> savedMonthsSet = new HashSet<>(selectedMonths);
        editor.putStringSet("saved_months", savedMonthsSet);

        firebaseService.saveFirebasePreferences("saved_categories", savedCategoriesSet);
        firebaseService.saveFirebasePreferences("saved_years", savedYearsSet);
        firebaseService.saveFirebasePreferences("saved_months", savedMonthsSet);
        editor.putBoolean("refreshMap", true);
        editor.apply();
        onBackPressed();
    }

    public void onClickCancel(View v){
        giveHapticFeedback();
        onBackPressed();

    }

    @Override
    public void onBackPressed(){
        sharedPreferences.edit().putBoolean("inFilterView", false).apply();
        sharedPreferences.edit().putBoolean("returnFromFilterView", true).apply();
        super.onBackPressed();
    }

}
