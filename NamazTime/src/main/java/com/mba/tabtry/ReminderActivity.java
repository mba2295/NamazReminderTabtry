package com.mba.tabtry;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * Created by Muhammad Bilal on 18/02/2016.
 */
public class ReminderActivity extends Activity {

    TextView namazName, date;
    String hour, mins, reqNamazTime, remaining;
    Double lat, lon;
    Button okay;
    MediaPlayer mediaPlayer;
    SharedPreferences sharedprefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reminder_layout);

        mediaPlayer = MediaPlayer.create(this, R.raw.adzanmekkah);
        mediaPlayer.start();

        sharedprefs = getSharedPreferences("dirPref", 0);

        Bundle bundle = getIntent().getExtras();
        namazName = (TextView) findViewById(R.id.namazNametv);
        namazName.setText(bundle.getString("prayer"));


        try {
            lat = Double.parseDouble(sharedprefs.getString("latitude", "24"));
            lon = Double.parseDouble(sharedprefs.getString("longitude", "67"));
        } catch (NullPointerException n) {
            n.printStackTrace();
        }
        Date d = new Date();
        date = (TextView) findViewById(R.id.datetv);
        date.setText(String.valueOf(d.getTime()));


        switch (bundle.getString("prayer")) {
            case "fajr":
                gettingRequestedTime(0, bundle.getString("prayer"));
                break;

            case "zhr":
                gettingRequestedTime(2, bundle.getString("prayer"));
                break;

            case "asr":
                gettingRequestedTime(3, bundle.getString("prayer"));
                break;

            case "maghrib":
                gettingRequestedTime(5, bundle.getString("prayer"));
                break;

            case "isha":
                gettingRequestedTime(6, bundle.getString("prayer"));
                Log.d("ACTIVITY called for", bundle.getString("prayer"));
                break;
        }


        okay = (Button) findViewById(R.id.okaybtn);
        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void gettingRequestedTime(int i, String prayerName) {
        reqNamazTime = getReqTime(i);
        if (reqNamazTime.contains("am")) {
            StringTokenizer tokens = new StringTokenizer(reqNamazTime, ":");
            hour = tokens.nextToken();
            remaining = tokens.nextToken();
            tokens = new StringTokenizer(remaining, " ");
            mins = tokens.nextToken();
        } else if (reqNamazTime.contains("pm")) {
            StringTokenizer tokens = new StringTokenizer(reqNamazTime, ":");
            hour = tokens.nextToken();
            int hours = Integer.parseInt(hour) + 12;
            hour = String.valueOf(hours);
            remaining = tokens.nextToken();
            tokens = new StringTokenizer(remaining, " ");
            mins = tokens.nextToken();
        } else {
            StringTokenizer tokens = new StringTokenizer(reqNamazTime, ":");
            hour = tokens.nextToken();
            mins = tokens.nextToken();
        }

        Bundle bundle = new Bundle();
        bundle.putString("prayer", prayerName);
        bundle.putString("hour", hour);
        bundle.putString("mins", mins);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getBaseContext(), ReminderReciever.class);
        intent.putExtras(bundle);
        PendingIntent pendingIntent
                = PendingIntent.getBroadcast(getBaseContext(),
                0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
        calendar.set(Calendar.MINUTE, Integer.parseInt(mins));
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Log.d("ACTIVITY at", hour + ":" + mins + " " + prayerName + "on dated" + calendar.getTime());

    }

    public String getReqTime(int i) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        boolean timeFormat = preferences.getBoolean("TimeFormat", false);
        String calcMethod = preferences.getString("CalMethod", "0");
        String juriMethod = preferences.getString("JuriMethod", "0");
        String latitudeMethod = preferences.getString("latitudeMethod", "3");

        double latitude = lat;
        double longitude = lon;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
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

        String reqTime = prayerTimes.get(i).toString();
        return reqTime;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.stop();
        finish();
    }
}
