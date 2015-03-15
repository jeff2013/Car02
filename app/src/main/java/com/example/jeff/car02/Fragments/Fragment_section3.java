package com.example.jeff.car02.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jeff.car02.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.mojio.mojiosdk.MojioClient;

/**
 * Created by jeff on 2015-03-14.
 */
public class Fragment_section3 extends SupportMapFragment {
    private GoogleMap map;

    private MojioClient mClient;

    public void setMojioClient(MojioClient client){
        mClient = client;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View tmp = super.onCreateView(inflater,container,savedInstanceState);
        map = getMap();

        return tmp;
    }

}
