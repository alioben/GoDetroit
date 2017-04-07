package acb.godetroit.notsafe.godetroit.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONObject;

import acb.godetroit.notsafe.godetroit.Entities.Alert;

/**
 * Created by benlalah on 25/01/17.
 */

public class SendAlertTask extends AsyncTask<String, Void, String> {
    private String text;
    private Context context;
    private double lat,lon;
    private AlertAdapter adapter;

    public SendAlertTask(Context context, AlertAdapter adapter, double lat, double lon){
        this.context = context;
        this.lat = lat;
        this.lon = lon;
        this.adapter = adapter;
    }
    @Override
    protected String doInBackground(String... params) {
        text = params[0];
        return JSONHelper.sendJSONAlert(text, lat, lon);
    }

    @Override
    protected void onPostExecute(String json) {
        try{
            JSONObject alert = new JSONObject(json).getJSONObject("alert");
            //TODO: Replace this with the received data ...
            Alert nalert = new Alert(alert.getInt("id"), text, alert.getInt("score"), alert.getDouble("distance"), alert.getDouble("danger"));
            adapter.data.add(0, nalert);
            adapter.notifyDataSetChanged();
            Toast.makeText(context, "Your alert has been published.", Toast.LENGTH_SHORT).show();
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(context, "Unable to publish the alert, retry later.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onProgressUpdate(Void... values) {}
}