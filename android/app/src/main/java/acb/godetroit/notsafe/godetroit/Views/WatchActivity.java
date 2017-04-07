package acb.godetroit.notsafe.godetroit.Views;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import acb.godetroit.notsafe.godetroit.Entities.Contact;
import acb.godetroit.notsafe.godetroit.R;
import acb.godetroit.notsafe.godetroit.Utils.SMSSender;

public class WatchActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Contact followed;
    private double longitude, latitude;

    private Timer timer;
    private int delayed_first = 0;
    private MarkerOptions markerPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        followed = (Contact) getIntent().getSerializableExtra("contact");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final Handler HandlerUpdateCoords = new Handler(){
            public void handleMessage(Message msg){
                LatLng coordinates = new LatLng(latitude, longitude);
                markerPosition = new MarkerOptions().position(coordinates).title(followed.getName());
                mMap.clear();
                mMap.addMarker(markerPosition);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinates));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15f));
            }
        };
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateLocation();
                HandlerUpdateCoords.obtainMessage().sendToTarget();
            }
        }, 0, SMSSender.intervalAlert/2);
    }

    public void updateLocation(){
        final Uri SMS_INBOX = Uri.parse("content://sms/inbox");
        Cursor cursor = this.getContentResolver().query(SMS_INBOX, null, "read=0", null, null);
        boolean new_message  = false;
        String new_body = "";
        String lastdate = "0";

        //ToastShort("SMS"+cursor.moveToNext());
        while (cursor.moveToNext()) {
            cursor.toString();
            String address = cursor.getString(cursor.getColumnIndex("address"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            String body = cursor.getString(cursor.getColumnIndex("body"));
            //ToastShort(id);
            //ToastShort("SMS date: "+date+"\tphone: "+address+"\tread: "+read+"\tbody: "+body);

            if(followed.getPhone().equals(address) &&
                    body.substring(0, 10).equals("#smsalert#") &&
                    compareDate(date, lastdate) > 0) {
                new_body = body;
                new_message = true;
                lastdate = date;
            }
        }

        if(!new_message && delayed_first == 3){
            ToastShort("The streaming of the location ended.");
            timer.cancel();
            WatchActivity.this.finish();
        } else if(new_message && parseMessage(new_body))
            delayed_first = 0;
        else
            delayed_first++;
    }

    private void ToastShort(final String message){
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(WatchActivity.this, message, Toast.LENGTH_SHORT).show();
            }});
    }

    private void LogIt(final String title, final String message){
        runOnUiThread(new Runnable() {
            public void run() {
                Log.i(title, message);
            }});
    }

    private boolean parseMessage(String body){
        String[] parts = body.split("\\#");
        try {
            latitude = Double.parseDouble(parts[2]);
            longitude = Double.parseDouble(parts[3]);
            //ToastShort("("+latitude+","+longitude+")");
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private int compareDate(String d1, String d2){
        int bigger = -1;
        try {

            long date1 = Long.parseLong(d1);
            long date2 = Long.parseLong(d2);
            bigger = (date1 > date2) ? 1: -1;
        } catch (Exception e){
            e.printStackTrace();
        }
        return bigger ;
    }

    @Override
    public void onDestroy(){
        timer.cancel();
        super.onDestroy();
    }
}
