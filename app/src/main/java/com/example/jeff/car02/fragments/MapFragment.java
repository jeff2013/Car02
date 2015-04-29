package com.example.jeff.car02.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jeff.car02.data.DataTypes;
import com.example.jeff.car02.data.XYDataSource;
import com.example.jeff.car02.utilities.singletonMojio;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mojio.mojiosdk.MojioClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by jeff on 2015-03-14.
 */
public class MapFragment extends SupportMapFragment {
    private GoogleMap map;

    private XYDataSource dataSource;
    private List<Polyline> segments;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View tmp = super.onCreateView(inflater,container,savedInstanceState);
        map = getMap();

        segments = new ArrayList<Polyline>();

        dataSource = new XYDataSource(singletonMojio.getMojioClient(getActivity().getApplicationContext()), 1000, 20000);
        dataSource.selectYOutput(DataTypes.SPEED);
        dataSource.addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object data) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<LatLng> positions = dataSource.getLocations();

                        float maxData = dataSource.getMaxY();

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();

                        LatLng last_pos = null;
                        if (positions.size() > 0)

                        {
                            last_pos = positions.get(0);
                            builder.include(positions.get(0));
                        }

                        List<LatLng> points = new ArrayList<LatLng>(2);
                        Log.d("Map update", "called, num_points = " + Integer.toString(positions.size()));

                        int i = 1;
                        for (; i < dataSource.size() && i < positions.size(); ++i)

                        {
                            float normalisedData = dataSource.getY(i).floatValue() / maxData;
                            int colour = Color.argb(0xff, (int) (0xff * (1 - normalisedData)), (int) (0xff * (normalisedData)), 0);

                            builder.include(positions.get(i));
                            if (i - 1 < segments.size()) {
                                Polyline line = segments.get(i - 1);
                                line.setVisible(true);
                                points.set(0, last_pos);
                                points.set(1, positions.get(i));
                                line.setPoints(points);
                                line.setColor(colour);
                            } else {
                                PolylineOptions opts = new PolylineOptions();
                                opts.color(colour);
                                opts.add(last_pos, positions.get(i));
                                opts.visible(true);
                                segments.add(map.addPolyline(opts));
                            }
                            last_pos = positions.get(i);
                        }

                        for (; i < segments.size(); ++i)

                        {
                            segments.get(i).setVisible(false);
                        }

                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
                    }
                });
            }
        });
        Thread dataThread = new Thread(dataSource);
        dataThread.start();

        return tmp;
    }

}
