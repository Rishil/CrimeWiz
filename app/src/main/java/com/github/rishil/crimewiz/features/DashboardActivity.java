package com.github.rishil.crimewiz.features;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

import com.github.rishil.crimewiz.R;
import com.github.rishil.crimewiz.base.BaseActivity;
import com.github.rishil.crimewiz.core.util.DatabaseHelper;

public class DashboardActivity extends BaseActivity {

    private ArrayList<String> categories, years, months;
    private StringBuilder year, month, selectedCategories;

    private TextView mostFrequentCrimeTextView, mostFrequentCrimeNumber, leastFrequentCrimeTextView,
            leastFrequentCrimeNumber, mostFrequentMonthNumber, mostFrequentMonthTextView,
            mostFrequentYearTextView, mostFrequentYearNumber,
            totalCrimeNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        loadUi();

        totalCrimeNumber = findViewById(R.id.totalCrimeNumber);

        mostFrequentCrimeNumber = findViewById(R.id.mostFrequentCrimeNumber);
        mostFrequentCrimeTextView = findViewById(R.id.mostFrequentCrimeTextView);

        leastFrequentCrimeNumber = findViewById(R.id.leastFrequentCrimeNumber);
        leastFrequentCrimeTextView = findViewById(R.id.leastFrequentCrimeTextView);

        mostFrequentMonthNumber = findViewById(R.id.mostFrequentMonthNumber);
        mostFrequentMonthTextView = findViewById(R.id.mostFrequentMonthTextView);
        mostFrequentYearTextView = findViewById(R.id.mostFrequentYearTextView);
        mostFrequentYearNumber = findViewById(R.id.mostFrequentYearNumber);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Cursor mCursor = sqLiteDatabase.rawQuery("SELECT * FROM " + DatabaseHelper.CRIME_TABLE, null);

        if (mCursor.moveToFirst()) {
            try {
                loadData();
            } catch (Exception e){
                hideUi();
            }
        } else {
            hideUi();
        }
        mCursor.close();
    }

    private void hideUi(){
        totalCrimeNumber.setText(R.string.no_data);
        mostFrequentCrimeNumber.setText(R.string.no_data);
        leastFrequentCrimeNumber.setText(R.string.no_data);
        mostFrequentMonthNumber.setText(R.string.no_data);
        mostFrequentYearNumber.setText(R.string.no_data);
        mostFrequentCrimeTextView.setText(R.string.no_data);
        leastFrequentCrimeTextView.setText(R.string.no_data);
        mostFrequentMonthTextView.setText(R.string.no_data);
        mostFrequentYearTextView.setText(R.string.no_data);
    }

    private void loadData(){
        loadCategories();
        loadYears();
        loadMonths();
        setTotalCrimes();
        setFrequentCrimes();
        setFrequentMonth();
        setFrequentYear();
    }

    private void setFrequentCrimes(){
        Cursor mostFreq = sqLiteDatabase.rawQuery(crimeFrequencyQuery("DESC"), null);
        for (int i = 0; i < mostFreq.getCount(); i++) {
            mostFreq.moveToNext();
            mostFrequentCrimeTextView.setText(mostFreq.getString(0));
            mostFrequentCrimeNumber.setText(mostFreq.getString(1));
        }

        mostFreq.close();

        Cursor leastFreq = sqLiteDatabase.rawQuery(crimeFrequencyQuery("ASC"), null);
        for (int i = 0; i < leastFreq.getCount(); i++) {
            leastFreq.moveToNext();
            leastFrequentCrimeTextView.setText(leastFreq.getString(0));
            leastFrequentCrimeNumber.setText(leastFreq.getString(1));
        }
        leastFreq.close();
    }

    private void setTotalCrimes(){
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.CRIME_TABLE
                + " WHERE (strftime('%Y'," + DatabaseHelper.DATE + ") = " + year + ")"
                + " AND (strftime('%m'," + DatabaseHelper.DATE + ") = " + month + ")"
                + " AND (" + DatabaseHelper.CATEGORY + " = " + selectedCategories + ")";
        Cursor totalCrimes = sqLiteDatabase.rawQuery(query, null); // execute statement
        for (int i = 0; i < totalCrimes.getCount(); i++) {
            totalCrimes.moveToNext();
            totalCrimeNumber.setText(totalCrimes.getString(0)); // append result to the CardView
        }
        totalCrimes.close(); // close db cursor to avoid db leaks
    }

    private void setFrequentMonth(){
        String query = "SELECT " + DatabaseHelper.DATE + ", COUNT(" + DatabaseHelper.CATEGORY
                + "), " + DatabaseHelper.CATEGORY + " FROM " + DatabaseHelper.CRIME_TABLE
                + " WHERE (strftime('%Y'," + DatabaseHelper.DATE + ") = " + year + ")"
                + " AND (strftime('%m'," + DatabaseHelper.DATE + ") = " + month + ")"
                + " AND (" + DatabaseHelper.CATEGORY + " = " + selectedCategories + ")"
                + " GROUP BY " + DatabaseHelper.DATE
                + " ORDER BY COUNT(" + DatabaseHelper.DATE + ")"
                + " DESC LIMIT 1";

        Cursor mostFreqMonth = sqLiteDatabase.rawQuery(query, null);
        String month = null;
        for (int i = 0; i < mostFreqMonth.getCount(); i++) {
            mostFreqMonth.moveToNext();
            month = mostFreqMonth.getString(0).substring(5,7);
            switch(month) {
                case "01":
                    month = "January";
                    break;
                case "02":
                    month = "February";
                    break;
                case "03":
                    month = "March";
                    break;
                case "04":
                    month = "April";
                    break;
                case "05":
                    month = "May";
                    break;
                case "06":
                    month = "June";
                    break;
                case "07":
                    month = "July";
                    break;
                case "08":
                    month = "August";
                    break;
                case "09":
                    month = "September";
                    break;
                case "10":
                    month = "October";
                    break;
                case "11":
                    month = "November";
                    break;
                case "12":
                    month = "December";
                    break;
            }

            String year = mostFreqMonth.getString(0).substring(0,4);
            mostFrequentMonthNumber.setText(month);
            mostFrequentMonthTextView.setText(year);
        }

        mostFreqMonth.close();
        if (month == null) hideUi();
    }

    private void setFrequentYear(){
        String query = "SELECT " + DatabaseHelper.DATE+ ", COUNT(" + DatabaseHelper.DATE
                + ") FROM " + DatabaseHelper.CRIME_TABLE
                + " WHERE (strftime('%Y'," + DatabaseHelper.DATE + ") = " + year + ")"
                + " AND (strftime('%m'," + DatabaseHelper.DATE + ") = " + month + ")"
                + " AND (" + DatabaseHelper.CATEGORY + " = " + selectedCategories + ")"
                + " GROUP BY strftime('%Y'," + DatabaseHelper.DATE + ")"
                + " ORDER BY COUNT (" + DatabaseHelper.DATE + ")"
                + " DESC LIMIT 1";

        Cursor mostFreqYr = sqLiteDatabase.rawQuery(query, null);

        String year = null;
        for (int i = 0; i < mostFreqYr.getCount(); i++) {
            mostFreqYr.moveToNext();
            String description =  mostFreqYr.getString(1) + " crimes";
            year = mostFreqYr.getString(0).substring(0,4);
            mostFrequentYearNumber.setText(year);
            mostFrequentYearTextView.setText(description);


        }
        mostFreqYr.close();

        if (year == null) hideUi();
    }


    private String crimeFrequencyQuery(String orderingType){
        String query = "SELECT " + DatabaseHelper.CATEGORY + ", COUNT(" + DatabaseHelper.CATEGORY
                + "), " + DatabaseHelper.CATEGORY + " FROM " + DatabaseHelper.CRIME_TABLE
                + " WHERE (strftime('%Y'," + DatabaseHelper.DATE + ") = " + year + ")"
                + " AND (strftime('%m'," + DatabaseHelper.DATE + ") = " + month + ")"
                + " AND (" + DatabaseHelper.CATEGORY + " = " + selectedCategories + ")"
                + " GROUP BY " + DatabaseHelper.CATEGORY
                + " ORDER BY COUNT(" + DatabaseHelper.CATEGORY + ") " + orderingType + " LIMIT 1";
        return query;
    }



    private void loadCategories(){
        categories = new ArrayList<>();
        Set<String> categoriesSet = sharedPreferences.getStringSet("saved_categories", null);
        if (categoriesSet != null) {
            categories.addAll(categoriesSet);
        }

        selectedCategories = new StringBuilder();
        for (int i = 0; i < categories.size(); i++) {
            if (i == 0) {
                selectedCategories.append("\'").append(categories.get(i)).append("\'");
            } else {
                selectedCategories.append(" OR " + DatabaseHelper.CATEGORY + " = \'").append(categories.get(i)).append("\'");
            }
        }
    }

    private void loadMonths(){
        months = new ArrayList<>();
        Set<String> monthsSet = sharedPreferences.getStringSet("saved_months", null);
        if (monthsSet != null) {
            months.addAll(monthsSet);
        }

        month = new StringBuilder();
        for (int i = 0; i < months.size(); i++){
            if (i == 0){
                month.append("\'").append(months.get(i)).append("\'");
            } else {
                month.append(" OR strftime('%m',").append(DatabaseHelper.DATE).append(") = \'").append(months.get(i)).append("\'");
            }
        }
    }

    private void loadYears(){
        years = new ArrayList<>();
        Set<String> yearsSet = sharedPreferences.getStringSet("saved_years", null);
        if (yearsSet != null) {
            years.addAll(yearsSet);
        }

        year = new StringBuilder();
        for (int i = 0; i < years.size(); i++){
            if (i == 0){
                year.append("\'").append(years.get(i)).append("\'");
            } else {
                year.append(" OR strftime('%Y',").append(DatabaseHelper.DATE).append(") = \'").append(years.get(i)).append("\'");
            }
        }

    }
}
