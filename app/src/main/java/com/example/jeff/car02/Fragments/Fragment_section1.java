package com.example.jeff.car02.Fragments;

import android.app.Activity;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jeff.car02.R;
import com.mojio.mojiosdk.MojioClient;
import com.mojio.mojiosdk.models.Trip;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_section1.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_section1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_section1 extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private MojioClient mojioClient;
    private TextView textView_totalC02;
    private TextView textView_FuelEfficiency;
    private TextView textView_DistanceTraveled;
    private float tripsC02;
    private float fuelEfficieny;
    private float DistanceTraveled;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment section1.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_section1 newInstance(String param1, String param2) {
        Fragment_section1 fragment = new Fragment_section1();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Fragment_section1() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_section1, container, false);
        textView_totalC02 = (TextView) v.findViewById(R.id.textview_C02);
        textView_FuelEfficiency = (TextView) v.findViewById(R.id.textview_FuelEfficiency);
        textView_DistanceTraveled = (TextView) v.findViewById(R.id.textView_DistanceTraveled);
        recieveData();
        return v;
    }

    public void recieveData(){
        Toast.makeText(Fragment_section1.this.getActivity(), "Success", Toast.LENGTH_SHORT).show();
        Map<String, String> queryParam = new HashMap();
        queryParam.put("limit", "1000");
        queryParam.put("offset", "0");
        mojioClient.get(Trip[].class, "Trips", queryParam, new MojioClient.ResponseListener<Trip[]>() {
            @Override
            public void onSuccess(Trip[] result) {
                tripsC02 = result[result.length-1].Distance *result[result.length-1].getFuelEfficiency() *2.3035f;
                Toast.makeText(Fragment_section1.this.getActivity(), "OnSuccess Reached", Toast.LENGTH_SHORT).show();
                textView_totalC02.setText(Float.toString(tripsC02) + " kgC02");
                textView_totalC02.invalidate();

                fuelEfficieny = result[result.length-1].getFuelEfficiency();
                textView_FuelEfficiency.setText(Float.toString(fuelEfficieny) + " L/100km");
                textView_FuelEfficiency.invalidate();

                DistanceTraveled = result[result.length-1].getDistance();
                textView_DistanceTraveled.setText(Float.toString(DistanceTraveled) + " km");
                textView_DistanceTraveled.invalidate();

                correctWidth(textView_totalC02, 500);
                correctWidth(textView_FuelEfficiency, 500);
                correctWidth(textView_DistanceTraveled, 500);
            }

            @Override
            public void onFailure(String error) {
                tripsC02 = 0f;
            }
        });

    }
    public void correctWidth(TextView textView, int desiredWidth)
    {
        Paint paint = new Paint();
        Rect bounds = new Rect();

        paint.setTypeface(textView.getTypeface());
        float textSize = textView.getTextSize();
        paint.setTextSize(textSize);
        String text = textView.getText().toString();
        paint.getTextBounds(text, 0, text.length(), bounds);

        while (bounds.width() > desiredWidth)
        {
            textSize--;
            paint.setTextSize(textSize);
            paint.getTextBounds(text, 0, text.length(), bounds);
        }

        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
/*
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
*/
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setMojio(MojioClient mojio){
        this.mojioClient = mojio;
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
