package com.github.rishil.crimewiz.core.util;

import java.util.ArrayList;

public class LinearRegression {

    private ArrayList<Double> yearData;
    private ArrayList<Double> numberOfCrimesData;

    public LinearRegression (ArrayList<Double> yearData, ArrayList<Double> numberOfCrimesData) {
        this.yearData = yearData;
        this.numberOfCrimesData = numberOfCrimesData;
    }

    public String getRegressionLine(){
        return "y = " + getYIntercept() + " + " + getLineGradient() + "x";
    }

    public Double getPredictedNumberOfCrimes(double yearToPredict) {
        double gradient = getLineGradient();
        double yIntercept = getYIntercept();

        // return y = B0 + B1 * x
        return yIntercept + (gradient * yearToPredict);
    }

    private Double getLineGradient() {
        if (yearData.size() >=2 && numberOfCrimesData.size() >=2){
            // get mean of X and Y
            Double xBar = getMeanNumberOfYears();
            Double yBar = getMeanNumberOfCrimes();

            // initialise the numerator and denominators
            Double num = 0.0;
            Double denom = 0.0;

            // calculate error of each variable for each year
            for (int i = 0; i < yearData.size(); i++) {
                // multiply the error for each x by each y
                num += (yearData.get(i) - xBar) * (numberOfCrimesData.get(i) - yBar);

                // square the error for each x
                denom += (yearData.get(i) - xBar) * (yearData.get(i) - xBar);
            }

            // return the gradient (slope)
            return num / denom;
        } else {
            return 0.0;
        }
    }

    private Double getYIntercept() {
        if (yearData.size() >=2 && numberOfCrimesData.size() >=2){
            return getMeanNumberOfCrimes() - (getLineGradient() * getMeanNumberOfYears());
        } else {
            return 0.0;
        }
    }

    public Double getMeanNumberOfCrimes() {
        if (yearData.size() >=1 && numberOfCrimesData.size() >=1){ // if we have crime data
            Double sum = 0.0;
            for (int i = 0; i < numberOfCrimesData.size(); i++) {
                sum = sum + numberOfCrimesData.get(i); // aggregate the crime data
            }
            return sum / numberOfCrimesData.size(); // calculate the average no. of crimes
        } else {
            return 0.0;
        }

    }

    private Double getMeanNumberOfYears() {
        Double sum = 0.0;
        for (int i = 0; i < yearData.size(); i++) {
            sum = sum + yearData.get(i); // aggregate the years
        }
        return sum/yearData.size(); // calculate the average year
    }


}