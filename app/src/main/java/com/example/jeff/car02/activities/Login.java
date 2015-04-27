package com.example.jeff.car02.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.jeff.car02.R;
import com.example.jeff.car02.utilities.singletonMojio;
import com.mojio.mojiosdk.MojioClient;


public class Login extends Activity {

    private static MojioClient mMojio;
    private Button mojioLoginButton;
    private static int OAUTH_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View contentView = View.inflate(this, R.layout.activity_login_simple, null);
        mMojio = singletonMojio.getMojioClient(null);
        setUpLogin(contentView);
        setContentView(contentView);
    }

    private void setUpLogin(View loginView) {
        mojioLoginButton = (Button) loginView.findViewById(R.id.btn_loginMojio);
        mojioLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(hasvalidConnection()) {
                   mMojio.launchLoginActivity(Login.this, OAUTH_REQUEST);
               }else{
                   internetUnavailable();
               }
            }
        });

    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == OAUTH_REQUEST) {
            //Toast.makeText(Login.this, "On Activity Result Called", Toast.LENGTH_LONG).show();
            // We now have a stored access token
            if (resultCode == RESULT_OK) {
                launchMainActivity();
            }else {
                loginFailure();
            }
        }
    }

    private boolean hasvalidConnection(){
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    private void launchMainActivity(){
        Intent mainActivity = new Intent(Login.this, MainActivity.class);
        startActivity(mainActivity);
    }

    private void internetUnavailable(){
        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
        builder.setMessage(R.string.connectionAlert);
        builder.setTitle(R.string.invalid_internet);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void loginFailure(){
        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
        builder.setMessage(R.string.LoginFailureBody);
        builder.setTitle(R.string.LoginFailureTitle);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}