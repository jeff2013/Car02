package com.example.jeff.car02.Fragments;

import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.jeff.car02.DataSource.DataSource;
import com.example.jeff.car02.DataSource.XYDataSource;
import com.example.jeff.car02.DataSource.XYDataSource;
import com.example.jeff.car02.R;
import com.example.jeff.car02.Utilities;
import com.mojio.mojiosdk.MojioClient;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    private DataSource data;
    private Thread dataThread;
    private OnFragmentInteractionListener mListener;
    private DynamicXYPlotUpdater plotUpdater;
    private LineAndPointFormatter format;
    private MojioClient mMojio;
    private XYDataSource d;

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
        // Format the date into a readable format
        dynamicPlot.setDomainValueFormat(new Format() {

            private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

            @Override
            public StringBuffer format(Object object, StringBuffer buffer, FieldPosition field) {
                long timestamp = ((Number) object).longValue();
                Date date = new Date(timestamp);
                return dateFormat.format(date, buffer, field);
            }

            @Override
            public Object parseObject(String string, ParsePosition position) {
                return null;
            }
        });
        // Create our Plot Update Observer
        plotUpdater = new DynamicXYPlotUpdater(dynamicPlot);
        // Set up formatting junk
        format = new LineAndPointFormatter();
        format.configure(getActivity().getApplicationContext(), R.xml.xyplot_formatter);
        // Set up a data source
        this.data = new XYDataSource(mMojio, 1000, 20000);
        setDataSource(data);
        enableDataSource();
        return view;
    }

    @Override
    public void onResume() {
        // Resume the data polling thread
        if(data != null) {
            data.resumeExecution();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        // If we have a data source, terminate it
        if(data != null) {
            data.pauseExecution();
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
    public void setDataSource(DataSource data) {
        this.data = data;
    }

    public void enableDataSource() {
        // Add the data as a series
        // Don't attempt to use a non XYSeries DataSource
        dynamicPlot.addSeries((XYSeries) this.data, format);
        // Add the observer
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
