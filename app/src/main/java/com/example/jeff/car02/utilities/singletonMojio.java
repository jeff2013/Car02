package com.example.jeff.car02.utilities;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.mojio.mojiosdk.MojioClient;

/**
 * Created by jeff on 2015-03-28.
 */
public class singletonMojio extends Activity{

    private static singletonMojio singletonMojioInstance;
    private static MojioClient mojioClient;

    private final static String MOJIO_APP_ID = "ddf63e97-865a-4b95-8e2f-d414d8e2d5b1";
    private final static String REDIRECT_URL = "myfirstmojio://"; // Example "myfirstmojio://"
    private final static String SANDBOX_SECRET_KEY = "872bca1d-9a0c-4ad4-932b-3b696658df55";
    private final static String LIVE_SECRET_KEY =  "7a28be7e-b669-4d3d-9445-301866f06ab4";

    private singletonMojio(Context context){
        mojioClient = new MojioClient(context, MOJIO_APP_ID, LIVE_SECRET_KEY, REDIRECT_URL);
    }

    public static MojioClient getMojioClient(Context context){
        if(mojioClient == null){
            singletonMojioInstance = new singletonMojio(context);
            return mojioClient;
        }
        return mojioClient;
    }
}
