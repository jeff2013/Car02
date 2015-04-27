package com.example.jeff.car02.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.jeff.car02.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_section4.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_section4#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_section4 extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;


    private SharedPreferences sharedPreferences;
    public static final String graphData = "Graph Number";

    private Button btn_ChangeC02Emissions;
    private Button btn_TotalC02Emissions;
    private Button btn_DistanceTraveled;
    private Button btn_ChangeInFuel;
    private Button btn_fuelEfficiency;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment section4.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_section4 newInstance(String param1, String param2) {
        Fragment_section4 fragment = new Fragment_section4();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Fragment_section4() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        sharedPreferences = getActivity().getSharedPreferences(graphData, Context.MODE_PRIVATE);


        Toast.makeText(getActivity(), "HELLO", Toast.LENGTH_SHORT).show();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =inflater.inflate(R.layout.fragment_section4, container, false);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        btn_ChangeC02Emissions  = (Button) v.findViewById(R.id.btn_C02Emissions);
        btn_ChangeC02Emissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("Data", 1).apply();
                Toast.makeText(getActivity(), "HELLO", Toast.LENGTH_SHORT).show();
            }
        });
        btn_TotalC02Emissions  = (Button) v.findViewById(R.id.btn_TotalC02Emissions);
        btn_TotalC02Emissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("Data", 0).apply();
                Toast.makeText(getActivity(), Integer.toString(sharedPreferences.getInt("Data", 0)), Toast.LENGTH_SHORT).show();
            }
        });
        btn_DistanceTraveled  = (Button) v.findViewById(R.id.btn_DistanceTraveled);
        btn_DistanceTraveled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("Data", 3).apply();
            }
        });
        btn_ChangeInFuel  = (Button) v.findViewById(R.id.btn_ChangeInFuel);
        btn_ChangeInFuel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("Data", 4).apply();
            }
        });
        btn_fuelEfficiency  = (Button) v.findViewById(R.id.btn_fuelEfficiency);
        btn_fuelEfficiency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("Data", 2).apply();
            }
        });
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
