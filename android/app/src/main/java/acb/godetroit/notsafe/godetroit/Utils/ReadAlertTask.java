package acb.godetroit.notsafe.godetroit.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.List;

import acb.godetroit.notsafe.godetroit.Entities.Alert;

/**
 * Created by benlalah on 25/01/17.
 */

public class ReadAlertTask extends AsyncTask<Void, Void, List<Alert>> {
    private Context context;
    private double lat, lon;
    private ListView listView;
    private AlertAdapter adapter;
    private int limit;

    public ReadAlertTask(Context context, ListView listview, AlertAdapter adapter, double lat, double lon, int limit){
        this.context = context;
        this.lat = lat;
        this.lon = lon;
        this.listView = listview;
        this.adapter = adapter;
        this.limit = limit;
    }

    public ReadAlertTask(Context context, ListView listview, AlertAdapter adapter, double lat, double lon){
        this.context = context;
        this.lat = lat;
        this.lon = lon;
        this.listView = listview;
        this.adapter = adapter;
        this.limit = 25;
    }

    @Override
    protected List<Alert> doInBackground(Void... params) {
        //Log.i("Alerts", "JSONHElper is helping to get alerts...");
        return JSONHelper.getJSONAlerts(lat, lon, limit);
    }

    @Override
    protected void onPostExecute(List<Alert> alerts) {
        //Log.i("Alerts", "Displaying alert in listview...");
        adapter = new AlertAdapter(context, alerts);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onPreExecute() {
        //Log.e("AsyncTask", "onPreExecute");
    }

    @Override
    protected void onProgressUpdate(Void... values) {}
}