package com.github.rishil.crimewiz.core.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import com.github.rishil.crimewiz.core.objects.Crime;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String CRIME_TABLE = "crime_table";
    public static final String CRIME_ID = "crime_id";
    public static final String DATE = "crime_date";
    public static final String CATEGORY = "crime_category";
    public static final String STREET = "crime_street";
    public static final String LATITUDE = "crime_latitude";
    public static final String LONGITUDE = "crime_longitude";

    private SQLiteDatabase sqLiteDatabase;

    public DatabaseHelper(Context context) {
        super(context, CRIME_TABLE, null, 1);
        sqLiteDatabase = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createCrimeTable = "CREATE TABLE " + CRIME_TABLE + " (" + CRIME_ID + " INTEGER PRIMARY KEY, " +
                DATE + " TEXT, " +
                CATEGORY + " TEXT, " +
                STREET + " TEXT, " +
                LATITUDE + " REAL, " +
                LONGITUDE + " REAL)";;
        sqLiteDatabase.execSQL(createCrimeTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CRIME_TABLE);
        onCreate(sqLiteDatabase);

    }


    boolean addData(Crime crime){
        int id = crime.getCrimeId();
        String date = crime.getDate();
        String category = crime.getCategory();
        String street = crime.getStreetName();
        double latitude = crime.getLatitude();
        double longitude = crime.getLongitude();
        long result;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        String query = "SELECT * FROM " + CRIME_TABLE + " WHERE " + CRIME_ID + " = " + id;
        Cursor c = db.rawQuery(query, null);

        if(c.getCount()>0) {
            // duplicate value so do nothing
            result = -1;
        } else {
            contentValues.put(CRIME_ID, id);
            contentValues.put(DATE, date + "-01");
            contentValues.put(CATEGORY, category);
            contentValues.put(STREET, street);
            contentValues.put(LATITUDE, latitude);
            contentValues.put(LONGITUDE, longitude);

            result = db.insert(CRIME_TABLE, null, contentValues);
        }
        c.close();
        return result != -1;
    }

    public Cursor getData(){
        String query = "SELECT * FROM " + CRIME_TABLE;
        return sqLiteDatabase.rawQuery(query, null);
    }

    public ArrayList<String> getCategories(){
        ArrayList<String> categories = new ArrayList<>();

        String query = "SELECT DISTINCT " + CATEGORY + " FROM " + CRIME_TABLE;

        Cursor categoryData = sqLiteDatabase.rawQuery(query, null);

        while (categoryData.moveToNext()){
            categories.add(categoryData.getString(0));
        }
        return categories;
    }

    public ArrayList<String> getYears(){
        ArrayList<String> years = new ArrayList<>();

        String query = "SELECT strftime('%Y', crime_date) FROM " + CRIME_TABLE;

        Cursor yearData = sqLiteDatabase.rawQuery(query, null);

        while (yearData.moveToNext()) {
            if (!years.contains(yearData.getString(0))){
                years.add(yearData.getString(0));
            }
        }

        yearData.close();

        return years;
    }

    public ArrayList<String> getMonths(){
        ArrayList<String> months = new ArrayList<>();
        String query = "SELECT strftime('%m', crime_date) FROM " + CRIME_TABLE +
                " ORDER BY strftime('%m', crime_date)";

        Cursor monthData = sqLiteDatabase.rawQuery(query, null);

        while (monthData.moveToNext()) {
            if (!months.contains(monthData.getString(0))){
                months.add(monthData.getString(0));
            }
        }

        monthData.close();
        return months;
    }

    public boolean deleteTable() {
        // delete the crime table
        SQLiteDatabase database = getReadableDatabase();
        int affectedRows = database.delete(CRIME_TABLE, null, null);
        return affectedRows > 0;
    }
}
