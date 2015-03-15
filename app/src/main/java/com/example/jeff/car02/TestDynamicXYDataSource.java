package com.example.jeff.car02;

import android.app.Activity;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;
import com.mojio.mojiosdk.MojioClient;
import com.mojio.mojiosdk.models.Event;
import com.mojio.mojiosdk.models.Mojio;
import com.mojio.mojiosdk.models.Trip;
import com.mojio.mojiosdk.models.Vehicle;
import com.mojio.mojiosdk.networking.MojioRequest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by reed on 3/14/15.
 * This is an example of an implementation of XYData Source
 */
public class TestDynamicXYDataSource extends DynamicXYDataSource {

    /**
     * These are used to store generated values
     */

    private MojioClient mojio;
    /**
     * Pass along params to the superclass
     * @param updateInterval
     */
    public TestDynamicXYDataSource(int updateInterval, MojioClient m, Activity activity) {
        super(updateInterval, activity);
        this.mojio = m;
    }

    /**
     * This method is used to get our data, and store it
     */
    public void getData() {
        Map<String, String> queryParam = new HashMap();
        queryParam.put("limit", "1000");
        queryParam.put("offset", "0");
        mojio.get(Trip[].class, "Trips", queryParam, new MojioClient.ResponseListener<Trip[]>() {
            public void onSuccess(Trip[] trips) {
                Trip latestTrip = trips[trips.length - 1];
                Map<String, String> queryParam = new HashMap();
                queryParam.put("limit", "1000");
                queryParam.put("offset", "0");
                // Add the id of the latest trip to the query options
                queryParam.put("id", latestTrip._id);
                // Get a list of Events for the latest trip
                mojio.get(Event[].class, "Trips/" + latestTrip._id + "/Events", queryParam, new MojioClient.ResponseListener<Event[]>() {
                    public void onSuccess(Event[] events) {
                        Event latestEvent = events[events.length - 1];
                        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                        locations.add(new LatLng(latestEvent.Location.Lat, latestEvent.Location.Lng));
                        Date d = new Date();

                        try {
                            d = dateFormatter.parse(latestEvent.Time);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        XYVals.add(new Pair<Number, Number>(d.getTime(), latestEvent.FuelEfficiency));
                        notifier.notifyObservers();
                    }

                    public void onFailure(String error) {
                        Log.e("Mojio API error: ", error);
                    }
                });
            }

            public void onFailure(String error) {

            }
        });
    }
}
