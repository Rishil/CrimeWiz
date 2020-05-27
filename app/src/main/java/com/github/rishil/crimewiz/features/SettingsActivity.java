package com.github.rishil.crimewiz.features;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Set;

import com.github.rishil.crimewiz.R;
import com.github.rishil.crimewiz.base.BaseActivity;

public class SettingsActivity extends BaseActivity {

    private DatabaseReference firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        loadUi();
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onClickClearDatabase(View v){
        giveHapticFeedback();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Are you sure you want to clear the database?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                boolean tableDeleted = databaseHelper.deleteTable();
                if (tableDeleted){
                    Toast.makeText(getApplicationContext(), "Database cleared",
                            Toast.LENGTH_SHORT).show();
                }
                giveHapticFeedback();
                dialog.dismiss();
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

        AlertDialog alert = builder.create();
        alert.show();
        setDialogButtons(alert);
    }

    public void onClickClearRecentSearches(View v){
        giveHapticFeedback();
        // clear
        firebaseDatabase.getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                if (data.child("users").child(firebaseService.getUserId()).child("search_history")
                        .exists()) {
                    data.child("users").child(firebaseService.getUserId()).child("search_history")
                            .getRef().removeValue();
                }
                Set<String> set = new HashSet<>();
                firebaseService.saveFirebasePreferences("search_history", set);
                sharedPreferences.edit().putStringSet("searchHistory", null).apply();
                Toast.makeText(getApplicationContext(), "Recent searches cleared."
                        , Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // do nothing
            }
        });
    }

    public void onClickDeleteAccount(View v){
        giveHapticFeedback();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Are you sure you want to delete your account? This process will" +
                            " take a couple of seconds to complete.");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // clear
                    firebaseDatabase.getRef().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot data) {
                            if (data.child("users").child(user.getUid()).exists()) {
                                data.child("users").child(user.getUid()).getRef().removeValue();
                            }
                    }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // do nothing
                        }
                    });

                    user.delete();
                    sharedPreferences.edit().putBoolean("deleteAccount", true).apply();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Error deleting the user. " +
                                    "Please try again"
                            , Toast.LENGTH_SHORT).show();
                }

                onBackPressed();
                giveHapticFeedback();
                dialog.dismiss();
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
    }
}
