package com.example.jeff.car02.data;

import android.util.Log;

import com.mojio.mojiosdk.MojioClient;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * A call to the Mojio API
 * @param <T> The return type of the call
 */
public class MojioCallable<T> implements Callable<T> {

    private MojioClient mojioClient;
    private Class<T> TypeParamClass;
    private HashMap<String, String> queryParams;
    private String queryURL;
    private final CountDownLatch latch;

    /**
     * Create a call to the mojio API
     * @param mojioClient The mojio client instance
     * @param TypeParamClass The class type of the call (Generics are annoying like that)
     * @param queryParams The Query Parameters to be passed
     * @param queryURL The Query Type (ex: /Trips)
     */
    public MojioCallable(MojioClient mojioClient, Class<T> TypeParamClass, HashMap<String, String> queryParams, String queryURL) {
        this.mojioClient = mojioClient;
        this.TypeParamClass = TypeParamClass;
        this.queryParams = queryParams;
        this.queryURL = queryURL;
        // Used to synchronize our API reads
        this.latch = new CountDownLatch(1);
    }

    /**
     *  Performs a get call to the mojio API
     * @return The result of the API call
     * @throws Exception Can be interrupted, but that really should never happen
     */
    public T call() throws Exception {
        // So we cant get mess with the values of things inside of nested classes
        // But we /can/ copy them to storage that is declared final
        // Hence the generically typed output wrapper
        final OutputWrapper<T> out = new OutputWrapper<T>();
        mojioClient.get(TypeParamClass, queryURL, queryParams, new MojioClient.ResponseListener<T>() {
            @Override
            public void onSuccess(T result) {
                // Place the result in the wrapper
                out.setVal(result);
                // Unblocks the thread
                latch.countDown();
            }
            @Override
            public void onFailure(String error) {
                Log.e("Mojio Request", error);
                // Unblocks the thread
                latch.countDown();
            }
        });
        // Block The Callable Thread until API call is completed
        latch.await();
        return out.getVal();
    }

    /**
     * A basic wrapper for transferring data from the anonymous class
     * @param <W> The return type of the call
     */
    private class OutputWrapper<W> {
        private W val;

        /**
         * Sets the value stored in the wrapper
         * @param in The value to be stored
         */
        public void setVal(W in) {
            val = in;
        }

        /**
         * Gets the value stored in the wrapper
         * @return the value in the wrapper
         */
        public W getVal() { return val; }
    }
}
