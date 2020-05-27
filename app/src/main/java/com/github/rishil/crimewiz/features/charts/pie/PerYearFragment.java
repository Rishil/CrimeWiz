package com.github.rishil.crimewiz.features.charts.pie;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Set;

import com.github.rishil.crimewiz.R;
import com.github.rishil.crimewiz.core.util.DatabaseHelper;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PerYearFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class PerYearFragment extends Fragment{

    private PieChart pieChart;
    private SQLiteDatabase sqLiteDatabase;
    private SharedPreferences sharedPreferences;

    private PieData data;
    private View currentView;
    private DatabaseHelper databaseHelper;

    private ArrayList<String> categories, years, months;

    public PerYearFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        currentView = inflater.inflate(R.layout.fragment_per_year_pie, container, false);
        setRetainInstance(true);



        pieChart = currentView.findViewById(R.id.pieChart);

        updateData();


        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(10,10,10,10);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(25f);
        pieChart.setTransparentCircleRadius(25f);
        pieChart.setDrawEntryLabels(false);

        // Inflate the layout for this fragment
        return currentView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Log.d("CrimeWiz", "onCreate");

        databaseHelper = new DatabaseHelper(getContext());
        sqLiteDatabase = databaseHelper.getWritableDatabase();
        if (getContext() != null) {
            sharedPreferences = getContext().getSharedPreferences("userSettings",
                    Context.MODE_PRIVATE);

            sharedPreferences.edit().putBoolean("yearFragmentFirstLaunch", true).apply();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        boolean categoryFragmentFirstLaunch =
                sharedPreferences.getBoolean("yearFragmentFirstLaunch", false);

        boolean returnFromFilterView =
                sharedPreferences.getBoolean("returnFromFilterView", false);

        boolean dataSetHasChanged=
                sharedPreferences.getBoolean("dataSetHasChanged", false);

        if (categoryFragmentFirstLaunch || returnFromFilterView || dataSetHasChanged){
            updateData();
            sharedPreferences.edit().putBoolean("yearFragmentFirstLaunch", false).apply();
            sharedPreferences.edit().putBoolean("returnFromFilterView", false).apply();
            sharedPreferences.edit().putBoolean("dataSetHasChanged", false).apply();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

    }

    private void updateData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    addDataToGraph();
                } catch (Exception e){
                    e.printStackTrace();
                }}
        }).start();
    }


    private void addDataToGraph(){

        ArrayList <PieEntry>  pieVals = new ArrayList<>();

        years = new ArrayList<>();
        months = new ArrayList<>();

        Set<String> yearsSet = sharedPreferences.getStringSet("saved_years", null);
        if (yearsSet != null) {
            years.addAll(yearsSet);
        }

        StringBuilder year = new StringBuilder();
        for (int i = 0; i < years.size(); i++){
            if (i == 0){
                year.append("\'").append(years.get(i)).append("\'");
            } else {
                year.append(" OR strftime('%Y',").append(DatabaseHelper.DATE).append(") = \'").append(years.get(i)).append("\'");
            }
        }

        categories = new ArrayList<>();

        Set<String> categoriesSet = sharedPreferences.getStringSet("saved_categories", null);
        if (categoriesSet != null) {
            categories.addAll(categoriesSet);
        }

        StringBuilder selectedCategories = new StringBuilder();
        for (int i = 0; i < categories.size(); i++){
            if (i == 0){
                selectedCategories.append("\'").append(categories.get(i)).append("\'");
            } else {
                selectedCategories.append(" OR " + DatabaseHelper.CATEGORY + " = \'").append(categories.get(i)).append("\'");
            }
        }

        months = new ArrayList<>();

        Set<String> monthsSet = sharedPreferences.getStringSet("saved_months", null);
        if (monthsSet != null) {
            months.addAll(monthsSet);
        }

        StringBuilder month = new StringBuilder();
        for (int i = 0; i < months.size(); i++){
            if (i == 0){
                month.append("\'").append(months.get(i)).append("\'");
            } else {
                month.append(" OR strftime('%m',").append(DatabaseHelper.DATE).append(") = \'").append(months.get(i)).append("\'");
            }
        }

        pieChart.clear();

        String query = "SELECT " + DatabaseHelper.DATE+ ", COUNT(" + DatabaseHelper.DATE
                + ") FROM " + DatabaseHelper.CRIME_TABLE
                + " WHERE (strftime('%Y'," + DatabaseHelper.DATE + ") = " + year + ")"
                + " AND (strftime('%m'," + DatabaseHelper.DATE + ") = " + month + ")"
                + " AND (" + DatabaseHelper.CATEGORY + " = " + selectedCategories + ")"
                + " GROUP BY strftime('%Y'," + DatabaseHelper.DATE + ")"
                + " ORDER BY COUNT (" + DatabaseHelper.DATE + ")";

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        ArrayList<String> entries = new ArrayList<>();
        for(int j = 0; j < cursor.getCount(); j++){
            cursor.moveToNext();

            String yearString = "" + cursor.getString(0).substring(0,4);
            float totalNumberOfCrimes = Float.parseFloat(cursor.getString(1));

            if (!entries.contains(yearString)){
                entries.add(cursor.getString(0));
                pieVals.add(new PieEntry(totalNumberOfCrimes, yearString));
            }

        }
        cursor.close();

        PieDataSet pieDataSet = new PieDataSet(pieVals, "");
        pieDataSet.setSliceSpace(3f);
        pieDataSet.setSelectionShift(5f);
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieDataSet.setValueTextColor(Color.BLACK);


        pieDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) Math.floor(value));
            }
        });

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextSize(10f);
        pieData.setValueTextColor(Color.BLACK);

        pieChart.setData(pieData);
        pieChart.invalidate();
        pieChart.refreshDrawableState();
    }


}
