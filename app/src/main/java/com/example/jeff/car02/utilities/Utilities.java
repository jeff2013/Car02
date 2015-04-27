package com.example.jeff.car02.utilities;

import com.mojio.mojiosdk.TimeFormatHelpers;

import org.joda.time.DateTime;

/**
 * Created by reed on 3/26/15.
 * A collection of utility methods intended for use on the data
 * received from the API
 */
public class Utilities {

    // This is the amount of CO2 produced when one liter of petrol is burned, in kg
    private static final float CO2_CONVERSION_FACTOR = 2.3035f;

    /**
     * The Mojio device reports distance, but not quite at the correct granularity
     * This method computes a more accurate value
     * @param prevDistance The previous total distance traveled, in km
     * @param speed The average speed in the interval between the previous measurement and now, in km/hour
     * @param deltaTime The change in time, measured in milliseconds
     * @return The new total distance traveled, in km
     */
    public static float getRealDistance(float prevDistance, float speed, long deltaTime) {
        return prevDistance + (speed * deltaTime)/(60*60*1000);
    }

    /**
     * The Mojio device reports fuel as a percentage
     * In our use case, that isn't quite what we need
     * Instead, we calculate the change in fuel based off of efficiency and distance
     * @param currDist The current total distance traveled, in km
     * @param currEfficiency The current average fuel efficiency , in L/100km
     * @param prevDist The total distance traveled at the last sample point, in km
     * @param prevEfficiency The average fuel efficiency at the last sample point, in L/100km
     * @return The change in fuel, in L
     */
    public static float getDeltaFuel(float currDist, float currEfficiency, float prevDist, float prevEfficiency) {
        // Due to the way we calculate delta fuel, if efficiency falls while idling (so delta distance is zero)
        // The change in fuel can go negative. This is bad.
        // Because it is kind of an edge case, and because we have no better way of doing it, we floor it at zero
        return Math.max(100*(currDist*currEfficiency - prevDist*prevEfficiency), 0f);
    }

    /**
     * Computes the total amount of fuel used during a trip
     * @param dist The distance traveled, in km
     * @param efficiency The average fuel efficiency, in L/100km
     * @return The amount of fuel consumed, in L
     */
    public static float getTotalFuelUsage(float dist, float efficiency) {
        return dist*efficiency*100;
    }

    /**
     * Computes the amount of CO2 produced when a quantity of fuel is consumed
     * @param fuelConsumed The amount of fuel consumed, in L
     * @return the amount of CO2 produced, in kg
     */
    public static float getCO2Production(float fuelConsumed) {
        return fuelConsumed*CO2_CONVERSION_FACTOR;
    }

    /**
     * Get the total amount of CO2 produced
     * @param dist The distance traveled, in km
     * @param efficiency The average fuel efficiency, in L/100km
     * @return the amount of CO2 produced, in kg
     */
    public static float getTotalCO2Production(float dist, float efficiency) {
        return getCO2Production(getDeltaFuel(dist, efficiency, 0, 0));
    }

    /**
     * Converts a date string from the Mojio API into a long
     * @param date A date string received from the API
     * @return a long representation of the date and time
     */
    public static long getTimeFromDateString(String date) {
        DateTime d = TimeFormatHelpers.fromServerFormatted(date);
        return d.toDate().getTime();
    }
}