package com.github.rishil.crimewiz.core.util;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import com.github.rishil.crimewiz.core.objects.Crime;

import static org.junit.Assert.*;

public class JsonParserTest {
    private String testJson = "";
    private int expectedVal;

    private void setUpTestCase(){
        // Json from a test query to the UK Police REST API
        // https://data.police.uk/api/crimes-street/all-crime?poly=51.502350899999996,
        // -0.12275829999999999:51.5123509,-0.12275829999999999:51.5123509,
        // -0.1327583:51.502350899999996,-0.1327583&date=2018-09

        // load in test file obtained from the REST API
        String jsonPath = "src/test/resources/test-crime.json";

        File initialFile = new File(jsonPath);
        try {
            InputStream inputStream = new FileInputStream(initialFile);
            Reader fr = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufReader = new BufferedReader(fr);
            StringBuffer readTextBuf = new StringBuffer();
            String line = bufReader.readLine();
            while(line != null) {
                readTextBuf.append(line);
                line = bufReader.readLine();
            }
            testJson = readTextBuf.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        expectedVal = 788;
    }

    @Test
    public void parse() {
        setUpTestCase();
        JsonParser jsonParser = new JsonParser();
        List<Crime> crimeList = jsonParser.parse(testJson);
        System.out.println("Expected Value: " + expectedVal);
        System.out.println("Obtained Value: " + crimeList.size());
        assertEquals(expectedVal, crimeList.size());
    }
}