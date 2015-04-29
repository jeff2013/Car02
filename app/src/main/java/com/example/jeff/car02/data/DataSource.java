
package com.example.jeff.car02.data;

import com.mojio.mojiosdk.MojioClient;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by reed on 3/24/15.
 * This is the base implementation of a Data Source
 * It is the layer used to poll the Mojio client for data
 * This is Runnable to prevent blocking on the main thread
 */
public abstract class DataSource implements Runnable{

    protected MyObserver notifier;
    protected MojioClient mojioClient;
    // The status of the thread
    protected boolean isRunning;

    public DataSource(MojioClient mojioClient) {
        this.mojioClient = mojioClient;
        notifier = new MyObserver();
        isRunning = true;
    }

    /**
     * Adds an observer
     * @param obs The observer to add
     * @see java.util.Observable#addObserver(java.util.Observer)
     */
    public void addObserver(Observer obs) {
        notifier.addObserver(obs);
    }

    /**
     * Deletes an observer
     * @param obs The observer to delete
     * @see java.util.Observable#deleteObserver(java.util.Observer)
     */
    public void deleteObserver(Observer obs) {
        notifier.deleteObserver(obs);
    }

    /**
     * Forces the DataSource to notify all observers
     */
    public void forceUpdate() {notifier.notifyObservers();}

    /**
     * Pauses the thread if possible
     */
    public void pauseExecution() {
        isRunning = false;
    }

    /**
     * Resumes the thread if possible
     */
    public void resumeExecution() {
        isRunning = true;
    }


    /**
     * Gets the amount of elements in the DataSource
     * @return The amount of elements
     */
    public abstract int size();

    /**
     * The Observer implementation
     * Used to register and notify observers that data has changed
     */
    class MyObserver extends Observable {
        @Override
        public void notifyObservers() {
            setChanged();
            super.notifyObservers();
        }
    }
}
