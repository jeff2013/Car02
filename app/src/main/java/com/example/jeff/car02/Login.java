package com.example.jeff.car02;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mojio.mojiosdk.MojioClient;


public class Login extends Activity {

    private static MojioClient mMojio;
    private Button mojioLoginButton;
    private Button mojioOauthButton;
    private EditText usernameEditor;
    private EditText passwordEditor;
    private WebView oAuth2WebView;
    private static int OAUTH_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View contentView = View.inflate(this, R.layout.activity_login_simple, null);
        mMojio = singletonMojio.getMojioClient(null);
        setUpLogin(contentView);
        setContentView(contentView);
    }

    private void setUpLogin(View loginView){
        mojioLoginButton = (Button) loginView.findViewById(R.id.btn_loginMojio);
        mojioLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //loginWithMojio();
                View mojioLoginView = View.inflate(Login.this, R.layout.activity_login_simple_mojio, null);
                setUpMojioLogin(mojioLoginView);
                setContentView(mojioLoginView);
            }
        });

    }

    private void setUpMojioLogin(View contentView){
        usernameEditor = (EditText) contentView.findViewById(R.id.editText_username);
        //usernameEditor.getText().toString();
        passwordEditor = (EditText) contentView.findViewById(R.id.editText_password);
        mojioOauthButton = (Button) contentView.findViewById(R.id.btn_OauthLogin);
        mojioOauthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasvalidConnection()){
                    webViewOverride();
                }
            }
        });
    }

    private void webViewOverride(){
        setContentView(R.layout.activity_oauth_login);
        //mMojio.launchLoginActivity(this, OAUTH_REQUEST);
        oAuth2WebView = (WebView) findViewById(R.id.loginwebview);
        oAuth2WebView.getSettings().setJavaScriptEnabled(true);
        oAuth2WebView.setWebViewClient(new WebViewClient(){
            public void onPageFinished(WebView oAuth2WebView, String url){
                super.onPageFinished(oAuth2WebView, url);
                String user="jeff2013";
                String password="Jeffreychang2008";
                oAuth2WebView.loadUrl("javascript:(function(){document.getElementById('EmailOrUserName').value = '"+user+"';document.getElementById('Password').value='"+password+"';document.getElementById('logon-form').submit();})()");
                oAuth2WebView.loadUrl("javascript:(function(){document.getElementById('AuthorizeForm').submit();})()");

            }
        });
        oAuth2WebView.loadUrl("https://api.moj.io/account/signin?ReturnUrl=%2FOAuth2Sandbox%2Fauthorize%3Fresponse_type%3Dtoken%26client_id%3Dddf63e97-865a-4b95-8e2f-d414d8e2d5b1%26redirect_uri%3Dmyfirstmojio%3A%2F%2F");
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == OAUTH_REQUEST) {
            Toast.makeText(Login.this, "On Activity Result Called", Toast.LENGTH_LONG).show();
            // We now have a stored access token
            if (resultCode == RESULT_OK) {
                Toast.makeText(Login.this, "On Activity Result Called", Toast.LENGTH_LONG).show();
                //getCurrentUser(); // Now attempt to get user info
                launchMainActivity();
            }
            else {
                Toast.makeText(Login.this, "On Activity Result login failed", Toast.LENGTH_LONG).show();
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
}