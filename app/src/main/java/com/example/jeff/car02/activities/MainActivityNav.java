package com.example.jeff.car02.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jeff.car02.R;
import com.example.jeff.car02.fragments.DynamicXYPlotFragment;
import com.example.jeff.car02.fragments.MapFragment;
import com.example.jeff.car02.fragments.SummaryFragment;
import com.example.jeff.car02.utilities.singletonMojio;
import com.mojio.mojiosdk.MojioClient;
import com.mojio.mojiosdk.models.User;
import com.mojio.mojiosdk.models.Vehicle;

import java.util.HashMap;

public class MainActivityNav extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {


    public static final String graphData = "Graph Number";
    SharedPreferences sharedPreferences;
    private MojioClient mMojio;
    private User mCurrentUser;
    private Vehicle[] mUserVehicles;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        sharedPreferences = getSharedPreferences(graphData, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("Data", 0).apply();
        mMojio = singletonMojio.getMojioClient(MainActivityNav.this);
        if(mMojio.isUserLoggedIn()){
            Toast.makeText(this, "MainActivity oncreate reached!", Toast.LENGTH_SHORT).show();
            getCurrentUser();
            //getUserVehicles();
            successful_Login();
        }else{
            Intent test = new Intent(this, Login.class);
            startActivity(test);
        }
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
                    Toast.makeText(MainActivityNav.this, "Problem getting users", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(MainActivityNav.this, "Problem getting users", Toast.LENGTH_LONG).show();
            }
        });
    }

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
                    Toast.makeText(MainActivityNav.this, "No vehicles found", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(MainActivityNav.this, "Problem getting vehicles", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void successful_Login(){
        setContentView(R.layout.activity_main_activity_nav);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mNavigationDrawerFragment != null) {
            if (!mNavigationDrawerFragment.isDrawerOpen()) {
                // Only show items in the action bar relevant to this screen
                // if the drawer is not showing. Otherwise, let the drawer
                // decide what to show in the action bar.
                getMenuInflater().inflate(R.menu.main_activity_nav, menu);
                restoreActionBar();
                return true;
            }
            return super.onCreateOptionsMenu(menu);
        }
        return false;
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static Fragment newInstance(int sectionNumber) {
            Fragment fragment;
            switch (sectionNumber){
                case 1: fragment = new DynamicXYPlotFragment();
                    break;
                case 2: fragment = new SummaryFragment();
                    break;
                default: fragment = new MapFragment();
            }
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_activity_nav, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivityNav) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
