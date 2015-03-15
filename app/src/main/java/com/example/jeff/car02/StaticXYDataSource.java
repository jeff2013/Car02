package com.example.jeff.car02;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.androidplot.xy.XYPlot;
import com.google.android.gms.maps.model.LatLng;
import com.mojio.mojiosdk.MojioClient;
import com.mojio.mojiosdk.models.Event;
import com.mojio.mojiosdk.models.Mojio;
import com.mojio.mojiosdk.models.Trip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;

/**
 * Created by reed on 3/14/15.
 */
public class StaticXYDataSource extends DynamicXYDataSource {

    private SharedPreferences sharedPreferences;
    public static final String graphData = "Graph Number";
    public int num;

    private MojioClient mojioClient;

    public StaticXYDataSource(MojioClient m, Activity activity) {
        // Tell the thread to never run, because the data source is static
        super(0, activity);
        this.mojioClient = m;
        // Set the data to the stored data
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

    public void forceDataRefresh() {
        notifier.notifyObservers();
    }

    public void getData() {
        Map<String, String> queryParam = new HashMap();
        queryParam.put("limit", "1000");
        queryParam.put("offset", "0");
        // Query the API for the list of trips
        mojioClient.get(Trip[].class, "Trips", queryParam, new MojioClient.ResponseListener<Trip[]>() {
            @Override
            public void onSuccess(Trip[] tripResult) {
                // Get the latest trip
                Trip latestTrip = tripResult[tripResult.length - 1];
                // Set our query options
                Map<String, String> queryParam = new HashMap();
                queryParam.put("limit", "1000");
                queryParam.put("offset", "0");
                // Add the id of the latest trip to the query options
                queryParam.put("id", latestTrip._id);
                // Get a list of Events for the latest trip
                mojioClient.get(Event[].class, "Trips/" + latestTrip._id + "/Events", queryParam, new MojioClient.ResponseListener<Event[]>() {
                    @Override
                    public void onSuccess(Event[] result) {
                        // Generate a set of XY values
                        ArrayList<Pair<Number, Number>> vals = new ArrayList<Pair<Number, Number>>();
                        // Set up our previous distance, used in our distance calculations
                        float prevDist = 0;
                        // Set up our date formatter
                        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                        // Iterate through all of our Events
                        for (int i = 1; i < result.length - 1; i++) {
                            // Create our date objects
                            Date prevd = new Date();
                            Date d = new Date();
                            // Try to get the Date, throw errors if there are failures, then continue to the next cycle
                            try {
                                d = dateFormatter.parse(result[i].Time);
                                prevd = dateFormatter.parse(result[i - 1].Time);
                            } catch (ParseException e) {
                                e.printStackTrace();
                                continue;
                            }
                            // add latlng to internal list of locations
                            locations.add(new LatLng(result[i].Location.Lat,result[i].Location.Lng));

                            // Calculate the total distance, we need to do this because the provided distance isn't granular enough
                            float distance = prevDist + result[i].Speed * (d.getTime() - prevd.getTime()) / (60 * 60 * 1000);
                            float deltaFuel = distance * result[i].FuelEfficiency - prevDist * result[i - 1].FuelEfficiency;
                            // Calculate CO2 Values, in KG of CO2
                            float deltaCO2 = (deltaFuel * 2.3035f * 100) / (d.getTime() - prevd.getTime());
                            float totalCO2 = distance * result[i].FuelEfficiency * 2.3035f * 100;
                            // Switch on which values to display
                            int selector = StaticXYDataSource.super.getPreference();
                            if(true) forceDataRefresh();
                            switch (selector) {
                                case 0:
                                    vals.add(new Pair<Number, Number>(d.getTime(), totalCO2));
                                    break;
                                case 1:
                                    vals.add(new Pair<Number, Number>(d.getTime(), deltaCO2));
                                    break;
                                case 2:
                                    vals.add(new Pair<Number, Number>(d.getTime(), result[i].FuelEfficiency));
                                    break;
                                case 3:
                                    vals.add(new Pair<Number, Number>(d.getTime(), distance));
                                    break;
                                case 4:
                                    vals.add(new Pair<Number, Number>(d.getTime(), deltaFuel));
                                    break;
                            }
                            Log.d("XY Vals Stuff", vals.size()+" Out of " + result.length);
                            // Do some junk to prepare for the next iteration
                            prevDist = distance;
                        }
                        XYVals = vals;
                        notifier.notifyObservers();
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e("Mojio API error", error);
                    }
                });

            }

            public void onFailure(String error) {
                Log.e("Mojio API error", error);
            }
        });
    }
}
