package com.example.jeff.car02;

import android.util.Pair;

import com.androidplot.xy.XYSeries;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * This Encapsulates our data passed to our dynamic plots
 * The plots need to register with their data sources
 * The Data source polls the Mojio api for data
 */
public abstract class DynamicXYDataSource implements Runnable, XYSeries {

    /**
     * Used to store XY values
     */
    protected ArrayList<Pair<Number, Number>> XYVals;
    protected ArrayList<LatLng> locations;
    private boolean isRunning;

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

    public float getMaxY(){
        float mx = 0;
        for(int i=0;i<XYVals.size();++i){
            float tmp = XYVals.get(i).second.floatValue();
            mx = mx < tmp ? tmp : mx;
        }
        return mx;
    }

    public List<LatLng> getLocations(){ return locations; }

    /**
     * Observer instance
     */
     protected MyObservable notifier;

    /**
     * The update interval in milliseconds
     */
    protected int updateInterval;

    /**
     * Creates a data source with an update interval in milliseconds
     * @param updateInterval
     */
    public DynamicXYDataSource(int updateInterval) {
        XYVals = new ArrayList<Pair<Number, Number>>();
        notifier = new MyObservable();
        this.updateInterval = updateInterval;
    }

    /**
     * Used To Fetch and Store data from the API
     */
    public abstract void getData();

    /**
     * Polls Mojio for data, then sleeps updateInterval milliseconds
     */
    @Override
    public void run() {
        isRunning = updateInterval > 0;
        try {
            do {
                getData();
                Thread.sleep(updateInterval);
            } while (isRunning);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void terminate() {
        isRunning = false;
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
