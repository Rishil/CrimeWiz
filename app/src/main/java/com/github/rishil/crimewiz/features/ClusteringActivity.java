package com.github.rishil.crimewiz.features;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

import com.github.rishil.crimewiz.R;
import com.github.rishil.crimewiz.base.BaseActivity;
import com.github.rishil.crimewiz.core.util.clustering.CrimeClusterIcon;

public class ClusteringActivity extends BaseActivity {
    private TableLayout clusterTableLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cluster);

        final ArrayList<CrimeClusterIcon> clusteredCrimes =
                getIntent().getParcelableArrayListExtra("clusteredCrimes");


        clusterTableLayout = findViewById(R.id.clusterTable);

        TextView multipleCrimesTextView = findViewById(R.id.multipleCrimesTextView);
        String title = clusteredCrimes.size() + " crimes";
        multipleCrimesTextView.setText(title);

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < clusteredCrimes.size(); i++) {
                        TableRow tableRow = new TableRow(getApplicationContext());

                        String category = clusteredCrimes.get(i).getTitle();
                        String date = clusteredCrimes.get(i).getDate();
                        String location = clusteredCrimes.get(i).getLocation()
                                .replaceAll("On or near ", "at ")
                                .replaceAll("Avenue", "Ave")
                                .replaceAll("Road", "Rd");

                        String catAndLocation = category + " " + location;

                        // date
                        TextView dateTextView = new TextView(getApplicationContext());
                        dateTextView.setText(date);
                        dateTextView.setGravity(Gravity.START);
                        dateTextView.setPadding(5, 0, 5, 0);
                        tableRow.addView(dateTextView);

                        // category and location
                        TextView categoryLocationTextView = new TextView(getApplicationContext());
                        categoryLocationTextView.setText(catAndLocation);
                        categoryLocationTextView.setGravity(Gravity.START);
                        categoryLocationTextView.setPadding(5, 0, 5, 0);
                        tableRow.addView(categoryLocationTextView);

                        clusterTableLayout.addView(tableRow);
                    }
                }
            };
            runnable.run();

    }

    public void onClickBack(View v){
        onBackPressed();
        sharedPreferences.edit().putBoolean("returnFromClusterView", true).apply();
    }
}
