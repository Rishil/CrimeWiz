package com.github.rishil.crimewiz.base;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.rishil.crimewiz.R;
import com.github.rishil.crimewiz.core.services.FirebaseService;
import com.github.rishil.crimewiz.core.util.DatabaseHelper;
import com.github.rishil.crimewiz.features.FilterActivity;
import com.github.rishil.crimewiz.features.DashboardActivity;
import com.github.rishil.crimewiz.features.LoginActivity;
import com.github.rishil.crimewiz.features.MapActivity;
import com.github.rishil.crimewiz.features.SettingsActivity;
import com.github.rishil.crimewiz.features.charts.ChartActivity;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION;


public class BaseActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener{

    protected SharedPreferences sharedPreferences;

    protected Toolbar toolbar;
    protected ActionBarDrawerToggle actionBarDrawerToggle;
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected TextView filtersTextView, emailTextView;

    protected View navHeaderView;

    protected DatabaseHelper databaseHelper;
    protected SQLiteDatabase sqLiteDatabase;

    protected FirebaseService firebaseService;
    private DatabaseReference firebaseDatabase;

    private static final int LOCATION_PERMISSION = 100;
    private Boolean loginActivityRunning = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        sharedPreferences = getSharedPreferences("userSettings", MODE_PRIVATE);
        loadUi();

        if (!sharedPreferences.getBoolean("locationPreference", false)){
            requestLocationPermission(this);
        }

        loadDatabase();

        firebaseService = new FirebaseService(this);
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();


        if (!getActivityName().contains("LoginActivity") && !sharedPreferences.getBoolean("deleteAccount", false)){
            refreshUserInfo();

        }

        try {
            checkConnection();
        } catch (Exception ignored) {}

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // prevent display from sleeping
    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            if(!loginActivityRunning && !firebaseService.isLoggedIn()) {
                    startLoginActivity();
                    loginActivityRunning = true;
            }


            if (!sharedPreferences.getBoolean("deleteAccount", false)){
                refreshUserInfo();

            }
            checkConnection();
        } catch (Exception e) {
            Log.e("NetworkService", "No network connection");
        }
    }

    private void refreshUserInfo(){
        String email = sharedPreferences.getString("email", null);
        if (email != null) {
            emailTextView = navHeaderView.findViewById(R.id.emailHeaderTextView);
            emailTextView.setText(email);

            final boolean firstLaunch = sharedPreferences.getBoolean("firstLaunch", false);
            if (sharedPreferences.getBoolean("infoSet", false)){
                    firebaseDatabase.child("users").child(firebaseService.getUserId()).child("saved_categories").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {

                                String val = Objects.requireNonNull(snapshot.getValue()).toString()
                                        .replace("[", "").replace("]", "")
                                        .replace(" ", "");

                                List<String> list = new ArrayList<>(Arrays.asList(val.split(",")));
                                Set<String> mySet = new HashSet<>(list);
                                if (!firstLaunch) {
                                    sharedPreferences.edit().putStringSet("saved_categories", mySet).apply();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });


                    firebaseDatabase.child("users").child(firebaseService.getUserId()).child("saved_months").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {
                            String val = Objects.requireNonNull(snapshot.getValue()).toString()
                                    .replace("[", "").replace("]", "")
                                    .replace(" ", "");

                            List<String> list = new ArrayList<>(Arrays.asList(val.split(",")));
                            Set<String> mySet = new HashSet<>(list);
                            if (!firstLaunch) {
                                sharedPreferences.edit().putStringSet("saved_months", mySet).apply();
                            }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });

                    firebaseDatabase.child("users").child(firebaseService.getUserId()).child("saved_years").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {
                            String val = Objects.requireNonNull(snapshot.getValue()).toString()
                                    .replace("[", "").replace("]", "")
                                    .replace(" ", "");

                            List<String> list = new ArrayList<>(Arrays.asList(val.split(",")));
                            Set<String> mySet = new HashSet<>(list);
                            if (!firstLaunch) {
                                sharedPreferences.edit().putStringSet("saved_years", mySet).apply();
                            }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
            }


        }

    }

    protected String getActivityName(){
        return this.getClass().getName();
    }


    protected void loadDatabase(){
        databaseHelper = new DatabaseHelper(this);
        sqLiteDatabase = databaseHelper.getWritableDatabase();
    }

    protected void loadUi(){
        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navHeaderView = navigationView.inflateHeaderView(R.layout.nav_header_layout);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout, toolbar, R.string.open_nav_drawer, R.string.close_nav_drawer);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        filtersTextView = findViewById(R.id.filtersTextView);

        filtersTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    giveHapticFeedback();
                    Intent filterActivity = new Intent(getApplicationContext(), FilterActivity.class);
                    startActivity(filterActivity);
                }
            });
    }

    protected void startHomeActivity(){
            Intent homeActivity = new Intent(getApplicationContext(), DashboardActivity.class);
            homeActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeActivity);
    }

    protected void startLoginActivity(){
        Intent loginActivity = new Intent(this, LoginActivity.class);
        loginActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginActivity);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_home:
                giveHapticFeedback();
                Intent homeActivity = new Intent(getApplicationContext(), DashboardActivity.class);
                homeActivity.addFlags(FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(homeActivity);
                break;
            case R.id.menu_map:
                giveHapticFeedback();
                Intent mapActivity = new Intent(getApplicationContext(), MapActivity.class);
                mapActivity.addFlags(FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(mapActivity);
                break;
            case R.id.menu_charts:
                giveHapticFeedback();
                Intent chartActivity = new Intent(getApplicationContext(), ChartActivity.class);
                chartActivity.addFlags(FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(chartActivity);
                break;
            case R.id.menu_settings:
                giveHapticFeedback();
                Intent settingsActivity = new Intent(getApplicationContext(), SettingsActivity.class);
                settingsActivity.addFlags(FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(settingsActivity);
                break;

            case R.id.menu_logout:
                giveHapticFeedback();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setMessage("Are you sure you want log out?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog
                        onBackPressed();
                        giveHapticFeedback();
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Logged Out", Toast.LENGTH_SHORT).show();
                        firebaseService.logout();
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        giveHapticFeedback();
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                setDialogButtons(alertDialog);

                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    public void saveCategories(){
        // save categories
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> savedCategoriesSet = new HashSet<>(databaseHelper.getCategories());
        if (sharedPreferences.getStringSet("saved_categories", null) == null){
            editor.putStringSet("saved_categories", savedCategoriesSet);
            editor.apply();
            firebaseService.saveFirebasePreferences("saved_categories", savedCategoriesSet);
        }
    }

    public void saveYears(){
        // save years
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> savedYearsSet = new HashSet<>(databaseHelper.getYears());

        if (sharedPreferences.getStringSet("saved_years", null) == null){
            editor.putStringSet("saved_years", savedYearsSet);
            editor.apply();
            firebaseService.saveFirebasePreferences("saved_years", savedYearsSet);

        }
    }

    public void saveMonths(){
        // save months
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> savedMonthsSet = new HashSet<>(databaseHelper.getMonths());

        if (sharedPreferences.getStringSet("saved_months", null) == null){
            editor.putStringSet("saved_months", savedMonthsSet);
            editor.apply();
            firebaseService.saveFirebasePreferences("saved_months", savedMonthsSet);

        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        if (getActivityName().contains("LoginActivity")){

            String error = sharedPreferences.getString("loginError", null);

            if (firebaseService.isRegistering() && error !=null){
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                    sharedPreferences.edit().remove("loginError").apply();
            }

            if (firebaseService.isLoggedIn()){
                startHomeActivity();
                Toast.makeText(this, "Logged in", Toast.LENGTH_SHORT).show();
            } else if (!firebaseService.isRegistering() && firebaseService.isLoggingIn() && error !=null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                sharedPreferences.edit().remove("loginError").apply();

            }
        }

        if (!firebaseService.isLoggedIn() && !(getActivityName().contains("LoginActivity"))) {
            sharedPreferences.edit().putBoolean("locationPreference", true).apply();
            startLoginActivity();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }


    public void checkConnection() {
        if (!getActivityName().contains("LoginActivity") || !getActivityName().contains("FilterActivity")){
            View currentView = getWindow().getDecorView();
            TextView offlineNetwork = currentView.findViewById(R.id.offlineNetwork);

            ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if (activeNetwork != null) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    // wifi
                    offlineNetwork.setVisibility(View.GONE);
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    // mobile data
                    offlineNetwork.setVisibility(View.GONE);
                }
            } else {
                // no connection
                offlineNetwork.setVisibility(View.VISIBLE);
            }
        }


    }

    public void giveHapticFeedback(){
        View currentView = getWindow().getDecorView();
        currentView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    }

    protected void requestLocationPermission(final Activity activity){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.location_request));
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(activity,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        LOCATION_PERMISSION);
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        setDialogButtons(alertDialog);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    sharedPreferences.edit().putBoolean("locationPreference", true).apply();

                }

            } else {
                sharedPreferences.edit().putBoolean("locationPreference", true).apply();
            }
        }
    }

    public void setDialogButtons(AlertDialog alertDialog){
        Button negativeButton =  alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setBackgroundColor(getResources().getColor(R.color.colorBlue));
        negativeButton.setPadding(10,10,10,10);


        Button positiveButton =  alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setBackgroundColor(getResources().getColor(R.color.colorBlue));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0,0,20,0);
        negativeButton.setLayoutParams(layoutParams);
    }

}
