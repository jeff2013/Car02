package com.example.jeff.car02;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Observer;

/**
 * Created by reed on 3/14/15.
 */
public class StaticXYDataSource extends DynamicXYDataSource {

    public StaticXYDataSource(ArrayList<Pair<Number, Number>> data) {
        // Tell the thread to never run, because the data source is static
        super(0);
        // Set the data to the stored data
        XYVals = data;
    }

    public String getTitle() {
        return "Static Data Source";
    }

    @Override
    public void addObserver(Observer obs) {
        super.addObserver(obs);
        // Force an immediate redraw
        notifier.notifyObservers();
    }

    public void getData() {
        // Does literally nothing on purpose
    }
}
