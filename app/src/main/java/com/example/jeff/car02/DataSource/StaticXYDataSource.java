package com.example.jeff.car02.DataSource;


import android.util.Log;

import com.androidplot.ui.YLayoutStyle;
import com.androidplot.xy.XYSeries;
import com.example.jeff.car02.Utilities;
import com.google.android.gms.maps.model.LatLng;
import com.mojio.mojiosdk.MojioClient;
import com.mojio.mojiosdk.TimeFormatHelpers;
import com.mojio.mojiosdk.models.Event;
import com.mojio.mojiosdk.models.Trip;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import static com.example.jeff.car02.DataSource.DataSource.dataTypes.*;

/**
 * Created by reed on 3/24/15.
 * An example implementation for Data Source
 * Statically graphs the data for the last trip
 */
public class StaticXYDataSource extends DataSource implements XYSeries{


    // This stores all the events we have received
    protected ArrayList<Event> events;
    // An ArrayList of Locations, used in the map
    protected List<LatLng> locations;
    // This is used to determine if we need a query
    protected boolean needQuery;
    // Determines what data is used for the x values
    private dataTypes xSelector = TIME;
    // Determines what data is used for the y values
    private dataTypes ySelector = TOTAL_FUEL;

    /**
     * Constructs a Test Data Source
     * @param mojioClient The Mojio Client instance
     */
    public StaticXYDataSource(MojioClient mojioClient) {
        super(mojioClient);
        needQuery = true;
        isRunning = true;
        events = new ArrayList<Event>();
        locations = new ArrayList<LatLng>();
    }

    /**
     * This is called whenever a thread is started using this object
     * All API requests /must/ be made in this block
     */
    @Override
    public void run() {
        // This is used to execute our FutureTasks
        // It has only one thread so we can do things in order
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        while(isRunning) {
            // If we need to, perform a query
            if (needQuery) {
                // Set up the the query for the last trip
                HashMap<String, String> queryParams = new HashMap<String, String>();
                queryParams.put("limit", "1");
                queryParams.put("offset", "0");
                queryParams.put("sortBy", "StartTime");
                queryParams.put("desc", "true");
                queryParams.put("criteria", "");
                // Create a FutureTask whose job is to fetch the latest trip
                FutureTask<Trip[]> t1 = new FutureTask<Trip[]>(new MojioCallable<Trip[]>(mojioClient, Trip[].class, queryParams, "Trips"));
                // Schedule our job to be executed
                executorService.execute(t1);
                try {
                    // Block until we get the latest trip
                    Trip latestTrip = t1.get()[0];
                    String id = latestTrip._id;
                    // Prepare for a new query
                    queryParams = new HashMap<String, String>();
                    queryParams.put("id", id);
                    queryParams.put("limit", "1000");
                    queryParams.put("offset", "0");
                    // Make an new future task and schedule it
                    FutureTask<Event[]> t2 = new FutureTask<Event[]>(new MojioCallable<Event[]>(mojioClient, Event[].class, queryParams, "Trips/" + id + "/Events"));
                    executorService.execute(t2);
                    // Block until we get our result, then convert them to a list and store
                    // We reset the distance to be something useful
                    events.clear();
                    Event[] e = t2.get();
                    for(int i = 0; i < e.length; i++) {
                        if(i > 0) {
                            // Get the real distance, and store it in the event
                            e[i].Distance = Utilities.getRealDistance(e[i - 1].Distance,
                                    e[i].Speed,
                                    Utilities.getTimeFromDateString(e[i].Time) -
                                            Utilities.getTimeFromDateString(e[i - 1].Time));
                        }
                        locations.add(new LatLng(e[i].Location.Lat, e[i].Location.Lng));
                        events.add(e[i]);
                    }
                    // Notify all observers that the data has been changed
                    notifier.notifyObservers();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                // We no longer need a query unless we are forced to re-update our data
                needQuery = false;
            }
        }
    }

    /**
     * Selects the X Data to be graphed
     * @param d A dataType enum
     */
    public void selectXOutput(dataTypes d) {
        xSelector = d;
    }

    /**
     * Selects the Y Data to be graphed
     * @param d A dataType enum
     */
    public void selectYOutput(dataTypes d) {
        ySelector = d;
    }

    @Override
    public int size() {
        return events.size();
    }

    @Override
    public Number getX(int i) {
        return getSelectedData(i, xSelector);
    }

    @Override
    public Number getY(int i) {
        return getSelectedData(i, ySelector);
    }

    public float getMaxY() {
        float maxY = 0f;
        for(int i = 0; i < events.size(); i++) {
            maxY = Math.max(maxY, (Float) getSelectedData(i, ySelector));
        }
        return maxY;
    }

    @Override
    public String getTitle() {
        return "THIS IS A TEST";
    }

    /**
     * returns the data at an index of events to be displayed, based on an enum value
     * @param i The index the data is stored at
     * @param selector a dataType enum, used to choose which data to return
     * @return The data requested
     */
    private Number getSelectedData(int i, dataTypes selector) {
        Number output = 0;
        switch (selector) {
            case TIME:
                DateTime d =  TimeFormatHelpers.fromServerFormatted(events.get(i).Time);
                output = d.toDate().getTime();
                break;
            case DISTANCE:
                output = events.get(i).Distance;
                break;
            case FUEL_EFFICIENCY:
                output= events.get(i).FuelEfficiency;
                break;
            case DELTA_FUEL:
                if(i > 0) {
                    output = Utilities.getDeltaFuel(events.get(i).Distance,
                            events.get(i).FuelEfficiency,
                            events.get(i-1).Distance,
                            events.get(i-1).FuelEfficiency);
                } else {
                    output = 0;
                }
                break;
            case TOTAL_FUEL:
                // Due to the way we get distance, there can be some weird stuff (negative fuel consumption)
                // So what we need to do is get the maximum fuel usage of the last two events, to make sure
                // that the slope never goes negative
                float currFuelUsage = Utilities.getTotalFuelUsage(events.get(i).Distance, events.get(i).FuelEfficiency);
                // Make sure we don't do anything stupid when i = 0
                float prevFuelUsage = 0;
                if(i > 0) {
                    prevFuelUsage = Utilities.getTotalFuelUsage(events.get(i - 1).Distance, events.get(i - 1).FuelEfficiency);
                }
                output = Math.max(currFuelUsage, prevFuelUsage);
                break;
            case TOTAL_CO2:
                output = Utilities.getTotalCO2Production(events.get(i).Distance, events.get(i).FuelEfficiency);
                break;
        }
        return output;
    }

    /**
     * Gets the list of locations
     * @return A list of locations
     */
    public List<LatLng> getLocations() {
        return locations;
    }

}
