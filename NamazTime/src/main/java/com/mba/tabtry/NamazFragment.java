package com.mba.tabtry;


import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */

public class NamazFragment extends Fragment implements LocationListener {

    String providers;
    View vie;
    float lat = (float) 0.926295, lon = (float) 0.130499;
    TextView datetv;
    Button datebtn;
    LocationManager locationManager;
    Calendar calendar = Calendar.getInstance();

    public NamazFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        getTime();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(com.mba.tabtry.R.layout.fragment_namaz, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vie = view;
        datetv = (TextView) view.findViewById(R.id.lattv);
        datebtn = (Button) view.findViewById(R.id.btndate);


        calendar = Calendar.getInstance();
        Date now = new Date();
        calendar.setTime(now);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        datetv.setText(sdf.format(calendar.getTime()));

        /////////////////Getting Latitude and Longitude
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        //Criteria for Location
        Criteria c = new Criteria();
        providers = locationManager.getBestProvider(c, false);
        Location location = locationManager.getLastKnownLocation(providers);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        if (location != null) {
            lat = (float) location.getLatitude();
            lon = (float) location.getLongitude();
            Toast.makeText(getContext(), "lat:" + lat + " lon:" + lon, Toast.LENGTH_SHORT).show();


        } else {
            Toast.makeText(getContext(), "Please turn on Location", Toast.LENGTH_LONG);
        }


        getTime();


        datebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog dpd = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // Display Selected date in textbox
                                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                                calendar.set(year, monthOfYear, dayOfMonth);
                                datetv.setText(sdf.format(calendar.getTime()));

                                getTime();


                            }
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                dpd.show();
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager.requestLocationUpdates(providers, (1000 * 60 * 60), (float) (20 * 1000), this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(this);
    }


    public void getTime() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

        boolean timeFormat = preferences.getBoolean("TimeFormat", false);
        String calcMethod = preferences.getString("CalMethod", "0");
        String juriMethod = preferences.getString("JuriMethod", "0");
        String latitudeMethod = preferences.getString("latitudeMethod", "3");

        double latitude = lat;
        double longitude = lon;
        Log.d("Lat LONG at namazFragment", lat + " latitude " + lon + " Longitude");
        double timezone = (Calendar.getInstance().getTimeZone()
                .getOffset(Calendar.getInstance().getTimeInMillis()))
                / (1000 * 60 * 60);
        PrayerCalculator prayers = new PrayerCalculator();


        //Timeformat from Shared Preferences
        if (timeFormat == false)
            prayers.setTimeFormat(prayers.Time12);
        else {
            prayers.setTimeFormat(prayers.Time24);

        }


        //Calculation Method form the Shared Preferences
        switch (calcMethod) {
            case "0":
                prayers.setCalcMethod(prayers.Karachi);
                break;

            case "1":
                prayers.setCalcMethod(prayers.ISNA);
                break;

            case "2":
                prayers.setCalcMethod(prayers.MWL);
                break;

            case "3":
                prayers.setCalcMethod(prayers.Makkah);
                break;

            case "4":
                prayers.setCalcMethod(prayers.Jafari);
                break;

            case "5":
                prayers.setCalcMethod(prayers.Egypt);
                break;

            case "6":
                prayers.setCalcMethod(prayers.Tehran);
                break;
        }

        //Juristic Method for Asr Time calculation
        switch (juriMethod) {
            case "0":
                prayers.setAsrJuristic(prayers.Shafii);
                break;
            case "1":
                prayers.setAsrJuristic(prayers.Hanafi);
                break;
        }


        switch (latitudeMethod) {
            case "0":
                prayers.setAdjustHighLats(prayers.None);
                break;

            case "1":
                prayers.setAdjustHighLats(prayers.MidNight);
                break;

            case "2":
                prayers.setAdjustHighLats(prayers.OneSeventh);
                break;

            case "3":
                prayers.setAdjustHighLats(prayers.AngleBased);
                break;
        }

        int[] offsets = {0, 0, 0, 0, 0, 0, 0};
        prayers.tune(offsets);


        ArrayList prayerTimes = prayers.getPrayerTimes(calendar, latitude,
                longitude, timezone);
        ArrayList prayerNames = prayers.getTimeNames();
        ArrayAdapter<String> timeAndName = null;
        ArrayList<String> pTimeAndName = new ArrayList<String>();

        for (int i = 0; i < prayerTimes.size(); i++) {
            pTimeAndName.add("\n" + prayerNames.get(i).toString() + " - "
                    + prayerTimes.get(i).toString());
        }

        ListView namazListview = (ListView) this.getView().findViewById(R.id.namazlist);
        ListAddapter adapter = new ListAddapter(this.getView().getContext(), R.layout.list_item, pTimeAndName);
        namazListview.setAdapter(adapter);
    }


    @Override
    public void onLocationChanged(Location location) {

        lat = (float) location.getLatitude();
        lon = (float) location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

class ListAddapter extends ArrayAdapter {

    private List<String> prayers;
    private int resources;
    private LayoutInflater inflater;


    public ListAddapter(Context context, int resource, ArrayList<String> prayers) {
        super(context, resource, prayers);

        this.prayers = prayers;
        this.resources = resource;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ;
    }

    @Override
    public View getView(int position, View prayer, ViewGroup parent) {

        if (prayer == null) {
            prayer = inflater.inflate(resources, null);
        }

        TextView PrayerNT = (TextView) prayer.findViewById(R.id.name);

        PrayerNT.setText(prayers.get(position));
        return prayer;
    }
}
