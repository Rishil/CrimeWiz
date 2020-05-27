package com.github.rishil.crimewiz.core.services;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.github.rishil.crimewiz.core.util.DatabaseHelper;

public class FirebaseService {

    private SharedPreferences sharedPreferences;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference firebaseDatabase;

    private Activity context;

    public FirebaseService(Activity context){
        this.context = context;
        firebaseAuth = getInstance();
        sharedPreferences = context.getSharedPreferences("userSettings", Context.MODE_PRIVATE);
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();

    }

    public boolean isLoggedIn(){
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }

    public void logout(){
        firebaseAuth.signOut();
        sharedPreferences.edit().clear().apply();
        sharedPreferences.edit().putBoolean("locationPreference", true).apply();
        sharedPreferences.edit().putBoolean("isLoggedIn" , false).apply();
        setLogin(false);

    }

    private void setLogin(Boolean login){
        sharedPreferences.edit().putBoolean("isLoggedIn", login).apply();
    }

    public String getUserId(){
        return Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
    }


    public void login(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("FirebaseService", "signInWithEmail:success");
                            setLogin(true);
                            if (firebaseAuth.getCurrentUser() !=null){
                                sharedPreferences.edit().putString("email",
                                        firebaseAuth.getCurrentUser().getEmail()).apply();

                                getFirestorePreferences();
                            }

                        } else {
                            Log.w("FirebaseService", "signInWithEmail:failure", task.getException());
                            setLogin(false);
                            Exception exception = task.getException();
                            String msg;
                            if (exception != null) {
                                msg = exception.getMessage();
                                sharedPreferences.edit().putString("loginError", msg).apply();
                                sharedPreferences.edit().putBoolean("loginException", true).apply();
                            }

                        }
                        sharedPreferences.edit().putBoolean("isLoggingIn", false).apply();

                    }
                });

    }

    public void createAccount(String email, String password){
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("FirebaseService", "createUserWithEmail:success");
                            setLogin(true);
                            sharedPreferences.edit().putString("email",
                                    firebaseAuth.getCurrentUser().getEmail()).apply();
                            getFirestorePreferences();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("FirebaseService", "createUserWithEmail:failure", task.getException());
                            setLogin(false);

                            Exception exception = task.getException();
                            String msg;
                            if (exception != null) {
                                msg = exception.getMessage();
                                sharedPreferences.edit().putString("loginError", msg).apply();
                            }
                        }

                        sharedPreferences.edit().putBoolean("isRegistering", false).apply();
                    }
                });

    }

    public void saveFirebasePreferences(String key, Set<String> value){
        firebaseDatabase.child("users").child(getUserId()).child(key).setValue(value.toString());
    }

    public boolean isLoggingIn(){
        return sharedPreferences.getBoolean("isLoggingIn", false);
    }

    public boolean isRegistering(){
        return sharedPreferences.getBoolean("isRegistering", false);
    }

    private FirebaseAuth getInstance(){
        return FirebaseAuth.getInstance();
    }

    private void getFirestorePreferences(){
        firebaseDatabase.getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                if (data.child("users").child(getUserId()).exists()) {
                    sharedPreferences.edit().putBoolean("infoSet", true).apply();

                } else {
                    firebaseDatabase.child("users").child(getUserId());
                    DatabaseHelper databaseHelper = new DatabaseHelper(context);

                    Set<String> savedCategoriesSet = new HashSet<>(databaseHelper.getCategories());
                    Set<String> savedYearsSet = new HashSet<>(databaseHelper.getYears());
                    Set<String> savedMonthsSet = new HashSet<>(databaseHelper.getMonths());


                    saveFirebasePreferences("saved_categories", savedCategoriesSet);
                    saveFirebasePreferences("saved_years", savedYearsSet);
                    saveFirebasePreferences("saved_months", savedMonthsSet);
                    sharedPreferences.edit().putBoolean("infoSet", true).apply();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
