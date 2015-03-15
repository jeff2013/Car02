package com.example.jeff.car02.Fragments;

import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.example.jeff.car02.DynamicXYDataSource;
import com.example.jeff.car02.MainActivity;
import com.example.jeff.car02.R;
import com.example.jeff.car02.StaticXYDataSource;
import com.example.jeff.car02.TestDynamicXYDataSource;
import com.mojio.mojiosdk.MojioClient;
import com.mojio.mojiosdk.models.Event;
import com.mojio.mojiosdk.models.Trip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DynamicXYPlotFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DynamicXYPlotFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DynamicXYPlotFragment extends Fragment {

    /**
     * The XY Plot instance
     */
    private XYPlot dynamicPlot;
    private DynamicXYDataSource data;
    private Thread dataThread;
    private OnFragmentInteractionListener mListener;
    private DynamicXYPlotUpdater plotUpdater;
    private LineAndPointFormatter format;
    private MojioClient mMojio;

    // Used to get updates from the data source
    private class DynamicXYPlotUpdater implements Observer {

        XYPlot plot;

        public DynamicXYPlotUpdater(XYPlot plot) {
            this.plot = plot;
        }

        /**
         * Redraws the plot when required
         * @param observable
         * @param data
         */
        @Override
        public void update(Observable observable, Object data) {
            //Add the data to our data series
            plot.redraw();
        }
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DynamicXYPlotFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DynamicXYPlotFragment newInstance() {
        DynamicXYPlotFragment fragment = new DynamicXYPlotFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public DynamicXYPlotFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dynamic_xyplot, container, false);
        // Get the plot instance
        dynamicPlot = (XYPlot) view.findViewById(R.id.XYPlot);
        // Create our Plot Update Observer
        plotUpdater = new DynamicXYPlotUpdater(dynamicPlot);
        // Set up formatting junk
        format = new LineAndPointFormatter();
        format.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        format.getLinePaint().setStrokeWidth(10);
        format.getLinePaint().setColor(Color.rgb(0x38, 0x9C, 0xFF));
        format.getFillPaint().setAlpha(0x00);
        Map<String, String> queryParam = new HashMap();
        queryParam.put("limit", "1000");
        queryParam.put("offset", "0");
        mMojio.get(Trip[].class, "Trips", queryParam, new MojioClient.ResponseListener<Trip[]>() {
            @Override
            public void onSuccess(Trip[] tripResult) {
                Trip latestTrip = tripResult[tripResult.length - 1];
                Map<String, String> queryParam = new HashMap();
                queryParam.put("limit", "1000");
                queryParam.put("offset", "0");
                queryParam.put("id", latestTrip._id);
                mMojio.get(Event[].class, "Trips/"+latestTrip._id+"/Events", queryParam, new MojioClient.ResponseListener<Event[]>() {
                    @Override
                    public void onSuccess(Event[] result) {
                        // Generate a set of XY values
                        ArrayList<Pair<Number, Number>> pairs = new ArrayList();
                        float prevDist = 0;
                        int count = 0;
                        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                        for(int i = 1; i < result.length - 1; i++) {
                            Date prevd = new Date();
                            Date d = new Date();
                            try {
                                d = dateFormatter.parse(result[i].Time);
                                prevd = dateFormatter.parse(result[i-1].Time);
                            } catch (ParseException e) {
                                Toast.makeText(DynamicXYPlotFragment.this.getActivity(), "Parse Error", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                            float distance = prevDist + result[i].Speed*(d.getTime()-prevd.getTime())/(60*60*1000);
                            float deltaFuel = distance*result[i].FuelEfficiency - prevDist*result[i-1].FuelEfficiency;
                            float DeltaCO2 = (deltaFuel*2.3035f/10)/(d.getTime() - prevd.getTime());
                            float totalCO2 = distance*result[i].FuelEfficiency*2.3035f;

                            pairs.add(new Pair<Number, Number>(d.getTime(), DeltaCO2));
                            prevDist = distance;
                        }
                        StaticXYDataSource data = new StaticXYDataSource(pairs);
                        setDataSource(data);
                        enableDataSource();
                    }

                    @Override
                    public void onFailure(String error) {

                    }
                });
            }

            public void onFailure(String error) {
                Log.d("Mojio API Error", error);
            }

        });
        return view;
    }

    @Override
    public void onResume() {
        // Resume the data polling thread
        dataThread = new Thread(data);
        dataThread.start();
        super.onResume();
    }

    @Override
    public void onPause() {
        // If we have a data source, terminate it
        if(data != null) {
            data.terminate();
        }
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Changes the data source
     * @param data
     */
    public void setDataSource(DynamicXYDataSource data) {
        this.data = data;
    }

    public void enableDataSource() {
        dynamicPlot.addSeries(this.data, format);
        this.data.addObserver(plotUpdater);
        dataThread = new Thread(data);
        dataThread.start();
    }

    public void setMojioClient(MojioClient m) {
        mMojio = m;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
