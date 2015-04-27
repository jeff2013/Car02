package com.example.jeff.car02.DataSource;


import android.util.Log;

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


/**
 * Created by reed on 3/24/15.
 * An example implementation for Data Source
 * Statically graphs the data for the last trip
 */
public class XYDataSource extends DataSource implements XYSeries{


    // This stores all the events we have received
    protected ArrayList<Event> events;
    // An ArrayList of Locations, used in the map
    protected List<LatLng> locations;
    // This is used to determine if we need a query
    protected boolean needQuery;
    // Determines what data is used for the x values
    private DataTypes xSelector = DataTypes.TIME;
    // Determines what data is used for the y values
    private DataTypes ySelector = DataTypes.DISTANCE;
    // The offset to the API calls
    private int offset = 0;
    // The current Trip object
    private Trip latestTrip;
    // How often we should update our data, in ms
    private int currentUpdateInterval;
    // The update rate when the car is on
    private int activeUpdateInterval;
    // The update rate when the car is off
    private int passiveUpdateInterval;
    // The last event with data that matters
    private Event lastImportantEvent;

    /**
     * Creates an XY Data Source
     * @param mojioClient The Mojio Client Instance
     * @param activeUpdateInterval The update interval when the ignition is on, in ms
     * @param passiveUpdateInterval The update interval when the ignition is off, in ms
     */
    public XYDataSource(MojioClient mojioClient, int activeUpdateInterval, int passiveUpdateInterval) {
        super(mojioClient);
        needQuery = true;
        isRunning = true;
        events = new ArrayList<Event>();
        locations = new ArrayList<LatLng>();
        this.activeUpdateInterval = activeUpdateInterval;
        this.passiveUpdateInterval = passiveUpdateInterval;
        // Set the current update rate to active
        this.currentUpdateInterval = activeUpdateInterval;
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
            try {
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
                    // Block until we get the latest trip
                    Trip tmp = t1.get()[0];
                    // If the trip received and the trip we are operating on are the same, do nothing
                    if(latestTrip != null && latestTrip._id.equals(tmp._id)) {
                        Thread.sleep(currentUpdateInterval);
                        continue;
                    } else {
                        lastImportantEvent = null;
                        // Set the trip to the new latest trip
                        latestTrip = tmp;
                        // Clear the events to prepare for a new trip
                        events.clear();
                        // Reset the offset
                        offset = 0;
                        // We no longer need a query unless we are forced to re-update our data
                        needQuery = false;
                    }
                }
                String id = latestTrip._id;
                // Prepare for a new query
                HashMap<String, String> queryParams = new HashMap<String, String>();
                queryParams.put("id", id);
                queryParams.put("limit", "1000");
                queryParams.put("offset", "" + offset);
                Log.d("XYDataSource", "Offset = " + offset);
                // Make an new future task and schedule it
                FutureTask<Event[]> t2 = new FutureTask<Event[]>(new MojioCallable<Event[]>(mojioClient, Event[].class, queryParams, "Trips/" + id + "/Events"));
                executorService.execute(t2);
                // Block until we get our result, then convert them to a list and store
                // We reset the distance to be something useful
                Event[] e = t2.get();
                // Add the events
                addEvents(e);
                offset += e.length;
                // Notify all observers that the data has been changed
                notifier.notifyObservers();

                Thread.sleep(currentUpdateInterval);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Do processing and add the event to the ArrayList
     * @param e the events to add
     */
    protected void addEvents(Event[] e) {
        // There appears to be an issue with the data sets coming from the simulator
        // It appears as if the server timestamps them, and the time data is /not/ correct
        // This throws off all of our calculations. Postpone all algorithm testing until
        // We can get  a good data set
        for(int i = 0; i < e.length; i++) {
            Event currEvent = e[i];
            // If the speed is zero, there is no good data here
            if(currEvent.Speed != 0) {
                if (lastImportantEvent != null) {
                    // Calculate Delta T
                    long dt = Utilities.getTimeFromDateString(currEvent.Time) - Utilities.getTimeFromDateString(lastImportantEvent.Time);
                    // Get the real distance, and store it in the event
                    currEvent.computedDistance = Utilities.getRealDistance(lastImportantEvent.computedDistance, currEvent.Speed, dt);

                    //Log.d("XYDataSource", "Delta T = " + dt + "MS");
                    //Log.d("XYDataSource", "Distance Traveled = " + currEvent.Distance + "KM");
                    //Log.d("XYDataSource", "Current Speed = " + currEvent.Speed + " KM/H");
                    // Check That delta fuel is never negative
                    float currFuel = Utilities.getTotalFuelUsage(currEvent.Distance, currEvent.FuelEfficiency);
                    float prevFuel = lastImportantEvent.FuelUsage;
                    currEvent.FuelUsage = Math.max(currFuel, prevFuel);
                    currEvent.CO2Emissions = Utilities.getCO2Production(currEvent.FuelUsage);
                } else {
                    currEvent.computedDistance = currEvent.Distance;
                }
                lastImportantEvent = currEvent;
            } else if(lastImportantEvent != null) {
                currEvent.computedDistance = lastImportantEvent.computedDistance;
            }
            if (currEvent.EventType.equals("IgnitionOn")) {
                currentUpdateInterval = activeUpdateInterval;
                // If the ignition is turned on, this means there is a new trip, so we need to query to get the latest trip
            } else if (currEvent.EventType.equals("IgnitionOff")) {
                currentUpdateInterval = passiveUpdateInterval;
                // Because the trip has ended, poll for new trips less frequently
                needQuery = true;
            }
            locations.add(new LatLng(currEvent.Location.Lat, currEvent.Location.Lng));
            events.add(currEvent);
        }
    }

    /**
     * Selects the X Data to be graphed
     * @param d A dataType enum
     */
    public void selectXOutput(DataTypes d) {
        xSelector = d;
    }

    /**
     * Selects the Y Data to be graphed
     * @param d A dataType enum
     */
    public void selectYOutput(DataTypes d) {
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
    private Number getSelectedData(int i, DataTypes selector) {
        Number output = 0;
        switch (selector) {
            case TIME:
                DateTime d =  TimeFormatHelpers.fromServerFormatted(events.get(i).Time);
                output = d.toDate().getTime();
                break;
            case DISTANCE:
                output = events.get(i).computedDistance;
                break;
            case SPEED:
                output = events.get(i).Speed;
                break;
            case FUEL_EFFICIENCY:
                output= events.get(i).FuelEfficiency;
                break;
            case DELTA_FUEL:
                if(i > 0) {
                    output = events.get(i).FuelUsage - events.get(i-1).FuelUsage;
                } else {
                    output = 0;
                }
                break;
            case TOTAL_FUEL:
                output = events.get(i).FuelUsage;
                break;
            case TOTAL_CO2:
                output = events.get(i).CO2Emissions;
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
