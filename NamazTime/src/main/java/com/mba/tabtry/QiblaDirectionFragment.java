package com.mba.tabtry;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class QiblaDirectionFragment extends Fragment implements SensorEventListener {


    Location currentLocation, MeccaLocation;
    private ImageView image, arrowIv;
    private float currentDegree = 0f;
    double arrowStarting;
    private SensorManager mSensorManager;
    TextView tvHeading;
    SharedPreferences sharedprefs;
    SharedPreferences.Editor editor;
    double longitude, latitude, altitude;

    public QiblaDirectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(com.mba.tabtry.R.layout.fragment_qibla_direction, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedprefs = getActivity().getSharedPreferences("dirPref", 0);

        getLocation();
        currentLocation.setLatitude(latitude);
        currentLocation.setLongitude(longitude);
        currentLocation.setAltitude(altitude);

        MeccaLocation.setLatitude(21.427378);
        MeccaLocation.setLongitude(39.814838);
        MeccaLocation.setAltitude(302.00);

        image = (ImageView) view.findViewById(R.id.imageViewCompass);
        arrowIv = (ImageView) view.findViewById(R.id.arrow_IV);
        // TextView that will tell the user what degree is he heading

        tvHeading = (TextView) view.findViewById(R.id.tvHeading);


        // initialize your android device sensor capabilities

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
    }

    float[] mGravity;
    float[] mGeomagnetic;

    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = Math.round(event.values[0]);
        GeomagneticField geoField = new GeomagneticField(Double
                .valueOf(latitude).floatValue(), Double
                .valueOf(longitude).floatValue(),
                Double.valueOf(altitude).floatValue(),
                System.currentTimeMillis());


        degree -= geoField.getDeclination();
        tvHeading.setText(Float.toString(degree));


        float bearTo = currentLocation.bearingTo(MeccaLocation);

        if (bearTo < 0) {
            bearTo = bearTo + 360;
        }
        float direction = bearTo - degree;
        if (direction < 0) {
            direction = direction + 360;
        }
        // create a rotation animation (reverse turn degree degrees)

        RotateAnimation ra = new RotateAnimation(

                currentDegree,

                direction,

                Animation.RELATIVE_TO_SELF, 0.5f,

                Animation.RELATIVE_TO_SELF,

                0.5f);
        RotateAnimation raArrow = new RotateAnimation(

                (float) arrowStarting,

                direction,

                Animation.RELATIVE_TO_SELF, 0.5f,

                Animation.RELATIVE_TO_SELF,

                0.5f);


        ra.setDuration(210);

        ra.setFillAfter(true);

        image.startAnimation(ra);
        arrowIv.startAnimation(raArrow);

        currentDegree = direction;
        arrowStarting = direction;
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),

                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    void getLocation() {
        if (sharedprefs.getString("latitude", "") != "" || sharedprefs.getString("latitude", "") != "0") {

            latitude = Double.parseDouble(sharedprefs.getString("latitude", "0"));
            longitude = Double.parseDouble(sharedprefs.getString("longitude", "0"));
            longitude = Double.parseDouble(sharedprefs.getString("altitude", "0"));
            Toast.makeText(getContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
            // thhese four line to calculate angle from user to Mecca.
            double lonDelta = (longitude * (Math.PI / 180) - 0.695096573227);
            double y = Math.sin(lonDelta) * Math.cos(latitude * (Math.PI / 180));
            double x = Math.cos(0.373893159) * Math.sin(latitude * (Math.PI / 180)) - Math.sin(0.373893159) * Math.cos(latitude * (Math.PI / 180)) * Math.cos(lonDelta);
            double bearing = Math.toDegrees(Math.atan2(y, x));
            arrowStarting = bearing;

            Log.e("====" + bearing + "====",
                    "Your Location is Lat: " + latitude +
                            "\nLong: " + longitude +
                            "\n lat: " + latitude * (Math.PI / 180) +
                            "\nlog: " + longitude * (Math.PI / 180) +
                            "\n lat: " + sharedprefs.getString("latitude", "") + "" +
                            " \nlog: " + sharedprefs.getString("longitude", ""));
        } else {
            Toast.makeText(getContext(), "If your location has changed press get location button", Toast.LENGTH_LONG).show();

        }
    }
}