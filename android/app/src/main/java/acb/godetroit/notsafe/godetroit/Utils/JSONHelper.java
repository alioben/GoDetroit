package acb.godetroit.notsafe.godetroit.Utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.net.HttpURLConnection;

import acb.godetroit.notsafe.godetroit.Entities.Alert;

public class JSONHelper {

    public static final String domain = "http://god.6te.net/";
    public static final String endpoint =  domain+"ajax.php";
    public static final String mappoint = domain+"showmap.html";

    public static void scoreUp(int id){
        HashMap<String, String> params = new HashMap<>();
        params.put("id", Double.toString(id));
        sendData("score", params);
    }

    public static double getAuthDanger(double lat, double lon, String type){
        HashMap<String, String> params = new HashMap<>();
        params.put("lat", Double.toString(lat));
        params.put("lon", Double.toString(lon));
        params.put("type", type);
        String response = sendData("danger", params);
        if(response.equals("-1"))
            return 0;
        double dang = 0;
        try{
            JSONObject obj = new JSONObject(response);
            dang = obj.getDouble("danger");
            assert(dang > 1);
        } finally {
            return dang;
        }

    }

    public static String sendJSONAlert(String text, double lat, double lon) {
        //Log.i("Alerts", "Requesting alerts from server...");
        HashMap<String, String> params = new HashMap<>();
        params.put("lat", Double.toString(lat));
        params.put("lon", Double.toString(lon));
        params.put("alert", text);
        return sendData("publish", params);
    }

    public static List<Alert> getJSONAlerts(double lat, double lon, int limit){
        List<Alert> list = new ArrayList<>();
        HashMap<String, String> params = new HashMap<>();
        params.put("lat", Double.toString(lat));
        params.put("lon", Double.toString(lon));
        params.put("limit", Integer.toString(limit));
        String s = sendData("get", params);
        if(s.equals("-1"))
            return list;

        JSONArray alerts;
        try {
            JSONObject obj = new JSONObject(s);
            alerts = obj.getJSONArray("alerts");
            for (int i = 0; i < alerts.length(); i++) {
                JSONObject a = alerts.getJSONObject(i);
                Alert new_alert = new Alert(a.getInt("id"), a.getString("alert"), a.getInt("score"), a.getDouble("distance"), a.getDouble("danger"));
                list.add(new_alert);
            }
        }finally{
            return list;
        }
    }

    public static String sendData(String function, Map<String, String> params){
        String results = "-1";
        try {
            String url = endpoint;

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            StringBuilder urlParams = new StringBuilder();
            urlParams.append("f="+function);
            for(Map.Entry<String, String> entry: params.entrySet()){
                urlParams.append("&"+entry.getKey()+"=");
                urlParams.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }


            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParams.toString());
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            if(responseCode != 200)
                return "-1";

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);

            in.close();

            results = response.toString();
            //Log.i("Data", "Received data: "+results);
        } catch(Exception e){
          e.printStackTrace();
        }  finally{
            return results;
        }
    }

}
