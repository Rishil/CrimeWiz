package com.github.rishil.crimewiz.features.charts.scatter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

import com.github.rishil.crimewiz.R;
import com.github.rishil.crimewiz.core.util.DatabaseHelper;
import com.github.rishil.crimewiz.core.util.LinearRegression;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RegressionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class RegressionFragment extends Fragment{

    private OnFragmentInteractionListener mListener;

    private CombinedChart combinedChart;
    private LinearRegression linearRegression;

    private XAxis xAxis;
    private SQLiteDatabase sqLiteDatabase;
    private SharedPreferences sharedPreferences;

    private View currentView;
    private DatabaseHelper databaseHelper;

    private ArrayList<String> categories, months, years, visibleCategories;

    private StringBuilder year, month, selectedCategories;

    private String regressionLine, averageNumberOfCrimes;

    TextView noDataTextView, xLabelTextView;
    LinearLayout yLinearLayout;

    private TextView regressionLineTextView, averageCrimesTextView, predictedOutputTextView;
    private Button clearButton;
    private EditText crimePredictEditText;

    private ArrayList<Double> yearsXValues, crimesYValues;
    public RegressionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        currentView = inflater.inflate(R.layout.fragment_regression_line, container, false);
        setRetainInstance(true);

        updateData();

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

            sharedPreferences.edit().putBoolean("regressionFragmentFirstLaunch", true).apply();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean categoryFragmentFirstLaunch =
                sharedPreferences.getBoolean("regressionFragmentFirstLaunch", false);

        boolean returnFromFilterView =
                sharedPreferences.getBoolean("returnFromFilterView", false);

        boolean dataSetHasChanged=
                sharedPreferences.getBoolean("dataSetHasChanged", false);

        if (categoryFragmentFirstLaunch || returnFromFilterView || dataSetHasChanged){
            updateData();
            sharedPreferences.edit().putBoolean("regressionFragmentFirstLaunch", false).apply();
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
                    updateUi();
                } catch (Exception e){
                    e.printStackTrace();
                }}
        }).start();

    }

    private void loadPreferences(){
        years = new ArrayList<>();
        months = new ArrayList<>();
        categories = new ArrayList<>();

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


        Set<String> categoriesSet = sharedPreferences.getStringSet("saved_categories", null);
        if (categoriesSet != null) {
            categories.addAll(categoriesSet);
        }

        selectedCategories = new StringBuilder();
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

        month = new StringBuilder();
        for (int i = 0; i < months.size(); i++){
            if (i == 0){
                month.append("\'").append(months.get(i)).append("\'");
            } else {
                month.append(" OR strftime('%m',").append(DatabaseHelper.DATE).append(") = \'").append(months.get(i)).append("\'");
            }
        }
    }


    private void updateUi(){
        regressionLineTextView  = currentView.findViewById(R.id.regressionLineTextView);
        averageCrimesTextView = currentView.findViewById(R.id.averageCrimesTextView);

        combinedChart = currentView.findViewById(R.id.combinedChart);
        combinedChart.getDescription().setEnabled(false);

        crimePredictEditText = currentView.findViewById(R.id.crimePredictEditText);
        crimePredictEditText.setImeActionLabel("Predict", KeyEvent.KEYCODE_ENTER);

        predictedOutputTextView = currentView.findViewById(R.id.predictCrimesTextView);
        clearButton = currentView.findViewById(R.id.clearButton);

        xAxis = combinedChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf(Math.round(value)).replaceAll(",", "");
            }
        });



        crimePredictEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                float input = Float.parseFloat(textView.getText().toString());
                double prediction = linearRegression.getPredictedNumberOfCrimes(input);
                if (prediction <=0) prediction = 0;

                String output = "" + (int) Math.round(prediction);
                predictedOutputTextView.setText(output);
                predictedOutputTextView.setGravity(Gravity.CENTER);
                crimePredictEditText.setVisibility(View.GONE);
                clearButton.setVisibility(View.VISIBLE);

                InputMethodManager imm = (InputMethodManager) Objects
                        .requireNonNull(getContext())
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);

                return true;
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                predictedOutputTextView.setText(getString(R.string.predict_default_text));
                crimePredictEditText.setVisibility(View.VISIBLE);
                clearButton.setVisibility(View.GONE);
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }
        });



        Objects.requireNonNull(this.getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    regressionLineTextView.setText(regressionLine);
                    averageCrimesTextView.setText(averageNumberOfCrimes);


                    if (yearsXValues.size() > 1){
                        CombinedData data = new CombinedData();
                        data.setData(generateScatterData());
                        data.setData(generateLineData());

                        combinedChart.clear();
                        combinedChart.setData(data);
                        combinedChart.invalidate();
                    } else {
                        combinedChart.clear();
                    }



                } catch (Exception e){
                    regressionLineTextView.setText(R.string.no_data);
                    averageCrimesTextView.setText(R.string.no_data);

                    e.printStackTrace();
                }
            }
        });



    }
    private void addDataToGraph() {
        loadPreferences();
        yearsXValues = new ArrayList<>();
        crimesYValues = new ArrayList<>();

        String query = "SELECT " + DatabaseHelper.DATE+ ", COUNT(" + DatabaseHelper.DATE
                + ") FROM " + DatabaseHelper.CRIME_TABLE
                + " WHERE (strftime('%Y'," + DatabaseHelper.DATE + ") = " + year + ")"
                + " AND (strftime('%m'," + DatabaseHelper.DATE + ") = " + month + ")"
                + " AND (" + DatabaseHelper.CATEGORY + " = " + selectedCategories + ")"
                + " GROUP BY strftime('%Y'," + DatabaseHelper.DATE + ")"
                + " ORDER BY COUNT (" + DatabaseHelper.DATE + ")";

        try {
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
            for(int i = 0; i < cursor.getCount(); i++){
                cursor.moveToNext();

                Double year = Double.parseDouble(cursor.getString(0).substring(0,4));
                String totalString = "" + cursor.getString(1);
                if (!yearsXValues.contains(year)){
                    yearsXValues.add(year);
                    crimesYValues.add(Double.parseDouble(totalString));
                }

            }
            cursor.close();
        } catch (Exception ignored) {}

        // calculate regression line
        linearRegression = new LinearRegression(yearsXValues, crimesYValues);
        regressionLine = linearRegression.getRegressionLine();
        averageNumberOfCrimes = "" + Math.round(linearRegression.getMeanNumberOfCrimes());

     }

     private ScatterData generateScatterData() {
         ScatterData scatterData = new ScatterData();
         ArrayList<Entry> scatterEntries = new ArrayList<>();
         for (int i = 0; i < yearsXValues.size(); i++) {
             scatterEntries.add(new Entry((float) Math.round(yearsXValues.get(i)), (float) Math.round(crimesYValues.get(i))));
         }

         ScatterDataSet set = new ScatterDataSet(scatterEntries, "Crime Data");
         set.setColor(getResources().getColor(R.color.light_green));
         set.setScatterShapeSize(7.5f);
         set.setDrawValues(false);
         set.setValueTextSize(10f);
         scatterData.addDataSet(set);
         xAxis.setAxisMinimum((float) Math.round(yearsXValues.get(0))-3);
         xAxis.setAxisMaximum((float) Math.round(yearsXValues.get(yearsXValues.size()-1))+8);


         return scatterData;
     }

     private LineData generateLineData(){

         float minYear = Math.round(yearsXValues.get(0))-30;
         float maxYear = Math.round(yearsXValues.get(yearsXValues.size()-1)+30);

         LineData lineData = new LineData();
         ArrayList<Entry> entries = new ArrayList<>();

         for(float i = minYear; i < maxYear; i++){
             double crimesDouble = linearRegression.getPredictedNumberOfCrimes(i);
             float crimesFloat = (float) crimesDouble;
             entries.add(new Entry(i,crimesFloat));
         }

         LineDataSet lineDataSet = new LineDataSet(entries, "Regression Line");
         lineDataSet.setDrawCircles(false);
         lineDataSet.setColor(getResources().getColor(R.color.dark_blue));
         lineDataSet.setLineWidth(2.5f);
         lineDataSet.setFillColor(getResources().getColor(R.color.dark_blue));
         lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
         lineDataSet.setDrawValues(false);
         lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
         lineData.addDataSet(lineDataSet);

         return lineData;
     }



}