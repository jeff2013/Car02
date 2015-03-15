package com.example.jeff.car02;

import android.util.Log;
import android.util.Pair;

import com.mojio.mojiosdk.MojioClient;
import com.mojio.mojiosdk.models.Mojio;
import com.mojio.mojiosdk.models.Trip;
import com.mojio.mojiosdk.models.Vehicle;
import com.mojio.mojiosdk.networking.MojioRequest;

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
    public TestDynamicXYDataSource(int updateInterval, MojioClient m) {
        super(updateInterval);
        this.mojio = m;
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
        Map<String, String> queryParam = new HashMap();
        queryParam.put("limit", "1000");
        queryParam.put("offset", "0");
        mojio.get(Vehicle[].class, "Vehicle", queryParam, new MojioClient.ResponseListener<Vehicle[]>() {
            public void onSuccess(Vehicle[] vehicles) {
                Map<String, String> queryParam = new HashMap();
                queryParam.put("id", vehicles[vehicles.length - 1].CurrentTrip + "");

                mojio.get(Trip.class, "Trips" + vehicles[vehicles.length - 1].CurrentTrip, queryParam, new MojioClient.ResponseListener<Trip>() {
                    @Override
                    public void onSuccess(Trip result) {
                        Date d = new Date();
                        XYVals.add(new Pair<Number, Number>(d.getTime(), result.FuelEfficiency));
                    }

                    @Override
                    public void onFailure(String error) {

                    }
                });
            }

            public void onFailure(String error) {

            }
        });
    }
}
