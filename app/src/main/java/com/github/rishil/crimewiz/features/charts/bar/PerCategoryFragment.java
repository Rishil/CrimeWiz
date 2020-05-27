package com.github.rishil.crimewiz.features.charts.bar;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Set;

import com.github.rishil.crimewiz.R;
import com.github.rishil.crimewiz.core.util.DatabaseHelper;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PerCategoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class PerCategoryFragment extends Fragment{

    private OnFragmentInteractionListener mListener;

    private BarChart barChart;
    private SQLiteDatabase sqLiteDatabase;
    private SharedPreferences sharedPreferences;

    private BarData data;
    private View currentView;

    private CardView progressBar;

    private ArrayList<String> categories, years, months, visibleCategories;

    TextView noDataTextView, xLabelTextView;
    LinearLayout yLinearLayout;

    public PerCategoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        currentView = inflater.inflate(R.layout.fragment_per_category_bar, container, false);
        setRetainInstance(true);


        barChart = currentView.findViewById(R.id.barChart);
        progressBar = currentView.findViewById(R.id.progressBarCardView);

        updateData();

        barChart.getXAxis().setGranularityEnabled(true);
        barChart.getXAxis().setLabelRotationAngle(-45);
        barChart.getXAxis().setTypeface(Typeface.DEFAULT_BOLD);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);


        // Inflate the layout for this fragment
        return currentView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Log.d("CrimeWiz", "onCreate");

        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
        sqLiteDatabase = databaseHelper.getWritableDatabase();
        if (getContext() != null) {
            sharedPreferences = getContext().getSharedPreferences("userSettings",
                    Context.MODE_PRIVATE);

            sharedPreferences.edit().putBoolean("categoryFragmentFirstLaunch", true).apply();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        boolean categoryFragmentFirstLaunch =
                sharedPreferences.getBoolean("categoryFragmentFirstLaunch", false);

        boolean returnFromFilterView =
                sharedPreferences.getBoolean("returnFromFilterView", false);

        boolean dataSetHasChanged=
                sharedPreferences.getBoolean("dataSetHasChanged", false);

        if (categoryFragmentFirstLaunch || returnFromFilterView || dataSetHasChanged){
            updateData();
            sharedPreferences.edit().putBoolean("categoryFragmentFirstLaunch", false).apply();
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    private void addDataToGraph(){
        final ArrayList<BarEntry> categoryVals = new ArrayList<>();

        // increment crimes for each chosen category
        for (int i = 0; i < getCrimeCount().size(); i++){
            BarEntry barEntry = new BarEntry(i, Float.parseFloat(getCrimeCount().get(i)));
            categoryVals.add(barEntry);
        }

        // get all categories (less resource intensive)
        ArrayList<String> crimeCategories = new ArrayList<>(getCrimeCategories());
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(crimeCategories));
        barChart.getXAxis().setTextColor(Color.BLACK);
        barChart.getXAxis().setLabelRotationAngle(-90);
        barChart.getAxisLeft().setTextColor(Color.BLACK);



        BarDataSet barDataSet = new BarDataSet(categoryVals, "Crimes per Category");
        barDataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
        barDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) Math.floor(value));
            }
        });

        ArrayList<IBarDataSet> dataSet = new ArrayList<>();
        dataSet.add(barDataSet);

        data = new BarData(dataSet);
        barChart.setData(data);
        barChart.invalidate();
        barChart.refreshDrawableState();

    }


    private ArrayList<String> getCrimeCategories(){
        String query = "SELECT " + DatabaseHelper.CATEGORY+ ", COUNT(" + DatabaseHelper.CATEGORY
                + ") FROM " + DatabaseHelper.CRIME_TABLE + " GROUP BY " + DatabaseHelper.CATEGORY
                + " ORDER BY COUNT(" + DatabaseHelper.CATEGORY + ")";



        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        ArrayList<String> xData = new ArrayList<>();


        for(int i = 0; i < cursor.getCount(); i++){
            cursor.moveToNext();
            if (visibleCategories.contains(cursor.getString(0))){
                        xData.add(cursor.getString(0));
            }
        }
        cursor.close();

        return xData;
    }

    private ArrayList<String> getCrimeCount(){
        categories = new ArrayList<>();
        years = new ArrayList<>();
        months = new ArrayList<>();

        Set<String> categoriesSet = sharedPreferences.getStringSet("saved_categories", null);
        if (categoriesSet != null) {
            categories.addAll(categoriesSet);
        }

        Set<String> yearsSet = sharedPreferences.getStringSet("saved_years", null);
        if (yearsSet != null) {
            years.addAll(yearsSet);
        }

        Set<String> monthsSet = sharedPreferences.getStringSet("saved_months", null);
        if (monthsSet != null) {
            months.addAll(monthsSet);
        }

        ArrayList<String> yData = new ArrayList<>();

        StringBuilder year = new StringBuilder();
        for (int i = 0; i < years.size(); i++){
            if (i == 0){
                year.append("\'").append(years.get(i)).append("\'");
            } else {
                year.append(" OR strftime('%Y',").append(DatabaseHelper.DATE).append(") = \'").append(years.get(i)).append("\'");
            }
        }

        StringBuilder month = new StringBuilder();
        for (int i = 0; i < months.size(); i++){
            if (i == 0){
                month.append("\'").append(months.get(i)).append("\'");
            } else {
                month.append(" OR strftime('%m',").append(DatabaseHelper.DATE).append(") = \'").append(months.get(i)).append("\'");
            }
        }



        String query = "SELECT " + DatabaseHelper.CATEGORY+ ", COUNT(" + DatabaseHelper.CATEGORY
                + "), " +  DatabaseHelper.DATE +" FROM " + DatabaseHelper.CRIME_TABLE
                + " WHERE (strftime('%Y'," + DatabaseHelper.DATE + ") = " + year + ")"
                + " AND (strftime('%m'," + DatabaseHelper.DATE + ") = " + month + ")"
                + " GROUP BY " + DatabaseHelper.CATEGORY
                + " ORDER BY COUNT (" + DatabaseHelper.CATEGORY + ")";

        visibleCategories = new ArrayList<>();

            try {
                Cursor cursor = sqLiteDatabase.rawQuery(query, null);
                for(int j = 0; j < cursor.getCount(); j++){
                    cursor.moveToNext();
                    if (categories.contains(cursor.getString(0))){ // check if category is selected by user
                        // add crimes for that category and year to bar chart
                        visibleCategories.add(cursor.getString(0));
                        yData.add(cursor.getString(1));
                    }

                }

                cursor.close();
            } catch (Exception ignored){ }

        return yData;
    }

}
