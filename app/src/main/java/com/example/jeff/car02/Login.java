package com.example.jeff.car02;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;
import com.mojio.mojiosdk.MojioClient;
import com.mojio.mojiosdk.models.User;
import com.mojio.mojiosdk.models.Vehicle;


public class Login extends Activity {
    //commitfile

    Button loginButton;
    private final static String MOJIO_APP_ID = "ddf63e97-865a-4b95-8e2f-d414d8e2d5b1";
    private final static String REDIRECT_URL = "myfirstmojio://"; // Example "myfirstmojio://"

    private static int OAUTH_REQUEST = 0;

    // The main mojio client object; allows l ogin and data retrieval to occur.
    private MojioClient mMojio;

    private User mCurrentUser;
    private Vehicle[] mUserVehicles;

    private Button mLoginButton;
    private TextView mUserName, mUserEmail;
    private ListView mVehicleList;
    private MapFragment mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        //mMojio = new MojioClient(this, MOJIO_APP_ID, null, REDIRECT_URL);
        loginButton = (Button) this.findViewById(R.id.btn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doOauth2Login();
            }
        });
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == OAUTH_REQUEST) {
            // We now have a stored access token
            if (resultCode == RESULT_OK) {
                Toast.makeText(Login.this, "Logged in successfully", Toast.LENGTH_LONG).show();
                //getCurrentUser(); // Now attempt to get user info
                login();
            }
            else {
                Toast.makeText(Login.this, "Problem logging in", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void doOauth2Login() {
        // Launch the OAuth request; this will launch a web view Activity for the user enter their login.
        // When the Activity finishes, we listen for it in the onActivityResult method
        mMojio.launchLoginActivity(this, OAUTH_REQUEST);
    }


    public void login(){
        Intent logged_in = new Intent(this, MainActivity.class);
        startActivity(logged_in);
    }



}
