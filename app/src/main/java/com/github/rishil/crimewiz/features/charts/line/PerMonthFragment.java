package com.github.rishil.crimewiz.features.charts.line;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import com.github.rishil.crimewiz.R;
import com.github.rishil.crimewiz.core.util.DatabaseHelper;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PerMonthFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class PerMonthFragment extends Fragment{

    private OnFragmentInteractionListener mListener;

    private LineChart lineChart;
    private SQLiteDatabase sqLiteDatabase;
    private SharedPreferences sharedPreferences;

    private PieData data;
    private View currentView;
    private DatabaseHelper databaseHelper;

    private ArrayList<String> categories, years, months, visibleCategories;

    TextView noDataTextView, xLabelTextView;
    LinearLayout yLinearLayout;

    public PerMonthFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        currentView = inflater.inflate(R.layout.fragment_per_month_line, container, false);
        setRetainInstance(true);



        lineChart = currentView.findViewById(R.id.lineChart);

        updateData();


        lineChart.getXAxis().setGranularityEnabled(true);
        lineChart.setDrawGridBackground(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.setExtraOffsets(5,10,5,5);
        lineChart.setDragDecelerationFrictionCoef(0.95f);

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

            sharedPreferences.edit().putBoolean("monthFragmentFirstLaunch", true).apply();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        boolean categoryFragmentFirstLaunch =
                sharedPreferences.getBoolean("monthFragmentFirstLaunch", false);

        boolean returnFromFilterView =
                sharedPreferences.getBoolean("returnFromFilterView", false);

        boolean dataSetHasChanged=
                sharedPreferences.getBoolean("dataSetHasChanged", false);

        if (categoryFragmentFirstLaunch || returnFromFilterView || dataSetHasChanged){
            updateData();
            sharedPreferences.edit().putBoolean("monthFragmentFirstLaunch", false).apply();
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


    private void addDataToGraph() {
        categories = new ArrayList<>();
        Set<String> categoriesSet = sharedPreferences.getStringSet("saved_categories", null);
        if (categoriesSet != null) {
            categories.addAll(categoriesSet);
        }

        StringBuilder selectedCategories = new StringBuilder();
        for (int i = 0; i < categories.size(); i++) {
            if (i == 0) {
                selectedCategories.append("\'").append(categories.get(i)).append("\'");
            } else {
                selectedCategories.append(" OR " + DatabaseHelper.CATEGORY + " = \'").append(categories.get(i)).append("\'");
            }
        }


        years = new ArrayList<>();
        Set<String> yearsSet = sharedPreferences.getStringSet("saved_years", null);
        if (yearsSet != null) {
            years.addAll(yearsSet);
        }

        months = new ArrayList<>();
        Set<String> monthsSet = sharedPreferences.getStringSet("saved_months", null);
        if (monthsSet != null) {
            months.addAll(monthsSet);
        }


        ArrayList<LineDataSet> lines = new ArrayList<>();

        lineChart.clear();


        StringBuilder month = new StringBuilder();
        for (int i = 0; i < months.size(); i++){
            if (i == 0){
                month.append("\'").append(months.get(i)).append("\'");
            } else {
                month.append(" OR strftime('%m',").append(DatabaseHelper.DATE).append(") = \'").append(months.get(i)).append("\'");
            }
        }

        for (int i = 0; i < years.size(); i++) {
            ArrayList<String> entries = new ArrayList<>();
            ArrayList<Entry> lineDataEntries = new ArrayList<>();
            int yearInt = Integer.parseInt(years.get(i));

            String query = "SELECT " + DatabaseHelper.DATE + ", COUNT(" + DatabaseHelper.CATEGORY
                    + "), " + DatabaseHelper.CATEGORY + " FROM " + DatabaseHelper.CRIME_TABLE
                    + " WHERE (strftime('%Y'," + DatabaseHelper.DATE + ") = " + "\'" + years.get(i) + "\')"
                    + " AND (strftime('%m'," + DatabaseHelper.DATE + ") = " + month + ")"
                    + " AND (" + DatabaseHelper.CATEGORY + " = " + selectedCategories + ")"
                    + " GROUP BY " + DatabaseHelper.DATE
                    + " ORDER BY " + DatabaseHelper.DATE;


            Cursor cursor = sqLiteDatabase.rawQuery(query, null);

            for (int j = 0; j < cursor.getCount(); j++) {
                cursor.moveToNext();

                String yearString = cursor.getString(0);
                float totalNumberOfCrimes = Float.parseFloat(cursor.getString(1));

                if (!entries.contains(yearString)) {
                    entries.add(j, cursor.getString(0));
                    if (yearString.contains(yearInt + "-01")) {
                        lineDataEntries.add(new Entry(0, totalNumberOfCrimes));
                    } else if (yearString.contains(yearInt + "-02")) {
                        lineDataEntries.add(new Entry(1, totalNumberOfCrimes));
                    } else if (yearString.contains(yearInt + "-03")) {
                        lineDataEntries.add(new Entry(2, totalNumberOfCrimes));
                    } else if (yearString.contains(yearInt + "-04")) {
                        lineDataEntries.add(new Entry(3, totalNumberOfCrimes));
                    } else if (yearString.contains(yearInt + "-05")) {
                        lineDataEntries.add(new Entry(4, totalNumberOfCrimes));
                    } else if (yearString.contains(yearInt + "-06")) {
                        lineDataEntries.add(new Entry(5, totalNumberOfCrimes));
                    } else if (yearString.contains(yearInt + "-07")) {
                        lineDataEntries.add(new Entry(6, totalNumberOfCrimes));
                    } else if (yearString.contains(yearInt + "-08")) {
                        lineDataEntries.add(new Entry(7, totalNumberOfCrimes));
                    } else if (yearString.contains(yearInt + "-09")) {
                        lineDataEntries.add(new Entry(8, totalNumberOfCrimes));
                    } else if (yearString.contains(yearInt + "-10")) {
                        lineDataEntries.add(new Entry(9, totalNumberOfCrimes));
                    } else if (yearString.contains(yearInt + "-11")) {
                        lineDataEntries.add(new Entry(10, totalNumberOfCrimes));
                    } else if (yearString.contains(yearInt + "-12")) {
                        lineDataEntries.add(new Entry(11, totalNumberOfCrimes));
                    } else {
                        Log.e("CursorError", "ELSE CLAUSE");
                    }

                }

            }
            LineDataSet lineDataSet = new LineDataSet(lineDataEntries, "" + yearInt);
            lineDataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.valueOf((int) Math.floor(value));
                }
            });

            Random rand = new Random();
            int randomColor = Color.rgb(rand.nextInt(255),
                    rand.nextInt(255),rand.nextInt(255));

            lineDataSet.setColor(randomColor);
            lineDataSet.setCircleColor(randomColor);
            lineDataSet.setDrawFilled(true);
            lineDataSet.setFillColor(randomColor);
            lineDataSet.setFillAlpha(50);
            lines.add(lineDataSet);
        }

            final String[] xVals = new String[] {"January", "February", "March",
                    "April", "May", "June", "July", "August", "September", "October",
                    "November", "December"};

            try{
                lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        if (value == -1){// null
                            return "No data";
                        } else {
                            return xVals[(int)value];

                        }
                    }
                });
            } catch (Exception ignored){}
            lineChart.getXAxis().setLabelRotationAngle(-90);

            ArrayList<ILineDataSet> allDataSets = new ArrayList<ILineDataSet>(lines);
            LineData data = new LineData(allDataSets);
            lineChart.setData(data);
            lineChart.invalidate();
            lineChart.refreshDrawableState();

    }


}