package com.github.rishil.crimewiz.core.util;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class LinearRegressionTest {
    private ArrayList<Double> yearData, numberOfCrimesData;
    private double yearInput, expectedVal;

    private void setUpTestCase(){
        yearData = new ArrayList<>();
        numberOfCrimesData = new ArrayList<>();
        for (double i = 2017; i < 2020; i++){ // x data = 2017, 2018, 2019
            yearData.add(i);
        }
        numberOfCrimesData.add(2000d); // y data = 2000, 2400, 2600
        numberOfCrimesData.add(2400d);
        numberOfCrimesData.add(2600d);
        yearInput = 2025;
        expectedVal = 4433;
    }

    @Test
    public void getPredictedNumberOfCrimes() {
        setUpTestCase();  // regressionLine = 300.0*x - 603067
        double delta = .5; // closeness of the assertEquals
        LinearRegression linearRegression = new LinearRegression(yearData, numberOfCrimesData);
        double output = linearRegression.getPredictedNumberOfCrimes(yearInput);
        System.out.println(linearRegression.getRegressionLine());
        System.out.println(linearRegression.getMeanNumberOfCrimes());
        System.out.println("Expected Value: " + expectedVal);
        System.out.println("Obtained Value: " + output);
        assertEquals(expectedVal, output, delta);
    }
}
