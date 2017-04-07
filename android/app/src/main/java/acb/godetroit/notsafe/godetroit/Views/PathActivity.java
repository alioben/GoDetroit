package acb.godetroit.notsafe.godetroit.Views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.android.rides.RideRequestButton;

import java.util.ArrayList;
import java.util.List;

import acb.godetroit.notsafe.godetroit.Entities.Alert;
import acb.godetroit.notsafe.godetroit.R;
import acb.godetroit.notsafe.godetroit.Utils.AlertAdapter;
import acb.godetroit.notsafe.godetroit.Utils.JSONHelper;
import acb.godetroit.notsafe.godetroit.Utils.ReadAlertTask;
import acb.godetroit.notsafe.godetroit.Utils.ReadDangerTask;

public class PathActivity extends AppCompatActivity implements View.OnTouchListener{
    // Traveling mode
    public enum TRAVELING {
        WALKING, DRIVING, TRANSIT, BIKING
    }
    private double startLat, startLon;
    private double endLat, endLon;
    private ProgressBar danger_meter, danger_meter_ppl;
    private NonScrollListView alertsList;
    private AlertAdapter alertAdapter;


    // Map
    private WebView map;
    private TRAVELING mode;
    private static final int LIMIT_ALERTS = 10;
    private Handler handlerOpenGmap = new Handler(){
        public void handleMessage(Message msg) {
            String uri = "http://maps.google.com/maps?saddr="+startLat+","+startLon+"&daddr="+endLat+","+endLon+"&directionsmode="+modeToStringGmap(mode);
            Log.i("GMAPS", uri);
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse(uri));
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path);

        // Extract the starting position
        startLat = this.getIntent().getDoubleExtra("startLat", 0);
        startLon = this.getIntent().getDoubleExtra("startLon", 0);
        endLat = this.getIntent().getDoubleExtra("destLat", 0);
        endLon = this.getIntent().getDoubleExtra("destLon", 0);
        mode = (TRAVELING) this.getIntent().getSerializableExtra("mode");

        Log.i("Path", "start location: ("+startLat+","+startLon+")");
        Log.i("Path", "end location: ("+endLat+","+endLon+")");

        alertsList = (NonScrollListView) findViewById(R.id.alertNearBy);
        danger_meter = (ProgressBar) findViewById(R.id.danger_meter);
        danger_meter.setProgressDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.progress_bar, null));
        danger_meter_ppl = (ProgressBar) findViewById(R.id.danger_meter_people);
        danger_meter_ppl.setProgressDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.progress_bar, null));

        // Get the alerts nearby the destination location
        new ReadAlertTask(this, alertsList, alertAdapter, endLat, endLon, LIMIT_ALERTS).execute();

        // Get the danger meter for everything
        new ReadDangerTask(danger_meter, endLat, endLon, "a").execute();
        new ReadDangerTask(danger_meter_ppl, endLat, endLon, "p").execute();

        // Initialize the map
        map = (WebView) findViewById(R.id.webmap);
        WebSettings wbset = map.getSettings();
        wbset.setJavaScriptEnabled(true);
        map.setWebViewClient(new myWebClient());
        Log.i("Path", "URL: "+buildMapURL(mode));
        map.loadUrl(buildMapURL(mode));

        // Initliaze toolbox
        map.setClickable(true);
        map.setEnabled(true);
        map.setFocusable(true);
        map.setFocusableInTouchMode(true);
        map.setOnTouchListener(this);

        // Request button Uber
        final RideRequestButton requestButton = (RideRequestButton) findViewById(R.id.requestUber);
        RideParameters rideParams = new RideParameters.Builder()
                .setPickupLocation(startLat, startLon, "", "")
                .setDropoffLocation(endLat, endLon, "", "") // Price estimate will only be provided if this is provided.
                .build();
        requestButton.setRideParameters(rideParams);

        Toast.makeText(this,"Touch the map to get detailed tranist details, and view the meters to get an overall idea about your safety level!", Toast.LENGTH_LONG).show();
    }


    private String buildMapURL(TRAVELING mode){
        StringBuilder url = new StringBuilder();
        url.append(JSONHelper.mappoint+"?");
        url.append("startLat="+startLat);
        url.append("&startLon="+startLon);
        url.append("&endLat="+endLat);
        url.append("&endLon="+endLon);
        url.append("&mode="+modeToString(mode));
        return url.toString();
    }

    private String modeToString(TRAVELING mode){
        switch(mode){
            case TRANSIT:
                return "t";
            case BIKING:
                return "b";
            case WALKING:
                return "w";
            case DRIVING:
                return "d";
        }
        return "d";
    }

    private String modeToStringGmap(TRAVELING mode){
        switch(mode){
            case TRANSIT:
                return "transit";
            case BIKING:
                return "bicycling";
            case WALKING:
                return "walking";
            case DRIVING:
                return "driving";
        }
        return "driving";
    }

    public class myWebClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub
            view.loadUrl(url);
            return true;

        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.webmap && event.getAction() == MotionEvent.ACTION_DOWN){
            handlerOpenGmap.obtainMessage().sendToTarget();
        }
        return false;
    }
}
