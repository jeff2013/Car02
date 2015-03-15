package com.example.jeff.car02.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.jeff.car02.R;
import com.example.jeff.car02.StaticXYDataSource;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.mojio.mojiosdk.MojioClient;
import com.mojio.mojiosdk.models.Event;
import com.mojio.mojiosdk.models.Trip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jeff on 2015-03-14.
 */
public class Fragment_section3 extends SupportMapFragment {
    private GoogleMap map;

    private MojioClient mMojio;

    public void setMojioClient(MojioClient client){
        mMojio = client;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View tmp = super.onCreateView(inflater,container,savedInstanceState);
        map = getMap();

        Map<String, String> queryParam = new HashMap();
        queryParam.put("limit", "1000");
        queryParam.put("offset", "0");
        mMojio.get(Trip[].class, "Trips", queryParam, new MojioClient.ResponseListener<Trip[]>() {
            @Override
            public void onSuccess(Trip[] tripResult) {
                Trip latestTrip = tripResult[tripResult.length - 1];
                Map<String, String> queryParam = new HashMap();
                queryParam.put("limit", "1000");
                queryParam.put("offset", "0");
                queryParam.put("id", latestTrip._id);
                mMojio.get(Event[].class, "Trips/" + latestTrip._id + "/Events", queryParam, new MojioClient.ResponseListener<Event[]>() {
                    @Override
                    public void onSuccess(Event[] result) {
                        float prevDist = 0;
                        int count = 0;
                        LatLng prevLatLng = null;
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                        for (int i = 1; i < result.length - 1; i++) {
                            Date prevd = new Date();
                            Date d = new Date();
                            try {
                                d = dateFormatter.parse(result[i].Time);
                                prevd = dateFormatter.parse(result[i - 1].Time);
                            } catch (ParseException e) {
                                Toast.makeText(Fragment_section3.this.getActivity(), "Parse Error", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                            float distance = prevDist + result[i].Speed * (d.getTime() - prevd.getTime()) / (60 * 60 * 1000);
                            float deltaFuel = distance * result[i].FuelEfficiency - prevDist * result[i - 1].FuelEfficiency;
                            float DeltaCO2 = (deltaFuel * 2.3035f / 10) / (d.getTime() - prevd.getTime());
                            float totalCO2 = distance * result[i].FuelEfficiency * 2.3035f;

                            prevDist = distance;

                            if(!result[i].Location.IsValid) continue;

                            MarkerOptions opt = new MarkerOptions();
                            LatLng latlng = new LatLng(result[i].Location.Lat,result[i].Location.Lng);
                            opt.draggable(false);
                            opt.position(new LatLng(result[i].Location.Lat,result[i].Location.Lng));
                            opt.flat(true);

                            builder.include(latlng);

                            map.addMarker(opt);
                        }

                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),100));
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.d("Mojio API Error", error);
                    }
                });
            }

            public void onFailure(String error) {
                Log.d("Mojio API Error", error);
            }
        });

        return tmp;
    }

}
