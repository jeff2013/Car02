package com.example.jeff.car02;

import android.util.Pair;

/**
 * Created by reed on 3/14/15.
 * This is an example of an implementation of XYData Source
 */
public class TestDynamicXYDataSource extends DynamicXYDataSource {

    /**
     * These are used to store generated values
     */
    private int counterX;

    /**
     * Pass along params to the superclass
     * @param updateInterval
     */
    public TestDynamicXYDataSource(int updateInterval) {
        super(updateInterval);
    }

    /**
     * get the graph title
     * @return
     */
    public String getTitle() {
        return "TEST TITLE";
    }

    /**
     * This method is used to get our data, and store it
     */
    public void getData() {
        XYVals.add(new Pair<Number, Number>((counterX), (counterX)*(counterX)));
        counterX++;
    }
}
