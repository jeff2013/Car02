package com.example.jeff.car02;

import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;

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
    private XYDataSource data;
    private Thread dataThread;
    private OnFragmentInteractionListener mListener;
    private DynamicXYPlotUpdater plotUpdater;
    private LineAndPointFormatter format;

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
        format.getVertexPaint().setAlpha(0x00);
        // This is for debug purposes, create a test data source
        //TODO: Remove debug stuffs
        TestXYDataSource data = new TestXYDataSource(1000);
        setDataSource(data);
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
    public void setDataSource(XYDataSource data) {
        if(this.data != null) {
            // Unregister the plot from the old data
            this.data.deleteObserver(plotUpdater);
            dynamicPlot.removeSeries(data);
        }
        data.terminate();
        // Change our data set
        this.data = data;
        // Register with the new data
        this.data.addObserver(plotUpdater);
        dynamicPlot.addSeries(this.data, format);
        // Start the polling thread
        dataThread = new Thread(data);
        dataThread.start();
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
