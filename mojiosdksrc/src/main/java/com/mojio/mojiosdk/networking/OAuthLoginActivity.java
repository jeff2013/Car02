package com.mojio.mojiosdk.networking;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mojio.mojiosdk.DataStorageHelper;
import com.mojio.mojiosdk.R;

public class OAuthLoginActivity extends Activity {

    private static String TAG = "MOJIO";

    private WebView _loginWebView;
    private DataStorageHelper _oauthHelper;
    private String _urlPath, _redirectUrl;
    private ProgressBar progressBar;
    private boolean isRedirecting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth_login);

        Bundle extras = getIntent().getExtras();
        _urlPath = extras.getString("URL_AUTH_PATH");
        _redirectUrl = extras.getString("REDIRECT_URL");
        _urlPath += "&redirect_uri=" + _redirectUrl; // Add redirectUrl
        _oauthHelper = new DataStorageHelper(this);

        isRedirecting = false;
        _loginWebView = (WebView) findViewById(R.id.loginwebview);
        progressBar = (ProgressBar) findViewById(R.id.webviewProgressBar);
        Log.d("Url: ", _redirectUrl);
        Log.e(TAG, "Auth url: " + _urlPath);
        _loginWebView.getSettings().setJavaScriptEnabled(true);
        /*_loginWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                _loginWebView.setVisibility(View.GONE);
                if(view.getProgress()<100 && progressBar.getVisibility() == View.GONE){
                    progressBar.setVisibility(View.VISIBLE);
                }
                progressBar.setProgress(view.getProgress());
                if(view.getProgress() == 100){
                    loadProgress++;
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(OAuthLoginActivity.this, "OnProgressChanged " + Integer.toString(loadProgress), Toast.LENGTH_SHORT).show();
                }
                super.onProgressChanged(view, newProgress);
            }
        });
        */

        _loginWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if(!isRedirecting){
                    progressBar.setVisibility(View.VISIBLE);
                    _loginWebView.setVisibility(View.GONE);
                }
                isRedirecting = false;
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                    if(!isRedirecting) {
                        progressBar.setVisibility(View.GONE);
                        //_loginWebView.setVisibility(View.VISIBLE);
                        Animation animation = AnimationUtils.loadAnimation(getBaseContext(), android.R.anim.fade_in);
                        //_loginWebView.startAnimation(animation);
                        _loginWebView.setVisibility(View.VISIBLE);
                    }
                super.onPageFinished(view, url);
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.e(TAG, "Redirecting to url: " + url);
                if (url.startsWith(_redirectUrl)) {
                    Log.d("WEBVIEW RETURN", "webview return");
                    // Note, the url returned cannot be parsed correctly via Uri parse.
                    // Need to manually pull out access_token, expires_in
                    String[] parameters = url.split("&");
                    String[] accessToken = parameters[0].split("=");
                    String[] expiresIn = parameters[2].split("=");

                    _oauthHelper.SetAccessToken(accessToken[1]);
                    _oauthHelper.SetAccessExpireTime(expiresIn[1]);

                    // Return in bundle, but also stored in shared prefs
                    Bundle bundle = new Bundle();
                    bundle.putString("accessToken", _oauthHelper.GetAccessToken());

                    Intent resultIntent = new Intent();
                    resultIntent.putExtras(bundle);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    view.loadUrl(url);
                    isRedirecting = true;
                }
                return true;
            }
        });
        _loginWebView.loadUrl(_urlPath);
    }
}
