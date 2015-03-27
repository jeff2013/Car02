package com.example.jeff.car02.DataSource;

import com.androidplot.xy.XYSeries;

import com.example.jeff.car02.Utilities;
import com.google.android.gms.maps.model.LatLng;
import com.mojio.mojiosdk.MojioClient;
import com.mojio.mojiosdk.models.Event;
import com.mojio.mojiosdk.models.Trip;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Created by reed on 3/24/15.
 * An example implementation for Data Source
 * Statically graphs the data for the last trip
 */
public class DyanmicXYDataSource extends StaticXYDataSource implements XYSeries{

    // The offset to the API calls
    private int offset = 0;
    // The current Trip object
    private Trip latestTrip;
    // How often we should update our data, in ms
    private int updateInterval;

    /**
     * Constructs a Test Data Source
     * @param mojioClient The Mojio Client instance
     * @param updateInterval How often the DataSource should poll for new data, in ms
     */
    public DyanmicXYDataSource(MojioClient mojioClient, int updateInterval) {
        super(mojioClient);
        this.updateInterval = updateInterval;
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
            // If we need to, perform a query to get the most recent trip
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
                    latestTrip = t1.get()[0];
                    // We don't need to get a new trip every iteration
                    needQuery = false;
                }
                // Set up the query to get the trips events
                HashMap<String, String> queryParams = new HashMap<String, String>();
                queryParams.put("id", latestTrip._id);
                queryParams.put("limit", "1000");
                // We use offset to avoid getting data twice
                queryParams.put("offset", offset + "");
                // Make an new future task and schedule it
                FutureTask<Event[]> t2 = new FutureTask<Event[]>(new MojioCallable<Event[]>(mojioClient,
                        Event[].class, queryParams,
                        "Trips/" + latestTrip._id + "/Events"));
                executorService.execute(t2);
                // Block until we get our result, then convert them to a list and store
                Event[] e = t2.get();
                for (int i = 0; i < e.length; i++) {
                    if (i > 0) {
                        // Get the real distance, and store it in the event
                        e[i].Distance = Utilities.getRealDistance(e[i - 1].Distance,
                                e[i].Speed,
                                Utilities.getTimeFromDateString(e[i].Time) - Utilities.getTimeFromDateString(e[i - 1].Time));
                    }
                    locations.add(new LatLng(e[i].Location.Lat, e[i].Location.Lng));
                    events.add(e[i]);
                }
                // Increment the offset
                offset+=e.length;
                // Notify all observers that the data has been changed
                notifier.notifyObservers();
                // Sleep the thread until the next update cycle
                Thread.sleep(updateInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

}

