package com.example.jeff.car02.activities;

import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.jeff.car02.fragments.DynamicXYPlotFragment;
import com.example.jeff.car02.fragments.Fragment_section1;
import com.example.jeff.car02.fragments.Fragment_section2;
import com.example.jeff.car02.fragments.Fragment_section3;
import com.example.jeff.car02.fragments.Fragment_section4;
import com.example.jeff.car02.R;
import com.example.jeff.car02.utilities.singletonMojio;
import com.mojio.mojiosdk.MojioClient;
import com.mojio.mojiosdk.models.User;
import com.mojio.mojiosdk.models.Vehicle;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    public static final String graphData = "Graph Number";
    SharedPreferences sharedPreferences;

    /**
     * The {@link ViewPager} that will host the sectiographn contents.
     */
    ViewPager mViewPager;

    /**these are the old bitmap activity_login2 layout objects
    Button btn_login;
    Bitmap bitmap;
    Bitmap bitmap_pressed;
    Button activeButton = null;
    int i = 0;
     */

    //Login objects
    Button loginButton;

    //Mojio keys/codes

    private final static String MOJIO_APP_ID = "ddf63e97-865a-4b95-8e2f-d414d8e2d5b1";
    private final static String REDIRECT_URL = "myfirstmojio://"; // Example "myfirstmojio://"
    private final static String SECRET_KEY= "872bca1d-9a0c-4ad4-932b-3b696658df55";
    private static int OAUTH_REQUEST = 0;

    // The main mojio client object; allows login and data retrieval to occur.
    private MojioClient mMojio;


    private User mCurrentUser;
    private Vehicle[] mUserVehicles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(graphData, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("Data", 0).apply();
        mMojio = singletonMojio.getMojioClient(MainActivity.this);
        //mMojio = new MojioClient(this, MOJIO_APP_ID, null, REDIRECT_URL);
        if(mMojio.isUserLoggedIn()){
            Toast.makeText(this, "MainActivity oncreate reached!", Toast.LENGTH_SHORT).show();
            getCurrentUser();
            //getUserVehicles();
            successful_Login();
        }else{
            Intent test = new Intent(this, Login.class);
            startActivity(test);
        }
        /*if(!mMojio.isUserLoggedIn()){
            doOauth2Login();
        } else if(hasvalidConnection()){
            successful_Login();
        } else {
            doOauth2Login();
        }
        */
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == OAUTH_REQUEST) {
            // We now have a stored access token
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "Logged in successfully main activity", Toast.LENGTH_LONG).show();
                //getCurrentUser(); // Now attempt to get user info
                getCurrentUser();
                successful_Login();
            }
            else {
                Toast.makeText(MainActivity.this, "Problem logging in", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void doOauth2Login() {
        // Launch the OAuth request; this will launch a web view Activity for the user enter their login.
        // When the Activity finishes, we listen for it in the onActivityResult method
        if(hasvalidConnection()){
            mMojio.launchLoginActivity(this, OAUTH_REQUEST);
        }else {
            View loginView = View.inflate(this, R.layout.activity_login_simple, null);
            //bitmapTouchSetUp(loginView);
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
            // Remember that you should never show the action bar if the
                // status bar is hidden, so hide that too if necessary.
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
            setContentView(loginView);
            loginSetUp(loginView);
        }
    }

    public void loginSetUp(View loginView){
        loginButton = (Button) loginView.findViewById(R.id.btn_loginMojio);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasvalidConnection()){
                    mMojio.launchLoginActivity(MainActivity.this, OAUTH_REQUEST);
                    successful_Login();
                }else{
                    Toast.makeText(MainActivity.this, "Please aquire internet access", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    private void getCurrentUser() {
        String entityPath = "Users"; // TODO need userID?
        HashMap<String, String> queryParams = new HashMap();

        mMojio.get(User[].class, entityPath, queryParams, new MojioClient.ResponseListener<User[]>() {
            @Override
            public void onSuccess(User[] result) {
                // Should have one result
                try {
                    mCurrentUser = result[0]; // Save user info so we can use ID later

                    // Show user data
                   /*
                    mUserName.setText("Hello " + mCurrentUser.FirstName + " " + mCurrentUser.LastName);
                    mUserEmail.setText(mCurrentUser.Email);
                    mLoginButton.setVisibility(View.GONE);
                    */
                    getUserVehicles();

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Problem getting users", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(MainActivity.this, "Problem getting users", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Now that we have the current user, we can use their ID to get data
    private void getUserVehicles() {
        String entityPath = String.format("Users/%s/Vehicles", mCurrentUser._id);
        HashMap<String, String> queryParams = new HashMap();
        queryParams.put("sortBy", "Name");
        queryParams.put("desc", "true");

        mMojio.get(Vehicle[].class, entityPath, queryParams, new MojioClient.ResponseListener<Vehicle[]>() {
            @Override
            public void onSuccess(Vehicle[] result) {
                mUserVehicles = result; // Save

                if (mUserVehicles.length == 0) {
                    Toast.makeText(MainActivity.this, "No vehicles found", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(MainActivity.this, "Problem getting vehicles", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void successful_Login(){
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Fragment position_fragment = new Fragment_section1();

            switch(position){
                case 0:
                    position_fragment = new DynamicXYPlotFragment();
                    break;
                case 1:
                    position_fragment = new Fragment_section1();
                    ((Fragment_section1)position_fragment).setMojio(mMojio);
                    break;
                case 2:
                    //TODO change back to launching fragment_section3() once map is fixed. Map crashes app.
                    //position_fragment = new Fragment_section4();
                    position_fragment = new Fragment_section3();
                    //((Fragment_section3)position_fragment).setMojioClient(mMojio);
                    break;
                case 3:
                    position_fragment = new Fragment_section4();
                    break;
                default:
                    position_fragment = new Fragment_section2();

                    break;
            }
            return position_fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
                case 3:
                    return getString(R.string.title_section4).toUpperCase(l);
            }
            return null;
        }
    }
}

//Sets up the touch functionality of the login button bitmap while resizing the bitmap by calling getResizedBitmap
    /*public void bitmapTouchSetUp(View loginView){
        bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.buttonbitmap);
        bitmap_pressed = BitmapFactory.decodeResource(this.getResources(), R.drawable.buttongraphic);

        Display display  = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int width = size.x;
        final int height = size.y;
        bitmap = getResizedBitmap(bitmap, height-height/4, width);
        bitmap_pressed = getResizedBitmap(bitmap_pressed,height-height/4, width);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
        BitmapDrawable bitmapDrawable_pressed = new BitmapDrawable(getResources(), bitmap_pressed);
        btn_login = (Button) loginView.findViewById(R.id.btn_login);
        activeButton = btn_login;
        setSelector(bitmapDrawable, bitmapDrawable_pressed);
        btn_login.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int eventPadTouch = event.getAction();
                float iX=event.getX();
                float iY=event.getY();

                switch (eventPadTouch) {
                    case MotionEvent.ACTION_DOWN:
                        if (iX>=0 & iY>=0 & iX<bitmap.getWidth() & iY<bitmap.getHeight()) { //Makes sure that X and Y are not less than 0, and no more than the height and width of the image.
                            if (bitmap.getPixel((int) iX, (int) iY)!=0) {
                                selectButton(btn_login);
                                if(hasvalidConnection()){
                                    Intent retryLogin = new Intent(MainActivity.this, MainActivity.class);
                                    startActivity(retryLogin);
                                }else {
                                    Toast.makeText(MainActivity.this, "You do not have a valid connection", Toast.LENGTH_SHORT).show();
                                }
                                // actual image area is clicked(alpha not equal to 0), do something
                            }
                        }
                        return true;
                }
                return false;
            }

        });
        selectButton(btn_login);
    }

    private void selectButton(Button button) {

        if (activeButton != null) {
            activeButton.setSelected(false);
            activeButton.setPressed(false);
            activeButton = null;
        }

        activeButton = button;
        if(i == 0){
            activeButton.setSelected(false);
            activeButton.setPressed(false);
            i++;
        } else {
            activeButton.setSelected(true);
            activeButton.setPressed(true);
            //RippleDrawable drawable = (RippleDrawable) activeButton.getBackground();
            //drawable.setVisible(true, true);
        }
    }

    public void setSelector(Drawable backgroundBitmap, Drawable pressedBitmap){
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[] {android.R.attr.state_pressed},
                pressedBitmap);
        states.addState(new int[] {android.R.attr.state_focused},
               backgroundBitmap);
        states.addState(new int[] { },
                backgroundBitmap);
        btn_login.setBackgroundDrawable(states);
    }

    //returns a bitmap that is resized to a specified height and width
    //In our case we pass in the screen's height and width.
    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);
        // RECREATE THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }
    */