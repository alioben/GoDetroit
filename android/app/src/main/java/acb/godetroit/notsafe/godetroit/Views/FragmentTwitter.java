package acb.godetroit.notsafe.godetroit.Views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import acb.godetroit.notsafe.godetroit.Entities.Alert;
import acb.godetroit.notsafe.godetroit.R;
import acb.godetroit.notsafe.godetroit.Utils.AlertAdapter;
import acb.godetroit.notsafe.godetroit.Utils.AsyncTaskHelper;
import acb.godetroit.notsafe.godetroit.Utils.GPSTracker;
import acb.godetroit.notsafe.godetroit.Utils.JSONHelper;
import acb.godetroit.notsafe.godetroit.Utils.ReadAlertTask;
import acb.godetroit.notsafe.godetroit.Utils.SendAlertTask;

public class FragmentTwitter extends Fragment {

    static final int PERMISSION_WATCH_ME = 1;
    static final String[] WATCH_PERMISSIONS = new String[]{
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.READ_CONTACTS};

    // List of alerts
    private TextView checkConn;
    private ListView listView;
    private AlertAdapter adapter;
    private GPSTracker gps;
    private TextView newAlert;
    private Button sendAlert;
    private LinearLayout alertLoader, newAlertLayout;
    private Timer timeLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_twitter, container, false);
        listView = (ListView) view.findViewById(R.id.alerts_list);
        checkConn = (TextView) view.findViewById(R.id.check_connection);

        newAlertLayout = (LinearLayout) view.findViewById(R.id.new_alert_layout);
        alertLoader  =(LinearLayout) view.findViewById(R.id.alert_loader);
        newAlert = (TextView) view.findViewById(R.id.alert_txt);
        sendAlert = (Button) view.findViewById(R.id.send_alert);
        gps = new GPSTracker(getActivity());

        sendAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SendAlertTask(getActivity(), (AlertAdapter)listView.getAdapter(), gps.getLatitude(), gps.getLongitude()).execute(newAlert.getText().toString());
                newAlert.setText("");
                closeKeyboard();
            }
        });

        //Log.i("Alerts","Reading alerts...");
        readAlerts();

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(grantResults.length == 0)
            return;
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            readAlerts();
        else {
            Toast.makeText(getActivity(), "Alerts cannot be displayed until you grant the permission.", Toast.LENGTH_LONG).show();
            listView.setVisibility(View.INVISIBLE);
            checkConn.setVisibility(View.VISIBLE);
        }
    }

    private void readAlerts(){
        if(!isNetworkConnected()){
            listView.setVisibility(View.INVISIBLE);
            newAlertLayout.setVisibility(View.INVISIBLE);
            checkConn.setVisibility(View.VISIBLE);
            alertLoader.setVisibility(View.GONE);
            //Log.e("Alerts","Not connected to network...");
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (getActivity().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED))
            requestPermissions(WATCH_PERMISSIONS, PERMISSION_WATCH_ME);
        else {
            alertLoader.setVisibility(View.VISIBLE);
            checkConn.setVisibility(View.INVISIBLE);
            newAlertLayout.setVisibility(View.INVISIBLE);
            //Log.i("Alerts","Connected to network + permissions...");
            ReadAlertTask task = null;
            task = new ReadAlertTask(getActivity(), listView, adapter, gps.getLatitude(), gps.getLongitude());
            task.execute();

            timeLoader = new Timer();
            final Handler showViews = new Handler(){
                public void handleMessage(Message msg) {
                    listView.setVisibility(View.VISIBLE);
                    alertLoader.setVisibility(View.GONE);
                    newAlertLayout.setVisibility(View.VISIBLE);
                }
            };
            timeLoader.schedule(new TimerTask() {
                @Override
                public void run() {
                    showViews.obtainMessage().sendToTarget();
                }
            }, 3000);
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private void closeKeyboard(){
        InputMethodManager inputManager =
                (InputMethodManager) getActivity().
                        getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(
                getActivity().getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
