package acb.godetroit.notsafe.godetroit.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;

import acb.godetroit.notsafe.godetroit.Entities.Alert;

/**
 * Created by benlalah on 25/01/17.
 */

public class ReadDangerTask extends AsyncTask<Void, Void, Double> {
    private double lat, lon;
    private ProgressBar bar;
    private String type;
    public ReadDangerTask(ProgressBar bar, double lat, double lon, String type) {
        this.lat = lat;
        this.lon = lon;
        this.bar = bar;
        this.type = type;
    }

    @Override
    protected Double doInBackground(Void... params) {
        return JSONHelper.getAuthDanger(lat, lon, type);
    }

    @Override
    protected void onPostExecute(Double danger) {
        bar.setProgress((int) (danger * 100));
    }

    @Override
    protected void onPreExecute() {
    }

}