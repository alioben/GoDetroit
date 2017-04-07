package acb.godetroit.notsafe.godetroit.Utils;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import acb.godetroit.notsafe.godetroit.Entities.Contact;

/**
 * Created by benlalah on 30/01/17.
 */

public class SMSSender extends IntentService {

    private Timer timer;
    private GPSTracker gps;
    private SmsManager sms;
    private Contact selected_contact;
    public final static int intervalAlert = 25*1000; // Time between two sms in seconds
    public SMSSender() {
        super("sms-sender");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy(){
        timer.cancel();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        //Log.i("SMSSender", "Initializing...");
        selected_contact = (Contact) intent.getSerializableExtra("contact");

        // Initilaize sms manager
        sms = SmsManager.getDefault();

        // Initialize the gps tracker
        gps = new GPSTracker(this);

        // Set a timer for every minute
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sms.sendTextMessage(selected_contact.getPhone(), null, "#smsalert#"+gps.getLatitude()+"#"+gps.getLongitude(), null, null);
            }
        }, 0, intervalAlert);
        return Service.START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }
}