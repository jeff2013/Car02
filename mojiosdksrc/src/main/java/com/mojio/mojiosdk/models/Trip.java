package com.mojio.mojiosdk.models;

import java.util.Objects;

public class Trip {

    public String MojioId;
    public String VehicleId;
    public String StartTime;
    public String LastUpdatedTime;
    public String EndTime;
    public float MaxSpeed;
    public float MaxAcceleration;
    public float MaxDeceleration;
    public int MaxRPM;
    public float FuelLevel;
    public float FuelEfficiency;
    public float Distance;
    public Object StartLocation;
    public Object LastKnownLocation;
    public Objects EndLocation;
    public Object StartAddress;
    public Object EndAddress;
    public boolean ForcefullyEnded;
    public float StartMilage;
    public float EndMilage;
    public float StartOdometer;
    public String _id;
    public boolean _deleted;
	
	public Trip(){
		
	}

    public float getFuelEfficiency() {
        return FuelEfficiency;
    }

    public float getDistance() {
        return Distance;
    }

}
