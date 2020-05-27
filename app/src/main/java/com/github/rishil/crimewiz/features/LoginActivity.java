package com.github.rishil.crimewiz.features;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.rishil.crimewiz.R;
import com.github.rishil.crimewiz.base.BaseActivity;

public class LoginActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    private EditText emailEditText, passwordEditText;
    private boolean isPasswordSecure;
    private ProgressBar authProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        try {
            loadUi();
        } catch (Exception e) {
            Log.d("FirebaseService", "couldn't load ui");
        }

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        authProgressBar = findViewById(R.id.authProgressBar);

        final View passwordStrengthView = findViewById(R.id.passwordStrength);
        final TextView strengthTextView = findViewById(R.id.strengthTextView);

        TextWatcher strengthChecker = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // no password
                isPasswordSecure = false;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String passwordStrength = CredentialStrength.checkPasswordStrength(passwordEditText.getText().toString());
                int passwordLength = passwordEditText.getText().length();
                if (passwordLength >= 9){
                    if (passwordStrength.equals("Strong")){
                        passwordStrengthView.setBackgroundColor(getResources().getColor(R.color.light_green));
                        strengthTextView.setText(passwordStrength);
                        isPasswordSecure = true;
                    } else if (passwordStrength.equals("Medium")){
                        passwordStrengthView.setBackgroundColor(getResources().getColor(R.color.orange));
                        strengthTextView.setText(passwordStrength);
                        isPasswordSecure = true;
                    }
                } else if (passwordLength == 0){
                    passwordStrengthView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    strengthTextView.setText("");
                } else {
                    passwordStrengthView.setBackgroundColor(getResources().getColor(R.color.light_red));
                    int needs = 8 - passwordLength;
                    String weakText;
                    if (needs > 1){
                        weakText = "Weak. Needs " + needs + " more characters";
                        isPasswordSecure = false;
                    } else if (needs == 1) {
                        weakText = "Weak. Needs " + needs + " more character";
                        isPasswordSecure = false;
                    } else {
                        weakText = "Weak.";
                        isPasswordSecure = true;
                    }
                    strengthTextView.setText(weakText);
                }
            }
        };

        passwordEditText.addTextChangedListener(strengthChecker);

        actionBarDrawerToggle.setDrawerIndicatorEnabled(false);

        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            if (!sharedPreferences.getBoolean("deleteAccount", false)){
                startHomeActivity();
            } else {
                // do nothing
            }
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                if (sharedPreferences.getBoolean("isLoggedIn", false)){
                    authProgressBar.setVisibility(View.GONE);
                }
                if (sharedPreferences.getBoolean("loginException", false)){
                    authProgressBar.setVisibility(View.GONE);
                }
                sharedPreferences.edit().remove("loginException").apply();

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (sharedPreferences.getBoolean("deleteAccount", false)){
            DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
            sharedPreferences.edit().putBoolean("isLoggedIn", false).apply();
            sharedPreferences.edit().remove("email").apply();

            firebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot data) {
                    if (data.child("users").child(firebaseService.getUserId()).exists()) {
                        sharedPreferences.edit().clear().apply();
                        sharedPreferences.edit().putBoolean("locationPreference", true).apply();
                        data.child("users").child(firebaseService.getUserId()).getRef().removeValue();
                        firebaseService.logout();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // do nothing
                }
            });
            sharedPreferences.edit().remove("deleteAccount").apply();
        }
        authProgressBar.setVisibility(View.GONE);

    }

    public void onClickLogin(View v){
        authProgressBar.setVisibility(View.VISIBLE);
        sharedPreferences.edit().putBoolean("isLoggingIn", true).apply();
        String email, password = null;

        try {
            email = emailEditText.getText().toString();
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                password = passwordEditText.getText().toString();
                if (password.length() > 0){
                    sharedPreferences.edit().remove("isLoggedIn").apply();
                    firebaseService.login(email, password);
                } else {
                    authProgressBar.setVisibility(View.GONE);
                }
            } else {
                authProgressBar.setVisibility(View.GONE);
            }
        } catch (Exception e){
            Log.e("LoginService", "Login exception");
            authProgressBar.setVisibility(View.GONE);
        }
        giveHapticFeedback();
    }

    public void onClickCreateAccount(View v){
        authProgressBar.setVisibility(View.VISIBLE);
        sharedPreferences.edit().putBoolean("isRegistering", true).apply();
        sharedPreferences.edit().putBoolean("firstLaunch", true).apply();
        String email;
        String password;


        try {
            email = emailEditText.getText().toString();
            password = passwordEditText.getText().toString();
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                password = passwordEditText.getText().toString();
                if (isPasswordSecure){
                    firebaseService.createAccount(email, password);
                } else {
                    authProgressBar.setVisibility(View.GONE);
                    if (password.length() == 0){
                        Toast.makeText(getApplicationContext(), "Please enter a password.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Your password does not meet the" +
                                        " security requirements.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                authProgressBar.setVisibility(View.GONE);
                if (email.length() == 0) {
                    if (password.length() == 0) {
                        Toast.makeText(getApplicationContext(), "Please enter registration details " +
                                "in the Email and Password fields", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Please enter an email address",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please check the details that you" +
                            " have entered.", Toast.LENGTH_SHORT).show();
                }

            }
        } catch (Exception e){
            authProgressBar.setVisibility(View.GONE);
            Log.e("LoginService", "Registration exception");
        }
        giveHapticFeedback();
    }

    private static class CredentialStrength {
        static String checkPasswordStrength(String password) {
            boolean containsUppercase = false;
            boolean containsLowercase = false;
            boolean containsSymbols = false;
            boolean containsNumber = false;
            int passwordLength = 0;

            passwordLength = password.length();

            for (int i = 0; i < passwordLength; i++) {
                char singleChar = password.charAt(i);
                // if we have an uppercase letter
                if (singleChar == Character.toUpperCase(singleChar)) {
                    containsUppercase = true;
                }

                // if we have a lowercase letter
                if (singleChar == Character.toLowerCase(singleChar)) {
                    containsLowercase = true;
                }

                // has special characters
                Pattern special = Pattern.compile("[^a-z0-9 ]",
                        Pattern.CASE_INSENSITIVE);
                Matcher symbols = special.matcher(Character.toString(singleChar));
                if (symbols.find()) {
                    containsSymbols = true;
                }

                // has digits
                if (Character.isDigit(singleChar)) {
                    containsNumber = true;
                }
            }

            if (containsUppercase && containsLowercase && containsSymbols && containsNumber) {
                return "Strong";
            } else if (containsUppercase && containsLowercase && containsSymbols) {
                return "Medium";
            } else if (containsUppercase && containsLowercase && containsNumber) {
                return "Medium";
            } else {
                return "Weak";
            }
        }

    }

}
