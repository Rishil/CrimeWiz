package com.github.rishil.crimewiz.features;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;


import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.github.rishil.crimewiz.R;
import com.github.rishil.crimewiz.base.BaseActivity;

public class SearchActivity extends BaseActivity {

    private TextView searchResultsTextView;
    private SearchView searchView;
    private ListView searchResultsListView;
    private ListView recentSearchesListView;
    private TextView recentSearchesTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchResultsTextView = findViewById(R.id.searchResultsTextView);
        searchView = findViewById(R.id.searchView);
        searchResultsListView = findViewById(R.id.resultsListView);
        recentSearchesListView = findViewById(R.id.recentSearchesListView);
        recentSearchesTextView = findViewById(R.id.recentTextView);


        final Geocoder geocoder = new Geocoder(this);

        searchView.setSubmitButtonEnabled(true);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setIconified(false);
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String searchParams = searchView.getQuery().toString();
                try {

                    final List<Address> results = geocoder.getFromLocationName(searchParams, 10);
                    Log.d("Results", results.toString());


                    ArrayList<String> searchResults = new ArrayList<>();
                    final ArrayList<LatLng> location = new ArrayList<>();

                    for (int i = 0; i < results.size(); i++){
                        if (results.get(i).getPostalCode() != null){
                            searchResults.add(results.get(i).getPostalCode());
                        } else {
                            searchResults.add(results.get(i).getFeatureName() + ", " +
                                    results.get(i).getSubAdminArea());
                        }

                        LatLng latLng = new LatLng(results.get(i).getLatitude(), results.get(i).getLongitude());
                        location.add(latLng);
                    }


                    showResults(searchResults);


                    searchResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Intent mapActivity = new Intent(view.getContext(), MapActivity.class);

                            mapActivity.putExtra("LatLng", location.get(i));

                            ArrayList<String> savedLocations = new ArrayList<>();

                            Set<String> set = sharedPreferences.getStringSet("searchHistory", null);
                            if (set != null) {
                                savedLocations.addAll(set);
                            }

                            String name = (String) adapterView.getItemAtPosition(i);
                            String valueToCheck = name + ",[" + location.get(i).latitude + ","
                                    + location.get(i).longitude + "]";

                            if (!savedLocations.contains(valueToCheck)){
                                savedLocations.add(valueToCheck);
                                Set<String> savedLocationsSet = new HashSet<>(savedLocations);
                                sharedPreferences.edit().putStringSet("searchHistory",
                                        savedLocationsSet).apply();
                                firebaseService.saveFirebasePreferences("search_history", savedLocationsSet);
                            }

                            sharedPreferences.edit().putBoolean("refreshMap", true).apply();
                            startActivity(mapActivity);


                        }
                    });


                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        // if firestore searches not null
        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseDatabase.getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                if (data.child("users").child(firebaseService.getUserId()).child("search_history").exists()){
                    String val = Objects.requireNonNull(data.child("users")
                            .child(firebaseService.getUserId()).child("search_history")
                            .getValue()).toString().trim().replaceAll("^\\[|\\]$", "");


                    List<String> list = new ArrayList<>(Arrays.asList(val.split("], ")));
                    for (int i = 0; i < list.size(); i++) {
                        if (!list.get(i).contains("]")) {
                            String element = list.get(i);
                            list.add(element + "]");
                            list.remove(list.get(i));
                        }
                    }

                    Set<String> mySet = new HashSet<>(list);
                    if (mySet.toString().contains(",")){
                        sharedPreferences.edit().putStringSet("searchHistory", mySet).apply();

                    }
                } else {
                    Set<String> set = new HashSet<>();
                    firebaseService.saveFirebasePreferences("search_history", set);
                    sharedPreferences.edit().putStringSet("searchHistory", null).apply();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (sharedPreferences.getStringSet("searchHistory", null) != null){
                Set<String> set = sharedPreferences.getStringSet("searchHistory", null);
                if (set != null) {
                    ArrayList<String> searchHistoryFromPrefs = new ArrayList<>(set);

                    ArrayList<String> locationNames = new ArrayList<>();
                    for (int i = 0; i < searchHistoryFromPrefs.size(); i++) {
                        String fullString = searchHistoryFromPrefs.get(i);
                        String location = fullString.substring(0, fullString.indexOf(","));

                        locationNames.add(location);
                    }

                    showRecentSearches(locationNames);

                    recentSearchesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            String location = (String) adapterView.getItemAtPosition(i);

                            ArrayList<String> searchHistoryFromPrefs = new
                                    ArrayList<>(Objects.requireNonNull(sharedPreferences
                                    .getStringSet("searchHistory", null)));

                            for (int j = 0; j < searchHistoryFromPrefs.size(); j++) {
                                String fullString = searchHistoryFromPrefs.get(j);

                                if (fullString.contains(location)) {
                                    String result = fullString.replace(location + " ", "");
                                    result = result + "]";
                                    result = result.substring(result.lastIndexOf("[") + 1, result.indexOf("]"));

                                    double lat = Double.parseDouble(result
                                            .substring(0, result.indexOf(",")));
                                    double lng = Double.parseDouble(result.
                                            substring(result.lastIndexOf(",") + 1));
                                    LatLng savedLatLng = new LatLng(lat, lng);
                                    Intent mapActivity = new Intent(view.getContext(), MapActivity.class);
                                    mapActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    mapActivity.putExtra("LatLng", savedLatLng);
                                    sharedPreferences.edit().putBoolean("refreshMap", true).apply();
                                    startActivity(mapActivity);
                                    finish();
                                }

                            }

                        }
                    });
                }
            } else {
                recentSearchesListView.setVisibility(View.GONE);
                recentSearchesTextView.setVisibility(View.GONE);
            }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sharedPreferences.getStringSet("searchHistory", null) !=null){
            recentSearchesListView.setVisibility(View.VISIBLE);
            recentSearchesTextView.setVisibility(View.VISIBLE);
        } else {
            recentSearchesListView.setVisibility(View.GONE);
            recentSearchesTextView.setVisibility(View.GONE);
        }
    }

    public void showRecentSearches(ArrayList<String> results){
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_list_item_1, results);
       recentSearchesListView.setAdapter(arrayAdapter);
       recentSearchesListView.setVisibility(View.VISIBLE);
       recentSearchesTextView.setVisibility(View.VISIBLE);
       arrayAdapter.notifyDataSetChanged();
    }

    public void showResults(ArrayList<String> results){
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_list_item_1, results);

        searchResultsListView.setAdapter(arrayAdapter);

        searchResultsTextView.setVisibility(View.VISIBLE);
        searchResultsListView.setVisibility(View.VISIBLE);
    }

    public void onClickBack(View view) {
        onBackPressed();
    }
}
