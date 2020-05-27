package com.github.rishil.crimewiz.core.util;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.github.rishil.crimewiz.core.objects.Crime;

public class JsonParser {
    private String crimeCategory, crimeType, streetName;
    private double crimeLongitude, crimeLatitude;
    private long crimeDate;

    private Context context;
    private JSONArray jsonArray;

    private DatabaseHelper databaseHelper;

    public JsonParser(Context aContext){
        context = aContext;
        databaseHelper = new DatabaseHelper(context);
    }

    JsonParser(){

    }

    public List<Crime> parse(String jsonString){

        List<Crime> crimesList = new ArrayList<>();

        try {
            jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject crimeObject = (JSONObject) jsonArray.get(i);
                Crime crime = new Crime();

                // if outcome status field is not null
                if (!crimeObject.isNull("outcome_status"))
                {
                    JSONObject outComeStatus = crimeObject.getJSONObject("outcome_status");

                    if(outComeStatus!=null)
                    {
                        if(outComeStatus.getString("category") != null)
                        {
                            crime.setOutcome(outComeStatus.getString("category"));
                        }
                    }

                } else {
                    crime.setOutcome("Unknown outcome");
                }

                crime.setCrimeid(crimeObject.getInt("id"));

                //Location Data
                JSONObject locationObject = crimeObject.getJSONObject("location");
                Double crimelat = locationObject.getDouble("latitude");
                Double crimeLng = locationObject.getDouble("longitude");

                //Street Data
                JSONObject streetObject = locationObject.getJSONObject("street");

                crime.setLatitude(crimelat);
                crime.setLongitude(crimeLng);
                crime.setStreetName(streetObject.getString("name"));
                crime.setStreetId(streetObject.getInt("id"));
                crime.setCategory(crimeObject.getString("category"));
                crime.setDate(crimeObject.getString("month"));

                if (databaseHelper != null){
                    // add to DB
                    boolean insertData = databaseHelper.addData(crime);
                    if (insertData){
                        System.out.println("SUCCESS: DB INSERT DATA");
                    } else {
                        System.out.println("FAILED: DB INSERT DATA");
                    }
                }

                crimesList.add(crime);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return crimesList;
    }
}
