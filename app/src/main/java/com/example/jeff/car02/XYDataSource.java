package com.example.jeff.car02;

import android.util.Pair;

import com.androidplot.xy.XYSeries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * This Encapsulates our data passed to our dynamic plots
 * The plots need to register with their data sources
 * The Data source polls the Mojio api for data
 */
public abstract class XYDataSource implements Runnable, XYSeries {

    /**
     * Used to store XY values
     */
    protected ArrayList<Pair<Number, Number>> XYVals;

    /**
     * Used to handle observation stuffs
     */
    class MyObservable extends Observable {
        @Override
        public void notifyObservers() {
            setChanged();
            super.notifyObservers();
        }
    }

    /**
     * Observer instance
     */
    private MyObservable notifier;

    /**
     * The update interval in milliseconds
     */
    protected int updateInterval;

    /**
     * Creates a data source with an update interval in milliseconds
     * @param updateInterval
     */
    public XYDataSource(int updateInterval) {
        XYVals = new ArrayList<Pair<Number, Number>>();
        notifier = new MyObservable();
        this.updateInterval = updateInterval;
    }

    /**
     * Polls Mojio for data, then sleeps updateInterval milliseconds
     */
    @Override
    public void run() {
        try {
            //TODO: Poll Mojio for data, and add data to the list
            notifier.notifyObservers();
            Thread.sleep(updateInterval);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add an observer to the data
     * @param obs
     */
    public void addObserver(Observer obs) {
        notifier.addObserver(obs);
    }

    /**
     * Remove an observer from the data
     * @param obs
     */
    public void deleteObserver(Observer obs) {
        notifier.deleteObserver(obs);
    }

    /**
     * Get the X value at the given position
     * @param index
     * @return
     */
    @Override
    public Number getX(int index) {
        return XYVals.get(index).first;
    }

    /**
     * Get the Y value at the given position
     * @param index
     * @return
     */
    @Override
    public Number getY(int index) {
        return XYVals.get(index).second;
    }

    /**
     * Get the amount of data stored
     * @return
     */
    @Override
    public int size() {
        return XYVals.size();
    }
}